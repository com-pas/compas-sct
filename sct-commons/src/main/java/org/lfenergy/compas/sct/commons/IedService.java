// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TIED;

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
}
