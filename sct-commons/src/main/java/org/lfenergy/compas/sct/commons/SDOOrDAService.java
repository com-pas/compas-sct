// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SDOOrDAService {

    public <T> Stream<T> getSDOOrDAs(TDOType tdoType, Class<T> clazz) {
        return tdoType.getSDOOrDA().stream()
                .filter(unNaming -> unNaming.getClass().equals(clazz))
                .map(clazz::cast);
    }

    public <T> Stream<T> getFilteredSDOOrDAs(TDOType tdoType, Class<T> clazz, Predicate<T> tSDOOrDAPredicate) {
        return getSDOOrDAs(tdoType, clazz).filter(tSDOOrDAPredicate);
    }

    public <T> Optional<T> findSDOOrDA(TDOType tdoType, Class<T> clazz, Predicate<T> tSDOOrDAPredicate) {
        return getFilteredSDOOrDAs(tdoType, clazz, tSDOOrDAPredicate).findFirst();
    }

}
