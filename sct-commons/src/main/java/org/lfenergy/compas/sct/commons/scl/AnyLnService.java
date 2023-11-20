// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDOI;

import java.util.Optional;
import java.util.stream.Stream;

public class AnyLnService {

    public Stream<TDOI> getDois(TAnyLN tAnyLN) {
        if (!tAnyLN.isSetDOI()) {
            return Stream.empty();
        }
        return tAnyLN.getDOI().stream();
    }

    public Optional<TDOI> findDoi(TAnyLN tAnyLN, String doiName) {
        return getDois(tAnyLN)
                .filter(tdoi -> doiName.equals(tdoi.getName()))
                .findFirst();
    }

}
