// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.dto.ConnectedApDTO;
import org.lfenergy.compas.sct.commons.dto.ControlBlock;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DAITracker;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class SclService {

    public static SclRootAdapter initScl(String hVersion, String hRevision) throws ScdException {
        UUID hId = UUID.randomUUID();
        return new SclRootAdapter(hId.toString(),hVersion,hRevision);
    }

    public static SclRootAdapter addHistoryItem(SCL scd, String who, String what, String why){
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        headerAdapter.addHistoryItem(who,what,why);
        return sclRootAdapter;
    }
    public static SclRootAdapter updateHeader(@NonNull SCL scd, @NonNull HeaderDTO headerDTO) {

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();

        boolean hUpdated = false;
        String hVersion = headerDTO.getVersion();
        String hRevision = headerDTO.getRevision();
        if(hVersion != null && !hVersion.equals(headerAdapter.getHeaderVersion())){
            headerAdapter.updateVersion(hVersion);
            hUpdated = true;
        }

        if(hRevision != null && !hRevision.equals(headerAdapter.getHeaderRevision())){
            headerAdapter.updateRevision(hRevision);
            hUpdated = true;
        }

        if(hUpdated && !headerDTO.getHistoryItems().isEmpty()){
            headerAdapter.addHistoryItem(
                    headerDTO.getHistoryItems().get(0).getWho(),
                    headerDTO.getHistoryItems().get(0).getWhat(),
                    headerDTO.getHistoryItems().get(0).getWhy()
            );
        }

        return sclRootAdapter;
    }


    public static IEDAdapter addIED(SCL scd, String iedName, SCL icd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.addIED(icd,iedName);
    }

    public static Optional<CommunicationAdapter> addSubnetworks(SCL scd, Set<SubNetworkDTO> subNetworks) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = null;
        if(!subNetworks.isEmpty()) {
            communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);

            for (SubNetworkDTO subNetworkDTO : subNetworks) {
                String snName = subNetworkDTO.getName();
                String snType = subNetworkDTO.getType();
                for (ConnectedApDTO accessPoint : subNetworkDTO.getConnectedAPs()) {
                    communicationAdapter.addSubnetwork(snName, snType,
                            accessPoint.getIedName(), accessPoint.getApName());
                }
            }
            return Optional.of(communicationAdapter);
        }
        return Optional.empty();
    }

    public static List<SubNetworkDTO> getSubnetwork(SCL scd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(false);
        return communicationAdapter.getSubNetworkAdapters()
                .stream()
                .map(subNetworkAdapter -> SubNetworkDTO.from(subNetworkAdapter))
                .collect(Collectors.toList());
    }

    public static List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException {

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        return lDeviceAdapter.getExtRefInfo();
    }


    public static List<ExtRefBindingInfo> getExtRefBinders(SCL scd, String iedName, String ldInst,
                                   String lnClass, String lnInst, String prefix, ExtRefSignalInfo signalInfo) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        AbstractLNAdapter abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
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

    public static void updateExtRefBinders(SCL scd, ExtRefInfo extRefInfo) throws ScdException {
        if(extRefInfo.getBindingInfo() == null || extRefInfo.getSignalInfo() == null){
            throw new ScdException("ExtRef Signal and/or Binding information are missing");
        }
        String iedName = extRefInfo.getHolderIEDName();
        String ldInst = extRefInfo.getHolderLDInst();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                    () -> new ScdException(
                            String.format(
                                    "Unknown LDevice (%s) in IED (%s)", ldInst, iedName
                            )
                    )
                );

        AbstractLNAdapter abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(extRefInfo.getHolderLnClass())
                .withLnInst(extRefInfo.getHolderLnInst())
                .withLnPrefix(extRefInfo.getHolderLnPrefix())
                .build();

        abstractLNAdapter.updateExtRefBinders(extRefInfo);
    }


    public static List<ControlBlock<?>> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException {


        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if(!signalInfo.isValid()){
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if(!bindingInfo.isValid()){
            throw new ScdException("Invalid or missing attributes in ExtRef binding info");
        }

        String iedName = extRefInfo.getHolderIEDName();
        if(bindingInfo.getIedName().equals(iedName)){
            throw new ScdException(String.format("Internal binding can't have control block"));
        }

        String ldInst = extRefInfo.getHolderLDInst();
        String lnClass = extRefInfo.getHolderLnClass();
        String lnInst = extRefInfo.getHolderLnInst();
        String prefix = extRefInfo.getHolderLnPrefix();
        // Check holder (IED,LD,LN) exists
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();

        lnAdapter.checkExtRefInfoCoherence(extRefInfo);

        // Get CBs
        IEDAdapter srcIEDAdapter = sclRootAdapter.getIEDAdapterByName(bindingInfo.getIedName());
        LDeviceAdapter srcLDeviceAdapter = srcIEDAdapter.getLDeviceAdapterByLdInst(extRefInfo.getBindingInfo().getLdInst())
                .orElseThrow();

        AbstractLNAdapter<?> srcLnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(srcLDeviceAdapter)
                .withLnClass(extRefInfo.getBindingInfo().getLnClass())
                .withLnInst(extRefInfo.getBindingInfo().getLnInst())
                .withLnPrefix(extRefInfo.getBindingInfo().getPrefix())
                .build();
        return srcLnAdapter.getControlSetByExtRefInfo(extRefInfo);
    }

    public static TExtRef updateExtRefSource(SCL scd, ExtRefInfo extRefInfo) throws ScdException {
        String iedName = extRefInfo.getHolderIEDName();
        String ldInst = extRefInfo.getHolderLDInst();
        String lnClass = extRefInfo.getHolderLnClass();
        String lnInst = extRefInfo.getHolderLnInst();
        String prefix = extRefInfo.getHolderLnPrefix();

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if(signalInfo == null || !signalInfo.isValid()){
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if(bindingInfo == null || !bindingInfo.isValid()){
            throw new ScdException("Invalid or missing attributes in ExtRef binding info");
        }
        if(bindingInfo.getIedName().equals(iedName)){
            throw new ScdException(String.format("Internal binding can't have control block"));
        }
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if(sourceInfo == null || !sourceInfo.isValid()){
            throw new ScdException("Invalid or missing attributes in ExtRef binding info");
        }

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );
        var anLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();
        return anLNAdapter.updateExtRefSource(extRefInfo);
    }

    public static Set<ResumedDataTemplate> getDAI(SCL scd, String iedName, String ldInst,
                                ResumedDataTemplate rDtt, boolean updatable) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = new IEDAdapter(sclRootAdapter,iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );

        return lDeviceAdapter.getDAI(rDtt, updatable);

    }

    public static void updateDAI(SCL scd, String iedName, String ldInst, ResumedDataTemplate rDtt) throws ScdException {
        long startTime = System.nanoTime();
        log.info(Utils.entering());
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dttAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(rDtt.getLnType())
                .orElseThrow(() -> new ScdException("Unknown LNodeType : " + rDtt.getLnType()));
        lNodeTypeAdapter.check(rDtt.getDoName(),rDtt.getDaName());

        if(TPredefinedBasicTypeEnum.OBJ_REF == rDtt.getBType()){
            Long sGroup = rDtt.getDaName().getDaiValues().keySet().stream().findFirst().orElse(-1L);
            String val = sGroup < 0 ? null : rDtt.getDaName().getDaiValues().get(sGroup);
            sclRootAdapter.checkObjRef(val);
        }

        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format("Unknown LDevice (%s) in IED (%s)", ldInst, iedName))
                );


        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(rDtt.getLnClass())
                .withLnInst(rDtt.getLnInst())
                .withLnPrefix(rDtt.getPrefix())
                .build();

        if(TPredefinedCDCEnum.ING == rDtt.getCdc() || TPredefinedCDCEnum.ASG == rDtt.getCdc() ){
            DAITracker daiTracker = new DAITracker(lnAdapter,rDtt.getDoName(),rDtt.getDaName());
            daiTracker.validateBoundedDAI();
        }
        lnAdapter.updateDAI(rDtt);
        log.info(Utils.leaving(startTime));
    }

    public static Set<Pair<Integer, String>> getEnumTypeElements(SCL scd, String idEnum) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dataTypeTemplateAdapter =  sclRootAdapter.getDataTypeTemplateAdapter();
        EnumTypeAdapter enumTypeAdapter = dataTypeTemplateAdapter.getEnumTypeAdapterById(idEnum)
                .orElseThrow(() -> new ScdException("Unknown EnumType Id: " +  idEnum));
        return enumTypeAdapter.getCurrentElem().getEnumVal().stream()
                .map(tEnumVal -> Pair.of(tEnumVal.getOrd(),tEnumVal.getValue()))
                .collect(Collectors.toSet());
    }
}