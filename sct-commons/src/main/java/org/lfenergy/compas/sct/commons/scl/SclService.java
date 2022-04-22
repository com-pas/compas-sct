// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SclService {

    public static final String UNKNOWN_LDEVICE_S_IN_IED_S = "Unknown LDevice (%s) in IED (%s)";
    public static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";

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
}
