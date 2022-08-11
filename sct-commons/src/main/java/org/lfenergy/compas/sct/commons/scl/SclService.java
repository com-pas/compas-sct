// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_ICDHEADER;

@Slf4j
public class SclService {

    public static final String UNKNOWN_LDEVICE_S_IN_IED_S = "Unknown LDevice (%s) in IED (%s)";
    public static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";
    public static final ObjectFactory objectFactory = new ObjectFactory();

    private SclService() {
        throw new IllegalStateException("SclService class");
    }

    public static SclRootAdapter initScl(Optional<UUID> hId, String hVersion, String hRevision) throws ScdException {
        UUID headerId = hId.orElseGet(UUID::randomUUID);
        SclRootAdapter scdAdapter = new SclRootAdapter(headerId.toString(), hVersion, hRevision);
        scdAdapter.addPrivate(PrivateService.createPrivate(TCompasSclFileType.SCD));
        return scdAdapter;
    }

    public static SclRootAdapter addHistoryItem(SCL scd, String who, String what, String why) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        headerAdapter.addHistoryItem(who, what, why);
        return sclRootAdapter;
    }

    public static SclRootAdapter updateHeader(@NonNull SCL scd, @NonNull HeaderDTO headerDTO) {
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

        return sclRootAdapter;
    }

    public static IEDAdapter addIED(SCL scd, String iedName, SCL icd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.addIED(icd, iedName);
    }

    public static Optional<CommunicationAdapter> addSubnetworks(SCL scd, Set<SubNetworkDTO> subNetworks, Optional<SCL> icd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter;
        if (!subNetworks.isEmpty()) {
            communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);

            for (SubNetworkDTO subNetworkDTO : subNetworks) {
                String snName = subNetworkDTO.getName();
                String snType = subNetworkDTO.getType();
                for (ConnectedApDTO accessPoint : subNetworkDTO.getConnectedAPs()) {
                    String iedName = accessPoint.getIedName();
                    String apName = accessPoint.getApName();
                    communicationAdapter.addSubnetwork(snName, snType, iedName, apName);

                    Optional<SubNetworkAdapter> subNetworkAdapter = communicationAdapter.getSubnetworkByName(snName);
                    if (subNetworkAdapter.isPresent()) {
                        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.get()
                                .getConnectedAPAdapter(iedName, apName);
                        connectedAPAdapter.copyAddressAndPhysConnFromIcd(icd);
                    }

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
                .map(SubNetworkDTO::from)
                .collect(Collectors.toList());
    }

    public static List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException {

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
                );
        return lDeviceAdapter.getExtRefInfo();
    }

    public static List<ExtRefBindingInfo> getExtRefBinders(SCL scd, String iedName, String ldInst,
                                                           String lnClass, String lnInst, String prefix, ExtRefSignalInfo signalInfo) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
                );
        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
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
        for (IEDAdapter iedA : sclRootAdapter.getIEDAdapters()) {
            potentialBinders.addAll(iedA.getExtRefBinders(signalInfo));
        }
        return potentialBinders;
    }

    public static void updateExtRefBinders(SCL scd, ExtRefInfo extRefInfo) throws ScdException {
        if (extRefInfo.getBindingInfo() == null || extRefInfo.getSignalInfo() == null) {
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
                                        UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName
                                )
                        )
                );

        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(extRefInfo.getHolderLnClass())
                .withLnInst(extRefInfo.getHolderLnInst())
                .withLnPrefix(extRefInfo.getHolderLnPrefix())
                .build();

        abstractLNAdapter.updateExtRefBinders(extRefInfo);
    }

    public static List<ControlBlock<?>> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException {

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

        String ldInst = extRefInfo.getHolderLDInst();
        String lnClass = extRefInfo.getHolderLnClass();
        String lnInst = extRefInfo.getHolderLnInst();
        String prefix = extRefInfo.getHolderLnPrefix();
        // Check holder (IED,LD,LN) exists
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
                );
        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();

        abstractLNAdapter.checkExtRefInfoCoherence(extRefInfo);

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
        if (signalInfo == null || !signalInfo.isValid()) {
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if (bindingInfo == null || !bindingInfo.isValid()) {
            throw new ScdException(INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO);
        }
        if (bindingInfo.getIedName().equals(iedName)) {
            throw new ScdException("Internal binding can't have control block");
        }
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if (sourceInfo == null || !sourceInfo.isValid()) {
            throw new ScdException(INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO);
        }

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
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
        IEDAdapter iedAdapter = new IEDAdapter(sclRootAdapter, iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
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
        lNodeTypeAdapter.check(rDtt.getDoName(), rDtt.getDaName());

        if (TPredefinedBasicTypeEnum.OBJ_REF == rDtt.getBType()) {
            Long sGroup = rDtt.getDaName().getDaiValues().keySet().stream().findFirst().orElse(-1L);
            String val = sGroup < 0 ? null : rDtt.getDaName().getDaiValues().get(sGroup);
            sclRootAdapter.checkObjRef(val);
        }

        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
                );

        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(rDtt.getLnClass())
                .withLnInst(rDtt.getLnInst())
                .withLnPrefix(rDtt.getPrefix())
                .build();

        if (TPredefinedCDCEnum.ING == rDtt.getCdc() || TPredefinedCDCEnum.ASG == rDtt.getCdc()) {
            DAITracker daiTracker = new DAITracker(lnAdapter, rDtt.getDoName(), rDtt.getDaName());
            daiTracker.validateBoundedDAI();
        }
        lnAdapter.updateDAI(rDtt);
        log.info(Utils.leaving(startTime));
    }

    public static Set<Pair<Integer, String>> getEnumTypeElements(SCL scd, String idEnum) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        EnumTypeAdapter enumTypeAdapter = dataTypeTemplateAdapter.getEnumTypeAdapterById(idEnum)
                .orElseThrow(() -> new ScdException("Unknown EnumType Id: " + idEnum));
        return enumTypeAdapter.getCurrentElem().getEnumVal().stream()
                .map(tEnumVal -> Pair.of(tEnumVal.getOrd(), tEnumVal.getValue()))
                .collect(Collectors.toSet());
    }

    public static SclRootAdapter importSTDElementsInSCD(@NonNull SclRootAdapter scdRootAdapter, Set<SCL> stds,
                                                        Map<Pair<String, String>, List<String>> comMap) throws ScdException {

        //Check SCD and STD compatibilities
        Map<String, Pair<TPrivate, List<SCL>>> mapICDSystemVersionUuidAndSTDFile = createMapICDSystemVersionUuidAndSTDFile(stds);
        checkSTDCorrespondanceWithLNodeCompasICDHeader(mapICDSystemVersionUuidAndSTDFile);
        // List all Private and remove duplicated one with same iedName
        Map<String, TPrivate> mapIEDNameAndPrivate = createMapIEDNameAndPrivate(scdRootAdapter);
        //For each Private.ICDSystemVersionUUID and Private.iedName find STD File
        for (Map.Entry<String, TPrivate> entry : mapIEDNameAndPrivate.entrySet()) {
            String iedName = entry.getKey();
            TPrivate tPrivate = entry.getValue();
            String icdSysVerUuid = PrivateService.getCompasICDHeader(tPrivate).map(TCompasICDHeader::getICDSystemVersionUUID).orElseThrow(
                    () -> new ScdException(ICD_SYSTEM_VERSION_UUID + " is not present in COMPAS-ICDHeader in LNode")
            );

            if (!mapICDSystemVersionUuidAndSTDFile.containsKey(icdSysVerUuid))
                throw new ScdException("There is no STD file found corresponding to " + stdCheckFormatExceptionMessage(tPrivate));
            // import /ied /dtt in Scd
            SCL std = mapICDSystemVersionUuidAndSTDFile.get(icdSysVerUuid).getRight().get(0);
            SclRootAdapter stdRootAdapter = new SclRootAdapter(std);
            IEDAdapter stdIedAdapter = new IEDAdapter(stdRootAdapter, std.getIED().get(0));
            Optional<TPrivate> optionalTPrivate = stdIedAdapter.getPrivateHeader(COMPAS_ICDHEADER.getPrivateType());
            if (optionalTPrivate.isPresent() && comparePrivateCompasICDHeaders(optionalTPrivate.get(), tPrivate)) {
                copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(optionalTPrivate.get(), tPrivate);
            } else throw new ScdException("COMPAS-ICDHeader is not the same in Substation and in IED");
            scdRootAdapter.addIED(std, iedName);

            //import connectedAP and rename ConnectedAP/@iedName
            CommunicationAdapter comAdapter = stdRootAdapter.getCommunicationAdapter(false);
            Set<SubNetworkDTO> subNetworkDTOSet = SubNetworkDTO.createDefaultSubnetwork(iedName, comAdapter, comMap);
            addSubnetworks(scdRootAdapter.getCurrentElem(), subNetworkDTOSet, Optional.of(std));
        }
        return scdRootAdapter;
    }

    private static void checkSTDCorrespondanceWithLNodeCompasICDHeader(Map<String, Pair<TPrivate, List<SCL>>> mapICDSystemVersionUuidAndSTDFile) throws ScdException {
        for (Pair<TPrivate, List<SCL>> pairOfPrivateAndSTDs : mapICDSystemVersionUuidAndSTDFile.values()) {
            if (pairOfPrivateAndSTDs.getRight().size() != 1) {
                TPrivate key = pairOfPrivateAndSTDs.getLeft();
                throw new ScdException("There are several STD files corresponding to " + stdCheckFormatExceptionMessage(key));
            }
        }
    }

    private static String stdCheckFormatExceptionMessage(TPrivate key) throws ScdException {
        Optional<TCompasICDHeader> optionalCompasICDHeader = PrivateService.getCompasICDHeader(key);
        return  HEADER_ID + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderId).orElse(null) +
                HEADER_VERSION + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderVersion).orElse(null) +
                HEADER_REVISION + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderRevision).orElse(null) +
                "and " + ICD_SYSTEM_VERSION_UUID + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getICDSystemVersionUUID).orElse(null);
    }

    private static Map<String, TPrivate> createMapIEDNameAndPrivate(SclRootAdapter scdRootAdapter) {
        return scdRootAdapter.getCurrentElem().getSubstation().get(0).getVoltageLevel().stream()
                .map(TVoltageLevel::getBay).flatMap(Collection::stream)
                .map(TBay::getFunction).flatMap(Collection::stream)
                .map(TFunction::getLNode).flatMap(Collection::stream)
                .map(TLNode::getPrivate).flatMap(Collection::stream)
                .filter(tPrivate ->
                        tPrivate.getType().equals(COMPAS_ICDHEADER.getPrivateType())
                                && PrivateService.getCompasICDHeader(tPrivate).isPresent() && PrivateService.getCompasICDHeader(tPrivate).get().getIEDName() != null)
                .collect(Collectors.toMap(tPrivate -> PrivateService.getCompasICDHeader(tPrivate).get().getIEDName(), Function.identity()));
    }

    private static Map<String, Pair<TPrivate, List<SCL>>> createMapICDSystemVersionUuidAndSTDFile(Set<SCL> stds) {
        Map<String, Pair<TPrivate, List<SCL>>> stringSCLMap = new HashMap<>();
        stds.forEach(std -> std.getIED().forEach(ied -> ied.getPrivate().forEach(tp ->
            PrivateService.getCompasICDHeader(tp).map(TCompasICDHeader::getICDSystemVersionUUID).ifPresent(icdSysVer -> {
                Pair<TPrivate, List<SCL>> pair = stringSCLMap.get(icdSysVer);
                List<SCL> list = pair != null ? pair.getRight() : new ArrayList<>();
                list.add(std);
                stringSCLMap.put(icdSysVer, Pair.of(tp, list));
            })
        )));
        return stringSCLMap;
    }

    private static boolean comparePrivateCompasICDHeaders(TPrivate iedPrivate, TPrivate scdPrivate) throws ScdException {
        TCompasICDHeader iedCompasICDHeader = PrivateService.getCompasICDHeader(iedPrivate).orElseThrow(
            () -> new ScdException(COMPAS_ICDHEADER + "not found in IED Private "));
        TCompasICDHeader scdCompasICDHeader = PrivateService.getCompasICDHeader(scdPrivate).orElseThrow(
            () -> new ScdException(COMPAS_ICDHEADER + "not found in LNode Private "));
        return iedCompasICDHeader.getIEDType().equals(scdCompasICDHeader.getIEDType())
                && iedCompasICDHeader.getICDSystemVersionUUID().equals(scdCompasICDHeader.getICDSystemVersionUUID())
                && iedCompasICDHeader.getVendorName().equals(scdCompasICDHeader.getVendorName())
                && iedCompasICDHeader.getIEDredundancy().equals(scdCompasICDHeader.getIEDredundancy())
                && iedCompasICDHeader.getIEDmodel().equals(scdCompasICDHeader.getIEDmodel())
                && iedCompasICDHeader.getHwRev().equals(scdCompasICDHeader.getHwRev())
                && iedCompasICDHeader.getSwRev().equals(scdCompasICDHeader.getSwRev())
                && iedCompasICDHeader.getHeaderId().equals(scdCompasICDHeader.getHeaderId())
                && iedCompasICDHeader.getHeaderRevision().equals(scdCompasICDHeader.getHeaderRevision())
                && iedCompasICDHeader.getHeaderVersion().equals(scdCompasICDHeader.getHeaderVersion());
    }

    private static void copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(TPrivate stdPrivate, TPrivate lNodePrivate) throws ScdException {
        TCompasICDHeader lNodeCompasICDHeader = PrivateService.getCompasICDHeader(lNodePrivate).orElseThrow(
                () -> new ScdException(COMPAS_ICDHEADER + " not found in LNode Private "));
        stdPrivate.getContent().clear();
        stdPrivate.getContent().add(objectFactory.createICDHeader(lNodeCompasICDHeader));
    }

    public static void removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(final SCL scl) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        List<LDeviceAdapter> lDeviceAdapters = sclRootAdapter.getIEDAdapters().stream()
            .map(IEDAdapter::getLDeviceAdapters).flatMap(List::stream).collect(Collectors.toList());

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

}
