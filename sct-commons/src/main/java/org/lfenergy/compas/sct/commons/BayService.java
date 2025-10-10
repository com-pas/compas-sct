// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TSubstation;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class BayService {

    public Stream<TBay> getBays(SCL scl) {
        return scl.getSubstation().stream().flatMap(this::getBays);
    }

    public Stream<TBay> getBays(TSubstation tSubstation) {
        return tSubstation.getVoltageLevel().stream()
                .flatMap(tVoltageLevel -> tVoltageLevel.getBay().stream());
    }

    public Stream<TBay> getFilteredBays(TSubstation tSubstation, Predicate<TBay> bayPredicate) {
        return getBays(tSubstation).filter(bayPredicate);
    }

    public Optional<TBay> findBay(TSubstation tSubstation, Predicate<TBay> bayPredicate) {
        return getFilteredBays(tSubstation, bayPredicate).findFirst();
    }

    public Optional<TBay> findBay(TSubstation tSubstation, String bayName) {
        return findBay(tSubstation, bay -> bay.getName().equals(bayName));
    }

}
