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
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_ICDHEADER;

/**
 * A representation of the <em><b>{@link SclService SclService}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>Initialization functions</li>
 *   <ol>
 *      <li>{@link SclService#initScl(Optional, String, String) <em>Initialize the <b>SCL </b> object</em>}</li>
 *      <li>{@link SclService#addHistoryItem(SCL, String, String, String) <em>Adds <b>History </b> object under <b>THeader </b> reference object</em>}</li>
 *      <li>{@link SclService#updateHeader(SCL, HeaderDTO) <em>Update <b>Header </b> reference object</em>}</li>
 *   </ol>
 *   <li>IED features</li>
 *   <ol>
 *       <li>{@link SclService#addIED(SCL, String, SCL) <em>Adds the <b>IED </b> object</em>}</li>
 *   </ol>
 *   <li>Communication features</li>
 *   <ol>
 *      <li>{@link SclService#getSubnetwork(SCL) <em>Returns list of <b>SubNetworkDTO </b></em>}</li>
 *      <li>{@link SclService#addSubnetworks(SCL, Set, Optional) <em>Adds the <b>Subnetwork </b> elements under <b>TCommunication </b> reference object</em>}</li>
 *   </ol>
 *   <li>ExtRef features</li>
 *   <ol>
 *      <li>{@link SclService#getExtRefInfo <em>Returns list of <b>ExtRefInfo </b></em>}</li>
 *      <li>{@link SclService#getExtRefBinders <em>Returns list of <b>ExtRefBindingInfo </b></em>}</li>
 *      <li>{@link SclService#updateExtRefBinders(SCL, ExtRefInfo) <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefBindingInfo </b> model</em>}</li>
 *      <li>{@link SclService#getExtRefSourceInfo <em>Returns list of <b>ExtRefSourceInfo </b></em>}</li>
 *      <li>{@link SclService#updateExtRefSource(SCL, ExtRefInfo) <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefSourceInfo </b> model</em>}</li>
 *   </ol>
 *   <li>DAI features</li>
 *   <ol>
 *      <li>{@link SclService#getDAI <em>Returns list of <b>ResumedDataTemplate </b></em>}</li>
 *      <li>{@link SclService#updateDAI(SCL, String, String, ResumedDataTemplate)
 *      <em>Update the <b>TDAI </b> reference object for given <b>iedName</b>, <b>ldInst </b> and <b>ResumedDataTemplate </b> model</em>}</li>
 *   </ol>
 *   <li>EnumType features</li>
 *   <ol>
 *      <li>{@link SclService#getEnumTypeElements(SCL, String) <em>Returns Map <b>(ord, enumVal) </b> of <b>TEnumType </b> reference object</em>}</li>
 *   </ol>
 *
 * </ul>
 * @see org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter
 * @see org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter
 * @see org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter
 * @see org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter
 */
@Slf4j
public class SclService {

    public static final String UNKNOWN_LDEVICE_S_IN_IED_S = "Unknown LDevice (%s) in IED (%s)";
    public static final String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";
    public static final ObjectFactory objectFactory = new ObjectFactory();

    private SclService() {
        throw new IllegalStateException("SclService class");
    }

    /**
     * Initialise SCD file with Header and Private SCLFileType
     * @param hId optional SCL Header ID, if empty random UUID will be created
     * @param hVersion SCL Header Version
     * @param hRevision SCL Header Revision
     * @return <em>SclRootAdapter</em> object as SCD file
     * @throws ScdException throws when inconsistenc in SCL file
     */
    public static SclRootAdapter initScl(Optional<UUID> hId, String hVersion, String hRevision) throws ScdException {
        UUID headerId = hId.orElseGet(UUID::randomUUID);
        SclRootAdapter scdAdapter = new SclRootAdapter(headerId.toString(), hVersion, hRevision);
        scdAdapter.addPrivate(PrivateService.createPrivate(TCompasSclFileType.SCD));
        return scdAdapter;
    }

    /**
     * Adds new HistoryItem in SCL file
     * @param scd SCL file in which new History should be added
     * @param who Who realize the action
     * @param what What kind of action is realized
     * @param why Why this action is done
     * @return <em>SclRootAdapter</em> object as SCD file
     */
    public static SclRootAdapter addHistoryItem(SCL scd, String who, String what, String why) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        headerAdapter.addHistoryItem(who, what, why);
        return sclRootAdapter;
    }

    /**
     * Updates Header of SCL file
     * @param scd SCL file in which Header should be updated
     * @param headerDTO Header new values
     * @return <em>SclRootAdapter</em> object as SCD file
     */
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

    /**
     * Adds IED in SCL file (Related DataTypeTemplate of SCL is updated also)
     * @param scd SCL file in which IED should be added
     * @param iedName name of IED to add in SCL
     * @param icd ICD containing IED to add and related DataTypeTemplate
     * @return <em>IEDAdapter</em> as added IED
     * @throws ScdException throws when inconsistency between IED to add and SCL file content
     */
    public static IEDAdapter addIED(SCL scd, String iedName, SCL icd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return sclRootAdapter.addIED(icd, iedName);
    }

    /**
     * Adds new SubNetworks in SCL file from ICD file
     * @param scd SCL file in which SubNetworks should be added
     * @param subNetworks list of SubNetworks DTO contenting SubNetwork and ConnectedAp parameter names
     * @param icd ICD file from which SubNetworks functional data are copied from
     * @return optional of <em>CommunicationAdapter</em> object as Communication node of SCL file.
     * Calling getParentAdapter() will give SCL file
     * @throws ScdException throws when no Communication in SCL and <em>createIfNotExists == false</em>
     */
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

    /**
     * Gets list of SCL SubNetworks
     * @param scd SCL file in which SubNetworks should be found
     * @return List of <em>SubNetworkDTO</em> from SCL
     * @throws ScdException throws when no Communication in SCL and <em>createIfNotExists == false</em>
     */
    public static List<SubNetworkDTO> getSubnetwork(SCL scd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(false);
        return communicationAdapter.getSubNetworkAdapters()
                .stream()
                .map(SubNetworkDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Gets all ExtRef from specific IED/LDevice in SCL file
     * @param scd SCL file in which ExtRefs should be found
     * @param iedName name of IED in which LDevice is localized
     * @param ldInst LdInst of LDevice in which all ExtRefs should be found
     * @return list of <em>ExtRefInfo</em> from specified parameter SCL/IED/LDevice
     * @throws ScdException throws when unknown specified IED or LDevice
     */
    public static List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException {

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(
                        () -> new ScdException(String.format(UNKNOWN_LDEVICE_S_IN_IED_S, ldInst, iedName))
                );
        return lDeviceAdapter.getExtRefInfo();
    }

    /**
     * Gets all possible ExtRefs to bind in SCL file with given ExtRef (<em>signalInfo</em>) in SCL file
     * @param scd SCL file in which ExtRefs should be found
     * @param iedName name of IED in which LDevice is localized
     * @param ldInst ldInst of LDevice in which LN is localized
     * @param lnClass lnClass of LN in which ExtRef signal to find binders is localized
     * @param lnInst lnInst of LN in which ExtRef signal to find binders is localized
     * @param prefix prefix of LN in which ExtRef signal to find binders is localized
     * @param signalInfo ExtRef signal for which we should find possible binders in SCL file binders
     * @return  list of <em>ExtRefBindingInfo</em> object (containing binding data for each LNode in current SCL file)
     * @throws ScdException throws when ExtRef contains inconsistency data
     */
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

    /**
     * Updates ExtRef binding data related to given ExtRef (<em>extRefInfo</em>) in given SCL file
     * @param scd SCL file in which ExtRef to update should be found
     * @param extRefInfo ExtRef signal for which we should find possible binders in SCL file
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
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

    /**
     * Gets all Control Blocks related to <em>extRefInfo</em> in given SCL file
     * @param scd SCL file in which ControlBlocks should be found
     * @param extRefInfo ExtRef signal for which we should find related ControlBlocks
     * @return list of <em>ControlBlock</em> object as ControlBlocks of LNode specified in <em>extRefInfo</em>
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
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

    /**
     * Updates ExtRef source binding data's based on given data in <em>extRefInfo</em>
     * @param scd SCL file in which ExtRef source data's to update should be found
     * @param extRefInfo new data for ExtRef source binding data
     * @return <em>TExtRef</em> object as update ExtRef with new source binding data
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
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

    /**
     * Gets a list of summarized DataTypeTemplate for DataAttribute DA (updatable or not) related to the one given
     * in <em>rDtt</em>
     * @param scd SCL file in which DataTypeTemplate of DAIs should be found
     * @param iedName name of IED in which DAs are localized
     * @param ldInst ldInst of LDevice in which DAIs are localized
     * @param rDtt reference summarized DataTypeTemplate related to IED DAIs
     * @param updatable true to retrieve DataTypeTemplate's related to only updatable DAIs, false to retrieve all
     * @return List of resumed DataTypeTemplate for DataAttribute (updatable or not)
     * @throws ScdException SCD illegal arguments exception, missing mandatory data
     */
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

    /**
     * Updates DAI based on given data in <em>rDtt</em>
     * @param scd SCL file in which DataTypeTemplate of DAI should be found
     * @param iedName name of IED in which DAI is localized
     * @param ldInst ldInst of LDevice in which DAI is localized
     * @param rDtt reference summarized DataTypeTemplate related to DAI to update
     * @throws ScdException when inconsistency are found in th SCL's
     *                     DataTypeTemplate. Which should normally not happens.
     */
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

    /**
     * Gets EnumTypes values of ID <em>idEnum</em> from DataTypeTemplate of SCL file
     * @param scd SCL file in which EnumType should be found
     * @param idEnum ID of EnumType for which values are retrieved
     * @return list of couple EnumType value and it's order
     * @throws ScdException throws when unkonown EnumType
     */
    public static Set<Pair<Integer, String>> getEnumTypeElements(SCL scd, String idEnum) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        EnumTypeAdapter enumTypeAdapter = dataTypeTemplateAdapter.getEnumTypeAdapterById(idEnum)
                .orElseThrow(() -> new ScdException("Unknown EnumType Id: " + idEnum));
        return enumTypeAdapter.getCurrentElem().getEnumVal().stream()
                .map(tEnumVal -> Pair.of(tEnumVal.getOrd(), tEnumVal.getValue()))
                .collect(Collectors.toSet());
    }

    /**
     * Imports IEDs, DataTypeTemplates and Communication nodes of STD files into SCL (SCD) file
     * <em><b>STD</b></em> : System Template Definition
     * To import STD into SCD, this step are realized
     * <ul>
     *     <li>Check SCD and STD compatibilities by checking if there is at least one ICD_SYSTEM_VERSION_UUID in
     *     LNode/Private CompasICDHeader of SCL/Substation/.. not present in IED/Private CompasICDHeader of STD  </li>
     *     <li>List all LNode/Private COMPAS-ICDHeader of SCL/Substation/.. and remove duplicated one with same iedName in order to
     *     ovoid repetition in actions </li>
     *     <li>For each Private.ICDSystemVersionUUID and Private.iedName in Substation/ of SCL find corresponding STD File</li>
     *     <li>import /IED /DataTypeTemplate  from that STD file and update IEDName of /IED in SCD file</li>
     *     <li>import connectedAP and rename ConnectedAP/@iedName in Communication node in SCD file</li>
     * </ul>
     * @param scdRootAdapter adapter object related to SCL file in which content of STD files are imported
     * @param stds list of STD files contenting datas to import into SCD
     * @param comMap couple of Subnetwork name and possible corresponding ConnectAP names
     * @return updated SCD file as <em>SclRootAdapter</em>
     * @throws ScdException throws when inconsistency between Substation of SCL content and gien STD files as :
     *      <ul>
     *           <li>ICD_SYSTEM_VERSION_UUID in IED/Private of STD is not present in COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *           <li>There are several STD files corresponding to ICD_SYSTEM_VERSION_UUID of COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *           <li>There is no STD file found corresponding to COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *           <li>COMPAS-ICDHeader is not the same in Substation/../LNode of SCL and in IED/Private of STD</li>
     *           <li>COMPAS_ICDHEADER in Substation/../LNode of SCL not found in IED/Private of STD</li>
     *      </ul>
     */
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

    /**
     * Checks SCD and STD compatibilities by checking if there is at least one ICD_SYSTEM_VERSION_UUID in
     *      Substation/../LNode/Private COMPAS-ICDHeader of SCL not present in IED/Private COMPAS-ICDHeader of STD
     * @param mapICDSystemVersionUuidAndSTDFile map of ICD_SYSTEM_VERSION_UUID and list of corresponding STD
     * @throws ScdException throws when there are several STD files corresponding to <em>ICD_SYSTEM_VERSION_UUID</em>
     * from Substation/../LNode/Private COMPAS-ICDHeader of SCL
     */
    private static void checkSTDCorrespondanceWithLNodeCompasICDHeader(Map<String, Pair<TPrivate, List<SCL>>> mapICDSystemVersionUuidAndSTDFile) throws ScdException {
        for (Pair<TPrivate, List<SCL>> pairOfPrivateAndSTDs : mapICDSystemVersionUuidAndSTDFile.values()) {
            if (pairOfPrivateAndSTDs.getRight().size() != 1) {
                TPrivate key = pairOfPrivateAndSTDs.getLeft();
                throw new ScdException("There are several STD files corresponding to " + stdCheckFormatExceptionMessage(key));
            }
        }
    }

    /**
     * Creates formatted message including data's of Private for Exception
     * @param key Private causing exception
     * @return formatted message
     * @throws ScdException throws when parameter not present in Private
     */
    private static String stdCheckFormatExceptionMessage(TPrivate key) throws ScdException {
        Optional<TCompasICDHeader> optionalCompasICDHeader = PrivateService.getCompasICDHeader(key);
        return  HEADER_ID + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderId).orElse(null) +
                HEADER_VERSION + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderVersion).orElse(null) +
                HEADER_REVISION + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderRevision).orElse(null) +
                "and " + ICD_SYSTEM_VERSION_UUID + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getICDSystemVersionUUID).orElse(null);
    }

    /**
     * Creates map of IEDName and related Private for all Privates COMPAS-ICDHeader in /Substation of SCL
     * @param scdRootAdapter SCL file in which Private should be found
     * @return map of Private and its IEDName parameter
     */
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

    /**
     * Sorts in map of ICD_SYSTEM_VERSION_UUID and related Private coupled with all corresponding STD for all given STD
     * @param stds list of STD to short
     * @return map of ICD_SYSTEM_VERSION_UUID attribute in IED/Private:COMPAS-ICDHeader and related Private coupled with
     * all corresponding STD
     */
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

    /**
     * Compares if two Private:COMPAS-ICDHeader have all attributes equal except IEDNane, BayLabel and IEDinstance
     * @param iedPrivate Private of IED from STD to compare
     * @param scdPrivate Private of LNode fro SCD to compare
     * @return <em>Boolean</em> value of check result
     * @throws ScdException throws when Private is not COMPAS_ICDHEADER one
     */
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

    /**
     * Copy Private COMPAS_ICDHEADER from LNode of SCD into Private COMPAS_ICDHEADER from IED of STD
     * @param stdPrivate Private of IED from STD in which to copy new data
     * @param lNodePrivate Private of IED from STD from which new data are taken
     * @throws ScdException throws when Private is not COMPAS_ICDHEADER one
     */
    private static void copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(TPrivate stdPrivate, TPrivate lNodePrivate) throws ScdException {
        TCompasICDHeader lNodeCompasICDHeader = PrivateService.getCompasICDHeader(lNodePrivate).orElseThrow(
                () -> new ScdException(COMPAS_ICDHEADER + " not found in LNode Private "));
        stdPrivate.getContent().clear();
        stdPrivate.getContent().add(objectFactory.createICDHeader(lNodeCompasICDHeader));
    }

    /**
     * Removes all ControlBlocks and DataSets for all LNs in SCL
     * @param scl SCL file for which ControlBlocks and DataSets should be deleted
     */
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

     /**
     * Activate used LDevice and Deactivate unused LDevice in {@link TLNode <em><b>TLNode </b></em>}
     * @param scd SCL file for which LDevice should be activated or deactivated
     * @return SclReport Object that contain SCL file and set of errors
     */
    public static SclReport updateLDeviceStatus(SCL scd) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        SubstationAdapter substationAdapter = sclRootAdapter.getSubstationAdapter();
        final List<Pair<String, String>> iedNameLdInstList = substationAdapter.getIedAndLDeviceNamesForLN0FromLNode();
        List<SclReport.ErrorDescription> errors = sclRootAdapter.getIEDAdapters().stream()
                .map(IEDAdapter::getLDeviceAdapters)
                .flatMap(Collection::stream)
                .map(LDeviceAdapter::getLN0Adapter)
                .map(ln0Adapter -> ln0Adapter.checkAndUpdateLDeviceStatus(iedNameLdInstList))
                .reduce(new ArrayList<>(),(sclReportErrors, partialSclReportErrors) -> {
                    sclReportErrors.addAll(partialSclReportErrors);
                    return sclReportErrors;
                });
        SclReport sclReport = new SclReport();
        sclReport.getErrorDescriptionList().addAll(errors);
        sclReport.setSclRootAdapter(sclRootAdapter);
        return sclReport;
     }
}
