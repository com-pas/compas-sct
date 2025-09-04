// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DaiService {

    public Stream<TDAI> getDais(TDOI tdoi) {
        return tdoi.getSDIOrDAI()
                .stream()
                .filter(TDAI.class::isInstance)
                .map(TDAI.class::cast);
    }
    public Stream<TDAI> getDais(TSDI tsdi) {
        return tsdi.getSDIOrDAI()
                .stream()
                .filter(TDAI.class::isInstance)
                .map(TDAI.class::cast);
    }

    public Stream<TDAI> getFilteredDais(TDOI tdoi, Predicate<TDAI> tdaiPredicate) {
        return getDais(tdoi).filter(tdaiPredicate);
    }

    public Stream<TDAI> getFilteredDais(TSDI tsdi, Predicate<TDAI> tdaiPredicate) {
        return getDais(tsdi).filter(tdaiPredicate);
    }

    public Optional<TDAI> findDai(TDOI tdoi, Predicate<TDAI> tdaiPredicate) {
        return getFilteredDais(tdoi, tdaiPredicate).findFirst();
    }

    public Optional<TDAI> findDai(TSDI tsdi, Predicate<TDAI> tdaiPredicate) {
        return getFilteredDais(tsdi, tdaiPredicate).findFirst();
    }

}
