// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VoltageLevelService {

    public Stream<TVoltageLevel> getVoltageLevels(SCL scd) {
        if (!scd.isSetSubstation()) {
            return Stream.empty();
        }
        return scd.getSubstation()
                .stream()
                .map(TSubstation::getVoltageLevel)
                .flatMap(Collection::stream);
    }

    public Optional<TVoltageLevel> findVoltageLevel(SCL scd, Predicate<TVoltageLevel> tVoltageLevelPredicate) {
        return getVoltageLevels(scd).filter(tVoltageLevelPredicate).findFirst();
    }
}
