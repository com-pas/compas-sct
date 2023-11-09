// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.sct.commons.api.ExtRefReader;

import java.util.stream.Stream;

public class ExtRefReaderService implements ExtRefReader {

    public Stream<TExtRef> getExtRefs(TAnyLN tAnyLN) {
        return switch (tAnyLN) {
            case LN0 ln0 -> ln0.isSetInputs() ? ln0.getInputs().getExtRef().stream() : Stream.empty();
            case TLN tln -> tln.isSetInputs() ? tln.getInputs().getExtRef().stream() : Stream.empty();
            default -> throw new IllegalStateException("Unexpected value: " + tAnyLN);
        };
    }
}
