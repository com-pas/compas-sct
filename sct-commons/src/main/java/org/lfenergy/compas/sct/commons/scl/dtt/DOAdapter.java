// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

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

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currElement Current reference
     */
    protected DOAdapter(LNodeTypeAdapter parentAdapter, TDO currElement) {
        super(parentAdapter,currElement);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDO().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("DO[%s and %s]",
                Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null),
                Utils.xpathAttributeFilter("type", currentElem.isSetType() ? currentElem.getType() : null));
    }

    /**
     * Gets linked DataTypeTemplateAdapter as parent
     * @return <em>DataTypeTemplateAdapter</em> object
     */
    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter.getDataTypeTemplateAdapter();
    }

    /**
     * Gets DOTypeAdapter from parent DataTypeTemplate
     * @return Optional of <em>DOTypeAdapter</em> object
     */
    public Optional<DOTypeAdapter> getDoTypeAdapter() {
        return getDataTypeTemplateAdapter().getDOTypeAdapterById(currentElem.getType());
    }

    /**
     * Gets DO element type
     * @return type of DO
     */
    public String getType() {
        return currentElem.getType();
    }
}
