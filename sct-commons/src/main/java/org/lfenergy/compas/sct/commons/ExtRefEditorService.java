// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.ExtRefEditor;
import org.lfenergy.compas.sct.commons.api.LnEditor;
import org.lfenergy.compas.sct.commons.domain.*;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;

import java.util.List;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

@Slf4j
@RequiredArgsConstructor
public class ExtRefEditorService implements ExtRefEditor {
    private static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";
    private static final String COMPAS_LNODE_STATUS = "COMPAS-LNodeStatus";
    private static final String LPHD0_PROXY = "LPHD0.Proxy";

    private final IedService iedService;
    private final LdeviceService ldeviceService;
    private final LnEditor lnEditor;

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
    public void epfPostProcessing(SCL scd) {
        iedService.getFilteredIeds(scd, ied -> !ied.getName().contains("TEST"))
                .forEach(tied -> ldeviceService.findLdevice(tied, LDEVICE_LDEPF)
                        .filter(ldepfLdevice -> PrivateUtils.extractStringPrivate(ldepfLdevice.getLN0(), COMPAS_LNODE_STATUS).map(status -> !status.equals(ActiveStatus.OFF.getValue())).orElse(false))
                        .ifPresent(ldepfLdevice -> ldepfLdevice.getLN0().getDOI()
                                .stream().filter(tdoi -> tdoi.getName().startsWith(INREF_PREFIX))
                                .forEach(tdoi -> {
                                    LN0 ln0 = ldepfLdevice.getLN0();
                                    DoLinkedToDaFilter doLinkedToSetSrcRef = new DoLinkedToDaFilter(tdoi.getName(), List.of(), SETSRCREF_DA_NAME, List.of());
                                    Optional<TDAI> setSrcRefDAI = lnEditor.getDOAndDAInstances(ln0, doLinkedToSetSrcRef);
                                    DoLinkedToDaFilter doLinkedPurPose = new DoLinkedToDaFilter(tdoi.getName(), List.of(), PURPOSE_DA_NAME, List.of());
                                    Optional<TDAI> purPoseDAI = lnEditor.getDOAndDAInstances(ln0, doLinkedPurPose);

                                    boolean isSetSrcRefExistAndEmpty = setSrcRefDAI.isPresent()
                                                                       && (!setSrcRefDAI.get().isSetVal()
                                                                           || (setSrcRefDAI.get().isSetVal()
                                                                               && setSrcRefDAI.get().getVal().getFirst().getValue().isEmpty()));
                                    boolean isPurposeExistAndMatchChannel = purPoseDAI.isPresent()
                                                                            && purPoseDAI.get().isSetVal()
                                                                            && (purPoseDAI.get().getVal().getFirst().getValue().startsWith("DYN_LDEPF_DIGITAL CHANNEL")
                                                                                || purPoseDAI.get().getVal().getFirst().getValue().startsWith("DYN_LDEPF_ANALOG CHANNEL"));
                                    if (isSetSrcRefExistAndEmpty && isPurposeExistAndMatchChannel) {
                                        DataObject dataObject = new DataObject();
                                        dataObject.setDoName(tdoi.getName());
                                        DataAttribute dataAttribute = new DataAttribute();
                                        dataAttribute.setDaName(SETSRCREF_DA_NAME);
                                        dataAttribute.setDaiValues(List.of(new DaVal(null, ldepfLdevice.getLdName() + "/" + LPHD0_PROXY)));
                                        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
                                        lnEditor.updateOrCreateDOAndDAInstances(ln0, doLinkedToDa);
                                    }
                                })));
    }

}
