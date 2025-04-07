// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LnodeTypeService {

    public Stream<TLNodeType> getLnodeTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getLNodeType().stream();
    }

    public Stream<TLNodeType> getFilteredLnodeTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getLnodeTypes(tDataTypeTemplates).filter(tlNodeTypePredicate);
    }

    public Optional<TLNodeType> findLnodeType(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getFilteredLnodeTypes(tDataTypeTemplates, tlNodeTypePredicate).findFirst();
    }

    public Optional<TLNodeType> findLnodeType(TDataTypeTemplates tDataTypeTemplates, String lNodeTypeId) {
        return getFilteredLnodeTypes(tDataTypeTemplates, tlNodeType -> tlNodeType.getId().equals(lNodeTypeId)).findFirst();
    }

}
