// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TSDI;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SdiService {

    public Stream<TSDI> getSdis(TDOI tdoi) {
        return tdoi.getSDIOrDAI()
                .stream()
                .filter(TSDI.class::isInstance)
                .map(TSDI.class::cast);
    }

    public Stream<TSDI> getFilteredSdis(TDOI tdoi, Predicate<TSDI> tsdiPredicate) {
        return getSdis(tdoi).filter(tsdiPredicate);
    }

    public Optional<TSDI> findSdi(TDOI tdoi, Predicate<TSDI> tsdiPredicate) {
        return getFilteredSdis(tdoi, tsdiPredicate).findFirst();
    }

    public Stream<TSDI> getSdis(TSDI tsdi) {
        return tsdi.getSDIOrDAI()
                .stream()
                .filter(TSDI.class::isInstance)
                .map(TSDI.class::cast);
    }

    public Stream<TSDI> getFilteredSdis(TSDI tsdi, Predicate<TSDI> tsdiPredicate) {
        return getSdis(tsdi).filter(tsdiPredicate);
    }

    public Optional<TSDI> findSdi(TSDI tsdi, Predicate<TSDI> tsdiPredicate) {
        return getFilteredSdis(tsdi, tsdiPredicate).findFirst();
    }

}
