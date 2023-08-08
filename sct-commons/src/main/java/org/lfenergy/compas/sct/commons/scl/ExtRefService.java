// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.*;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.Utils.isExtRefFeedBySameControlBlock;

public final class ExtRefService {

    private static final String MESSAGE_MISSING_IED_NAME_PARAMETER = "IED.name parameter is missing";

    private ExtRefService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Updates iedName attribute of all ExtRefs in the Scd.
     *
     * @return list of encountered errors
     */
    public static List<SclReportItem> updateAllExtRefIedNames(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        List<SclReportItem> iedErrors = validateIed(sclRootAdapter);
        if (!iedErrors.isEmpty()) {
            return iedErrors;
        }
        Map<String, IEDAdapter> icdSystemVersionToIed = sclRootAdapter.streamIEDAdapters()
                .collect(Collectors.toMap(
                        iedAdapter -> iedAdapter.getCompasICDHeader()
                                .map(TCompasICDHeader::getICDSystemVersionUUID)
                                .orElseThrow(), // Value presence is checked by method validateIed called above
                        Function.identity()
                ));

        return sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .filter(LDeviceAdapter::hasLN0)
                .map(LDeviceAdapter::getLN0Adapter)
                .filter(LN0Adapter::hasInputs)
                .map(LN0Adapter::getInputsAdapter)
                .map(inputsAdapter -> inputsAdapter.updateAllExtRefIedNames(icdSystemVersionToIed))
                .flatMap(List::stream).collect(Collectors.toList());
    }

    private static List<SclReportItem> validateIed(SclRootAdapter sclRootAdapter) {
        List<SclReportItem> iedErrors = new ArrayList<>(checkIedCompasIcdHeaderAttributes(sclRootAdapter));
        iedErrors.addAll(checkIedUnityOfIcdSystemVersionUuid(sclRootAdapter));
        return iedErrors;
    }

    private static List<SclReportItem> checkIedCompasIcdHeaderAttributes(SclRootAdapter sclRootAdapter) {
        return sclRootAdapter.streamIEDAdapters()
                .map(iedAdapter -> {
                            Optional<TCompasICDHeader> compasPrivate = iedAdapter.getCompasICDHeader();
                            if (compasPrivate.isEmpty()) {
                                return iedAdapter.buildFatalReportItem(String.format("IED has no Private %s element", PrivateEnum.COMPAS_ICDHEADER.getPrivateType()));
                            }
                            if (StringUtils.isBlank(compasPrivate.get().getICDSystemVersionUUID())
                                    || StringUtils.isBlank(compasPrivate.get().getIEDName())) {
                                return iedAdapter.buildFatalReportItem(String.format("IED private %s as no icdSystemVersionUUID or iedName attribute",
                                        PrivateEnum.COMPAS_ICDHEADER.getPrivateType()));
                            }
                            return null;
                        }
                ).filter(Objects::nonNull)
                .toList();
    }

    private static List<SclReportItem> checkIedUnityOfIcdSystemVersionUuid(SclRootAdapter sclRootAdapter) {
        Map<String, List<TIED>> systemVersionToIedList = sclRootAdapter.getCurrentElem().getIED().stream()
                .collect(Collectors.groupingBy(ied -> PrivateService.extractCompasPrivate(ied, TCompasICDHeader.class)
                        .map(TCompasICDHeader::getICDSystemVersionUUID)
                        .orElse("")));

        return systemVersionToIedList.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> SclReportItem.fatal(entry.getValue().stream()
                                .map(tied -> new IEDAdapter(sclRootAdapter, tied))
                                .map(IEDAdapter::getXPath)
                                .collect(Collectors.joining(", ")),
                        "/IED/Private/compas:ICDHeader[@ICDSystemVersionUUID] must be unique" +
                                " but the same ICDSystemVersionUUID was found on several IED."))
                .toList();
    }

    /**
     * Create All DataSet and ControlBlock in the SCL based on the ExtRef
     *
     * @param scd input SCD object. It could be modified by adding new DataSet and ControlBlocks
     * @return list of encountered errors
     */
    public static List<SclReportItem> createDataSetAndControlBlocks(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Stream<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters);
        return createDataSetAndControlBlocks(lDeviceAdapters);
    }

    /**
     * Create All DataSet and ControlBlock for the ExtRef in given IED
     *
     * @param scd           input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName the name of the IED where the ExtRef are
     * @return list of encountered errors
     */
    public static List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        return createDataSetAndControlBlocks(iedAdapter.streamLDeviceAdapters());

    }

    /**
     * Create All DataSet and ControlBlock for the ExtRef in this LDevice
     *
     * @param scd               input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName     the name of the IED where the ExtRef are
     * @param targetLDeviceInst the name of the LDevice where the ExtRef are
     * @return list of encountered errors
     */
    public static List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst) {
        if (StringUtils.isBlank(targetIedName)) {
            throw new ScdException(MESSAGE_MISSING_IED_NAME_PARAMETER);
        }
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(targetLDeviceInst);
        return createDataSetAndControlBlocks(Stream.of(lDeviceAdapter));
    }

    private static List<SclReportItem> createDataSetAndControlBlocks(Stream<LDeviceAdapter> lDeviceAdapters) {
        return lDeviceAdapters
                .map(LDeviceAdapter::createDataSetAndControlBlocks)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Configure the network for all the ControlBlocks.
     * Create (or update if already existing) these elements
     * - the Communication/SubNetwork/ConnectedAP/GSE element, for the GSEControl blocks
     * - the Communication/SubNetwork/ConnectedAP/SMV element, for the SampledValueControl blocks
     *
     * @param scd                         input SCD object. The object will be modified with the new DataGSESet and SMV elements
     * @param controlBlockNetworkSettings a method tha gives the network configuration information for a given ControlBlock
     * @param rangesPerCbType             provide NetworkRanges for GSEControl and SampledValueControl. NetworkRanges contains :
     *                                    start-end app APPID range (long value), start-end MAC-Addresses (Mac-Addresses values: Ex: "01-0C-CD-01-01-FF")
     * @return list of encountered errors
     * @see Utils#macAddressToLong(String) for the expected MAC address format
     * @see ControlBlockNetworkSettings
     * @see ControlBlockNetworkSettings.RangesPerCbType
     * @see ControlBlockNetworkSettings.NetworkRanges
     */
    public static List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                RangesPerCbType rangesPerCbType) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.gse(), ControlBlockEnum.GSE));
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.sampledValue(), ControlBlockEnum.SAMPLED_VALUE));
        return sclReportItems;
    }
    private static List<SclReportItem> configureNetworkForControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                        NetworkRanges networkRanges, ControlBlockEnum controlBlockEnum) {
        PrimitiveIterator.OfLong appIdIterator = Utils.sequence(networkRanges.appIdStart(), networkRanges.appIdEnd());
        Iterator<String> macAddressIterator = Utils.macAddressSequence(networkRanges.macAddressStart(), networkRanges.macAddressEnd());

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(iedAdapter ->
                        iedAdapter.streamLDeviceAdapters()
                                .filter(LDeviceAdapter::hasLN0)
                                .map(LDeviceAdapter::getLN0Adapter)
                                .flatMap(ln0Adapter -> ln0Adapter.streamControlBlocks(controlBlockEnum))
                                .map(controlBlockAdapter -> configureControlBlockNetwork(controlBlockNetworkSettings, appIdIterator, macAddressIterator, controlBlockAdapter)))
                .flatMap(Optional::stream)
                .toList();
    }
    private static Optional<SclReportItem> configureControlBlockNetwork(ControlBlockNetworkSettings controlBlockNetworkSettings, PrimitiveIterator.OfLong appIdIterator, Iterator<String> macAddressIterator, ControlBlockAdapter controlBlockAdapter) {
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);
        if (settingsOrError.errorMessage() != null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because: " + settingsOrError.errorMessage()));
        }
        Settings settings = settingsOrError.settings();
        if (settings == null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because no settings was provided"));
        }
        if (settings.vlanId() == null) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because no Vlan Id was provided in the settings"));
        }
        if (!appIdIterator.hasNext()) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because range of appId is exhausted"));
        }
        if (!macAddressIterator.hasNext()) {
            return Optional.of(controlBlockAdapter.buildFatalReportItem(
                    "Cannot configure network for this ControlBlock because range of MAC Address is exhausted"));
        }

        return controlBlockAdapter.configureNetwork(appIdIterator.nextLong(), macAddressIterator.next(), settings.vlanId(), settings.vlanPriority(),
                settings.minTime(), settings.maxTime());
    }

    /**
     * Remove ExtRef which are fed by same Control Block
     *
     * @return list ExtRefs without duplication
     */
    public static List<TExtRef> filterDuplicatedExtRefs(List<TExtRef> tExtRefs) {
        List<TExtRef> filteredList = new ArrayList<>();
        tExtRefs.forEach(tExtRef -> {
            if (filteredList.stream().noneMatch(t -> isExtRefFeedBySameControlBlock(tExtRef, t)))
                filteredList.add(tExtRef);
        });
        return filteredList;
    }

    /**
     * ExtRef Binding For LDevice (inst=LDEPF) that matching LDEPF configuration
     * @param scd SCL
     * @param settings ILDEPFSettings
     * @return list of encountered errors
     */
    public static List<SclReportItem> manageBindingForLDEPF(SCL scd, ILDEPFSettings settings) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        List<SclReportItem> sclReportItems = new ArrayList<>();
        sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> !iedAdapter.getName().equals(IED_TEST_NAME))
                .map(iedAdapter -> iedAdapter.findLDeviceAdapterByLdInst(LDEVICE_LDEPF))
                .flatMap(Optional::stream)
                .forEach(lDeviceAdapter ->
                        lDeviceAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems)
                                .forEach(extRefBayRef -> settings.getLDEPFSettingDataMatchExtRef(extRefBayRef.extRef())
                                        .ifPresent(lDPFSettingMatchingExtRef -> {
                                            List<TIED> iedSources = settings.getIedSources(sclRootAdapter, extRefBayRef.compasBay(), lDPFSettingMatchingExtRef);
                                            if (iedSources.size() == 1) {
                                                updateLDEPFExtRefBinding(extRefBayRef.extRef(), iedSources.get(0), lDPFSettingMatchingExtRef);
                                                sclReportItems.addAll(updateLDEPFDos(lDeviceAdapter, extRefBayRef.extRef(), lDPFSettingMatchingExtRef));
                                            } else {
                                                if (iedSources.size() > 1) {
                                                    sclReportItems.add(SclReportItem.warning(null, "There is more than one IED source to bind the signal " +
                                                            "/IED@name=" + extRefBayRef.iedName() + "/LDevice@inst=LDEPF/LN0" +
                                                            "/ExtRef@desc=" + extRefBayRef.extRef().getDesc()));
                                                }
                                            }
                                        }))
                );
        return sclReportItems;

    }

    private static void updateLDEPFExtRefBinding(TExtRef extRef, TIED iedSource, LDEPFSettingData setting) {
        extRef.setIedName(iedSource.getName());
        extRef.setLdInst(setting.getLdInst());
        extRef.getLnClass().add(setting.getLnClass());
        extRef.setLnInst(setting.getLnInst());
        if(setting.getLnPrefix() != null){
            extRef.setPrefix(setting.getLnPrefix());
        }
        String doName = StringUtils.isEmpty(setting.getDoInst()) || StringUtils.isBlank(setting.getDoInst()) || setting.getDoInst().equals("0") ? setting.getDoName() : setting.getDoName() + setting.getDoInst();
        extRef.setDoName(doName);
    }

    private static List<SclReportItem> updateLDEPFDos(LDeviceAdapter lDeviceAdapter, TExtRef extRef, LDEPFSettingData setting) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<DoNameAndDaName> doNameAndDaNameList = List.of(
                new DoNameAndDaName(CHNUM1_DO_NAME, DU_DA_NAME),
                new DoNameAndDaName(LEVMOD_DO_NAME, SETVAL_DA_NAME),
                new DoNameAndDaName(MOD_DO_NAME, STVAL_DA_NAME),
                new DoNameAndDaName(SRCREF_DO_NAME, SETSRCREF_DA_NAME)
        );
        if (setting.getChannelDigitalNum() != null && setting.getChannelAnalogNum() == null) {
            //digital
            lDeviceAdapter.findLnAdapter(LN_RBDR, String.valueOf(setting.getChannelDigitalNum()), null)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
            lDeviceAdapter.findLnAdapter(LN_RBDR, String.valueOf(setting.getChannelDigitalNum()), LN_PREFIX_B)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
        }
        if (setting.getChannelDigitalNum() == null && setting.getChannelAnalogNum() != null) {
            //analog
            lDeviceAdapter.findLnAdapter(LN_RADR, String.valueOf(setting.getChannelAnalogNum()), null)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
            lDeviceAdapter.findLnAdapter(LN_RADR, String.valueOf(setting.getChannelAnalogNum()), LN_PREFIX_A)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
        }
        return sclReportItems;
    }

    private static Optional<SclReportItem> updateVal(AbstractLNAdapter<?> lnAdapter, String doName, String daName, TExtRef extRef, LDEPFSettingData setting) {
        String value = switch (daName) {
            case DU_DA_NAME -> setting.getChannelShortLabel();
            case SETVAL_DA_NAME ->
                    LN_PREFIX_B.equals(lnAdapter.getPrefix()) || LN_PREFIX_A.equals(lnAdapter.getPrefix()) ? setting.getChannelLevModQ() : setting.getChannelLevMod();
            case STVAL_DA_NAME -> LdeviceStatus.ON.getValue();
            case SETSRCREF_DA_NAME -> computeDaiValue(lnAdapter, extRef, setting.getDaName());
            default -> null;
        };
        return lnAdapter.getDOIAdapterByName(doName).updateDAI(daName, value);
    }

    private record DoNameAndDaName(String doName, String daName) {
    }

    private static String computeDaiValue(AbstractLNAdapter<?> lnAdapter, TExtRef extRef, String daName) {
        if (LN_PREFIX_B.equals(lnAdapter.getPrefix()) || LN_PREFIX_A.equals(lnAdapter.getPrefix())) {
            return extRef.getIedName() +
                    extRef.getLdInst() + "/" +
                    StringUtils.trimToEmpty(extRef.getPrefix()) +
                    extRef.getLnClass().get(0) +
                    StringUtils.trimToEmpty(extRef.getLnInst()) + "." +
                    extRef.getDoName() + "." + Q_DA_NAME;
        } else {
            return extRef.getIedName() +
                    extRef.getLdInst() + "/" +
                    StringUtils.trimToEmpty(extRef.getPrefix()) +
                    extRef.getLnClass().get(0) +
                    StringUtils.trimToEmpty(extRef.getLnInst()) + "." +
                    extRef.getDoName() + "." +
                    daName;
        }
    }

}
