// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.Set;

/**
 * Service class that will be used to retrieve {@link SCL <em>SCL</em>} XML Files.
 * The ElementsProvider class needs to be able to find/list entries.
 * <p> The following features are supported:</p>
 * <ul>
 *   <li>Communication features</li>
 *   <ol>
 *      <li>{@link SclElementsProvider#getSubnetwork(SCL) <em>Returns list of <b>SubNetworkDTO </b></em>}</li>
 *   </ol>
 *   <li>ExtRef features</li>
 *   <ol>
 *      <li>{@link SclElementsProvider#getExtRefInfo <em>Returns list of <b>ExtRefInfo </b></em>}</li>
 *      <li>{@link SclElementsProvider#getExtRefBinders <em>Returns list of <b>ExtRefBindingInfo </b></em>}</li>
 *      <li>{@link SclElementsProvider#getExtRefSourceInfo <em>Returns list of <b>ExtRefSourceInfo </b></em>}</li>
 *   </ol>
 *   <li>DAI features</li>
 *   <ol>
 *      <li>{@link SclElementsProvider#getDAI <em>Returns list of <b>DataAttributeRef </b></em>}</li>
 *   </ol>
 *   <li>EnumType features</li>
 *   <ol>
 *      <li>{@link SclElementsProvider#getEnumTypeValues(SCL, String) <em>Returns Map <b>(ord, enumVal) </b> of <b>TEnumType </b> reference object</em>}</li>
 *   </ol>
 * </ul>
 */
public interface SclElementsProvider {
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
     * Gets all Control Blocks related to <em>extRefInfo</em> in given SCL file
     *
     * @param scd        SCL file in which ControlBlocks should be found
     * @param extRefInfo ExtRef signal for which we should find related ControlBlocks
     * @return list of <em>ControlBlock</em> object as ControlBlocks of LNode specified in <em>extRefInfo</em>
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    List<ControlBlock> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

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
     * Gets EnumTypes values of ID <em>idEnum</em> from DataTypeTemplate of SCL file
     *
     * @param scd       SCL file in which EnumType should be found
     * @param idEnum    ID of EnumType for which values are retrieved
     * @return list of couple EnumType value and it's order
     * @throws ScdException throws when unknown EnumType
     */
    Set<EnumValDTO> getEnumTypeValues(SCL scd, String idEnum) throws ScdException;

}
