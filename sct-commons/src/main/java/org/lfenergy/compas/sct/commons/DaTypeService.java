// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DaTypeService {

    public Stream<TDAType> getDaTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getDAType().stream();
    }

    public Stream<TDAType> getFilteredDaTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TDAType> tdaTypePredicate) {
        return getDaTypes(tDataTypeTemplates).filter(tdaTypePredicate);
    }

    public Optional<TDAType> findDaType(TDataTypeTemplates tDataTypeTemplates, Predicate<TDAType> tdaTypePredicate) {
        return getFilteredDaTypes(tDataTypeTemplates, tdaTypePredicate).findFirst();
    }

}
