// SPDX-FileCopyrightText: 2025 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ldevice;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;

import java.util.Set;

public record LDeviceActivation(String modStValCurrentValue, boolean isPresentInSubstation, Set<String> enumValues, TAnyLN tln) {
}
