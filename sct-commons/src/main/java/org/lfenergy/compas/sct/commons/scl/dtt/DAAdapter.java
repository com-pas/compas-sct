// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

/**
 * A representation of the model object <em><b>{@link org.lfenergy.compas.scl2007b4.model.TDA DA}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link DAAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *       <li>{@link DAAdapter#getDATypeAdapter() <em>Returns the value of the <b>DATypeAdapter </b> reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DAAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link DAAdapter#getType() <em>Returns the value of the <b>type </b>attribute</em>}</li>
 *      <li>{@link DAAdapter#getName <em>Returns the value of the <b>type </b>name</em>}</li>
 *      <li>{@link DAAdapter#getBType <em>Returns the value of the <b>bType </b>attribute</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link DAAdapter#hasSameContentAs <em>Compare Two <b>TAbstractDataAttribute </b></em>}</li>
 *    </ul>
 * </ol>
 * @see org.lfenergy.compas.scl2007b4.model.TAbstractDataAttribute
 * @see org.lfenergy.compas.scl2007b4.model.TDA
 * @see org.lfenergy.compas.scl2007b4.model.TBDA
 * @see org.lfenergy.compas.scl2007b4.model.TSDO
 */
@Getter
public class DAAdapter extends AbstractDataAttributeAdapter<DOTypeAdapter, TDA> implements IDataTemplate, IDTTComparable<TDA> {
    protected DAAdapter(DOTypeAdapter parentAdapter, TDA currentElem) {
        super(parentAdapter, currentElem);
    }



    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDOOrDA().contains(currentElem);
    }

    @Override
    public void check(DaTypeName daTypeName) throws ScdException {
        super.check(daTypeName);
        daTypeName.setFc(currentElem.getFc());
    }

}
