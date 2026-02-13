// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class IedService {

    public Stream<TIED> getFilteredIeds(SCL scd, Predicate<TIED> iedPredicate) {
        return scd.getIED().stream().filter(iedPredicate);
    }

    public Optional<TIED> findIed(SCL scd, Predicate<TIED> iedPredicate) {
        return getFilteredIeds(scd, iedPredicate).findFirst();
    }

    public Optional<TIED> findIed(SCL scd, String iedName) {
        if (null == iedName)
            throw new ScdException("The given iedName is null");
        return findIed(scd, tied -> iedName.equals(tied.getName()));
    }
}
