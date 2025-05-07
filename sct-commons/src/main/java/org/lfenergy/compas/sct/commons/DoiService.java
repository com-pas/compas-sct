// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDOI;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DoiService {

    public Stream<TDOI> getDois(TAnyLN tAnyLN) {
        return tAnyLN.getDOI().stream();
    }

    public Stream<TDOI> getFilteredDois(TAnyLN tAnyLN, Predicate<TDOI> tdoiPredicate) {
        return getDois(tAnyLN).filter(tdoiPredicate);
    }

    public Optional<TDOI> findDoi(TAnyLN tAnyLN, Predicate<TDOI> tdoiPredicate) {
        return getFilteredDois(tAnyLN, tdoiPredicate).findFirst();
    }

    public Optional<TDOI> findDoiByName(TAnyLN tAnyLN, String doiName) {
        return findDoi(tAnyLN, tdoi -> tdoi.getName().equals(doiName));
    }

}
