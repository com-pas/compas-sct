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

    Stream<DataAttributeRef> getAllDataObjectsAndDataAttributes(TDataTypeTemplates tDataTypeTemplates);

    Stream<DataAttributeRef> getFilteredDataObjectsAndDataAttributes(TDataTypeTemplates dtt, TAnyLN anyLn, DataAttributeRef filter);

    Optional<DataAttributeRef> findDataObjectsAndDataAttributesByDataReference(TDataTypeTemplates dtt, String lNodeTypeId, String dataAttributeRef);

}
