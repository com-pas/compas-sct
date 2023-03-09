// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.lfenergy.compas.scl2007b4.model.TDurationInMilliSec;
import org.lfenergy.compas.sct.commons.scl.ied.ControlBlockAdapter;

/**
 * This interface has a single method which provides network settings for a ControlBlock.
 * These are used to create:
 * - the Communication/SubNetwork/ConnectedAP/GSE element, which is the network configuration of a GSEControl block
 * - the Communication/SubNetwork/ConnectedAP/SMV element, which is the network configuration  of a SampledValueControl block
 * It is a FunctionalInterface, so it can be implemented with a lambda expression.
 *
 * @see org.lfenergy.compas.sct.commons.util.ControlBlockNetworkSettingsCsvHelper
 */
@FunctionalInterface
public interface ControlBlockNetworkSettings {

    /**
     * This method provides a vlanId, vlanPriority, minTime, maxTime for this ControlBlock.
     * vlanPriority will be ignored when vlanId is null.
     *
     * @param controlBlockAdapter ControlBlock for which we want to configure the communication section
     * @return network settings : All fields are optional (meaning fields can be null).
     * When the return value itself is null, the communication section will not be configured for this ControlBlock.
     */
    Settings getNetworkSettings(ControlBlockAdapter controlBlockAdapter);

    /**
     * Network settings for ControlBlock communication
     *
     * @param vlanId       id of the vlan
     * @param vlanPriority priority for the vlan
     * @param minTime      minTime for GSE communication element
     * @param maxTime      maxTime for GSE communication element
     */
    record Settings(Integer vlanId, Byte vlanPriority, TDurationInMilliSec minTime, TDurationInMilliSec maxTime) {
    }

    /**
     * NetworkRanges for GSEControl and SampledValueControl
     *
     * @param gse          NetworkRanges for GSEControl
     * @param sampledValue NetworkRanges for SampledValueControl
     */
    record RangesPerCbType(NetworkRanges gse, NetworkRanges sampledValue) {
    }

    /**
     * Range of APPID and range of MAC-Address
     *
     * @param appIdStart      range start for APPID (inclusive)
     * @param appIdEnd        range end for APPID (inclusive)
     * @param macAddressStart range start for MAC-Addresses (inclusive). Ex: "01-0C-CD-01-00-00"
     * @param macAddressEnd   range end for MAC-Addresses (inclusive). Ex: "01-0C-CD-01-01-FF"
     */
    record NetworkRanges(long appIdStart, long appIdEnd, String macAddressStart, String macAddressEnd) {
    }
}
