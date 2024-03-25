// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TBDA;
import org.lfenergy.compas.scl2007b4.model.TDAType;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BDAService {

    public Stream<TBDA> getBDAs(TDAType tdaType) {
        return tdaType.getBDA().stream();
    }

    public Stream<TBDA> getFilteredBDAs(TDAType tdaType, Predicate<TBDA> tTBDAPredicate) {
        return getBDAs(tdaType).filter(tTBDAPredicate);
    }

    public Optional<TBDA> findBDA(TDAType tdaType, Predicate<TBDA> tBDAPredicate) {
        return getFilteredBDAs(tdaType, tBDAPredicate).findFirst();
    }

}
