// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0


package org.lfenergy.compas.sct.commons.service;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.util.ILDEPFSettings;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;

public interface IExtRefService {

    /**
     * Updates iedName attribute of all ExtRefs in the Scd.
     *
     * @return list of encountered errors
     */
    List<SclReportItem> updateAllExtRefIedNames(SCL scd);

    /**
     * Create All DataSet and ControlBlock in the SCL based on the ExtRef
     *
     * @param scd input SCD object. It could be modified by adding new DataSet and ControlBlocks
     * @return list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd);

    /**
     * Create All DataSet and ControlBlock for the ExtRef in given IED
     *
     * @param scd           input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName the name of the IED where the ExtRef are
     * @return list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName);

    /**
     * Create All DataSet and ControlBlock for the ExtRef in this LDevice
     *
     * @param scd               input SCD object. The object will be modified with the new DataSet and ControlBlocks
     * @param targetIedName     the name of the IED where the ExtRef are
     * @param targetLDeviceInst the name of the LDevice where the ExtRef are
     * @return list of encountered errors
     */
    List<SclReportItem> createDataSetAndControlBlocks(SCL scd, String targetIedName, String targetLDeviceInst);

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
