// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Optional;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TDO DO}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link DOAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *       <li>{@link DOAdapter#getDoTypeAdapter() <em>Returns the value of the <b>DoTypeAdapter </b> reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DOAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link DOAdapter#getType <em>Returns the value of the <b>type </b>attribute</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TUnNaming
 * @see org.lfenergy.compas.scl2007b4.model.TDA
 * @see org.lfenergy.compas.scl2007b4.model.TBDA
 * @see org.lfenergy.compas.scl2007b4.model.TSDO
 */
public class DOAdapter extends SclElementAdapter<LNodeTypeAdapter, TDO> implements IDataTemplate{
    protected DOAdapter(LNodeTypeAdapter parentAdapter, TDO currElement) {
        super(parentAdapter,currElement);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDO().contains(currentElem);
    }

    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter.getDataTypeTemplateAdapter();
    }

    public Optional<DOTypeAdapter> getDoTypeAdapter() {
        return getDataTypeTemplateAdapter().getDOTypeAdapterById(currentElem.getType());
    }

    public String getType() {
        return currentElem.getType();
    }
}
