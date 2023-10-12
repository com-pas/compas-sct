// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.ExtRefEditor;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.NetworkRanges;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.RangesPerCbType;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.Settings;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.SettingsOrError;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.Utils.isExtRefFeedBySameControlBlock;

public class ExtRefService implements ExtRefEditor {
    private static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";

    @Override
    public void updateExtRefBinders(SCL scd, ExtRefInfo extRefInfo) throws ScdException {
        if (extRefInfo.getBindingInfo() == null || extRefInfo.getSignalInfo() == null) {
            throw new ScdException("ExtRef Signal and/or Binding information are missing");
        }
        String iedName = extRefInfo.getHolderIEDName();
        String ldInst = extRefInfo.getHolderLDInst();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(() -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName)));

        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(extRefInfo.getHolderLnClass())
                .withLnInst(extRefInfo.getHolderLnInst())
                .withLnPrefix(extRefInfo.getHolderLnPrefix())
                .build();

        abstractLNAdapter.updateExtRefBinders(extRefInfo);
    }

    @Override
    public TExtRef updateExtRefSource(SCL scd, ExtRefInfo extRefInfo) throws ScdException {
        String iedName = extRefInfo.getHolderIEDName();
        String ldInst = extRefInfo.getHolderLDInst();
        String lnClass = extRefInfo.getHolderLnClass();
        String lnInst = extRefInfo.getHolderLnInst();
        String prefix = extRefInfo.getHolderLnPrefix();

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if (signalInfo == null || !signalInfo.isValid()) {
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if (bindingInfo == null || !bindingInfo.isValid()) {
            throw new ScdException(INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO);
        }
        if (bindingInfo.getIedName().equals(iedName) || TServiceType.POLL.equals(bindingInfo.getServiceType())) {
            throw new ScdException("Internal binding can't have control block");
        }
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if (sourceInfo == null || !sourceInfo.isValid()) {
            throw new ScdException(INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO);
        }

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst);
        AbstractLNAdapter<?> anLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();
        return anLNAdapter.updateExtRefSource(extRefInfo);
    }

    @Override
    public List<SclReportItem> updateAllExtRefIedNames(SCL scd) {
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
                .flatMap(List::stream).toList();
    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, Set<FcdaForDataSetsCreation> allowedFcdas) {
        checkFcdaInitDataPresence(allowedFcdas);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Stream<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters);
        return createDataSetAndControlBlocks(lDeviceAdapters, allowedFcdas);
    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, Set<FcdaForDataSetsCreation> allowedFcdas) {
        checkFcdaInitDataPresence(allowedFcdas);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        return createDataSetAndControlBlocks(iedAdapter.streamLDeviceAdapters(), allowedFcdas);

    }

    @Override
    public List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst, Set<FcdaForDataSetsCreation> allowedFcdas) {
        if (StringUtils.isBlank(targetIedName)) {
            throw new ScdException("IED.name parameter is missing");
        }
        checkFcdaInitDataPresence(allowedFcdas);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(targetIedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(targetLDeviceInst);
        return createDataSetAndControlBlocks(Stream.of(lDeviceAdapter), allowedFcdas);
    }

    private void checkFcdaInitDataPresence(Set<FcdaForDataSetsCreation> allowedFcdas) {
        if (allowedFcdas == null || allowedFcdas.isEmpty()) {
            throw new ScdException("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
        }
    }

    private List<SclReportItem> createDataSetAndControlBlocks(Stream<LDeviceAdapter> lDeviceAdapters, Set<FcdaForDataSetsCreation> allowedFcdas) {
        return lDeviceAdapters
                .map(lDeviceAdapter -> lDeviceAdapter.createDataSetAndControlBlocks(allowedFcdas))
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                   RangesPerCbType rangesPerCbType) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.gse(), ControlBlockEnum.GSE));
        sclReportItems.addAll(configureNetworkForControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType.sampledValue(), ControlBlockEnum.SAMPLED_VALUE));
        return sclReportItems;
    }

    @Override
    public List<SclReportItem> manageBindingForLDEPF(SCL scd, ILDEPFSettings settings) {
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

    private List<SclReportItem> validateIed(SclRootAdapter sclRootAdapter) {
        List<SclReportItem> iedErrors = new ArrayList<>(checkIedCompasIcdHeaderAttributes(sclRootAdapter));
        iedErrors.addAll(checkIedUnityOfIcdSystemVersionUuid(sclRootAdapter));
        return iedErrors;
    }

    private List<SclReportItem> checkIedCompasIcdHeaderAttributes(SclRootAdapter sclRootAdapter) {
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

    private List<SclReportItem> checkIedUnityOfIcdSystemVersionUuid(SclRootAdapter sclRootAdapter) {
        Map<String, List<TIED>> systemVersionToIedList = sclRootAdapter.getCurrentElem().getIED().stream()
                .collect(Collectors.groupingBy(ied -> PrivateUtils.extractCompasPrivate(ied, TCompasICDHeader.class)
                        .map(TCompasICDHeader::getICDSystemVersionUUID)
                        .orElse("")));

        return systemVersionToIedList.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> SclReportItem.error(entry.getValue().stream()
                                .map(tied -> new IEDAdapter(sclRootAdapter, tied))
                                .map(IEDAdapter::getXPath)
                                .collect(Collectors.joining(", ")),
                        "/IED/Private/compas:ICDHeader[@ICDSystemVersionUUID] must be unique" +
                                " but the same ICDSystemVersionUUID was found on several IED."))
                .toList();
    }

    private List<SclReportItem> configureNetworkForControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
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
    private Optional<SclReportItem> configureControlBlockNetwork(ControlBlockNetworkSettings controlBlockNetworkSettings, PrimitiveIterator.OfLong appIdIterator, Iterator<String> macAddressIterator, ControlBlockAdapter controlBlockAdapter) {
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

    private void updateLDEPFExtRefBinding(TExtRef extRef, TIED iedSource, LDEPFSettingData setting) {
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

    private List<SclReportItem> updateLDEPFDos(LDeviceAdapter lDeviceAdapter, TExtRef extRef, LDEPFSettingData setting) {
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

    private Optional<SclReportItem> updateVal(AbstractLNAdapter<?> lnAdapter, String doName, String daName, TExtRef extRef, LDEPFSettingData setting) {
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

    private String computeDaiValue(AbstractLNAdapter<?> lnAdapter, TExtRef extRef, String daName) {
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
