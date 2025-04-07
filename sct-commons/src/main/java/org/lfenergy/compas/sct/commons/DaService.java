// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.scl2007b4.model.TDOType;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DaService {

    public Stream<TDA> getDAs(TDOType tdoType) {
        return tdoType.getSDOOrDA().stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDA.class))
                .map(TDA.class::cast);
    }

    public Stream<TDA> getFilteredDAs(TDOType tdoType, Predicate<TDA> tdaPredicate) {
        return getDAs(tdoType).filter(tdaPredicate);
    }

    public Optional<TDA> findDA(TDOType tdoType, Predicate<TDA> tdaPredicate) {
        return getFilteredDAs(tdoType, tdaPredicate).findFirst();
    }

    public Optional<TDA> findDA(TDOType tdoType, String daName) {
        return getFilteredDAs(tdoType, tda -> tda.getName().equals(daName)).findFirst();
    }

}
