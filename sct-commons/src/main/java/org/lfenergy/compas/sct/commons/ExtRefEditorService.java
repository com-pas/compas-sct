// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.lfenergy.compas.sct.commons.model.epf.TChannelLevMod;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LnId;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

@Slf4j
@RequiredArgsConstructor
public class ExtRefEditorService implements ExtRefEditor {
    private static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";
    private static final List<DoNameAndDaName> DO_DA_MAPPINGS = List.of(
            new DoNameAndDaName(CHNUM1_DO_NAME, DU_DA_NAME),
            new DoNameAndDaName(LEVMOD_DO_NAME, SETVAL_DA_NAME),
            new DoNameAndDaName(MOD_DO_NAME, STVAL_DA_NAME),
            new DoNameAndDaName(SRCREF_DO_NAME, SETSRCREF_DA_NAME)
    );

    private final IedService iedService;
    private final LdeviceService ldeviceService;
    private final LnEditor lnEditor;
    private final DataTypeTemplatesService dataTypeTemplatesService;

    @Getter
    private final List<SclReportItem> errorHandler = new ArrayList<>();

    /**
     * Provides valid IED sources according to EPF configuration.<br/>
     * EPF verification include:<br/>
     * 1. COMPAS-Bay verification that should be closed to the provided Flow Kind<br/>
     * 2. COMPAS-ICDHeader verification that should match the provided parameters<br/>
     * 3. Active LDevice source object that should match the provided parameters<br/>
     * 4. Active LNode source object that should match the provided parameters<br/>
     * 5. Valid DataTypeTemplate Object hierarchy that should match the DO/DA/BDA parameters<br/>
     *
     * @param scl SCL object
     * @param compasBay      TCompasBay represent Bay Private
     * @param channel        TChannel represent parameters
     * @return the IED sources matching the LDEPF parameters
     */
    private List<TIED> getIedSources(SCL scl, TCompasBay compasBay, TChannel channel) {
        return scl.getIED().stream()
                .filter(tied -> {
                    Optional<TCompasBay> tCompasBay = PrivateUtils.extractCompasPrivate(tied, TCompasBay.class);
                    return (channel.getBayScope().equals(TCBScopeType.BAY_EXTERNAL)
                            && tCompasBay.stream().noneMatch(bay -> bay.getUUID().equals(compasBay.getUUID())))
                            || (channel.getBayScope().equals(TCBScopeType.BAY_INTERNAL)
                            && tCompasBay.stream().anyMatch(bay -> bay.getUUID().equals(compasBay.getUUID())));
                }).filter(tied -> doesIcdHeaderMatchLDEPFChannel(tied, channel))
                .filter(tied -> ldeviceService.findLdevice(tied, channel.getLDInst())
                        .filter(tlDevice -> ldeviceService.getLdeviceStatus(tlDevice).map(ActiveStatus.ON::equals).orElse(false))
                        .map(tlDevice -> getActiveLNSourceByLDEPFChannel(tlDevice, channel)
                                .map(tAnyLN -> isValidDataTypeTemplate(scl.getDataTypeTemplates(), tAnyLN, channel))
                                .orElse(false))
                        .orElse(false))
                .limit(2)
                .toList();
    }

    /**
     * Provides a list of ExtRef and associated Bay <br/>
     * - The location of ExtRef should be in LDevice (inst=LDEPF) <br/>
     * - ExtRef that lacks Bay or ICDHeader Private is not returned <br/>
     *
     * @return list of ExtRef and associated Bay
     */
    private List<ExtRefInfo.ExtRefWithBayReference> getExtRefWithBayReferenceInLDEPF(TDataTypeTemplates dataTypeTemplates, TIED tied, final TLDevice tlDevice) {
        List<ExtRefInfo.ExtRefWithBayReference> extRefBayReferenceList = new ArrayList<>();
        String lDevicePath = "SCL/IED[@name=\"" + tied.getName() + "\"]/AccessPoint/Server/LDevice[@inst=\"" + tlDevice.getInst() + "\"]";
        Optional<TCompasBay> tCompasBay = PrivateUtils.extractCompasPrivate(tied, TCompasBay.class);
        if (tCompasBay.isEmpty()) {
            errorHandler.add(SclReportItem.error(lDevicePath, "The IED has no Private Bay"));
            if (PrivateUtils.extractCompasPrivate(tied, TCompasICDHeader.class).isEmpty()) {
                errorHandler.add(SclReportItem.error(lDevicePath, "The IED has no Private compas:ICDHeader"));
            }
            return Collections.emptyList();
        }

        if (dataTypeTemplatesService.isDoModAndDaStValExist(dataTypeTemplates, tlDevice.getLN0().getLnType())) {
            extRefBayReferenceList.addAll(tlDevice.getLN0()
                    .getInputs()
                    .getExtRef().stream()
                    .map(extRef -> new ExtRefInfo.ExtRefWithBayReference(tied.getName(), tCompasBay.get(), extRef)).toList());
        } else {
            errorHandler.add(SclReportItem.error(lDevicePath, "DO@name=Mod/DA@name=stVal not found in DataTypeTemplate"));
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
                && extRef.getDesc().startsWith("DYN_LDEPF_DIGITAL CHANNEL " + tChannel.getChannelNum() + "_1_BOOLEAN")
                && extRef.getDesc().endsWith("_" + tChannel.getDAName() + "_1");
        return extRef.isSetDesc() && (doesExtRefDescMatchAnalogChannel || doesExtRefDescMatchDigitalChannel)
                && extRef.isSetPLN() && Utils.lnClassEquals(extRef.getPLN(), tChannel.getLNClass())
                && extRef.isSetPDO() && extRef.getPDO().equals(tChannel.getDOName());
    }

    /**
     * Verify whether the IED satisfies the EPF channel for the private element `TCompasICDHeader`
     *
     * @param tied TIED
     * @param channel    TChannel
     * @return true if the TCompasICDHeader matches the EPF channel
     */
    private static boolean doesIcdHeaderMatchLDEPFChannel(TIED tied, TChannel channel) {
        Optional<TCompasICDHeader> tCompasICDHeader = PrivateUtils.extractCompasPrivate(tied, TCompasICDHeader.class);
        return tCompasICDHeader.map(compasICDHeader -> compasICDHeader.getIEDType().value().equals(channel.getIEDType())
                        && compasICDHeader.getIEDredundancy().value().equals(channel.getIEDRedundancy().value())
                        && compasICDHeader.getIEDSystemVersioninstance().toString().equals(channel.getIEDSystemVersionInstance()))
                .orElse(false);
    }

    /**
     * Provides Active LN Object that satisfies the EPF channel attributes (lnClass, lnInst, prefix)
     *
     * @param tlDevice TLDevice
     * @param channel        TChannel
     * @return AbstractLNAdapter object that matches the EPF channel
     */
    private Optional<TAnyLN> getActiveLNSourceByLDEPFChannel(TLDevice tlDevice, TChannel channel) {
        return Stream.concat(Stream.of(tlDevice.getLN0()), tlDevice.getLN().stream())
                .filter(tAnyLN -> {
                    LnId lnId = LnId.from(tAnyLN);
                    return lnId.lnClass().equals(channel.getLNClass())
                            && lnId.lnInst().equals(channel.getLNInst())
                            && trimToEmpty(channel.getLNPrefix()).equals(trimToEmpty(lnId.prefix()));
                }).findFirst()
                .filter(tAnyLN -> lnEditor.getDaiModStValValue(tAnyLN).map(ActiveStatus.ON::equals).orElse(true));
    }

    /**
     * Verify whether the LN satisfies the EPF channel parameters for Data Type Template elements.
     *
     * @param dtt TDataTypeTemplates
     * @param tAnyLN TAnyLN
     * @param channel   TChannel
     * @return true if the LN matches the EPF channel
     */
    private boolean isValidDataTypeTemplate(TDataTypeTemplates dtt, TAnyLN tAnyLN, TChannel channel) {
        if (isBlank(channel.getDOName())) {
            return true;
        }
        String doName = isBlank(channel.getDOInst()) || channel.getDOInst().equals("0") ? channel.getDOName() : channel.getDOName() + channel.getDOInst();
        String daName = channel.getDAName();
        List<String> sdoNames = new ArrayList<>();
        List<String> bdaNames = new ArrayList<>();
        if (isNotBlank(channel.getSDOName())) {
            sdoNames.add(channel.getSDOName());
        }
        if (isNotBlank(channel.getBDAName())) {
            bdaNames.add(channel.getBDAName());
        }
        if (isNotBlank(channel.getSBDAName())) {
            bdaNames.add(channel.getSBDAName());
        }
        DoLinkedToDaFilter doLinkedToDaFilter = new DoLinkedToDaFilter(doName,  sdoNames, daName, bdaNames);
       return dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, tAnyLN.getLnType(), doLinkedToDaFilter)
               .findFirst().isPresent();
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
    public List<SclReportItem> manageBindingForLDEPF(SCL scd, EPF epf) {
        errorHandler.clear();
        if (!epf.isSetChannels()) return errorHandler;
        log.info("Processing %d EPF setting channels".formatted(epf.getChannels().getChannel().size()));
        iedService.getFilteredIeds(scd, ied -> !ied.getName().contains("TEST"))
                .forEach(tied -> ldeviceService.findLdevice(tied, LDEVICE_LDEPF)
                        .ifPresent(tlDevice -> getExtRefWithBayReferenceInLDEPF(scd.getDataTypeTemplates(), tied, tlDevice)
                                .forEach(extRefBayRef -> epf.getChannels().getChannel().stream().filter(tChannel -> doesExtRefMatchLDEPFChannel(extRefBayRef.extRef(), tChannel))
                                        .findFirst().ifPresent(channel -> {
                                            List<TIED> iedSources = getIedSources(scd, extRefBayRef.compasBay(), channel);
                                            if (iedSources.size() == 1) {
                                                updateLDEPFExtRefBinding(extRefBayRef.extRef(), iedSources.getFirst(), channel);
                                                updateLDEPFDos(scd.getDataTypeTemplates(), tied, tlDevice, extRefBayRef.extRef(), channel);
                                            } else {
                                                if (iedSources.size() > 1) {
                                                    errorHandler.add(SclReportItem.warning(null, "There is more than one IED source to bind the signal " +
                                                            "/IED@name=" + extRefBayRef.iedName() + "/LDevice@inst=LDEPF/LN0" +
                                                            "/ExtRef@desc=" + extRefBayRef.extRef().getDesc()));
                                                }
                                                // If the source IED is not found, there will be no update or report message.
                                            }
                                        }))));
        return errorHandler;
    }

    @Override
    public void epfPostProcessing(SCL scd) {
        iedService.getFilteredIeds(scd, ied -> !ied.getName().contains("TEST"))
                .forEach(tied -> ldeviceService.findLdevice(tied, LDEVICE_LDEPF)
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
                                        DataObject dataObject = new DataObject();
                                        dataObject.setDoName(tdoi.getName());
                                        DataAttribute dataAttribute = new DataAttribute();
                                        dataAttribute.setDaName(SETSRCREF_DA_NAME);
                                        dataAttribute.setDaiValues(List.of(new DaVal(null, tied.getName()+tlDevice.getInst()+"/LPHD0.Proxy")));
                                        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
                                        lnEditor.updateOrCreateDOAndDAInstances(tlDevice.getLN0(), doLinkedToDa);
                                    }
                                })));
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
        log.info(("LDEPF - Update Binding => ExtRef(desc=%s) " +
                "ExtRef(iedName=%s), ExtRef(LdInst=%s), ExtRef(LNClass=%s), ExtRef(LNInst=%s), ExtRef(prefix=%s), ExtRef(doName=%s)").formatted(extRef.getDesc(), extRef.getIedName(), extRef.getLdInst(), extRef.getLnClass().getFirst(), extRef.getLnInst(), extRef.isSetPrefix() ? extRef.getPrefix() : "", doName));
    }

    private String computeDaiValue(TAnyLN tAnyLN, TExtRef extRef, String daName) {
        if (LN_PREFIX_B.equals(LnId.from(tAnyLN).prefix()) || LN_PREFIX_A.equals(LnId.from(tAnyLN).prefix())) {
            return extRef.getIedName() +
                    extRef.getLdInst() + "/" +
                    trimToEmpty(extRef.getPrefix()) +
                    extRef.getLnClass().getFirst() +
                    trimToEmpty(extRef.getLnInst()) + "." +
                    extRef.getDoName() + "." + Q_DA_NAME;
        } else {
            return extRef.getIedName() +
                    extRef.getLdInst() + "/" +
                    trimToEmpty(extRef.getPrefix()) +
                    extRef.getLnClass().getFirst() +
                    trimToEmpty(extRef.getLnInst()) + "." +
                    extRef.getDoName() + "." +
                    daName;
        }
    }

   private void updateLDEPFDos(TDataTypeTemplates dtt, TIED tied, TLDevice tlDevice, TExtRef tExtRef, TChannel setting) {
       // Digital
        if (setting.getChannelType().equals(TChannelType.DIGITAL)) {
            lnEditor.findLn(tlDevice, tAnyLN -> LnId.from(tAnyLN).lnClass().equals(LN_RBDR) && LnId.from(tAnyLN).lnInst().equals(setting.getChannelNum()))
                    .ifPresent(tln -> updateDaiValue(dtt, tied, tlDevice, tln, tExtRef, setting));
            lnEditor.findLn(tlDevice, tAnyLN -> LnId.from(tAnyLN).lnClass().equals(LN_RBDR) && LnId.from(tAnyLN).lnInst().equals(setting.getChannelNum())
                            && LnId.from(tAnyLN).prefix().equals(LN_PREFIX_B))
                    .ifPresent(tln -> updateDaiValue(dtt, tied, tlDevice, tln, tExtRef, setting));
        }
       // Analog
        if (setting.getChannelType().equals(TChannelType.ANALOG)) {
            lnEditor.findLn(tlDevice, tAnyLN -> LnId.from(tAnyLN).lnClass().equals(LN_RADR) && LnId.from(tAnyLN).lnInst().equals(setting.getChannelNum()))
                    .ifPresent(tln -> updateDaiValue(dtt, tied, tlDevice, tln, tExtRef, setting));
            lnEditor.findLn(tlDevice, tAnyLN -> LnId.from(tAnyLN).lnClass().equals(LN_RADR) && LnId.from(tAnyLN).lnInst().equals(setting.getChannelNum())
                            && LnId.from(tAnyLN).prefix().equals(LN_PREFIX_A))
                    .ifPresent(tln -> updateDaiValue(dtt, tied, tlDevice, tln, tExtRef, setting));
        }
    }

    private void updateDaiValue(TDataTypeTemplates dtt, TIED tied, TLDevice tlDevice, TAnyLN tln, TExtRef tExtRef, TChannel setting) {
        DO_DA_MAPPINGS.forEach(doNameAndDaName -> Optional.ofNullable(getNewDaiValue(doNameAndDaName.daName, tln, tExtRef, setting))
                .ifPresent(newDaiValue -> updateDaiVal(dtt, tied, tlDevice, tln, DoLinkedToDaFilter.from(doNameAndDaName.doName, doNameAndDaName.daName), newDaiValue)));
    };

    private String getNewDaiValue(String daName,TAnyLN tAnyLN, TExtRef extRef, TChannel setting) {
        return switch (daName) {
            case DU_DA_NAME -> setting.isSetChannelShortLabel() ? setting.getChannelShortLabel(): null;
            case SETVAL_DA_NAME -> {
                if(LN_PREFIX_B.equals(LnId.from(tAnyLN).prefix()) || LN_PREFIX_A.equals(LnId.from(tAnyLN).prefix())){
                    yield setting.isSetChannelLevModQ() && !setting.getChannelLevModQ().equals(TChannelLevMod.NA) ? setting.getChannelLevModQ().value(): null;
                } else {
                    yield setting.isSetChannelLevMod() && !setting.getChannelLevMod().equals(TChannelLevMod.NA) ? setting.getChannelLevMod().value(): null;
                }
            }
            case STVAL_DA_NAME -> ActiveStatus.ON.getValue();
            case SETSRCREF_DA_NAME -> computeDaiValue(tAnyLN, extRef, setting.getDAName());
            default -> null;
        };
    }

    private void updateDaiVal(TDataTypeTemplates dtt, TIED tied, TLDevice tlDevice, TAnyLN tln, DoLinkedToDaFilter doLinkedToDaFilter, String newDaiValue) {
        dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, tln.getLnType(), doLinkedToDaFilter)
                .map(doLinkedToDa1 -> lnEditor.getDoLinkedToDaCompletedFromDAI(tied, tlDevice.getInst(), tln, doLinkedToDa1))
                .findFirst()
                .filter(doLinkedToDa1 -> {
                    if (!doLinkedToDa1.isUpdatable()){
                        errorHandler.add(SclReportItem.warning(tied.getName() + "/" + LDEVICE_LDSUIED + "/" + LnId.from(tln).lnClass() + "/DOI@name=\"" + doLinkedToDaFilter.doName() + "\"/DAI@name=\"" + doLinkedToDaFilter.daName() + "\"/Val", "The DAI cannot be updated"));
                    }
                    return doLinkedToDa1.isUpdatable();
                })
                .ifPresent(doLinkedToDa1 -> {
                    TVal tVal = new TVal();
                    tVal.setValue(newDaiValue);
                    doLinkedToDa1.dataAttribute().addDaVal(tVal);
                    lnEditor.updateOrCreateDOAndDAInstances(tln, doLinkedToDa1);
                    log.info("LDEPF - Update DOI => LN(lnClass=%s, inst=%s, prefix=%s) / DOI(name=%s)/DAI(name=%s) with value=%s".formatted(LnId.from(tln).lnClass(), LnId.from(tln).lnInst(), LnId.from(tln).prefix(), doLinkedToDaFilter.doName(), doLinkedToDaFilter.daName(), newDaiValue));
                });
    }

    private record DoNameAndDaName(String doName, String daName) {
    }

}
