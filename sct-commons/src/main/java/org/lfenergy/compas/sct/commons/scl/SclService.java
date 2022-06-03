// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl.extensions.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.*;
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
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SclService {

    public static final String UNKNOWN_LDEVICE_S_IN_IED_S = "Unknown LDevice (%s) in IED (%s)";
    public static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";
    public static final String COMPAS_ICDHEADER = "COMPAS-ICDHeader";
    public static final String ICD_SYSTEM_VERSION_UUID = "ICDSystemVersionUUID";

    private SclService() {
        throw new IllegalStateException("SclService class");
    }

    public static SclRootAdapter initScl(Optional<UUID> hId, String hVersion, String hRevision) throws ScdException {
        UUID headerId = hId.orElseGet(UUID::randomUUID);
        return new SclRootAdapter(headerId.toString(), hVersion, hRevision);
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

    public static SclRootAdapter importIEDInSCD(@NonNull SclRootAdapter scdRootAdapter, List<SCL> stds) throws ScdException {
        // List all Private /Substation/VoltageLevel/Bay/Function/LNode/Private/compas:ICDHeader
        //Remove duplicated one with same iedName

        Map<String, TPrivate> tPrivateMap = new HashMap<>();

        scdRootAdapter.getCurrentElem().getSubstation().get(0).getVoltageLevel()
                .forEach(tVoltageLevel -> tVoltageLevel.getBay()
                       .forEach(tBay -> tBay.getFunction()
                             .forEach(tFunction -> tFunction.getLNode()
                                     .forEach(tlNode -> tlNode.getPrivate()
                                             .forEach(tPrivate -> {
                                                 if (tPrivate.getType().equals(COMPAS_ICDHEADER)) {
                                                     tPrivate.getContent().stream()
                                                             .filter(o -> !o.toString().trim().isBlank())
                                                             .forEach(o -> tPrivateMap.put((((Element) o).getAttribute("IEDName")), tPrivate));
                                                 }
                                             })
                                     )
                             )
                       )
                );

        //For each Private.ICDSystemVersionUUID and Private.iedName find STD File
        for (Map.Entry<String, TPrivate> entry : tPrivateMap.entrySet()) {
            String iedName = entry.getKey();
            TPrivate tPrivate = entry.getValue();
//if =! 1 error
            List<SCL> matchedStds = findStds(tPrivate, stds);
            if (matchedStds.size() != 1)
                throw new ScdException("There is no STD file found or there are several STD files corresponding to " +
                        "HeaderId = " + getValueFromPrivate(tPrivate, "headerId") +
                        " HeaderVersion = " + getValueFromPrivate(tPrivate, "headerVersion") +
                        " HeaderRevision = " + getValueFromPrivate(tPrivate, "headerRevision") +
                        "and ICDSystemVersionUUID = " + getValueFromPrivate(tPrivate, ICD_SYSTEM_VERSION_UUID));
            else {
                //else import /dtt in Scd
                // import /ied and give /ied.name =  Private.iedName
                SCL std = matchedStds.get(0);
                SclRootAdapter stdRootAdapter = new SclRootAdapter(stds.get(0));
                scdRootAdapter.getCurrentElem().setDataTypeTemplates(std.getDataTypeTemplates());

                //if /IED/Private/compas:ICDHeader == /Substation/VoltageLevel/Bay/Function/LNode/Private/compas:ICDHeader (~3param)
                IEDAdapter stdIedAdapter = new IEDAdapter(stdRootAdapter, std.getIED().get(0));
                /*stdIedAdapter.getCurrentElem().getPrivate()
                        .forEach(tp -> {
                            if (tp.getType().equals(COMPAS_ICDHEADER)) {
                                //copy 3 param into /IED/Private/compas:ICDHeader ( @BayLabel @iedName @IEDinstance)
                                try {
                                    checkSTDPrivateAndLNodePrivate(tp, tPrivate);
                                } catch (ScdException e) {
                                    e.getMessage();
                                }
                            }
                        });*/

                stdIedAdapter.getCurrentElem().getPrivate().stream()
                        .filter(tp -> tp.getType().equals(COMPAS_ICDHEADER))
                        .map(tpTemp ->  checkSTDPrivateAndLNodePrivate(tpTemp, tPrivate))
                        .findFirst()
                        .orElseThrow(() -> new ScdException("COMPAS-ICDHeader is not the same in Substation and in IED"));


                stdIedAdapter.setIEDName(iedName);
                scdRootAdapter.getCurrentElem().getIED().add(stdIedAdapter.getCurrentElem());

                //import connectedAP (correspondance from file)
                //and rename Communication/Subnetwork/ConnectedAP/@iedName by /Substation/VoltageLevel/Bay/Function/LNode/Private/compas:ICDHeader @iedName
                try {
                    addSubnetworks(scdRootAdapter.getCurrentElem(), createDefaultSubnetworkIntoSCD(iedName));//, std);
                } catch (ScdException e) {
                    e.printStackTrace();
                }
                stds.remove(std);
            }
        }
        return scdRootAdapter;
    }

    private static Set<SubNetworkDTO> createDefaultSubnetworkIntoSCD(String iedName){
        final Map<Pair<String, String>, List<String>> comMap = Map.of(
                Pair.of("RSPACE_PROCESS_NETWORK", "8-MMS"), Arrays.asList("PROCESS_AP", "TOTO_AP_GE"),
                Pair.of("RSPACE_ADMIN_NETWORK","IP"), Arrays.asList("ADMINISTRATION_AP","TATA_AP_EFFACEC"));
        Set<SubNetworkDTO> subNetworkDTOS = new HashSet<>();
        comMap.forEach((subnetworkNameType, apNames) -> {
            SubNetworkDTO subNetworkDTO = new SubNetworkDTO(subnetworkNameType.getLeft(), subnetworkNameType.getRight());
            apNames.forEach(s -> {
                ConnectedApDTO connectedApDTO = new ConnectedApDTO();
                connectedApDTO.setApName(s);
                connectedApDTO.setIedName(iedName);
                subNetworkDTO.getConnectedAPs().add(connectedApDTO);
            });
        });
        return subNetworkDTOS;
    }

    private static List<SCL> findStds(TPrivate tPrivate, List<SCL> stds){
        List<SCL> existingStds = new ArrayList<>();
        String icdSystemVersionUuid = getValueFromPrivate(tPrivate, ICD_SYSTEM_VERSION_UUID);

        stds.forEach(std -> std.getIED().forEach(ied -> ied.getPrivate().forEach(tp -> {
            if (tp.getType().equals(COMPAS_ICDHEADER)) {
                tp.getContent().stream()
                        .filter(o -> !o.toString().trim().isBlank())
                        .forEach(o -> {
                            if(((Element) o).getAttribute(ICD_SYSTEM_VERSION_UUID).equals(icdSystemVersionUuid)){
                                     existingStds.add(std);
                            }
                        });
            }
        })));
        return existingStds;
    }

    private static String getValueFromPrivate(TPrivate tPrivate, String attributName) {
        Element element = (Element) tPrivate.getContent().stream()
                .filter(o -> !o.toString().trim().isBlank())
                .findFirst()
                .get();
        return element.getAttribute(attributName);
    }

    private static boolean checkSTDPrivateAndLNodePrivate(TPrivate iedPrivate, TPrivate scdPrivate) {
        List<String> attributNames = Arrays.asList("IEDType", "ICDSystemVersionUUID", "VendorName", "IEDredundancy",
                "IEDmodel", "hwRev", "swRev", "headerId", "headerVersion", "headerRevision");

        Element iedElement = (Element) iedPrivate.getContent().get(1);
        Element scdElement = (Element) scdPrivate.getContent().get(1);
        boolean isEq = attributNames.stream().map(s -> iedElement.getAttribute(s).equals(scdElement.getAttribute(s))).reduce(true, (a, b) -> a && b);
        if(isEq){
            iedPrivate.getContent().clear();
            iedPrivate.getContent().add(scdElement);
            return true;
        } else return false;
    }

    private static void checkSTDPrivateAndLNodePrivate_(TPrivate iedPrivate, TPrivate scdPrivate) throws ScdException {
        TCompasICDHeader compasICDHeader = getCompasICDHeader(iedPrivate);
        TCompasICDHeader scdCompasICDHeader = getCompasICDHeader(scdPrivate);
        String bayLabel = scdCompasICDHeader.getBayLabel();
        scdCompasICDHeader.setBayLabel(null);
        String iedName = scdCompasICDHeader.getIEDName();
        scdCompasICDHeader.setIEDName(null);
        String iedInstance = scdCompasICDHeader.getIEDinstance();
        scdCompasICDHeader.setIEDinstance(null);

        if(compasICDHeader.equals(scdCompasICDHeader)){
            compasICDHeader.setBayLabel(bayLabel);
            compasICDHeader.setIEDinstance(iedInstance);
            compasICDHeader.setIEDName(iedName);
            iedPrivate.getContent().clear();
            iedPrivate.getContent().add(compasICDHeader);
        } else throw new ScdException("COMPAS-ICDHeader is not the same in Substation and in IED");
    }

    private static TCompasICDHeader getCompasICDHeader(TPrivate tPrivate) throws ScdException {
        Optional<Object> headerObject = tPrivate.getContent().stream()
                .filter(o -> !o.toString().trim().isBlank())
                .findFirst();
            if (headerObject.isPresent()) {
                JAXBElement<TCompasICDHeader> header = (JAXBElement) headerObject.get();
                return header.getValue();
            } else {
                throw new ScdException("Empty COMPAS ICD HEADER Private type : " + COMPAS_ICDHEADER);
            }
    }
}
