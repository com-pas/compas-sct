// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl.extensions.commons.CompasExtensionsConstants;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.CommonConstants;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DAITracker;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.VoltageLevelAdapter;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.CommonConstants.ICD_SYSTEM_VERSION_UUID;

@Slf4j
public class SclService {

    public static final String UNKNOWN_LDEVICE_S_IN_IED_S = "Unknown LDevice (%s) in IED (%s)";
    public static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";

    private SclService() {
        throw new IllegalStateException("SclService class");
    }

    public static SclRootAdapter initScl(Optional<UUID> hId, String hVersion, String hRevision) throws ScdException {
        UUID headerId = hId.orElseGet(UUID::randomUUID);
        SclRootAdapter scdAdapter = new SclRootAdapter(headerId.toString(), hVersion, hRevision);
        scdAdapter.addPrivate(initSclFileType());
        return scdAdapter;
    }

    private static TPrivate initSclFileType() {
        TPrivate fileTypePrivate = new TPrivate();
        fileTypePrivate.setType(CommonConstants.COMPAS_SCL_FILE_TYPE);
        JAXBElement<TCompasSclFileType> compasFileType = new JAXBElement<>(
                new QName(CompasExtensionsConstants.COMPAS_EXTENSION_NS_URI, CommonConstants.SCL_FILE_TYPE),
                TCompasSclFileType.class, TCompasSclFileType.SCD);
        fileTypePrivate.getContent().add(compasFileType);
        return fileTypePrivate;
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

    public static SclRootAdapter addSubstation(@NonNull SCL scd, @NonNull SCL ssd) throws ScdException {
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        if (scdRootAdapter.getCurrentElem().getSubstation().size() > 1
                || ssdRootAdapter.currentElem.getSubstation().size() != 1) {
            throw new ScdException("SCD file must have one or zero Substation and " +
                    "SCD file must have one Substation. The files are rejected.");
        }
        TSubstation ssdTSubstation = ssdRootAdapter.currentElem.getSubstation().get(0);
        if (scdRootAdapter.getCurrentElem().getSubstation().isEmpty()) {
            scdRootAdapter.getCurrentElem().getSubstation().add(ssdTSubstation);
            return scdRootAdapter;
        } else {
            TSubstation scdTSubstation = scdRootAdapter.currentElem.getSubstation().get(0);
            if (scdTSubstation.getName().equalsIgnoreCase(ssdTSubstation.getName())) {
                SubstationAdapter scdSubstationAdapter = scdRootAdapter.getSubstationAdapter(scdTSubstation.getName());
                for (TVoltageLevel tvl : ssdTSubstation.getVoltageLevel()) {
                    updateVoltageLevel(scdSubstationAdapter, tvl);
                }
            } else
                throw new ScdException("SCD file must have only one Substation and the Substation name from SSD file is" +
                        " different from the one in SCD file. The files are rejected.");
        }
        return scdRootAdapter;
    }

    private static void updateVoltageLevel(@NonNull SubstationAdapter scdSubstationAdapter, TVoltageLevel vl) throws ScdException {
        if (scdSubstationAdapter.getVoltageLevelAdapter(vl.getName()).isPresent()) {
            VoltageLevelAdapter scdVoltageLevelAdapter = scdSubstationAdapter.getVoltageLevelAdapter(vl.getName())
                    .orElseThrow(() -> new ScdException("Unable to create VoltageLevelAdapter"));
            for (TBay tbay : vl.getBay()) {
                updateBay(scdVoltageLevelAdapter, tbay);
            }
        } else {
            scdSubstationAdapter.getCurrentElem().getVoltageLevel().add(vl);
        }
    }

    private static void updateBay(@NonNull VoltageLevelAdapter scdVoltageLevelAdapter, TBay tBay) {
        if (scdVoltageLevelAdapter.getBayAdapter(tBay.getName()).isPresent()) {
            scdVoltageLevelAdapter.getCurrentElem().getBay()
                    .removeIf(t -> t.getName().equalsIgnoreCase(tBay.getName()));
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        } else {
            scdVoltageLevelAdapter.getCurrentElem().getBay().add(tBay);
        }
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
            String icdSysVerUuid = getCompasICDHeader(tPrivate).map(TCompasICDHeader::getICDSystemVersionUUID).orElseThrow(
                    () -> new ScdException(ICD_SYSTEM_VERSION_UUID + " is not present in COMPAS-ICDHeader in LNode")
            );

            if (!mapICDSystemVersionUuidAndSTDFile.containsKey(icdSysVerUuid))
                throw new ScdException("There is no STD file found corresponding to " + stdCheckFormatExceptionMessage(tPrivate));
            // import /ied /dtt in Scd
            SCL std = mapICDSystemVersionUuidAndSTDFile.get(icdSysVerUuid).getRight().get(0);
            SclRootAdapter stdRootAdapter = new SclRootAdapter(std);
            IEDAdapter stdIedAdapter = new IEDAdapter(stdRootAdapter, std.getIED().get(0));
            Optional<TPrivate> optionalTPrivate = stdIedAdapter.getPrivateHeader(CommonConstants.COMPAS_ICDHEADER);
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

    private static String stdCheckFormatExceptionMessage(TPrivate key) {
        return  CommonConstants.HEADER_ID + " = " + getCompasICDHeader(key).map(TCompasICDHeader::getHeaderId).orElse(null) +
                CommonConstants.HEADER_VERSION + " = " + getCompasICDHeader(key).map(TCompasICDHeader::getHeaderVersion).orElse(null) +
                CommonConstants.HEADER_REVISION + " = " + getCompasICDHeader(key).map(TCompasICDHeader::getHeaderRevision).orElse(null) +
                "and " + ICD_SYSTEM_VERSION_UUID + " = " + getCompasICDHeader(key).map(TCompasICDHeader::getICDSystemVersionUUID).orElse(null);
    }

    private static Map<String, TPrivate> createMapIEDNameAndPrivate(SclRootAdapter scdRootAdapter) {
        return scdRootAdapter.getCurrentElem().getSubstation().get(0).getVoltageLevel().stream()
                .map(TVoltageLevel::getBay).flatMap(Collection::stream)
                .map(TBay::getFunction).flatMap(Collection::stream)
                .map(TFunction::getLNode).flatMap(Collection::stream)
                .map(TLNode::getPrivate).flatMap(Collection::stream)
                .filter(tPrivate ->
                        tPrivate.getType().equals(CommonConstants.COMPAS_ICDHEADER)
                                && getCompasICDHeader(tPrivate).isPresent() && getCompasICDHeader(tPrivate).get().getIEDName() != null)
                .collect(Collectors.toMap(tPrivate -> getCompasICDHeader(tPrivate).get().getIEDName(), Function.identity()));
    }

    private static Map<String, Pair<TPrivate, List<SCL>>> createMapICDSystemVersionUuidAndSTDFile(Set<SCL> stds) {
        Map<String, Pair<TPrivate, List<SCL>>> stringSCLMap = new HashMap<>();
        stds.forEach(std -> std.getIED().forEach(ied -> ied.getPrivate().forEach(tp -> {
            getCompasICDHeader(tp).map(TCompasICDHeader::getICDSystemVersionUUID).ifPresent(icdSysVer -> {
                Pair<TPrivate, List<SCL>> pair = stringSCLMap.get(icdSysVer);
                List<SCL> list = pair != null ? pair.getRight() : new ArrayList<>();
                list.add(std);
                stringSCLMap.put(icdSysVer, Pair.of(tp, list));
            });
        })));
        return stringSCLMap;
    }

    private static boolean comparePrivateCompasICDHeaders(TPrivate iedPrivate, TPrivate scdPrivate) throws ScdException {
        TCompasICDHeader iedCompasICDHeader = getCompasICDHeader(iedPrivate).orElseThrow(
                () -> new ScdException(CommonConstants.COMPAS_ICDHEADER + "not found in IED Private "));
        TCompasICDHeader scdCompasICDHeader = getCompasICDHeader(scdPrivate).orElseThrow(
                () -> new ScdException(CommonConstants.COMPAS_ICDHEADER + "not found in LNode Private "));
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
        TCompasICDHeader lNodeCompasICDHeader = getCompasICDHeader(lNodePrivate).orElseThrow(
                () -> new ScdException(CommonConstants.COMPAS_ICDHEADER + "not found in LNode Private "));
        stdPrivate.getContent().clear();
        stdPrivate.getContent().add(lNodeCompasICDHeader);

    }

    private static Optional<TCompasICDHeader> getCompasICDHeader(TPrivate tPrivate) {
        Optional<JAXBElement<TCompasICDHeader>> tCompasICDHeader = !tPrivate.getType().equals(CommonConstants.COMPAS_ICDHEADER) ? Optional.empty() :
                tPrivate.getContent().stream()
                        .filter(JAXBElement.class::isInstance)
                        .map(o -> (JAXBElement<TCompasICDHeader>) o)
                        .findFirst();
        return tCompasICDHeader.map(JAXBElement::getValue);
    }

}
