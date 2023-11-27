// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TExtRef;

import java.util.stream.Stream;

public interface ExtRefReader {

    Stream<TExtRef> getExtRefs(TAnyLN tAnyLN);
}
