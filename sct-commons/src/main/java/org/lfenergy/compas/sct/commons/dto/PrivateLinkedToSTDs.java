// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TPrivate;

import java.util.List;

public record PrivateLinkedToSTDs(TPrivate tPrivate, List<SCL> stdList) {

}
