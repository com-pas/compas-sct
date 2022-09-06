// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.dtt.AbstractDataTypeAdapter DataTemplate}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link AbstractDataTypeAdapter#getDataTypeTemplateAdapter <em>get DataTypeTemplateAdapter</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link AbstractDataTypeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link AbstractDataTypeAdapter#hasSameContentAs <em>Compare Two SCL element</em>}</li>
 *    </ul>
 * </ol>
 * @see org.lfenergy.compas.sct.commons.scl.SclElementAdapter
 * @see org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter
 * @see org.lfenergy.compas.sct.commons.scl.dtt.IDataTemplate
 * @see org.lfenergy.compas.sct.commons.scl.dtt.IDTTComparable
 */
public abstract class AbstractDataTypeAdapter<T>
        extends SclElementAdapter<DataTypeTemplateAdapter, T>  implements IDataTemplate, IDTTComparable<T>{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    protected AbstractDataTypeAdapter(DataTypeTemplateAdapter parentAdapter, T currentElem) {
        super(parentAdapter, currentElem);
    }

}
