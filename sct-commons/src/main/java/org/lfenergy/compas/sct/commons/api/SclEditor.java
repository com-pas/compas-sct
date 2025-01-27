// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.UUID;

/**
 * Service class that will be used to create, update or delete elements in {@link SCL <em>SCL</em>} XML Files.
 * <p> The following features are supported: </p>
 * <ul>
 *   <li>Initialization functions</li>
 *   <ol>
 *      <li>{@link SclEditor#initScl <em>Initialize the <b>SCL </b> object</em>}</li>
 *      <li>{@link SclEditor#addHistoryItem <em>Adds <b>History </b> object under <b>THeader </b> reference object</em>}</li>
 *      <li>{@link SclEditor#updateHeader <em>Update <b>Header </b> reference object</em>}</li>
 *   </ol>
 *   <li>IED features</li>
 *   <ol>
 *       <li>{@link SclEditor#addIED <em>Adds the <b>IED </b> object</em>}</li>
 *   </ol>
 *   <li>Communication features</li>
 *   <ol>
 *      <li>{@link SclEditor#addSubnetworks <em>Adds the <b>Subnetwork </b> elements under <b>TCommunication </b> reference object</em>}</li>
 *   </ol>
 *   <li>DAI features</li>
 *   <ol>
 *      <li>{@link SclEditor#updateDAI <em>Update the <b>TDAI </b> reference object for given <b>iedName</b>, <b>ldInst </b> and <b>DataAttributeRef </b> model</em>}</li>
 *   </ol>
 * </ul>
 * @see ExtRefEditor
 * @see SubstationEditor
 * @see HmiEditor
 */
public interface SclEditor {

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
     * Add or update SubNetworks in SCL file from ICD file and rename ConnectedAP/@iedName
     * @param scd         SCL file in which SubNetworks should be added
     * @param std         STD file from which SubNetworks functional data are copied from
     * @param stdIedName     Ied Name
     */
    void addSubnetworks(SCL scd, SCL std, String stdIedName) throws ScdException;

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
     * @throws ScdException     throws when inconsistency between Substation of SCL content and gien STD files as :
     *                          <ul>
     *                              <li>ICD_SYSTEM_VERSION_UUID in IED/Private of STD is not present in COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *                              <li>There are several STD files corresponding to ICD_SYSTEM_VERSION_UUID of COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *                              <li>There is no STD file found corresponding to COMPAS-ICDHeader in Substation/../LNode of SCL</li>
     *                              <li>COMPAS-ICDHeader is not the same in Substation/../LNode of SCL and in IED/Private of STD</li>
     *                              <li>COMPAS_ICDHEADER in Substation/../LNode of SCL not found in IED/Private of STD</li>
     *                          </ul>
     */
    void importSTDElementsInSCD(SCL scd, List<SCL> stds) throws ScdException;

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
