// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

public abstract class AbstractDataTypeAdapter<T>
        extends SclElementAdapter<DataTypeTemplateAdapter, T>  implements IDataTemplate, IDTTComparable<T>{

    /**
     * Constructor
     * @param parentAdapter input
     * @param currentElem input
     */
    protected AbstractDataTypeAdapter(DataTypeTemplateAdapter parentAdapter, T currentElem) {
        super(parentAdapter, currentElem);
    }

}
