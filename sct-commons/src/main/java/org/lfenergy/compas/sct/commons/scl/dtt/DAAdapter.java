// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.Utils;

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
    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    protected DAAdapter(DOTypeAdapter parentAdapter, TDA currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDOOrDA().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("DA[name=%s and type=%s]",
                Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null),
                Utils.xpathAttributeFilter("type", currentElem.isSetType() ? currentElem.getType() : null));
    }

    /**
     * Updates DA Type Name
     * @param daTypeName DA Type Name to update
     * @throws ScdException
     */
    @Override
    public void check(DaTypeName daTypeName) throws ScdException {
        super.check(daTypeName);
        daTypeName.setFc(currentElem.getFc());
    }

}
