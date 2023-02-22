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

    record Settings(
        Integer vlanId,
        Byte vlanPriority,
        TDurationInMilliSec minTime,
        TDurationInMilliSec maxTime) {
    }
}
