// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.service.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct. commons.dto.*;
import org.lfenergy.compas.sct. commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct. commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct. commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct. commons.scl.com.SubNetworkAdapter;
import org.lfenergy.compas.sct. commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct. commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct. commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct. commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct. commons.scl.icd.IcdHeader;
import org.lfenergy.compas.sct. commons.scl.ied.*;
import org.lfenergy.compas.sct. commons.scl.sstation.SubstationAdapter;
import org.lfenergy.compas.sct.commons.service.ISclService;
import org.lfenergy.compas.sct. commons.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct. commons.util.CommonConstants.IED_TEST_NAME;
import static org.lfenergy.compas.sct. commons.util.PrivateEnum.COMPAS_ICDHEADER;

@Slf4j
public class SclService implements ISclService {

    @Override
    public SCL initScl(final UUID hId, final String hVersion, final String hRevision) throws ScdException {
        SclRootAdapter scdAdapter = new SclRootAdapter(hId.toString(), hVersion, hRevision);
        scdAdapter.addPrivate(PrivateUtils.createPrivate(TCompasSclFileType.SCD));
        return scdAdapter.getCurrentElem();
    }

    @Override
    public void addHistoryItem(SCL scd, String who, String what, String why) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        headerAdapter.addHistoryItem(who, what, why);
    }

    @Override
    public void updateHeader(@NonNull SCL scd, @NonNull HeaderDTO headerDTO) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        boolean hUpdated = false;
        String hVersion = headerDTO.getVersion();
        String hRevision = headerDTO.getRevision();
        if (hVersion != null && !hVersion.equals(headerAdapter.getHeaderVersion())) {
            headerAdapter.updateVersion(hVersion);
            hUpdated = true;
        }
        if (hRevision != null && !hRevision.equals(headerAdapter.getHeaderRevision())) {
            headerAdapter.updateRevision(hRevision);
            hUpdated = true;
        }
        if (hUpdated && !headerDTO.getHistoryItems().isEmpty()) {
            headerAdapter.addHistoryItem(
                    headerDTO.getHistoryItems().get(0).getWho(),
                    headerDTO.getHistoryItems().get(0).getWhat(),
                    headerDTO.getHistoryItems().get(0).getWhy()
            );
        }
    }

    @Override
    public void addIED(SCL scd, String iedName, SCL icd) throws ScdException {
         new SclRootAdapter(scd).addIED(icd, iedName);
    }

    @Override
    public void addSubnetworks(SCL scd, List<SubNetworkDTO> subNetworks, SCL icd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter;
        if (!subNetworks.isEmpty()) {
            communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);
            for (SubNetworkDTO subNetworkDTO : subNetworks) {
                String snName = subNetworkDTO.getName();
                String snType = subNetworkDTO.getType();
                for (ConnectedApDTO accessPoint : subNetworkDTO.getConnectedAPs()) {
                    String iedName = accessPoint.iedName();
                    String apName = accessPoint.apName();
                    communicationAdapter.addSubnetwork(snName, snType, iedName, apName);

                    Optional<SubNetworkAdapter> subNetworkAdapter = communicationAdapter.getSubnetworkByName(snName);
                    if (subNetworkAdapter.isPresent()) {
                        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.get().getConnectedAPAdapter(iedName, apName);
                        connectedAPAdapter.copyAddressAndPhysConnFromIcd(icd);
                    }
                }
            }
        }
    }

    @Override
    public List<SubNetworkDTO> getSubnetwork(SCL scd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(false);
        return communicationAdapter.getSubNetworkAdapters().stream()
                .map(SubNetworkDTO::from)
                .toList();
    }

    @Override
    public List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException {
        LDeviceAdapter lDeviceAdapter = createLDeviceAdapter(scd, iedName, ldInst);
        return lDeviceAdapter.getExtRefInfo();
    }

    @Override
    public List<ExtRefBindingInfo> getExtRefBinders(SCL scd, String iedName, String ldInst, String lnClass, String lnInst, String prefix, ExtRefSignalInfo signalInfo) throws ScdException {
        LDeviceAdapter lDeviceAdapter = createLDeviceAdapter(scd, iedName, ldInst);
        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();

        // check for signal existence
        abstractLNAdapter.isExtRefExist(signalInfo);

        // find potential binders for the signalInfo
        return lDeviceAdapter.getParentAdapter().getParentAdapter().streamIEDAdapters()
                .map(iedAdapter1 -> iedAdapter1.getExtRefBinders(signalInfo))
                .flatMap(Collection::stream)
                .sorted()
                .toList();
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
                .orElseThrow(() -> new ScdException(String.format(UNKNOWN_LDEVICE_IN_IED, ldInst, iedName)));

        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(extRefInfo.getHolderLnClass())
                .withLnInst(extRefInfo.getHolderLnInst())
                .withLnPrefix(extRefInfo.getHolderLnPrefix())
                .build();

        abstractLNAdapter.updateExtRefBinders(extRefInfo);
    }

    @Override
    public List<ControlBlock> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException {

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if (!signalInfo.isValid()) {
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if (!bindingInfo.isValid()) {
            throw new ScdException(INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO);
        }

        String iedName = extRefInfo.getHolderIEDName();
        if (bindingInfo.getIedName().equals(iedName)) {
            throw new ScdException("Internal binding can't have control block");
        }

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);

        // Get CBs
        IEDAdapter srcIEDAdapter = sclRootAdapter.getIEDAdapterByName(bindingInfo.getIedName());
        LDeviceAdapter srcLDeviceAdapter = srcIEDAdapter.findLDeviceAdapterByLdInst(extRefInfo.getBindingInfo().getLdInst())
                .orElseThrow();

        List<AbstractLNAdapter<?>> aLNAdapters = srcLDeviceAdapter.getLNAdaptersIncludingLN0();

        return aLNAdapters.stream()
                .map(abstractLNAdapter1 -> abstractLNAdapter1.getControlBlocksForMatchingFCDA(extRefInfo))
                .flatMap(Collection::stream)
                .toList();
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

        LDeviceAdapter lDeviceAdapter = createLDeviceAdapter(scd, iedName, ldInst);
        AbstractLNAdapter<?> anLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();
        return anLNAdapter.updateExtRefSource(extRefInfo);
    }

    @Override
    public Set<DataAttributeRef> getDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef, boolean updatable) throws ScdException {
        LDeviceAdapter lDeviceAdapter = createLDeviceAdapter(scd, iedName, ldInst);
        return lDeviceAdapter.getDAI(dataAttributeRef, updatable);
    }

    @Override
    public void updateDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef) throws ScdException {
        long startTime = System.nanoTime();
        log.info(Utils.entering());
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dttAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(dataAttributeRef.getLnType())
                .orElseThrow(() -> new ScdException("Unknown LNodeType : " + dataAttributeRef.getLnType()));
        lNodeTypeAdapter.check(dataAttributeRef.getDoName(), dataAttributeRef.getDaName());

        if (TPredefinedBasicTypeEnum.OBJ_REF == dataAttributeRef.getBType()) {
            Long sGroup = dataAttributeRef.getDaName().getDaiValues().keySet().stream().findFirst().orElse(-1L);
            String val = sGroup < 0 ? null : dataAttributeRef.getDaName().getDaiValues().get(sGroup);
            sclRootAdapter.checkObjRef(val);
        }

        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(() -> new ScdException(String.format(UNKNOWN_LDEVICE_IN_IED, ldInst, iedName)));

        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(dataAttributeRef.getLnClass())
                .withLnInst(dataAttributeRef.getLnInst())
                .withLnPrefix(dataAttributeRef.getPrefix())
                .build();

        if (TPredefinedCDCEnum.ING == dataAttributeRef.getCdc() || TPredefinedCDCEnum.ASG == dataAttributeRef.getCdc()) {
            DAITracker daiTracker = new DAITracker(lnAdapter, dataAttributeRef.getDoName(), dataAttributeRef.getDaName());
            daiTracker.validateBoundedDAI();
        }
        lnAdapter.updateDAI(dataAttributeRef);
        log.info(Utils.leaving(startTime));
    }

    @Override
    public Set<EnumValDTO> getEnumTypeValues(SCL scd, String idEnum) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        EnumTypeAdapter enumTypeAdapter = dataTypeTemplateAdapter.getEnumTypeAdapterById(idEnum)
                .orElseThrow(() -> new ScdException("Unknown EnumType Id: " + idEnum));
        return enumTypeAdapter.getCurrentElem().getEnumVal().stream()
                .map(tEnumVal -> new EnumValDTO(tEnumVal.getOrd(), tEnumVal.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public void importSTDElementsInSCD(SCL scd, List<SCL> stds, List<SubNetworkTypeDTO> subNetworkTypes) throws ScdException {

        //Check SCD and STD compatibilities
        Map<String, PrivateLinkedToStds> mapICDSystemVersionUuidAndSTDFile = PrivateUtils.createMapICDSystemVersionUuidAndSTDFile(stds);
        PrivateUtils.checkSTDCorrespondanceWithLNodeCompasICDHeader(mapICDSystemVersionUuidAndSTDFile);
        // List all Private and remove duplicated one with same iedName
        // For each Private.ICDSystemVersionUUID and Private.iedName find STD File
        List<String> iedNamesUsed = new ArrayList<>();
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        PrivateUtils.streamIcdHeaders(scd)
                .forEach(icdHeader -> {
                    if (!iedNamesUsed.contains(icdHeader.getIedName())) {
                        String iedName = icdHeader.getIedName();
                        iedNamesUsed.add(iedName);
                        String icdSysVerUuid = icdHeader.getIcdSystemVersionUUID();
                        if (!mapICDSystemVersionUuidAndSTDFile.containsKey(icdSysVerUuid))
                            throw new ScdException("There is no STD file found corresponding to " + icdHeader);
                        // import /ied /dtt in Scd
                        SCL std = mapICDSystemVersionUuidAndSTDFile.get(icdSysVerUuid).stdList().get(0);
                        SclRootAdapter stdRootAdapter = new SclRootAdapter(std);
                        IEDAdapter stdIedAdapter = new IEDAdapter(stdRootAdapter, std.getIED().get(0));
                        Optional<TPrivate> optionalTPrivate = stdIedAdapter.getPrivateHeader(COMPAS_ICDHEADER.getPrivateType());
                        if (optionalTPrivate.isPresent() && optionalTPrivate.flatMap(PrivateUtils::extractCompasICDHeader).map(IcdHeader::new).get().equals(icdHeader)) {
                            PrivateUtils.copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(optionalTPrivate.get(), icdHeader.toTCompasICDHeader());
                        } else throw new ScdException("COMPAS-ICDHeader is not the same in Substation and in IED");
                        scdRootAdapter.addIED(std, iedName);

                        //import connectedAP and rename ConnectedAP/@iedName
                        TCommunication communication = stdRootAdapter.getCurrentElem().getCommunication();
                        List<SubNetworkDTO> subNetworkDTOSet = SubNetworkDTO.createDefaultSubnetwork(iedName, communication, subNetworkTypes);
                        addSubnetworks(scdRootAdapter.getCurrentElem(), subNetworkDTOSet, std);
                    }
                });
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
    public List<SclReportItem> updateLDeviceStatus(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        SubstationAdapter substationAdapter = sclRootAdapter.getSubstationAdapter();
        final List<Pair<String, String>> iedNameLdInstList = substationAdapter.getIedAndLDeviceNamesForLN0FromLNode();
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .map(LDeviceAdapter::getLN0Adapter)
                .map(ln0Adapter -> ln0Adapter.updateLDeviceStatus(iedNameLdInstList))
                .flatMap(Optional::stream)
                .toList();
    }

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
    public List<SclReportItem> updateDoInRef(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .map(LDeviceAdapter::getLN0Adapter)
                .map(LN0Adapter::updateDoInRef)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public List<SclReportItem> manageMonitoringLns(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> !iedAdapter.getName().contains(IED_TEST_NAME))
                .map(IEDAdapter::manageMonitoringLns)
                .flatMap(List::stream)
                .toList();
    }
}
