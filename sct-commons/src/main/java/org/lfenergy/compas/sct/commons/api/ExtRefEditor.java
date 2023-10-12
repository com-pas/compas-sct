// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.ILDEPFSettings;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Set;

/**
 * Service class that will be used to create, update or delete elements related to the {@link TExtRef <em>TExtRef</em>} object.
 * <p> The following features are supported: </p>
 * <ul>
 *   <li>ExtRef features</li>
 *   <ol>
 *      <li>{@link ExtRefEditor#updateExtRefBinders <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefBindingInfo </b> model</em>}</li>
 *      <li>{@link ExtRefEditor#updateExtRefSource <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefSourceInfo </b> model</em>}</li>
 *      <li>{@link ExtRefEditor#updateAllExtRefIedNames <em>Update the iedName attribute in all <b>TExtRef</b></em>}</li>
 *      <li>{@link ExtRefEditor#createDataSetAndControlBlocks(SCL) <em>Create DataSet and ControlBlock based on the <b>TExtRef</b></em>}</li>
 *      <li>{@link ExtRefEditor#createDataSetAndControlBlocks(SCL, String) <em>Create DataSet and ControlBlock based on the <b>TExtRef</b> in given <b>IED</b></em>}</li>
 *      <li>{@link ExtRefEditor#createDataSetAndControlBlocks(SCL, String, String) <em>Create DataSet and ControlBlock based on the <b>TExtRef</b> in given <b>IED</b> and <b>LDevice</b></em>}</li>
 *      <li>{@link ExtRefEditor#configureNetworkForAllControlBlocks <em>Configure the network for the <b>ControlBlocks</b></em>}</li>
 *      <li>{@link ExtRefEditor#manageBindingForLDEPF <em>Manage <b>TExtRef</b> Binding For LDevice (inst=LDEPF) within LDEPF configuration</em>}</li>
 *   </ol>
 * </ul>
 */
public interface ExtRefEditor {

    /**
     * Updates ExtRef binding data related to given ExtRef (<em>extRefInfo</em>) in given SCL file
     *
     * @param scd        SCL file in which ExtRef to update should be found
     * @param extRefInfo ExtRef signal for which we should find possible binders in SCL file
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    void updateExtRefBinders(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

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
     * Updates iedName attribute of all ExtRefs in the Scd.
     *
     * @return list of encountered errors
     */
    List<SclReportItem> updateAllExtRefIedNames(SCL scd);

    /**
     * Create All DataSet and ControlBlock in the SCL based on the ExtRef
     *
     * @param scd          input SCD object. It could be modified by adding new DataSet and ControlBlocks
     * @param allowedFcdas List of allowed FCDA for DataSets and Control Blocks creation
     * @return list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd, Set<FcdaForDataSetsCreation> allowedFcdas);

    /**
     * Create All DataSet and ControlBlock for the ExtRef in given IED
     *
     * @param scd           input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName the name of the IED where the ExtRef are
     * @param allowedFcdas  List of allowed FCDA for DataSets and Control Blocks creation
     * @return list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, Set<FcdaForDataSetsCreation> allowedFcdas);

    /**
     * Create All DataSet and ControlBlock for the ExtRef in given IED and LDevice
     *
     * @param scd               input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName     the name of the IED where the ExtRef are
     * @param targetLDeviceInst the name of the LDevice where the ExtRef are
     * @param allowedFcdas      List of allowed FCDA for DataSets and Control Blocks creation
     * @return list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst, Set<FcdaForDataSetsCreation> allowedFcdas);

    /**
     * Configure the network for all the ControlBlocks.
     * Create (or update if already existing) these elements
     * - the Communication/SubNetwork/ConnectedAP/GSE element, for the GSEControl blocks
     * - the Communication/SubNetwork/ConnectedAP/SMV element, for the SampledValueControl blocks
     *
     * @param scd                         input SCD object. The object will be modified with the new DataGSESet and SMV elements
     * @param controlBlockNetworkSettings a method tha gives the network configuration information for a given ControlBlock
     * @param rangesPerCbType             provide NetworkRanges for GSEControl and SampledValueControl. NetworkRanges contains :
     *                                    start-end app APPID range (long value), start-end MAC-Addresses (Mac-Addresses values: Ex: "01-0C-CD-01-01-FF")
     * @return list of encountered errors
     * @see Utils#macAddressToLong(String) for the expected MAC address format
     * @see ControlBlockNetworkSettings
     * @see ControlBlockNetworkSettings.RangesPerCbType
     * @see ControlBlockNetworkSettings.NetworkRanges
     */
    List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                          ControlBlockNetworkSettings.RangesPerCbType rangesPerCbType);

    /**
     * ExtRef Binding For LDevice (inst=LDEPF) that matching LDEPF configuration
     * @param scd SCL
     * @param settings ILDEPFSettings
     * @return list of encountered errors
     */
    List<SclReportItem> manageBindingForLDEPF(SCL scd, ILDEPFSettings settings);

}
