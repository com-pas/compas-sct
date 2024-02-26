// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;

import java.util.Optional;

public interface LNEditor {

    Optional<TDAI> getDOAndDAInstances(TAnyLN tAnyLN, DoTypeName doTypeName, DaTypeName daTypeName);

    void updateOrCreateDOAndDAInstances(TAnyLN tAnyLN, DataAttributeRef dataAttributeRef);

}
