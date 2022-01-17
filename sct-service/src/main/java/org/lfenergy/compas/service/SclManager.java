// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.service;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.dto.ConnectedApDTO;
import org.lfenergy.compas.sct.commons.dto.ControlBlock;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.dto.LNodeDTO;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SclManager {


    public SclRootAdapter addHistoryItem(SCL scd, String who, String what, String why){
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        headerAdapter.addHistoryItem(who,what,why);
        return sclRootAdapter;
    }

    public IEDAdapter addIED(SCL scd, String iedName, SCL icd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.addIED(icd,iedName);
    }

    public CommunicationAdapter addSubnetworks(SCL scd, Set<SubNetworkDTO> subNetworks) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);
        for(SubNetworkDTO subNetworkDTO : subNetworks){
            String snName = subNetworkDTO.getName();
            String snType = subNetworkDTO.getType();
            for (ConnectedApDTO accessPoint : subNetworkDTO.getConnectedAPs()) {
                communicationAdapter.addSubnetwork(snName, snType,
                        accessPoint.getIedName(), accessPoint.getApName());
            }
        }
        return communicationAdapter;
    }

    public List<SubNetworkDTO> getSubnetwork(SCL scd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(false);
        return communicationAdapter.getSubNetworkAdapters()
                .stream()
                .map(subNetworkAdapter -> SubNetworkDTO.from(subNetworkAdapter))
                .collect(Collectors.toList());
    }

    public List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException {

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapter(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        return lDeviceAdapter.getExtRefInfo();
    }


    public List<ExtRefBindingInfo> getExtRefBinders(SCL scd, String iedName, String ldInst,
                                   String lnClass, String lnInst, ExtRefSignalInfo signalInfo) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapter(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        AbstractLNAdapter abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .build();

        // check for signal existence
        // The below throws exception if the signal doesn't exist
        abstractLNAdapter.getExtRefsBySignalInfo(signalInfo);

        // find potential binders for the signalInfo
        List<ExtRefBindingInfo> potentialBinders = new ArrayList<>();
        for(IEDAdapter iedA : sclRootAdapter.getIEDAdapters()){
            potentialBinders.addAll(iedA.getExtRefBinders(signalInfo));
        }
        return potentialBinders;
    }

    public void updateExtRefBinders(SCL scd, String iedName, String ldInst, LNodeDTO lNodeDTO) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = new IEDAdapter(sclRootAdapter,iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                    () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );

        AbstractLNAdapter abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lNodeDTO.getNodeClass())
                .withLnInst(lNodeDTO.getInst())
                .withLnPrefix(lNodeDTO.getPrefix())
                .build();

        abstractLNAdapter.updateExtRefBinders(lNodeDTO.getExtRefs());
    }


    public List<ControlBlock<?>> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException {


        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if(!signalInfo.isValid()){
            throw new IllegalArgumentException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if(!bindingInfo.isValid()){
            throw new IllegalArgumentException("Invalid or missing attributes in ExtRef binding info");
        }

        String iedName = extRefInfo.getHolderIedName();
        if(bindingInfo.getIedName().equals(iedName)){
            throw new IllegalArgumentException(String.format("Internal binding can't have control block"));
        }

        String ldInst = extRefInfo.getHolderLdInst();
        String lnClass = extRefInfo.getHolderLnClass();
        String lnInst = extRefInfo.getHolderLnInst();
        // Check holder (IED,LD,LN) exists
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapter(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .build();

        lnAdapter.checkExtRefInfoCoherence(extRefInfo);

        // Get CBs
        IEDAdapter srcIEDAdapter = sclRootAdapter.getIEDAdapter(bindingInfo.getIedName());
        return srcIEDAdapter.getControlBlocksByBindingInfo(extRefInfo);
    }

    public TExtRef updateExtRefSource(SCL scd, ExtRefInfo extRefInfo) throws ScdException {
        String iedName = extRefInfo.getHolderIedName();
        String ldInst = extRefInfo.getHolderLdInst();
        String lnClass = extRefInfo.getHolderLnClass();
        String lnInst = extRefInfo.getHolderLnInst();

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if(signalInfo == null || !signalInfo.isValid()){
            throw new IllegalArgumentException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if(bindingInfo == null || !bindingInfo.isValid()){
            throw new IllegalArgumentException("Invalid or missing attributes in ExtRef binding info");
        }
        if(bindingInfo.getIedName().equals(iedName)){
            throw new IllegalArgumentException(String.format("Internal binding can't have control block"));
        }
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if(sourceInfo == null ){
            throw new IllegalArgumentException("Invalid or missing attributes in ExtRef source info");
        }

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapter(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        var anLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .build();
        return anLNAdapter.updateExtRefSource(extRefInfo);
    }
}
