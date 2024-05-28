// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.model.cbcom.CBCom;
import org.lfenergy.compas.sct.commons.model.da_comm.DACOMM;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;

/**
 * Service class that will be used to create, update or delete elements related to the {@link TExtRef <em>TExtRef</em>} object.
 * <p> The following features are supported: </p>
 * <ul>
 *   <li>ExtRef features</li>
 *   <ol>
 *      <li>{@link ControlBlockEditor#configureNetworkForAllControlBlocks <em>Configure the network for the <b>ControlBlocks</b></em>}</li>
 *      <li>{@link ControlBlockEditor#removeAllControlBlocksAndDatasetsAndExtRefSrcBindings <em>Removes all ControlBlocks and DataSets for all LNs in <b>SCL</b></em>}</li>
 *      <li>{@link ControlBlockEditor#analyzeDataGroups(SCL)} <em>Checks Control Blocks, DataSets and FCDA number limitation into Access Points </em>}</li>
 *   </ol>
 * </ul>
 */
public interface ControlBlockEditor {


    /**
     * Removes all ControlBlocks and DataSets for all LNs in SCL
     *
     * @param scl SCL file for which ControlBlocks and DataSets should be deleted
     */
    void removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(final SCL scl);

    /**
     * Checks Control Blocks, DataSets and FCDA number limitation into Access Points
     *
     * @param scd SCL file for which LDevice should be activated or deactivated
     * @return list of encountered errors
     */
    List<SclReportItem> analyzeDataGroups(SCL scd);

    /**
     * Create All DataSet and ControlBlock in the SCL based on the ExtRef
     *
     * @param scd          input SCD object. It could be modified by adding new DataSet and ControlBlocks
     * @param dacomm       object containing a list of allowed FCDA for DataSets and Control Blocks creation
     * @return             list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd, DACOMM dacomm);

    /**
     * Configure the network for all the ControlBlocks.
     * Create (or update if already existing) these elements
     * - the Communication/SubNetwork/ConnectedAP/GSE element, for the GSEControl blocks
     * - the Communication/SubNetwork/ConnectedAP/SMV element, for the SampledValueControl blocks
     *
     * @param scd   input SCD object. The object will be modified with the new GSE and SMV elements
     * @param cbCom communication settings to configure Control Block Communication
     * @return list of encountered errors
     * @see Utils#macAddressToLong(String) for the expected MAC address format
     */
    List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, CBCom cbCom);

    /**
     * Configure the network for all the ControlBlocks, reusing APPID and MAC-Address from given Communication section when they already exists.
     * Create (or update if already existing) these elements
     * - the Communication/SubNetwork/ConnectedAP/GSE element, for the GSEControl blocks
     * - the Communication/SubNetwork/ConnectedAP/SMV element, for the SampledValueControl blocks
     * For a ControlBlock (IED.name, LD.inst, GSEControl/SampledValueControl.name), if there is a matching network configuration in communicationToReuse (ConnectedAP.iedName, GSE/SMV.ldInst, GSE/SMV.cbName),
     * then reuse APPID and MAC-Address from communicationToReuse,
     * else use range provided in cbCom.
     * APPID and MAC-Address of communicationToReuse for ControlBlocks that exists in scd are excluded from the ranges given in cbCom.
     *
     * @param scd                  input SCD object. The object will be modified with the new GSE and SMV elements
     * @param cbCom                communication settings to configure Control Block Communication
     * @param subnetworksToReuse   subnetworks to search for existing APPID and MAC-Address for ControlBlock
     * @return list of encountered errors
     * @see Utils#macAddressToLong(String) for the expected MAC address format
     */
    List<SclReportItem> configureNetworkForAllControlBlocks(SCL scd, CBCom cbCom, List<TSubNetwork> subnetworksToReuse);

}
