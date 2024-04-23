// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;

import java.util.Optional;

public class DaTypeService {

    public Optional<TDAType> findDaType(TDataTypeTemplates tDataTypeTemplates, String daTypeId) {
        return tDataTypeTemplates.getDAType().stream()
                .filter(tdaType -> tdaType.isSetId()  && tdaType.getId().equals(daTypeId)).findFirst();
    }

}
