// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.domain.DataRef;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface DataTypeTemplateReader {

    Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates tDataTypeTemplates);

    Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId);

    Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, String doName);

    Optional<DoLinkedToDa> findDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, DataRef dataRef);

    List<String> getEnumValues(TDataTypeTemplates dataTypeTemplates, String enumId);
}
