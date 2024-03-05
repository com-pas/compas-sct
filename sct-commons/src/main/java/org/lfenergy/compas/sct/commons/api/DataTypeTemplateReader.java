// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;

import java.util.Optional;
import java.util.stream.Stream;

public interface DataTypeTemplateReader {

    boolean isDoModAndDaStValExist(TDataTypeTemplates dtt, String lNodeTypeId);

    Stream<DataAttributeRef> getAllDOAndDA(TDataTypeTemplates tDataTypeTemplates);

    Stream<DataAttributeRef> getFilteredDOAndDA(TDataTypeTemplates dtt, TAnyLN anyLn, DataAttributeRef filter);

    Optional<DataAttributeRef> findDOAndDA(TDataTypeTemplates dtt, String lNodeTypeId, DataAttributeRef dataAttributeRef);

}
