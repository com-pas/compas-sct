// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Collection;
import java.util.Optional;

public abstract class AbstractDataTypeAdapter<T>
        extends SclElementAdapter<DataTypeTemplateAdapter, T>  implements IDataTemplate, IDTTComparable<T>{

    protected AbstractDataTypeAdapter(DataTypeTemplateAdapter parentAdapter, T currentElem) {
        super(parentAdapter, currentElem);
    }

}
