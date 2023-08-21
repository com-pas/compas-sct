// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.service;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A representation of the <em><b>{@link  ISclService SclService}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>Initialization functions</li>
 *   <ol>
 *      <li>{@link ISclService#initScl(UUID, String, String) <em>Initialize the <b>SCL </b> object</em>}</li>
 *      <li>{@link ISclService#addHistoryItem(SCL, String, String, String) <em>Adds <b>History </b> object under <b>THeader </b> reference object</em>}</li>
 *      <li>{@link ISclService#updateHeader(SCL, HeaderDTO) <em>Update <b>Header </b> reference object</em>}</li>
 *   </ol>
 *   <li>IED features</li>
 *   <ol>
 *       <li>{@link ISclService#addIED(SCL, String, SCL) <em>Adds the <b>IED </b> object</em>}</li>
 *   </ol>
 *   <li>Communication features</li>
 *   <ol>
 *      <li>{@link ISclService#getSubnetwork(SCL) <em>Returns list of <b>SubNetworkDTO </b></em>}</li>
 *      <li>{@link ISclService#addSubnetworks(SCL, List, SCL) <em>Adds the <b>Subnetwork </b> elements under <b>TCommunication </b> reference object</em>}</li>
 *   </ol>
 *   <li>ExtRef features</li>
 *   <ol>
 *      <li>{@link ISclService#getExtRefInfo <em>Returns list of <b>ExtRefInfo </b></em>}</li>
 *      <li>{@link ISclService#getExtRefBinders <em>Returns list of <b>ExtRefBindingInfo </b></em>}</li>
 *      <li>{@link ISclService#updateExtRefBinders(SCL, ExtRefInfo) <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefBindingInfo </b> model</em>}</li>
 *      <li>{@link ISclService#getExtRefSourceInfo <em>Returns list of <b>ExtRefSourceInfo </b></em>}</li>
 *      <li>{@link ISclService#updateExtRefSource(SCL, ExtRefInfo) <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefSourceInfo </b> model</em>}</li>
 *   </ol>
 *   <li>DAI features</li>
 *   <ol>
 *      <li>{@link ISclService#getDAI <em>Returns list of <b>DataAttributeRef </b></em>}</li>
 *      <li>{@link ISclService#updateDAI(SCL, String, String, DataAttributeRef)
 *      <em>Update the <b>TDAI </b> reference object for given <b>iedName</b>, <b>ldInst </b> and <b>DataAttributeRef </b> model</em>}</li>
 *   </ol>
 *   <li>EnumType features</li>
 *   <ol>
 *      <li>{@link  ISclService#getEnumTypeValues(SCL, String) <em>Returns Map <b>(ord, enumVal) </b> of <b>TEnumType </b> reference object</em>}</li>
 *   </ol>
 *
 * </ul>
 *
 * @see HeaderAdapter
 * @see SubstationAdapter
 * @see IEDAdapter
 * @see CommunicationAdapter
 * @see DataTypeTemplateAdapter
 */
public interface ISclService {

    String UNKNOWN_LDEVICE_IN_IED = "Unknown LDevice (%s) in IED (%s)";
    String INVALID_OR_MISSING_ATTRIBUTES_IN_EXT_REF_BINDING_INFO = "Invalid or missing attributes in ExtRef binding info";

    /**
     * Initialise SCD file with Header and Private SCLFileType
     *
     * @param hId       SCL Header ID
     * @param hVersion  SCL Header Version
     * @param hRevision SCL Header Revision
     * @return <em>SCL</em> SCD object
     * @throws ScdException throws when inconsistance in SCL file
     */
    SCL initScl(final UUID hId, final String hVersion, final String hRevision) throws ScdException;
    /**
     * Adds new HistoryItem in SCL file
     *
     * @param scd  SCL file in which new History should be added
     * @param who  Who realize the action
     * @param what What kind of action is realized
     * @param why  Why this action is done
     */
    void addHistoryItem(SCL scd, String who, String what, String why);

    /**
     * Updates Header of SCL file
     *
     * @param scd       SCL file in which Header should be updated
     * @param headerDTO Header new values
     */
    void updateHeader(@NonNull SCL scd, @NonNull HeaderDTO headerDTO);

    /**
     * Adds IED in SCL file (Related DataTypeTemplate of SCL is updated also)
     *
     * @param scd     SCL file in which IED should be added
     * @param iedName name of IED to add in SCL
     * @param icd     ICD containing IED to add and related DataTypeTemplate
     * @throws ScdException throws when inconsistency between IED to add and SCL file content
     */
    void addIED(SCL scd, String iedName, SCL icd) throws ScdException;

    /**
     * Adds new SubNetworks in SCL file from ICD file
     *
     * @param scd         SCL file in which SubNetworks should be added
     * @param subNetworks List of SubNetworks DTO contenting SubNetwork and ConnectedAp parameter names
     * @param icd         ICD file from which SubNetworks functional data are copied from
     * @throws ScdException throws when no Communication in SCL and <em>createIfNotExists == false</em>
     */
    void addSubnetworks(SCL scd, List<SubNetworkDTO> subNetworks, SCL icd) throws ScdException;

    /**
     * Gets list of SCL SubNetworks
     *
     * @param scd SCL file in which SubNetworks should be found
     * @return List of <em>SubNetworkDTO</em> from SCL
     * @throws ScdException throws when no Communication in SCL and <em>createIfNotExists == false</em>
     */
    List<SubNetworkDTO> getSubnetwork(SCL scd) throws ScdException;

    /**
     * Gets all ExtRef from specific IED/LDevice in SCL file
     *
     * @param scd     SCL file in which ExtRefs should be found
     * @param iedName name of IED in which LDevice is localized
     * @param ldInst  LdInst of LDevice in which all ExtRefs should be found
     * @return list of <em>ExtRefInfo</em> from specified parameter SCL/IED/LDevice
     * @throws ScdException throws when unknown specified IED or LDevice
     */
    List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException;

    /**
     * Create LDevice
     *
     * @param scd     SCL file in which LDevice should be found
     * @param iedName name of IED in which LDevice is localized
     * @param ldInst  LdInst of LDevice for which adapter is created
     * @return created LDevice adapter
     */
    default LDeviceAdapter createLDeviceAdapter(SCL scd, String iedName, String ldInst) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        return iedAdapter.findLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(() -> new ScdException(String.format(UNKNOWN_LDEVICE_IN_IED, ldInst, iedName)));
    }

    /**
     * Gets all possible ExtRefs to bind in SCL file with given ExtRef (<em>signalInfo</em>) in SCL file
     *
     * @param scd        SCL file in which ExtRefs should be found
     * @param iedName    name of IED in which LDevice is localized
     * @param ldInst     ldInst of LDevice in which LN is localized
     * @param lnClass    lnClass of LN in which ExtRef signal to find binders is localized
     * @param lnInst     lnInst of LN in which ExtRef signal to find binders is localized
     * @param prefix     prefix of LN in which ExtRef signal to find binders is localized
     * @param signalInfo ExtRef signal for which we should find possible binders in SCL file binders
     * @return list of <em>ExtRefBindingInfo</em> object (containing binding data for each LNode in current SCL file) sorted by {@link ExtRefBindingInfo#compareTo(ExtRefBindingInfo) compareTo} method.
     * @throws ScdException throws when ExtRef contains inconsistency data
     */
    List<ExtRefBindingInfo> getExtRefBinders(SCL scd, String iedName, String ldInst, String lnClass, String lnInst, String prefix, ExtRefSignalInfo signalInfo) throws ScdException;

    /**
     * Updates ExtRef binding data related to given ExtRef (<em>extRefInfo</em>) in given SCL file
     *
     * @param scd        SCL file in which ExtRef to update should be found
     * @param extRefInfo ExtRef signal for which we should find possible binders in SCL file
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    void updateExtRefBinders(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

    /**
     * Gets all Control Blocks related to <em>extRefInfo</em> in given SCL file
     *
     * @param scd        SCL file in which ControlBlocks should be found
     * @param extRefInfo ExtRef signal for which we should find related ControlBlocks
     * @return list of <em>ControlBlock</em> object as ControlBlocks of LNode specified in <em>extRefInfo</em>
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    List<ControlBlock> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

    /**
     * Updates ExtRef source binding data's based on given data in <em>extRefInfo</em>
     *
     * @param scd        SCL file in which ExtRef source data's to update should be found
     * @param extRefInfo new data for ExtRef source binding data
     * @return <em>TExtRef</em> object as update ExtRef with new source binding data
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    TExtRef updateExtRefSource(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

    /**
     * Gets a list of summarized DataTypeTemplate for DataAttribute DA (updatable or not) related to the one given
     * in <em>dataAttributeRef</em>
     *
     * @param scd       SCL file in which DataTypeTemplate of DAIs should be found
     * @param iedName   name of IED in which DAs are localized
     * @param ldInst    ldInst of LDevice in which DAIs are localized
     * @param dataAttributeRef      reference summarized DataTypeTemplate related to IED DAIs
     * @param updatable true to retrieve DataTypeTemplate's related to only updatable DAIs, false to retrieve all
     * @return Set of Data Attribute Reference for DataAttribute (updatable or not)
     * @throws ScdException SCD illegal arguments exception, missing mandatory data
     */
    Set<DataAttributeRef> getDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef, boolean updatable) throws ScdException;

    /**
     * Updates DAI based on given data in <em>dataAttributeRef</em>
     *
     * @param scd     SCL file in which DataTypeTemplate of DAI should be found
     * @param iedName name of IED in which DAI is localized
     * @param ldInst  ldInst of LDevice in which DAI is localized
     * @param dataAttributeRef    reference summarized DataTypeTemplate related to DAI to update
     * @throws ScdException when inconsistency are found in th SCL's
     *                      DataTypeTemplate. Which should normally not happens.
     */
    void updateDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef) throws ScdException;

    /**
     * Gets EnumTypes values of ID <em>idEnum</em> from DataTypeTemplate of SCL file
     *
     * @param scd       SCL file in which EnumType should be found
     * @param idEnum    ID of EnumType for which values are retrieved
     * @return list of couple EnumType value and it's order
     * @throws ScdException throws when unknown EnumType
     */
    Set<EnumValDTO> getEnumTypeValues(SCL scd, String idEnum) throws ScdException;

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
     *
     * @param scd               SCL object in which content of STD files are imported
     * @param stds              list of STD files contenting datas to import into SCD
     * @param subNetworkTypes   couple of Subnetwork name and possible corresponding ConnectedAP names
     * @throws ScdException     throws when inconsistency between Substation of SCL content and gien STD files as :
     *                          <ul>
     *                              <li>ICD_SYSTEM_VERSION_UUID in IED/Private of STD is not present in COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *                              <li>There are several STD files corresponding to ICD_SYSTEM_VERSION_UUID of COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *                              <li>There is no STD file found corresponding to COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *                              <li>COMPAS-ICDHeader is not the same in Substation/../LNode of SCL and in IED/Private of STD</li>
     *                              <li>COMPAS_ICDHEADER in Substation/../LNode of SCL not found in IED/Private of STD</li>
     *                          </ul>
     */
    void importSTDElementsInSCD(SCL scd, List<SCL> stds, List<SubNetworkTypeDTO> subNetworkTypes) throws ScdException;

    /**
     * Removes all ControlBlocks and DataSets for all LNs in SCL
     *
     * @param scl SCL file for which ControlBlocks and DataSets should be deleted
     */
    void removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(final SCL scl);

    /**
     * Activate used LDevice and Deactivate unused LDevice in {@link TLNode <em><b>TLNode </b></em>}
     *
     * @param scd SCL file for which LDevice should be activated or deactivated
     * @return list of encountered errors
     */
    List<SclReportItem> updateLDeviceStatus(SCL scd);

    /**
     * Checks Control Blocks, DataSets and FCDA number limitation into Access Points
     *
     * @param scd SCL file for which LDevice should be activated or deactivated
     * @return list of encountered errors
     */
    List<SclReportItem> analyzeDataGroups(SCL scd);

    /**
     * Update DAIs of DO InRef in all LN0 of the SCD using matching ExtRef information.
     *
     * @param scd SCL file for which DOs InRef should be updated with matching ExtRef information
     * @return list of encountered errors
     */
    List<SclReportItem> updateDoInRef(SCL scd);

    /**
     * Update and/or create Monitoring LNs (LSVS and LGOS) for bound GOOSE and SMV Control Blocks
     *
     * @param scd SCL file for which  LNs (LSVS and LGOS) should be updated and/or created in each LDevice LDSUIED with matching ExtRef information
     * @return list of encountered errors
     */
    List<SclReportItem> manageMonitoringLns(SCL scd);

}
