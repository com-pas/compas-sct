// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDaFilter;

import java.util.Optional;
import java.util.stream.Stream;

public interface DataTypeTemplateReader {

    boolean isDoModAndDaStValExist(TDataTypeTemplates dtt, String lNodeTypeId);

    Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates tDataTypeTemplates);

    Stream<DoLinkedToDa> getFilteredDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, DoLinkedToDaFilter doLinkedToDaFilter);

    Optional<DoLinkedToDa> findDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, DoLinkedToDaFilter doLinkedToDaFilter);

    Stream<String> getEnumValues(TDataTypeTemplates dataTypeTemplates, String lnType, DoLinkedToDaFilter doLinkedToDaFilter);
}
