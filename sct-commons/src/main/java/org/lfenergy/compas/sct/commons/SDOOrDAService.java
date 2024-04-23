// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SDOOrDAService {


    public Stream<TSDO> getSDOs(TDOType tdoType) {
        return tdoType.getSDOOrDA().stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TSDO.class))
                .map(TSDO.class::cast);
    }

    public Stream<TDA> getDAs(TDOType tdoType) {
        return tdoType.getSDOOrDA().stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDA.class))
                .map(TDA.class::cast);
    }

    public Stream<TSDO> getFilteredSDOs(TDOType tdoType, Predicate<TSDO> tsdoPredicate) {
        return getSDOs(tdoType).filter(tsdoPredicate);
    }

    public Stream<TDA> getFilteredDAs(TDOType tdoType, Predicate<TDA> tdaPredicate) {
        return getDAs(tdoType).filter(tdaPredicate);
    }

    public Optional<TSDO> findSDO(TDOType tdoType, Predicate<TSDO> tsdoPredicate) {
        return getFilteredSDOs(tdoType, tsdoPredicate).findFirst();
    }

    public Optional<TDA> findDA(TDOType tdoType, Predicate<TDA> tdaPredicate) {
        return getFilteredDAs(tdoType, tdaPredicate).findFirst();
    }

}
