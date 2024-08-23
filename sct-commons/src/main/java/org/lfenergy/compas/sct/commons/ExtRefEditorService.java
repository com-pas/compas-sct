// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.RequiredArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.ExtRefEditor;
import org.lfenergy.compas.sct.commons.api.LnEditor;
import org.lfenergy.compas.sct.commons.domain.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.epf.EPF;
import org.lfenergy.compas.sct.commons.model.epf.TCBScopeType;
import org.lfenergy.compas.sct.commons.model.epf.TChannel;
import org.lfenergy.compas.sct.commons.model.epf.TChannelType;
import org.lfenergy.compas.sct.commons.scl.ExtRefService;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

@RequiredArgsConstructor
public class ExtRefEditorService implements ExtRefEditor {
    private static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";
    private static final Map<String, String> voltageCodification = Map.of(
            "3", "HT",
            "4", "HT",
            "5", "THT",
            "6", "THT",
            "7", "THT"
    );

    private final IedService iedService;
    private final LdeviceService ldeviceService;
    private final LnEditor lnEditor;
    private final ExtRefService extRefService;
    private final DataTypeTemplatesService dataTypeTemplatesService;

    /**
     * Provides valid IED sources according to EPF configuration.<br/>
     * EPF verification include:<br/>
     * 1. COMPAS-Bay verification that should be closed to the provided Flow Kind<br/>
     * 2. COMPAS-ICDHeader verification that should match the provided parameters<br/>
     * 3. Active LDevice source object that should match the provided parameters<br/>
     * 4. Active LNode source object that should match the provided parameters<br/>
     * 5. Valid DataTypeTemplate Object hierarchy that should match the DO/DA/BDA parameters<br/>
     *
     * @param sclRootAdapter SCL scl object
     * @param compasBay      TCompasBay represent Bay Private
     * @param channel        TChannel represent parameters
     * @return the IED sources matching the LDEPF parameters
     */
    private static List<TIED> getIedSources(SclRootAdapter sclRootAdapter, TCompasBay compasBay, TChannel channel) {
        return sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> (channel.getBayScope().equals(TCBScopeType.BAY_EXTERNAL)
                        && iedAdapter.getPrivateCompasBay().stream().noneMatch(bay -> bay.getUUID().equals(compasBay.getUUID())))
                        || (channel.getBayScope().equals(TCBScopeType.BAY_INTERNAL)
                        && iedAdapter.getPrivateCompasBay().stream().anyMatch(bay -> bay.getUUID().equals(compasBay.getUUID()))))
                .filter(iedAdapter -> doesIcdHeaderMatchLDEPFChannel(iedAdapter, channel))
                .filter(iedAdapter -> getActiveSourceLDeviceByLDEPFChannel(iedAdapter, channel)
                        .map(lDeviceAdapter -> getActiveLNSourceByLDEPFChannel(lDeviceAdapter, channel)
                                .map(lnAdapter -> isValidDataTypeTemplate(lnAdapter, channel))
                                .orElse(false))
                        .orElse(false))
                .map(IEDAdapter::getCurrentElem)
                .limit(2)
                .toList();
    }

    /**
     * Provides a list of ExtRef and associated Bay <br/>
     * - The location of ExtRef should be in LDevice (inst=LDEPF) <br/>
     * - ExtRef that lacks Bay or ICDHeader Private is not returned <br/>
     *
     * @param sclReportItems List of SclReportItem
     * @return list of ExtRef and associated Bay
     */
    private List<ExtRefInfo.ExtRefWithBayReference> getExtRefWithBayReferenceInLDEPF(TDataTypeTemplates dataTypeTemplates, TIED tied, final TLDevice tlDevice, final List<SclReportItem> sclReportItems) {
        List<ExtRefInfo.ExtRefWithBayReference> extRefBayReferenceList = new ArrayList<>();
        String lDevicePath = "SCL/IED[@name=\"" + tied.getName() + "\"]/AccessPoint/Server/LDevice[@inst=\"" + tlDevice.getInst() + "\"]";
        Optional<TCompasBay> tCompasBay = PrivateUtils.extractCompasPrivate(tied, TCompasBay.class);
        if (tCompasBay.isEmpty()) {
            sclReportItems.add(SclReportItem.error(lDevicePath, "The IED has no Private Bay"));
            if (PrivateUtils.extractCompasPrivate(tied, TCompasICDHeader.class).isEmpty()) {
                sclReportItems.add(SclReportItem.error(lDevicePath, "The IED has no Private compas:ICDHeader"));
            }
            return Collections.emptyList();
        }

        if (dataTypeTemplatesService.isDoModAndDaStValExist(dataTypeTemplates, tlDevice.getLN0().getLnType())) {
            extRefBayReferenceList.addAll(tlDevice.getLN0()
                    .getInputs()
                    .getExtRef().stream()
                    .map(extRef -> new ExtRefInfo.ExtRefWithBayReference(tied.getName(), tCompasBay.get(), extRef)).toList());
        } else {
            sclReportItems.add(SclReportItem.error(lDevicePath, "DO@name=Mod/DA@name=stVal not found in DataTypeTemplate"));
        }
        return extRefBayReferenceList;
    }

    /**
     * Verify if an Extref matches the EPF Channel or not.
     *
     * @param extRef   TExtRef
     * @param tChannel TChannel
     * @return true if the TExtRef matches the EPF channel
     */
    private static Boolean doesExtRefMatchLDEPFChannel(TExtRef extRef, TChannel tChannel) {
        Boolean doesExtRefDescMatchAnalogChannel = tChannel.getChannelType().equals(TChannelType.ANALOG)
                && extRef.getDesc().startsWith("DYN_LDEPF_ANALOG CHANNEL " + tChannel.getChannelNum() + "_1_AnalogueValue")
                && extRef.getDesc().endsWith("_" + tChannel.getDAName() + "_1");
        Boolean doesExtRefDescMatchDigitalChannel = tChannel.getChannelType().equals(TChannelType.DIGITAL)
                && extRef.getDesc().startsWith("DYN_LDEPF_DIGITAL CHANNEL " + tChannel.getChannelNum() + "_1_BOOLEEN")
                && extRef.getDesc().endsWith("_" + tChannel.getDAName() + "_1");
        return extRef.isSetDesc() && (doesExtRefDescMatchAnalogChannel || doesExtRefDescMatchDigitalChannel)
                && extRef.isSetPLN() && Utils.lnClassEquals(extRef.getPLN(), tChannel.getLNClass())
                && extRef.isSetPDO() && extRef.getPDO().equals(tChannel.getDOName());
    }

    /**
     * Verify whether the IED satisfies the EPF channel for the private element `TCompasICDHeader`
     *
     * @param iedAdapter IEDAdapter
     * @param channel    TChannel
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
     *
     * @param iedAdapter IEDAdapter
     * @param channel    TChannel
     * @return LDeviceAdapter object that matches the EPF channel
     */
    private static Optional<LDeviceAdapter> getActiveSourceLDeviceByLDEPFChannel(IEDAdapter iedAdapter, TChannel channel) {
        LdeviceService ldeviceService = new LdeviceService();
        return ldeviceService.findLdevice(iedAdapter.getCurrentElem(), tlDevice -> tlDevice.getInst().equals(channel.getLDInst()))
                .filter(tlDevice -> ldeviceService.getLdeviceStatus(tlDevice).map(ActiveStatus.ON::equals).orElse(false))
                .map(tlDevice -> new LDeviceAdapter(iedAdapter, tlDevice));
    }

    /**
     * Provides Active LN Object that satisfies the EPF channel attributes (lnClass, lnInst, prefix)
     *
     * @param lDeviceAdapter LDeviceAdapter
     * @param channel        TChannel
     * @return AbstractLNAdapter object that matches the EPF channel
     */
    private static Optional<AbstractLNAdapter<?>> getActiveLNSourceByLDEPFChannel(LDeviceAdapter lDeviceAdapter, TChannel channel) {
        return lDeviceAdapter.getLNAdaptersIncludingLN0()
                .stream()
                .filter(lnAdapter -> lnAdapter.getLNClass().equals(channel.getLNClass())
                        && lnAdapter.getLNInst().equals(channel.getLNInst())
                        && trimToEmpty(channel.getLNPrefix()).equals(trimToEmpty(lnAdapter.getPrefix())))
                .findFirst()
                .filter(lnAdapter -> lnAdapter.getDaiModStValValue()
                        .map(status -> status.equals(ActiveStatus.ON.getValue()))
                        .orElse(true));
    }

    /**
     * Verify whether the LN satisfies the EPF channel parameters for Data Type Template elements.
     *
     * @param lnAdapter AbstractLNAdapter
     * @param channel   TChannel
     * @return true if the LN matches the EPF channel
     */
    private static boolean isValidDataTypeTemplate(AbstractLNAdapter<?> lnAdapter, TChannel channel) {
        if (isBlank(channel.getDOName())) {
            return true;
        }
        String doName = isBlank(channel.getDOInst()) || channel.getDOInst().equals("0") ? channel.getDOName() : channel.getDOName() + channel.getDOInst();
        DoTypeName doTypeName = new DoTypeName(doName);
        if (isNotBlank(channel.getSDOName())) {
            doTypeName.getStructNames().add(channel.getSDOName());
        }
        DaTypeName daTypeName = new DaTypeName(channel.getDAName());
        if (isNotBlank(channel.getBDAName())) {
            daTypeName.setBType(TPredefinedBasicTypeEnum.STRUCT);
            daTypeName.getStructNames().add(channel.getBDAName());
        }
        if (isNotBlank(channel.getSBDAName())) {
            daTypeName.getStructNames().add(channel.getSBDAName());
        }
        return lnAdapter.getDataTypeTemplateAdapter().getLNodeTypeAdapterById(lnAdapter.getLnType())
                .filter(lNodeTypeAdapter -> {
                    try {
                        lNodeTypeAdapter.checkDoAndDaTypeName(doTypeName, daTypeName);
                    } catch (ScdException ex) {
                        return false;
                    }
                    return true;
                }).isPresent();
    }

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
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        if (!epf.isSetChannels()) return sclReportItems;
        iedService.getFilteredIeds(scd, ied -> !ied.getName().contains("TEST"))
                .forEach(tied -> ldeviceService.findLdevice(tied, tlDevice -> LDEVICE_LDEPF.equals(tlDevice.getInst()))
                        .ifPresent(tlDevice -> getExtRefWithBayReferenceInLDEPF(scd.getDataTypeTemplates(), tied, tlDevice, sclReportItems)
                                .forEach(extRefBayRef -> epf.getChannels().getChannel().stream().filter(tChannel -> doesExtRefMatchLDEPFChannel(extRefBayRef.extRef(), tChannel))
                                        .findFirst().ifPresent(channel -> {
                                            List<TIED> iedSources = getIedSources(sclRootAdapter, extRefBayRef.compasBay(), channel);
                                            if (iedSources.size() == 1) {
                                                updateLDEPFExtRefBinding(extRefBayRef.extRef(), iedSources.get(0), channel);
                                                LDeviceAdapter lDeviceAdapter = new LDeviceAdapter(new IEDAdapter(sclRootAdapter, tied.getName()), tlDevice);
                                                sclReportItems.addAll(updateLDEPFDos(lDeviceAdapter, extRefBayRef.extRef(), channel));
                                            } else {
                                                if (iedSources.size() > 1) {
                                                    sclReportItems.add(SclReportItem.warning(null, "There is more than one IED source to bind the signal " +
                                                            "/IED@name=" + extRefBayRef.iedName() + "/LDevice@inst=LDEPF/LN0" +
                                                            "/ExtRef@desc=" + extRefBayRef.extRef().getDesc()));
                                                }
                                                // If the source IED is not found, there will be no update or report message.
                                            }
                                        }))));
        return sclReportItems;
    }

    @Override
    public void epfPostProcessing(SCL scd) {
        iedService.getFilteredIeds(scd, ied -> !ied.getName().contains("TEST"))
                .forEach(tied -> ldeviceService.findLdevice(tied, tlDevice -> LDEVICE_LDEPF.equals(tlDevice.getInst()))
                        .ifPresent(tlDevice -> tlDevice.getLN0().getDOI()
                                .stream().filter(tdoi -> tdoi.getName().startsWith(INREF_PREFIX))
                                .forEach(tdoi -> {
                                    DoLinkedToDaFilter doLinkedToSetSrcRef = new DoLinkedToDaFilter(tdoi.getName(), List.of(), SETSRCREF_DA_NAME, List.of());
                                    Optional<TDAI> setSrcRefDAI = lnEditor.getDOAndDAInstances(tlDevice.getLN0(), doLinkedToSetSrcRef);
                                    DoLinkedToDaFilter doLinkedPurPose = new DoLinkedToDaFilter(tdoi.getName(), List.of(), PURPOSE_DA_NAME, List.of());
                                    Optional<TDAI> purPoseDAI = lnEditor.getDOAndDAInstances(tlDevice.getLN0(), doLinkedPurPose);

                                    boolean isSetSrcRefExistAndEmpty = setSrcRefDAI.isPresent()
                                            && (!setSrcRefDAI.get().isSetVal()
                                            || (setSrcRefDAI.get().isSetVal()
                                            && setSrcRefDAI.get().getVal().getFirst().getValue().isEmpty()));
                                    boolean isPurposeExistAndMatchChannel = purPoseDAI.isPresent()
                                            && purPoseDAI.get().isSetVal()
                                            && (purPoseDAI.get().getVal().getFirst().getValue().startsWith("DYN_LDEPF_DIGITAL CHANNEL")
                                            || purPoseDAI.get().getVal().getFirst().getValue().startsWith("DYN_LDEPF_ANALOG CHANNEL"));
                                    if(isSetSrcRefExistAndEmpty && isPurposeExistAndMatchChannel) {

                                        DoLinkedToDa doLinkedToDa = new DoLinkedToDa();
                                        DataObject dataObject = new DataObject();
                                        dataObject.setDoName(tdoi.getName());
                                        doLinkedToDa.setDataObject(dataObject);
                                        DataAttribute dataAttribute = new DataAttribute();
                                        dataAttribute.setDaName(SETSRCREF_DA_NAME);
                                        dataAttribute.setDaiValues(List.of(new DaVal(null, tied.getName()+tlDevice.getInst()+"/LPHD0.Proxy")));
                                        doLinkedToDa.setDataAttribute(dataAttribute);
                                        lnEditor.updateOrCreateDOAndDAInstances(tlDevice.getLN0(), doLinkedToDa);
                                    }
                                })));
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

    private void updateLDEPFExtRefBinding(TExtRef extRef, TIED iedSource, TChannel setting) {
        extRef.setIedName(iedSource.getName());
        extRef.setLdInst(setting.getLDInst());
        extRef.getLnClass().add(setting.getLNClass());
        extRef.setLnInst(setting.getLNInst());
        if (!isBlank(setting.getLNPrefix())) {
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
            case SETVAL_DA_NAME -> LN_PREFIX_B.equals(lnAdapter.getPrefix()) || LN_PREFIX_A.equals(lnAdapter.getPrefix()) ? setting.getChannelLevModQ().value() : setting.getChannelLevMod().value();
            case STVAL_DA_NAME -> ActiveStatus.ON.getValue();
            case SETSRCREF_DA_NAME -> computeDaiValue(lnAdapter, extRef, setting.getDAName());
            default -> null;
        };
        return lnAdapter.getDOIAdapterByName(doName).updateDAI(daName, value);
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

    @Override
    public void debindCompasFlowsAndExtRefsBasedOnVoltageLevel(SCL scd) {
        scd.getSubstation()
                .stream()
                .flatMap(tSubstation -> tSubstation.getVoltageLevel().stream())
                .map(TVoltageLevel::getName)
                .filter(tVoltageLevelName -> !"0".equals(tVoltageLevelName))
                .forEach(tVoltageLevelName -> scd.getIED().stream()
                        .flatMap(ldeviceService::getLdevices)
                        .filter(TLDevice::isSetLN0)
                        .filter(tlDevice -> tlDevice.getLN0().isSetInputs())
                        .forEach(tlDevice -> {
                            String flowSource = voltageCodification.get(tVoltageLevelName);
                            extRefService.getCompasFlows(tlDevice)
                                    .filter(TCompasFlow::isSetFlowSourceVoltageLevel)
                                    .filter(TCompasFlow::isSetExtRefiedName)
                                    .forEach(tCompasFlow -> {
                                        if (!tCompasFlow.getFlowSourceVoltageLevel().equals(flowSource)) {
                                            //debind extRefs correspondind to compas flow
                                            extRefService.getMatchingExtRefs(tlDevice, tCompasFlow)
                                                    .forEach(extRefService::clearExtRefBinding);
                                            //debind all compas flow
                                            extRefService.clearCompasFlowBinding(tCompasFlow);
                                        }
                                    });
                        })
                );
    }


    @Override
    public List<SclReportItem> updateIedNameBasedOnLnode(SCL scl) {
        Map<TopoKey, TBay> bayByTopoKey = scl.getSubstation().stream()
                .flatMap(tSubstation -> tSubstation.getVoltageLevel().stream())
                .flatMap(tVoltageLevel -> tVoltageLevel.getBay().stream())
                .map(tBay -> PrivateUtils.extractCompasPrivate(tBay, TCompasTopo.class)
                        .filter(tCompasTopo -> isNotBlank(tCompasTopo.getNode()) && Objects.nonNull(tCompasTopo.getNodeOrder()))
                        .map(tCompasTopo -> new BayTopoKey(tBay, new TopoKey(tCompasTopo.getNode(), tCompasTopo.getNodeOrder())))
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(BayTopoKey::topoKey, BayTopoKey::bay));

        List<SclReportItem> sclReportItems = new ArrayList<>();
        scl.getIED().stream()
                .flatMap(ldeviceService::getLdevices)
                .forEach(tlDevice ->
                        extRefService.getCompasFlows(tlDevice)
                                .filter(tCompasFlow -> Objects.nonNull(tCompasFlow.getFlowSourceBayNode()) && Objects.nonNull(tCompasFlow.getFlowSourceBayNodeOrder()))
                                .forEach(tCompasFlow ->
                                        Optional.ofNullable(bayByTopoKey.get(new TopoKey(tCompasFlow.getFlowSourceBayNode().toString(), tCompasFlow.getFlowSourceBayNodeOrder())))
                                                .flatMap(tBay -> tBay.getFunction().stream()
                                                        .flatMap(tFunction -> tFunction.getLNode().stream())
                                                        .filter(tlNode -> Objects.equals(tlNode.getLdInst(), tCompasFlow.getExtRefldinst())
                                                                && Objects.equals(tlNode.getLnInst(), tCompasFlow.getExtReflnInst())
                                                                && Utils.lnClassEquals(tlNode.getLnClass(), tCompasFlow.getExtReflnClass())
                                                                && Objects.equals(tlNode.getPrefix(), tCompasFlow.getExtRefprefix()))
                                                        .filter(tlNode -> {
                                                            Optional<TCompasICDHeader> tCompasICDHeader = PrivateUtils.extractCompasPrivate(tlNode, TCompasICDHeader.class);
                                                            if (tCompasICDHeader.isPresent()) {
                                                                return Objects.equals(tCompasFlow.getFlowSourceIEDType(), tCompasICDHeader.get().getIEDType())
                                                                        && Objects.equals(tCompasFlow.getFlowIEDSystemVersioninstance(), tCompasICDHeader.get().getIEDSystemVersioninstance())
                                                                        && Objects.equals(tCompasFlow.getFlowSourceIEDredundancy(), tCompasICDHeader.get().getIEDredundancy());
                                                            } else {
                                                                sclReportItems.add(SclReportItem.error("", ("The substation LNode with following attributes : IedName:%s / LdInst:%s / LnClass:%s / LnInst:%s  " +
                                                                        "does not contain the needed (COMPAS - ICDHeader) private")
                                                                        .formatted(tlNode.getIedName(), tlNode.getLdInst(), tlNode.getLnClass().getFirst(), tlNode.getLnInst())));
                                                                return false;
                                                            }
                                                        })
                                                        .map(TLNode::getIedName)
                                                        .reduce(checkOnlyOneIed(tCompasFlow, tBay, sclReportItems))
                                                )
                                                .ifPresentOrElse(iedName -> {
                                                            extRefService.getMatchingExtRefs(tlDevice, tCompasFlow).forEach(tExtRef -> tExtRef.setIedName(iedName));
                                                            tCompasFlow.setExtRefiedName(iedName);
                                                        },
                                                        () -> {
                                                            extRefService.getMatchingExtRefs(tlDevice, tCompasFlow).forEach(extRefService::clearExtRefBinding);
                                                            extRefService.clearCompasFlowBinding(tCompasFlow);
                                                        }
                                                )
                                )
                );
        return sclReportItems;
    }

    private static BinaryOperator<String> checkOnlyOneIed(TCompasFlow tCompasFlow, TBay tBay, List<SclReportItem> sclReportItems) {
        return (iedName1, iedName2) -> {
            sclReportItems.add(SclReportItem.error("",
                    ("Several LNode@IedName ('%s', '%s') are found in the bay '%s' for the following compas-flow attributes :" +
                            " @FlowSourceIEDType '%s' @FlowSourceIEDredundancy '%s' @FlowIEDSystemVersioninstance '%s'").
                            formatted(iedName1, iedName2, tBay.getName(), tCompasFlow.getFlowSourceIEDType(), tCompasFlow.getFlowSourceIEDredundancy(), tCompasFlow.getFlowIEDSystemVersioninstance())));
            return iedName1;
        };
    }

    record TopoKey(String FlowNode, BigInteger FlowNodeOrder) {
    }

    record BayTopoKey(TBay bay, TopoKey topoKey) {
    }

    private record DoNameAndDaName(String doName, String daName) {
    }

}
