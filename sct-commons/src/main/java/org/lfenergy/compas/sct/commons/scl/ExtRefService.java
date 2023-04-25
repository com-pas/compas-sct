// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettingsSupplier.LDEPFSetting;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo.ExtRefBayReference;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.IED_TEST_NAME;
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
    public static SclReport updateAllExtRefIedNames(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        List<SclReportItem> iedErrors = validateIed(sclRootAdapter);
        if (!iedErrors.isEmpty()) {
            return new SclReport(sclRootAdapter, iedErrors);
        }
        Map<String, IEDAdapter> icdSystemVersionToIed = sclRootAdapter.streamIEDAdapters()
                .collect(Collectors.toMap(
                        iedAdapter -> iedAdapter.getCompasICDHeader()
                                .map(TCompasICDHeader::getICDSystemVersionUUID)
                                .orElseThrow(), // Value presence is checked by method validateIed called above
                        Function.identity()
                ));

        List<SclReportItem> extRefErrors = sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .filter(LDeviceAdapter::hasLN0)
                .map(LDeviceAdapter::getLN0Adapter)
                .filter(LN0Adapter::hasInputs)
                .map(LN0Adapter::getInputsAdapter)
                .map(inputsAdapter -> inputsAdapter.updateAllExtRefIedNames(icdSystemVersionToIed))
                .flatMap(List::stream).toList();

        return new SclReport(sclRootAdapter, extRefErrors);
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
     * @return a report with all errors encountered
     */
    public static SclReport createDataSetAndControlBlocks(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Stream<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters);
        return createDataSetAndControlBlocks(sclRootAdapter, lDeviceAdapters);
    }

    /**
     * Create All DataSet and ControlBlock for the ExtRef in given IED
     *
     * @param scd           input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName the name of the IED where the ExtRef are
     * @return a report with all the errors encountered
     */
    public static SclReport createDataSetAndControlBlocks(SCL scd, String targetIedName) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        return createDataSetAndControlBlocks(sclRootAdapter, iedAdapter.streamLDeviceAdapters());

    }

    /**
     * Create All DataSet and ControlBlock for the ExtRef in this LDevice
     *
     * @param scd               input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName     the name of the IED where the ExtRef are
     * @param targetLDeviceInst the name of the LDevice where the ExtRef are
     * @return a report with all encountered errors
     */
    public static SclReport createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst) {
        if (StringUtils.isBlank(targetIedName)) {
            throw new ScdException(MESSAGE_MISSING_IED_NAME_PARAMETER);
        }
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(targetLDeviceInst);
        return createDataSetAndControlBlocks(sclRootAdapter, Stream.of(lDeviceAdapter));
    }

    private static SclReport createDataSetAndControlBlocks(SclRootAdapter sclRootAdapter, Stream<LDeviceAdapter> lDeviceAdapters) {
        List<SclReportItem> sclReportItems = lDeviceAdapters
                .map(LDeviceAdapter::createDataSetAndControlBlocks)
                .flatMap(List::stream)
                .toList();
        return new SclReport(sclRootAdapter, sclReportItems);
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
     * @return a report with all the errors encountered
     * @see Utils#macAddressToLong(String) for the expected MAC address format
     * @see ControlBlockNetworkSettings
     * @see ControlBlockNetworkSettings.RangesPerCbType
     * @see ControlBlockNetworkSettings.NetworkRanges
     */
    public static SclReport configureNetworkForAllControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                RangesPerCbType rangesPerCbType) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.gse(), ControlBlockEnum.GSE));
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.sampledValue(), ControlBlockEnum.SAMPLED_VALUE));
        return new SclReport(new SclRootAdapter(scd), sclReportItems);
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
     * @param settingsSupplier LDEPFSettingsSupplier
     * @return a report contains errors
     */
    public static SclReport manageBindingForLDEPF(SCL scd, LDEPFSettingsSupplier settingsSupplier) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<ExtRefBayReference> extRefBayReferences = sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> !iedAdapter.getName().equals(IED_TEST_NAME))
                .map(iedAdapter -> iedAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems))
                .flatMap(List::stream).toList();
        for (ExtRefBayReference extRefBayRef: extRefBayReferences){
            var lDPFSettingMatchingExtRef = settingsSupplier.getLDEPFSettingMatchingExtRef(extRefBayRef.extRef());
            if(lDPFSettingMatchingExtRef.isPresent()){
                List<TIED> iedSources = settingsSupplier.getIedSources(sclRootAdapter, extRefBayRef.compasBay(), lDPFSettingMatchingExtRef.get());
                if(iedSources.size() != 1) {
                    if(iedSources.size() > 1) {
                        sclReportItems.add(SclReportItem.warning(null, "There is more than one IED source to bind the signal " +
                                "/IED@name="+extRefBayRef.iedName()+"/LDevice@inst=LDEPF/LN0" +
                                "/ExtRef@desc="+extRefBayRef.extRef().getDesc()));
                    }
                    continue;
                }
                updateLDEPFExtRefBinding(extRefBayRef.extRef(), iedSources.get(0), lDPFSettingMatchingExtRef.get());
            }
        }
        return new SclReport(sclRootAdapter, sclReportItems);
    }

    private static void updateLDEPFExtRefBinding(TExtRef extRef, TIED iedSource, LDEPFSetting setting) {
        extRef.setIedName(iedSource.getName());
        extRef.setLdInst(setting.ldInst());
        extRef.getLnClass().add(setting.lnClass());
        extRef.setLnInst(setting.lnInst());
        if(setting.lnPrefix() != null){
            extRef.setPrefix(setting.lnPrefix());
        }
        var doName = setting.doInst().equals("0") ? setting.doName() : setting.doName()+setting.doInst() ;
        extRef.setDoName(doName);

    }

}
