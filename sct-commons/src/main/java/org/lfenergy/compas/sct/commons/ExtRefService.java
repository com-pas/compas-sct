// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.ExtRefEditor;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.epf.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.Utils.*;

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
    public List<SclReportItem> manageBindingForLDEPF(SCL scd, EPF epf) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        if(!epf.isSetChannels()) return sclReportItems;
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> !iedAdapter.getName().contains("TEST"))
                .map(iedAdapter -> iedAdapter.findLDeviceAdapterByLdInst(LDEVICE_LDEPF))
                .flatMap(Optional::stream)
                .forEach(lDeviceAdapter -> lDeviceAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems)
                        .forEach(extRefBayRef -> epf.getChannels().getChannel().stream().filter(tChannel -> doesExtRefMatchLDEPFChannel(extRefBayRef.extRef(), tChannel))
                                .findFirst().ifPresent(channel -> {
                                    List<TIED> iedSources = getIedSources(sclRootAdapter, extRefBayRef.compasBay(), channel);
                                    if (iedSources.size() == 1) {
                                        updateLDEPFExtRefBinding(extRefBayRef.extRef(), iedSources.get(0), channel);
                                        sclReportItems.addAll(updateLDEPFDos(lDeviceAdapter, extRefBayRef.extRef(), channel));
                                    } else {
                                        if (iedSources.size() > 1) {
                                            sclReportItems.add(SclReportItem.warning(null, "There is more than one IED source to bind the signal " +
                                                    "/IED@name=" + extRefBayRef.iedName() + "/LDevice@inst=LDEPF/LN0" +
                                                    "/ExtRef@desc=" + extRefBayRef.extRef().getDesc()));
                                        }
                                        // If the source IED is not found, there will be no update or report message.
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
                            if (isBlank(compasPrivate.get().getICDSystemVersionUUID())
                                    || isBlank(compasPrivate.get().getIEDName())) {
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
                .filter(entry -> isNotBlank(entry.getKey()))
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> SclReportItem.error(entry.getValue().stream()
                                .map(tied -> new IEDAdapter(sclRootAdapter, tied))
                                .map(IEDAdapter::getXPath)
                                .collect(Collectors.joining(", ")),
                        "/IED/Private/compas:ICDHeader[@ICDSystemVersionUUID] must be unique" +
                                " but the same ICDSystemVersionUUID was found on several IED."))
                .toList();
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
     * Provides valid IED sources according to EPF configuration.<br/>
     * EPF verification include:<br/>
     * 1. COMPAS-Bay verification that should be closed to the provided Flow Kind<br/>
     * 2. COMPAS-ICDHeader verification that should match the provided parameters<br/>
     * 3. Active LDevice source object that should match the provided parameters<br/>
     * 4. Active LNode source object that should match the provided parameters<br/>
     * 5. Valid DataTypeTemplate Object hierarchy that should match the DO/DA/BDA parameters<br/>
     * @param sclRootAdapter SCL scl object
     * @param compasBay TCompasBay represent Bay Private
     * @param channel TChannel represent parameters
     * @return the IED sources matching the LDEPF parameters
     */
    private static List<TIED> getIedSources(SclRootAdapter sclRootAdapter, TCompasBay compasBay, TChannel channel) {
        return sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> (channel.getBayScope().equals(TCBscopeType.BAY_EXTERNAL)
                        && iedAdapter.getPrivateCompasBay().stream().noneMatch(bay -> bay.getUUID().equals(compasBay.getUUID())))
                        || (channel.getBayScope().equals(TCBscopeType.BAY_INTERNAL)
                        && iedAdapter.getPrivateCompasBay().stream().anyMatch(bay -> bay.getUUID().equals(compasBay.getUUID()))))
                .filter(iedAdapter -> doesIcdHeaderMatchLDEPFChannel(iedAdapter, channel))
                .filter(iedAdapter -> getActiveSourceLDeviceByLDEPFChannel(iedAdapter, channel)
                        .map(lDeviceAdapter -> getActiveLNSourceByLDEPFChannel(lDeviceAdapter, channel)
                                .map(lnAdapter -> isValidDataTypeTemplate(lnAdapter, channel))
                                .orElse(false))
                        .orElse(false))
                .map(IEDAdapter::getCurrentElem).limit(2).toList();
    }

    /**
     * Verify if an Extref matches the EPF Channel or not.
     * @param extRef TExtRef
     * @param tChannel TChannel
     * @return true if the TExtRef matches the EPF channel
     */
    private static Boolean doesExtRefMatchLDEPFChannel(TExtRef extRef, TChannel tChannel) {
        Boolean doesExtRefDescMatchAnalogChannel = tChannel.getChannelType().equals(TChannelType.ANALOG)
                && extRef.getDesc().startsWith("DYN_LDEPF_ANALOG CHANNEL " + tChannel.getChannelNum()+"_1_AnalogueValue")
                && extRef.getDesc().endsWith("_" + tChannel.getDAName() + "_1");
        Boolean doesExtRefDescMatchDigitalChannel = tChannel.getChannelType().equals(TChannelType.DIGITAL)
                && extRef.getDesc().startsWith("DYN_LDEPF_DIGITAL CHANNEL " + tChannel.getChannelNum()+"_1_BOOLEEN")
                && extRef.getDesc().endsWith("_" + tChannel.getDAName() + "_1");
        return extRef.isSetDesc() && (doesExtRefDescMatchAnalogChannel || doesExtRefDescMatchDigitalChannel)
                && extRef.isSetPLN() && Utils.lnClassEquals(extRef.getPLN(), tChannel.getLNClass())
                && extRef.isSetPDO() && extRef.getPDO().equals(tChannel.getDOName());
    }

    /**
     * Verify whether the IED satisfies the EPF channel for the private element `TCompasICDHeader`
     * @param iedAdapter IEDAdapter
     * @param channel TChannel
     * @return true if the TCompasICDHeader matches the EPF channel
     */
    private static boolean doesIcdHeaderMatchLDEPFChannel(IEDAdapter iedAdapter, TChannel channel) {
        return iedAdapter.getCompasICDHeader()
                .map(compasICDHeader -> compasICDHeader.getIEDType().value().equals(channel.getIEDType())
                        && compasICDHeader.getIEDredundancy().value().equals(channel.getIEDRedundancy().value())
                        && compasICDHeader.getIEDSystemVersioninstance().toString().equals(channel.getIEDSystemVersionInstance()))
                .orElse(false);
    }

    /**
     * Provides Active LDevice according to EPF channel's inst attribute
     * @param iedAdapter IEDAdapter
     * @param channel TChannel
     * @return LDeviceAdapter object that matches the EPF channel
     */
    private static Optional<LDeviceAdapter> getActiveSourceLDeviceByLDEPFChannel(IEDAdapter iedAdapter, TChannel channel) {
        return iedAdapter.findLDeviceAdapterByLdInst(channel.getLDInst())
                .filter(lDeviceAdapter -> new Ldevice(lDeviceAdapter.getCurrentElem()).getLdeviceStatus()
                        .map(status -> status.equals(LdeviceStatus.ON))
                        .orElse(false));
    }

    /**
     * Provides Active LN Object that satisfies the EPF channel attributes (lnClass, lnInst, prefix)
     * @param lDeviceAdapter LDeviceAdapter
     * @param channel TChannel
     * @return AbstractLNAdapter object that matches the EPF channel
     */
    private static Optional<AbstractLNAdapter<?>> getActiveLNSourceByLDEPFChannel(LDeviceAdapter lDeviceAdapter, TChannel channel) {
        return lDeviceAdapter.getLNAdaptersIncludingLN0().stream()
                .filter(lnAdapter -> lnAdapter.getLNClass().equals(channel.getLNClass())
                        && lnAdapter.getLNInst().equals(channel.getLNInst())
                        && trimToEmpty(channel.getLNPrefix()).equals(trimToEmpty(lnAdapter.getPrefix())))
                .findFirst()
                .filter(lnAdapter -> lnAdapter.getDaiModStValValue()
                        .map(status -> status.equals(LdeviceStatus.ON.getValue()))
                        .orElse(true));
    }

    /**
     *  Verify whether the LN satisfies the EPF channel parameters for Data Type Template elements.
     * @param lnAdapter AbstractLNAdapter
     * @param channel TChannel
     * @return true if the LN matches the EPF channel
     */
    private static boolean isValidDataTypeTemplate(AbstractLNAdapter<?> lnAdapter, TChannel channel) {
        if(isBlank(channel.getDOName())){
            return true;
        }
        String doName = isBlank(channel.getDOInst()) || channel.getDOInst().equals("0") ? channel.getDOName() : channel.getDOName() + channel.getDOInst();
        DoTypeName doTypeName = new DoTypeName(doName);
        if(isNotBlank(channel.getSDOName())){
            doTypeName.getStructNames().add(channel.getSDOName());
        }
        DaTypeName daTypeName = new DaTypeName(channel.getDAName());
        if(isNotBlank(channel.getBDAName())){
            daTypeName.setBType(TPredefinedBasicTypeEnum.STRUCT);
            daTypeName.getStructNames().add(channel.getBDAName());
        }
        if(isNotBlank(channel.getSBDAName())){
            daTypeName.getStructNames().add(channel.getSBDAName());
        }
        return lnAdapter.getDataTypeTemplateAdapter().getLNodeTypeAdapterById(lnAdapter.getLnType())
                .filter(lNodeTypeAdapter -> {
                    try {
                        lNodeTypeAdapter.check(doTypeName, daTypeName);
                    } catch (ScdException ex) {
                        return false;
                    }
                    return true;
                }).isPresent();
    }

    private void updateLDEPFExtRefBinding(TExtRef extRef, TIED iedSource, TChannel setting) {
        extRef.setIedName(iedSource.getName());
        extRef.setLdInst(setting.getLDInst());
        extRef.getLnClass().add(setting.getLNClass());
        extRef.setLnInst(setting.getLNInst());
        if(!isBlank(setting.getLNPrefix())) {
            extRef.setPrefix(setting.getLNPrefix());
        }
        String doName = isBlank(setting.getDOInst()) || setting.getDOInst().equals("0") ? setting.getDOName() : setting.getDOName() + setting.getDOInst();
        extRef.setDoName(doName);
    }

    private List<SclReportItem> updateLDEPFDos(LDeviceAdapter lDeviceAdapter, TExtRef extRef, TChannel setting) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<DoNameAndDaName> doNameAndDaNameList = List.of(
                new DoNameAndDaName(CHNUM1_DO_NAME, DU_DA_NAME),
                new DoNameAndDaName(LEVMOD_DO_NAME, SETVAL_DA_NAME),
                new DoNameAndDaName(MOD_DO_NAME, STVAL_DA_NAME),
                new DoNameAndDaName(SRCREF_DO_NAME, SETSRCREF_DA_NAME)
        );
        if (setting.getChannelType().equals(TChannelType.DIGITAL)) {
            //digital
            lDeviceAdapter.findLnAdapter(LN_RBDR, setting.getChannelNum(), null)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
            lDeviceAdapter.findLnAdapter(LN_RBDR, setting.getChannelNum(), LN_PREFIX_B)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
        }
        if (setting.getChannelType().equals(TChannelType.ANALOG)) {
            //analog
            lDeviceAdapter.findLnAdapter(LN_RADR, setting.getChannelNum(), null)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
            lDeviceAdapter.findLnAdapter(LN_RADR, setting.getChannelNum(), LN_PREFIX_A)
                    .ifPresent(lnAdapter -> doNameAndDaNameList.forEach(doNameAndDaName -> updateVal(lnAdapter, doNameAndDaName.doName, doNameAndDaName.daName, extRef, setting).ifPresent(sclReportItems::add)));
        }
        return sclReportItems;
    }

    private Optional<SclReportItem> updateVal(AbstractLNAdapter<?> lnAdapter, String doName, String daName, TExtRef extRef, TChannel setting) {
        String value = switch (daName) {
            case DU_DA_NAME -> setting.getChannelShortLabel();
            case SETVAL_DA_NAME ->
                    LN_PREFIX_B.equals(lnAdapter.getPrefix()) || LN_PREFIX_A.equals(lnAdapter.getPrefix()) ? setting.getChannelLevModQ().value() : setting.getChannelLevMod().value();
            case STVAL_DA_NAME -> LdeviceStatus.ON.getValue();
            case SETSRCREF_DA_NAME -> computeDaiValue(lnAdapter, extRef, setting.getDAName());
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
                    trimToEmpty(extRef.getPrefix()) +
                    extRef.getLnClass().get(0) +
                    trimToEmpty(extRef.getLnInst()) + "." +
                    extRef.getDoName() + "." + Q_DA_NAME;
        } else {
            return extRef.getIedName() +
                    extRef.getLdInst() + "/" +
                    trimToEmpty(extRef.getPrefix()) +
                    extRef.getLnClass().get(0) +
                    trimToEmpty(extRef.getLnInst()) + "." +
                    extRef.getDoName() + "." +
                    daName;
        }
    }

}
