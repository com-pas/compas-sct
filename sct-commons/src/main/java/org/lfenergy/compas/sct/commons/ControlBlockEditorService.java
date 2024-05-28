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
        Map<CbKey, AppIdAndMac> appIdsAndMacsToReuse = subnetworksToReuse != null && !subnetworksToReuse.isEmpty() ?
                computeAppIdsAndMacToReuse(scd, subnetworksToReuse)
                : Collections.emptyMap();
        return Stream.concat(
                        configureNetworkForControlBlocks(scd, appIdsAndMacsToReuse, cbCom, TCBType.GOOSE),
                        configureNetworkForControlBlocks(scd, appIdsAndMacsToReuse, cbCom, TCBType.SV))
                .toList();
    }

    private Stream<SclReportItem> configureNetworkForControlBlocks(SCL scl, Map<CbKey, AppIdAndMac> appIdsAndMacsToReuse, CBCom cbCom, TCBType tcbType) {
        CbComSettings cbComSettings;
        try {
            cbComSettings = parseCbCom(cbCom, tcbType);
        } catch (ScdException ex) {
            return Stream.of(SclReportItem.error("Control Block Communication setting files", ex.getMessage()));
        }
        List<Long> appIdToReuse = appIdsAndMacsToReuse.values().stream().map(AppIdAndMac::appId).toList();
        List<Long> macToReuse = appIdsAndMacsToReuse.values().stream().map(AppIdAndMac::mac).toList();
        PrimitiveIterator.OfLong appIdIterator = cbComSettings.appIds.filter(appId -> !appIdToReuse.contains(appId)).iterator();
        PrimitiveIterator.OfLong macIterator = cbComSettings.macAddresses.filter(mac -> !macToReuse.contains(mac)).iterator();
        return scl.getIED().stream()
                .flatMap(tied ->
                        tied.getAccessPoint()
                                .stream()
                                .filter(tAccessPoint -> tAccessPoint.isSetServer() && tAccessPoint.getServer().isSetLDevice())
                                .flatMap(tAccessPoint -> tAccessPoint.getServer().getLDevice().stream()
                                        .map(tlDevice -> new IedApLd(tied, tAccessPoint.getName(), tlDevice))
                                )
                )
                .filter(iedApLd -> iedApLd.lDevice().isSetLN0())
                .flatMap(iedApLd -> controlService.getControls(iedApLd.lDevice().getLN0(), ControlBlockEnum.from(tcbType).getControlBlockClass())
                        .map(tControl -> {
                            CriteriaOrError criteriaOrError = getCriteria(iedApLd.ied(), tcbType, tControl.getName());
                            if (criteriaOrError.errorMessage != null) {
                                return Optional.of(SclReportItem.error(iedApLd.getXPath(), criteriaOrError.errorMessage));
                            }
                            Settings settings = cbComSettings.settingsByCriteria.get(criteriaOrError.criteria);
                            if (settings == null) {
                                return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because: No controlBlock communication settings found with these " + criteriaOrError.criteria);
                            }
                            AppIdAndMac reuseAppIdAndMac = appIdsAndMacsToReuse.get(new CbKey(iedApLd.ied.getName(), iedApLd.lDevice.getInst(), tControl.getName()));
                            return configureControlBlockNetwork(scl.getCommunication(), settings, appIdIterator, macIterator, tControl, iedApLd, reuseAppIdAndMac);
                        })
                        .flatMap(Optional::stream)
                );
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

    private Optional<SclReportItem> configureControlBlockNetwork(TCommunication tCommunication, Settings settings, PrimitiveIterator.OfLong appIdIterator, PrimitiveIterator.OfLong macAddressIterator, TControl tControl, IedApLd iedApLd, AppIdAndMac reuseAppIdAndMac) {
        Optional<TConnectedAP> optConApAdapter = findConnectedAp(tCommunication, iedApLd.ied.getName(), iedApLd.apName);
        if (optConApAdapter.isEmpty()) {
            return newError(iedApLd, tControl, "Cannot configure communication for ControlBlock because no ConnectedAP found for AccessPoint");
        }
        TConnectedAP tConnectedAP = optConApAdapter.get();
        if (settings.vlanId() == null) {
            return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because no Vlan Id was provided in the settings");
        }
        AppIdAndMac appIdAndMac;
        if (reuseAppIdAndMac != null) {
            appIdAndMac = reuseAppIdAndMac;
        } else {
            if (!appIdIterator.hasNext()) {
                return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because range of appId is exhausted");
            }
            if (!macAddressIterator.hasNext()) {
                return newError(iedApLd, tControl, "Cannot configure communication for this ControlBlock because range of MAC Address is exhausted");
            }
            appIdAndMac = new AppIdAndMac(appIdIterator.nextLong(), macAddressIterator.nextLong());
        }

        List<TP> listOfPs = new ArrayList<>();
        listOfPs.add(newP(APPID_P_TYPE, Utils.toHex(appIdAndMac.appId(), APPID_LENGTH)));
        listOfPs.add(newP(MAC_ADDRESS_P_TYPE, Utils.longToMacAddress(appIdAndMac.mac())));
        listOfPs.add(newP(VLAN_ID_P_TYPE, Utils.toHex(settings.vlanId(), VLAN_ID_LENGTH)));
        if (settings.vlanPriority() != null) {
            listOfPs.add(newP(VLAN_PRIORITY_P_TYPE, String.valueOf(settings.vlanPriority())));
        }

        switch (tControl) {
            case TGSEControl ignored -> updateGseOrCreateIfNotExists(tConnectedAP, iedApLd.lDevice().getInst(), tControl.getName(), listOfPs, SclConstructorHelper.newDurationInMilliSec(settings.minTime), SclConstructorHelper.newDurationInMilliSec(settings.maxTime));
            case TSampledValueControl ignored -> updateSmvOrCreateIfNotExists(tConnectedAP, iedApLd.lDevice().getInst(), tControl.getName(), listOfPs);
            default -> throw new ScdException("Unsupported Control Block type for communication configuration : " + tControl.getClass().getName());
        }
        return Optional.empty();
    }

    private Map<CbKey, AppIdAndMac> computeAppIdsAndMacToReuse(SCL scd, List<TSubNetwork> subnetworksToReuse) {
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
                        .flatMap(tControlBlock -> AppIdAndMac.from(tControlBlock.getAddress())
                                .map(appIdAndMac -> new SimpleEntry<>(new CbKey(tConnectedAP.getIedName(), tControlBlock.getLdInst(), tControlBlock.getCbName()), appIdAndMac))
                                .stream()
                        )
                )
                .filter(entry -> allControlBlocksInScd.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Optional<TConnectedAP> findConnectedAp(TCommunication tCommunication, String iedName, String apName) {
        if (tCommunication == null || !tCommunication.isSetSubNetwork()) {
            return Optional.empty();
        }
        return tCommunication.getSubNetwork().stream()
                .filter(TSubNetwork::isSetConnectedAP)
                .flatMap(tSubNetwork -> tSubNetwork.getConnectedAP().stream())
                .filter(tConnectedAP -> iedName.equals(tConnectedAP.getIedName()) && apName.equals(tConnectedAP.getApName()))
                .findFirst();
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
     * Pair of APPID and MAC-Address
     *
     * @param appId APPID
     * @param mac   MAC-Address
     */
    record AppIdAndMac(long appId, long mac) {
        static Optional<AppIdAndMac> from(TAddress address) {
            if (address == null) {
                return Optional.empty();
            }
            return Utils.extractFromP(APPID_P_TYPE, address.getP())
                    .map(appId -> Integer.parseInt(appId, HEXADECIMAL_BASE))
                    .flatMap(appId -> Utils.extractFromP(MAC_ADDRESS_P_TYPE, address.getP())
                            .map(Utils::macAddressToLong)
                            .map(macAddress -> new AppIdAndMac(appId, macAddress)));
        }
    }
}
