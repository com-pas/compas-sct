// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.ControlBlockEditor;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.cbcom.*;
import org.lfenergy.compas.sct.commons.model.da_comm.DACOMM;
import org.lfenergy.compas.sct.commons.scl.ControlService;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleEntry;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newAddress;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newP;

@RequiredArgsConstructor
public class ControlBlockEditorService implements ControlBlockEditor {

    private static final int MAX_VLAN_ID = 0x0FFF;
    private static final int MAX_VLAN_PRIORITY = 7;
    private static final String NONE = "none";
    private static final String APPID_P_TYPE = "APPID";
    private static final String MAC_ADDRESS_P_TYPE = "MAC-Address";
    private static final String VLAN_ID_P_TYPE = "VLAN-ID";
    private static final String VLAN_PRIORITY_P_TYPE = "VLAN-PRIORITY";
    private static final int APPID_LENGTH = 4;
    private static final int VLAN_ID_LENGTH = 3;
    private static final String MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM = "Error in Control Block communication setting file: vlan is missing attribute ";
    private static final int HEXADECIMAL_BASE = 16;
    private final ControlService controlService;
    private final LdeviceService ldeviceService;
    private final ConnectedAPService connectedAPService;
    private final SubNetworkService subNetworkService;

    @Override
    public List<SclReportItem> analyzeDataGroups(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .map(iedAdapter -> {
                    List<SclReportItem> list = new ArrayList<>();
                    list.addAll(iedAdapter.checkDataGroupCoherence());
                    list.addAll(iedAdapter.checkBindingDataGroupCoherence());
                    return list;
                }).flatMap(Collection::stream).toList();
    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, DACOMM dacomm) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .map(lDeviceAdapter -> lDeviceAdapter.createDataSetAndControlBlocks(dacomm.getFCDAs().getFCDA()))
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public void removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(final SCL scl) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        List<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters).toList();
        // LN0
        lDeviceAdapters.stream()
                .map(LDeviceAdapter::getLN0Adapter)
                .forEach(ln0 -> {
                    ln0.removeAllControlBlocksAndDatasets();
                    ln0.removeAllExtRefSourceBindings();
                });
        // Other LN
        lDeviceAdapters.stream()
                .map(LDeviceAdapter::getLNAdapters).flatMap(List::stream)
                .forEach(LNAdapter::removeAllControlBlocksAndDatasets);
    }

    @Override
    public List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, CBCom cbCom) {
        return configureNetworkForAllControlBlocks(scd, cbCom, Collections.emptyList());
    }

    @Override
    public List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, CBCom cbCom, List<TSubNetwork> subnetworksToReuse) {
        Map<CbKey, AppId> appIdsToReuse = subnetworksToReuse != null && !subnetworksToReuse.isEmpty() ?
                computeAppIdsToReuse(scd, subnetworksToReuse)
                : Collections.emptyMap();
        Map<CbKey, Mac> macsToReuse = subnetworksToReuse != null && !subnetworksToReuse.isEmpty() ?
                computeMacToReuse(scd, subnetworksToReuse)
                : Collections.emptyMap();
        return Stream.concat(
                        configureNetworkForControlBlocks(scd, appIdsToReuse, macsToReuse, cbCom, TCBType.GOOSE),
                        configureNetworkForControlBlocks(scd, appIdsToReuse, macsToReuse, cbCom, TCBType.SV))
                .toList();
    }

    private Stream<SclReportItem> configureNetworkForControlBlocks(SCL scl, Map<CbKey, AppId> appIdsToReuse, Map<CbKey, Mac> macsToReuse, CBCom cbCom, TCBType tcbType) {
        CbComSettings cbComSettings;
        try {
            cbComSettings = parseCbCom(cbCom, tcbType);
        } catch (ScdException ex) {
            return Stream.of(SclReportItem.error("Control Block Communication setting files", ex.getMessage()));
        }
        List<Long> appIdToReuse = appIdsToReuse.values().stream().map(AppId::appId).toList();
        List<Long> macToReuse = macsToReuse.values().stream().map(Mac::mac).toList();
        PrimitiveIterator.OfLong appIdIterator = cbComSettings.appIds().filter(appId -> !appIdToReuse.contains(appId)).iterator();
        List<Long> macAddresseList = cbComSettings.macAddresses().filter(mac -> !macToReuse.contains(mac)).boxed().toList();
        return scl.getIED().stream()
                .filter(tied -> tied.getAccessPoint().stream()
                        .anyMatch(tAccessPoint -> tAccessPoint.isSetServer() && tAccessPoint.getServer().isSetLDevice()))
                .flatMap(tied -> {
                    List<Long> excludedMacAddresses = findExcludedMacAddresses(scl, tied, tcbType);
                    Iterator<Long> authorizedMacAdressList = macAddresseList.stream().filter(mac -> !excludedMacAddresses.contains(mac)).iterator();
                    return tied.getAccessPoint()
                            .stream()
                            .flatMap(accessPoint -> accessPoint.getServer().getLDevice().stream()
                                    .flatMap(lDevice -> controlService.getControls(lDevice.getLN0(), ControlBlockEnum.from(tcbType).getControlBlockClass())
                                            .map(tControl -> {
                                                String apName = accessPoint.getName();
                                                IedApLd iedApLd = new IedApLd(tied, apName, lDevice);
                                                CriteriaOrError criteriaOrError = getCriteria(tied, tcbType, tControl.getName());
                                                if (criteriaOrError.errorMessage != null) {
                                                    return Optional.of(SclReportItem.error("""
                                                            /SCL/IED[@name="%s"]""".formatted(tied.getName()), criteriaOrError.errorMessage));
                                                }

                                                Settings settings = cbComSettings.settingsByCriteria.get(criteriaOrError.criteria);
                                                if (settings == null) {
                                                    return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because: No controlBlock communication settings found with these " + criteriaOrError.criteria);
                                                }
                                                AppId reuseAppId = appIdsToReuse.get(new CbKey(tied.getName(), lDevice.getInst(), tControl.getName()));
                                                Mac reuseMac = macsToReuse.get(new CbKey(tied.getName(), lDevice.getInst(), tControl.getName()));
                                                TCommunication tCommunication = scl.getCommunication();

                                                Optional<TConnectedAP> optionalTConnectedAP = subNetworkService.getSubNetworks(tCommunication)
                                                        .flatMap(tSubNetwork -> connectedAPService.getFilteredConnectedAP(tSubNetwork, connectedAP -> tied.getName().equals(connectedAP.getIedName()) && apName.equals(connectedAP.getApName())))
                                                        .findFirst();
                                                if (optionalTConnectedAP.isEmpty()) {
                                                    return newError(iedApLd, tControl, "Cannot configure communication for ControlBlock because no ConnectedAP found for AccessPoint");
                                                }
                                                return configureControlBlockNetwork(tCommunication, settings, appIdIterator, authorizedMacAdressList, tControl, iedApLd, reuseAppId, reuseMac);
                                            })).flatMap(Optional::stream));
                });
    }

    // FIXME: pas besoin de l'AcessPoint car le ldInst est unique dans tout l'IED.
    // FIXME: renvoyer un Optional parce qu'un ControlBlock n'a qu'une seule adresse
    private Stream<Long> getMacFromControlBlock(SCL scl, TControl tControl, IedApLd iedApLd) {
        TCommunication tCommunication = scl.getCommunication();
        // FIXME: utiliser le potentiel des Optional
        Optional<TConnectedAP> tConnectedAPOptional = subNetworkService.getSubNetworks(tCommunication)
                .flatMap(tSubNetwork -> connectedAPService.getFilteredConnectedAP(tSubNetwork, connectedAP -> iedApLd.ied().getName().equals(connectedAP.getIedName()) && iedApLd.apName().equals(connectedAP.getApName()))).findFirst();
        if (tConnectedAPOptional.isEmpty()) {
            return Stream.empty();
        }
        TConnectedAP tConnectedAP = tConnectedAPOptional.get();
        switch (tControl) {
            case TGSEControl ignored -> {
                Optional<TGSE> optGse = tConnectedAP.isSetGSE() ?
                        tConnectedAP.getGSE().stream().filter(gse1 -> Objects.equals(iedApLd.lDevice().getInst(), gse1.getLdInst()) && Objects.equals(tControl.getName(), gse1.getCbName())).findFirst()
                        : Optional.empty();
                if (optGse.isPresent() && optGse.get().isSetAddress()) {
                    Optional<TP> macAdress = optGse.get().getAddress().getP().stream()
                            .filter(tp -> tp.getType().equals(MAC_ADDRESS_P_TYPE)).findFirst();
                    if (macAdress.isPresent()) return Stream.of(Utils.macAddressToLong(macAdress.get().getValue()));
                }
            }
            case TSampledValueControl ignored -> {
                Optional<TSMV> optSmv = tConnectedAP.isSetSMV() ?
                        tConnectedAP.getSMV().stream().filter(smv1 -> Objects.equals(iedApLd.lDevice().getInst(), smv1.getLdInst()) && Objects.equals(tControl.getName(), smv1.getCbName())).findFirst()
                        : Optional.empty();
                if (optSmv.isPresent() && optSmv.get().isSetAddress()) {
                    Optional<TP> macAdress = optSmv.get().getAddress().getP().stream()
                            .filter(tp -> tp.getType().equals(MAC_ADDRESS_P_TYPE)).findFirst();
                    if (macAdress.isPresent()) return Stream.of(Utils.macAddressToLong(macAdress.get().getValue()));
                }
            }
            default -> {
                return Stream.empty();
            }
        }
        return Stream.empty();
    }

    private List<Long> findExcludedMacAddresses(SCL scl, TIED ied, TCBType tcbType) {
        //We remove the addresses that are in a ExtRef of the IED.
        List<Long> directLinkMacAdresses = getExtRefAddress(scl, ied).collect(Collectors.toCollection(ArrayList::new));

        //We search for every IED that have an ExtRef that comes from the current IED and remove every address they have in their CB and ExtRef
        List<TIED> iedWithExtRefFromCurrentCB = scl.getIED().stream()
                .filter(tied -> !tied.getName().equals(ied.getName()))
                .filter(tied -> ldeviceService.getLdevices(tied).
                        flatMap(tlDevice -> {
                            //FIXME: plutêt mettre un filter(lDevice -> lDevice.getLN0().isSetInputs()) plutôt qu'un if
                            if (tlDevice.getLN0().getInputs() != null) {
                                return tlDevice.getLN0().getInputs().getExtRef().stream();
                            }
                            return Stream.empty();
                        })
                        //FIXME: tExtRef.getIedName() peut être null.
                        .anyMatch(tExtRef -> tExtRef.getIedName().equals(ied.getName()))
                )
                .toList();

        //addresses from CB
        List<Long> addressesFromCBIed = getAddressFromListOfIed(scl, tcbType, iedWithExtRefFromCurrentCB).toList();

        List<Long> addressesFromExtRefIed = iedWithExtRefFromCurrentCB.stream()
                .flatMap(tied -> getExtRefAddress(scl, tied))
                .toList();

        directLinkMacAdresses.addAll(addressesFromCBIed);
        directLinkMacAdresses.addAll(addressesFromExtRefIed);
        return directLinkMacAdresses;
    }

    private Stream<Long> getExtRefAddress(SCL scl, TIED tied) {
        return ldeviceService.getLdevices(tied)
                .filter(lDevice -> lDevice.getLN0().isSetInputs())
                .flatMap(tlDevice -> tlDevice.getLN0().getInputs().getExtRef().stream())
                .filter(TExtRef::isSetSrcCBName)
                .map(tExtRef -> new CbKey(tExtRef.getIedName(), tExtRef.getLdInst(), tExtRef.getSrcCBName()))
                .flatMap(cbKey -> scl.getCommunication().getSubNetwork().stream()
                        .flatMap(tSubNetwork -> tSubNetwork.getConnectedAP().stream()
                                .filter(tConnectedAP -> tConnectedAP.getIedName().equals(cbKey.iedName()))
                                .flatMap(tConnectedAP -> Stream.concat(tConnectedAP.getGSE().stream(), tConnectedAP.getSMV().stream()))
                                .filter(cBlock -> cBlock.getCbName().equals(cbKey.cbName()) && cBlock.getLdInst().equals(cbKey.LDInst()))))
                .filter(TControlBlock::isSetAddress)
                .flatMap(cBlock -> {
                    return Utils.extractFromP(MAC_ADDRESS_P_TYPE, cBlock.getAddress().getP()).stream();
                })
                .map(Utils::macAddressToLong);
    }

    private Stream<Long> getAddressFromListOfIed(SCL scl, TCBType tcbType, List<TIED> iedsToGetCBFrom) {
        //FIXME: You can simplify by searching directly in Communication section
//        Set<String> iedNames = iedsToGetCBFrom.stream().map(TIED::getName).collect(Collectors.toSet());
//        return scl.getCommunication().getSubNetwork().stream()
//                .flatMap(tSubNetwork -> connectedAPService.getFilteredConnectedAP(tSubNetwork, connectedAP -> iedNames.contains(connectedAP.getIedName())))
//                .flatMap(tConnectedAP -> switch (tcbType) {
//                    case GOOSE -> tConnectedAP.getGSE().stream();
//                    case SV -> tConnectedAP.getSMV().stream();
//                    default -> throw new IllegalArgumentException("Unsupported TCBType " + tcbType);
//                })
//                .filter(TControlBlock::isSetAddress)
//                .flatMap(tControlBlock -> Utils.extractFromP(MAC_ADDRESS_P_TYPE, tControlBlock.getAddress().getP()).stream())
//                .map(Utils::macAddressToLong);
        return iedsToGetCBFrom.stream()
                .flatMap(tied -> ldeviceService.getLdevices(tied)
                        .flatMap(lDevice -> controlService.getControls(lDevice.getLN0(), ControlBlockEnum.from(tcbType).getControlBlockClass())
                                .flatMap(cBlock -> {
                                    TAccessPoint accessPoint = tied.getAccessPoint().stream().filter(ap -> ap.getServer().getLDevice().stream().anyMatch(tlDevice -> tlDevice.getLdName().equals(lDevice.getLdName()))).findFirst().orElseThrow();
                                    return getMacFromControlBlock(scl, cBlock, new IedApLd(tied, accessPoint.getName(), lDevice));
                                })));
    }

    private CbComSettings parseCbCom(CBCom cbCom, TCBType tcbType) {
        TRange appIdRange = Optional.ofNullable(cbCom.getAppIdRanges()).map(AppIdRanges::getAppIdRange).stream()
                .flatMap(Collection::stream)
                .filter(tRange -> tcbType.equals(tRange.getCBType()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Control Block Communication setting files does not contain AppIdRange for cbType " + tcbType.value()));
        LongStream appIds = LongStream.range(Long.parseLong(appIdRange.getStart(), HEXADECIMAL_BASE), Long.parseLong(appIdRange.getEnd(), HEXADECIMAL_BASE) + 1);

        TRange macRange = Optional.ofNullable(cbCom.getMacRanges()).map(MacRanges::getMacRange).stream()
                .flatMap(Collection::stream)
                .filter(tRange -> tcbType.equals(tRange.getCBType()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Control Block Communication setting files does not contain MacRange for cbType " + tcbType.value()));
        LongStream macAddresses = LongStream.range(Utils.macAddressToLong(macRange.getStart()), Utils.macAddressToLong(macRange.getEnd()) + 1);

        Map<Criteria, Settings> settingsByCriteria = Optional.ofNullable(cbCom.getVlans()).map(Vlans::getVlan).stream()
                .flatMap(Collection::stream)
                .filter(vlan -> tcbType.equals(vlan.getCBType()))
                .collect(Collectors.toMap(this::vlanToCriteria, this::vlanToSetting));

        return new CbComSettings(appIds, macAddresses, settingsByCriteria);
    }

    private Optional<SclReportItem> configureControlBlockNetwork(TCommunication tCommunication, Settings settings, PrimitiveIterator.OfLong appIdIterator, Iterator<Long> macAddressesAuthorized, TControl tControl, IedApLd iedApLd, AppId reuseAppId, Mac reuseMac) {
        Optional<TConnectedAP> optionalTConnectedAP = subNetworkService.getSubNetworks(tCommunication)
                .flatMap(tSubNetwork -> connectedAPService.getFilteredConnectedAP(tSubNetwork, connectedAP -> iedApLd.ied().getName().equals(connectedAP.getIedName()) && iedApLd.apName().equals(connectedAP.getApName())))
                .findFirst();
        if (optionalTConnectedAP.isEmpty()) {
            return newError(iedApLd, tControl, "Cannot configure communication for ControlBlock because no ConnectedAP found for AccessPoint");
        }
        if (settings.vlanId() == null) {
            return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because no Vlan Id was provided in the settings");
        }
        AppId appId;
        Mac mac;
        if (reuseAppId != null) {
            appId = reuseAppId;
        } else {
            if (!appIdIterator.hasNext()) {
                return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because range of appId is exhausted");
            }
            appId = new AppId(appIdIterator.next());
        }

        if (reuseMac != null) {
            mac = reuseMac;
        } else {
            if (!macAddressesAuthorized.hasNext()) {
                return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because range of mac addresses is exhausted");
            }
            mac = new Mac(macAddressesAuthorized.next());
        }

        List<TP> listOfPs = new ArrayList<>();
        listOfPs.add(newP(APPID_P_TYPE, Utils.toHex(appId.appId(), APPID_LENGTH)));
        listOfPs.add(newP(MAC_ADDRESS_P_TYPE, Utils.longToMacAddress(mac.mac())));
        listOfPs.add(newP(VLAN_ID_P_TYPE, Utils.toHex(settings.vlanId(), VLAN_ID_LENGTH)));
        if (settings.vlanPriority() != null) {
            listOfPs.add(newP(VLAN_PRIORITY_P_TYPE, String.valueOf(settings.vlanPriority())));
        }

        TConnectedAP tConnectedAP = optionalTConnectedAP.orElseThrow();
        switch (tControl) {
            case TGSEControl ignored -> updateGseOrCreateIfNotExists(tConnectedAP, iedApLd.lDevice().getInst(), tControl.getName(), listOfPs, SclConstructorHelper.newDurationInMilliSec(settings.minTime), SclConstructorHelper.newDurationInMilliSec(settings.maxTime));
            case TSampledValueControl ignored -> updateSmvOrCreateIfNotExists(tConnectedAP, iedApLd.lDevice().getInst(), tControl.getName(), listOfPs);
            default -> throw new ScdException("Unsupported Control Block type for communication configuration : " + tControl.getClass().getName());
        }
        return Optional.empty();
    }

    private Map<CbKey, AppId> computeAppIdsToReuse(SCL scd, List<TSubNetwork> subnetworksToReuse) {
        List<CbKey> allControlBlocksInScd = scd.getIED().stream()
                .flatMap(tIed -> ldeviceService.getLdevices(tIed)
                        .filter(TLDevice::isSetLN0)
                        .flatMap(tlDevice -> Stream.concat(tlDevice.getLN0().getGSEControl().stream(), tlDevice.getLN0().getSampledValueControl().stream())
                                .map(tControlWithIEDName -> new CbKey(tIed.getName(), tlDevice.getInst(), tControlWithIEDName.getName()))
                        ))
                .toList();
        return subnetworksToReuse.stream()
                .flatMap(tSubNetwork -> tSubNetwork.getConnectedAP().stream())
                .flatMap(tConnectedAP -> Stream.concat(tConnectedAP.getGSE().stream(), tConnectedAP.getSMV().stream())
                        .flatMap(tControlBlock -> AppId.from(tControlBlock.getAddress())
                                .map(appId -> new SimpleEntry<>(new CbKey(tConnectedAP.getIedName(), tControlBlock.getLdInst(), tControlBlock.getCbName()), appId))
                                .stream()
                        )
                )
                .filter(entry -> allControlBlocksInScd.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<CbKey, Mac> computeMacToReuse(SCL scd, List<TSubNetwork> subnetworksToReuse) {
        List<CbKey> allControlBlocksInScd = scd.getIED().stream()
                .flatMap(tIed -> ldeviceService.getLdevices(tIed)
                        .filter(TLDevice::isSetLN0)
                        .flatMap(tlDevice -> Stream.concat(tlDevice.getLN0().getGSEControl().stream(), tlDevice.getLN0().getSampledValueControl().stream())
                                .map(tControlWithIEDName -> new CbKey(tIed.getName(), tlDevice.getInst(), tControlWithIEDName.getName()))
                        ))
                .toList();
        return subnetworksToReuse.stream()
                .flatMap(tSubNetwork -> tSubNetwork.getConnectedAP().stream())
                .flatMap(tConnectedAP -> Stream.concat(tConnectedAP.getGSE().stream(), tConnectedAP.getSMV().stream())
                        .flatMap(tControlBlock -> Mac.from(tControlBlock.getAddress())
                                .map(mac -> new SimpleEntry<>(new CbKey(tConnectedAP.getIedName(), tControlBlock.getLdInst(), tControlBlock.getCbName()), mac))
                                .stream()
                        )
                )
                .filter(entry -> allControlBlocksInScd.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void updateGseOrCreateIfNotExists(TConnectedAP tConnectedAP, String ldInst, String cbName, List<TP> listOfP, TDurationInMilliSec minTime, TDurationInMilliSec maxTime) {
        Optional<TGSE> optGse = tConnectedAP.isSetGSE() ?
                tConnectedAP.getGSE().stream().filter(gse1 -> Objects.equals(ldInst, gse1.getLdInst()) && Objects.equals(cbName, gse1.getCbName())).findFirst()
                : Optional.empty();
        TGSE gse = optGse
                .orElseGet(() -> {
                            TGSE newGse = new TGSE();
                            newGse.setLdInst(ldInst);
                            newGse.setCbName(cbName);
                            tConnectedAP.getGSE().add(newGse);
                            return newGse;
                        }
                );
        gse.setAddress(newAddress(listOfP));
        gse.setMinTime(minTime);
        gse.setMaxTime(maxTime);
    }

    /**
     * Create A SMV Section or update an existing SMV Section (the network configuration of a SampledValueControl block)..
     */
    private void updateSmvOrCreateIfNotExists(TConnectedAP tConnectedAP, String ldInst, String cbName, List<TP> listOfP) {
        Optional<TSMV> optSmv = tConnectedAP.isSetSMV() ?
                tConnectedAP.getSMV().stream().filter(smv1 -> Objects.equals(ldInst, smv1.getLdInst()) && Objects.equals(cbName, smv1.getCbName())).findFirst()
                : Optional.empty();
        TSMV smv = optSmv
                .orElseGet(() -> {
                            TSMV newSmv = new TSMV();
                            newSmv.setLdInst(ldInst);
                            newSmv.setCbName(cbName);
                            tConnectedAP.getSMV().add(newSmv);
                            return newSmv;
                        }
                );
        smv.setAddress(newAddress(listOfP));
    }

    private static Optional<SclReportItem> newError(IedApLd iedApLd, TControl tControl, String message) {
        return Optional.of(SclReportItem.error(iedApLd.getXPath() + "/LN0/" + controlBlockXPath(tControl),
                message));
    }

    private static String controlBlockXPath(TControl tControl) {
        return ControlBlockEnum.from(tControl.getClass()).getElementName() + "[@name=\"" + tControl.getName() + "\"]";
    }

    public CriteriaOrError getCriteria(TIED tied, TCBType cbType, String cbName) {
        Optional<TCompasSystemVersion> compasSystemVersion = PrivateUtils.extractCompasPrivate(tied, TCompasSystemVersion.class);
        if (compasSystemVersion.isEmpty()) {
            return new CriteriaOrError(null, "No private COMPAS-SystemVersion found in this IED");
        }
        if (StringUtils.isBlank(compasSystemVersion.get().getMainSystemVersion())
            || (StringUtils.isBlank(compasSystemVersion.get().getMinorSystemVersion()))) {
            return new CriteriaOrError(null, "Missing MainSystemVersion or MinorSystemVersion attribute in COMPAS-SystemVersion private of IED");
        }
        String systemVersionWithoutV = removeVFromSystemVersion(compasSystemVersion.get());
        Optional<TCompasICDHeader> compasICDHeader = PrivateUtils.extractCompasPrivate(tied, TCompasICDHeader.class);
        if (compasICDHeader.isEmpty()) {
            return new CriteriaOrError(null, "No private COMPAS-ICDHeader found in this IED");
        }
        if (compasICDHeader.get().getIEDSystemVersioninstance() == null) {
            return new CriteriaOrError(null, "No IEDSystemVersioninstance in the COMPAS-ICDHeader of this IED");
        }
        TBayIntOrExt bayIntOrExt = cbName.endsWith("I") ? TBayIntOrExt.BAY_INTERNAL : TBayIntOrExt.BAY_EXTERNAL;

        return new CriteriaOrError(
                new Criteria(cbType,
                        systemVersionWithoutV,
                        TIEDType.fromValue(compasICDHeader.get().getIEDType().value()),
                        TIEDRedundancy.fromValue(compasICDHeader.get().getIEDredundancy().value()),
                        compasICDHeader.get().getIEDSystemVersioninstance(),
                        bayIntOrExt), null);
    }

    private String removeVFromSystemVersion(TCompasSystemVersion compasSystemVersion) {
        String[] minorVersionParts = compasSystemVersion.getMinorSystemVersion().split("\\.");
        return (minorVersionParts.length == 3) ?
                compasSystemVersion.getMainSystemVersion() + "." + minorVersionParts[0] + "." + minorVersionParts[1]
                : null;
    }

    private Criteria vlanToCriteria(TVlan vlan) {
        requireNotNull(vlan.getCBType(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "CBType");
        requireNotBlank(vlan.getXY(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "XY");
        requireNotBlank(vlan.getZW(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "ZW");
        requireNotNull(vlan.getIEDType(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "IEDType");
        requireNotNull(vlan.getIEDRedundancy(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "IEDRedundancy");
        requireNotBlank(vlan.getIEDSystemVersionInstance(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "IEDSystemVersionInstance");
        requireNotNull(vlan.getBayIntOrExt(), MISSING_ATTRIBUTE_IN_GSE_SMV_CB_COM + "BayIntOrExt");

        return new Criteria(
                vlan.getCBType(),
                vlan.getXY() + "." + vlan.getZW(),
                vlan.getIEDType(),
                vlan.getIEDRedundancy(),
                toIedSystemVersionInstance(vlan.getIEDSystemVersionInstance()),
                vlan.getBayIntOrExt()
        );
    }

    private Settings vlanToSetting(TVlan vlan) {
        return new Settings(toVLanId(vlan.getVlanId()), toVlanPriority(vlan.getVlanPriority()), toDurationInMilliSec(vlan.getMinTime()), toDurationInMilliSec(vlan.getMaxTime()));
    }

    private void requireNotBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new ScdException(message);
        }
    }

    private void requireNotNull(Object o, String message) {
        if (Objects.isNull(o)) {
            throw new ScdException(message);
        }
    }

    private BigInteger toIedSystemVersionInstance(String strIedSystemVersionInstance) {
        if (StringUtils.isBlank(strIedSystemVersionInstance)) {
            return null;
        }
        BigInteger iedSystemVersionInstance;
        try {
            iedSystemVersionInstance = new BigInteger(strIedSystemVersionInstance);
        } catch (NumberFormatException e) {
            throw new ScdException("Error in Control Block communication setting file: IED System Version Instance must be an integer, but got : %s".formatted(strIedSystemVersionInstance));
        }
        return iedSystemVersionInstance;
    }

    private Integer toVLanId(String strVlanId) {
        if (StringUtils.isBlank(strVlanId) || NONE.equalsIgnoreCase(strVlanId)) {
            return null;
        }
        int vlanId;
        try {
            vlanId = Integer.parseInt(strVlanId);
        } catch (NumberFormatException e) {
            throw new ScdException("Error in Control Block communication setting file: VLAN ID must be an integer or '%s', but got : %s".formatted(NONE, strVlanId));
        }
        if (vlanId < 0 || vlanId > MAX_VLAN_ID) {
            throw new ScdException("Error in Control Block communication setting file: VLAN ID must be between 0 and %d, but got : %s".formatted(MAX_VLAN_ID, strVlanId));
        }
        return vlanId;
    }

    private static Byte toVlanPriority(String strVlanPriority) {
        if (StringUtils.isBlank(strVlanPriority) || NONE.equalsIgnoreCase(strVlanPriority)) {
            return null;
        }
        byte vlanPriority;
        try {
            vlanPriority = Byte.parseByte(strVlanPriority);
        } catch (NumberFormatException e) {
            throw new ScdException("Error in Control Block communication setting file: VLAN Priority must be an integer or '%s', but got : %s".formatted(NONE, strVlanPriority));
        }
        if (vlanPriority < 0 || vlanPriority > MAX_VLAN_PRIORITY) {
            throw new ScdException("Error in Control Block communication setting file: VLAN PRIORITY must be between 0 and %d, but got : %s".formatted(MAX_VLAN_PRIORITY, strVlanPriority));
        }
        return vlanPriority;
    }

    private TDurationInMilliSec toDurationInMilliSec(String strDuration) {
        if (StringUtils.isBlank(strDuration) || NONE.equalsIgnoreCase(strDuration)) {
            return null;
        }
        long duration;
        try {
            duration = Long.parseLong(strDuration);
        } catch (NumberFormatException e) {
            throw new ScdException("Error in Control Block communication setting file: VLAN MinTime and MaxTime must be an integer or '%s', but got : %s".formatted(NONE, strDuration));
        }
        return SclConstructorHelper.newDurationInMilliSec(duration);
    }

    /**
     * Key to search for a control block communication setting
     */
    public record Criteria(TCBType cbType, String systemVersionWithoutV, TIEDType iedType, TIEDRedundancy iedRedundancy, BigInteger iedSystemVersionInstance, TBayIntOrExt bayIntOrExt) {
    }

    /**
     * Communication settings for ControlBlock or Error message
     */
    public record CriteriaOrError(Criteria criteria, String errorMessage) {
    }

    /**
     * Communication settings for ControlBlock
     */
    public record Settings(Integer vlanId, Byte vlanPriority, TDurationInMilliSec minTime, TDurationInMilliSec maxTime) {
    }

    /**
     * All settings of CbCom in a useful format
     */
    record CbComSettings(LongStream appIds, LongStream macAddresses, Map<Criteria, Settings> settingsByCriteria) {
    }

    record IedApLd(TIED ied, String apName, TLDevice lDevice) {
        String getXPath() {
            return """
                    /SCL/IED[@name="%s"]/AccessPoint[@name="%s"]/Server/LDevice[@inst="%s"]""".formatted(ied.getName(), apName, lDevice.getInst());
        }
    }

    /**
     * ControlBlock key. Values that uniquely identify a ControlBlock in a SCD.
     *
     * @param iedName name of IED containing the ControlBlock
     * @param LDInst  inst of LD containing the ControlBlock
     * @param cbName  name of the ControlBlock
     */
    record CbKey(String iedName, String LDInst, String cbName) {
    }

    /**
     * APPID
     *
     * @param appId APPID
     */
    record AppId(long appId) {
        static Optional<AppId> from(TAddress address) {
            if (address == null) {
                return Optional.empty();
            }
            return Utils.extractFromP(APPID_P_TYPE, address.getP())
                    .map(appId -> Integer.parseInt(appId, HEXADECIMAL_BASE))
                    .map(AppId::new);
        }
    }

    /**
     * MAC-Address
     *
     * @param mac MAC-Address
     */
    record Mac(long mac) {
        static Optional<Mac> from(TAddress address) {
            if (address == null) {
                return Optional.empty();
            }
            return Utils.extractFromP(MAC_ADDRESS_P_TYPE, address.getP())
                    .map(Utils::macAddressToLong)
                    .map(Mac::new);
        }
    }
}
