// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.scl2007b4.model.TUnNaming;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.SDIAdapter DOIAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link SDIAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
 *      <li>{@link SDIAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link SDIAdapter#addDAI <em>Add <b>TDAI </b> under this object</em>}</li>
 *      <li>{@link SDIAdapter#addSDOI <em>Add <b>TSDI </b> under this object</em>}</li>
 *      <li>{@link SDIAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *    </ul>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TSDI
 */
public class SDIAdapter extends SclElementAdapter<SclElementAdapter, TSDI> implements IDataParentAdapter {

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    protected SDIAdapter(SclElementAdapter parentAdapter, TSDI currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter instanceof IDataParentAdapter dataParentAdapter && dataParentAdapter.getSDIOrDAI().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("SDI[%s]",
                Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    @Override
    public String getName() {
        return currentElem.getName();
    }

    @Override
    public List<TUnNaming> getSDIOrDAI() {
        return currentElem.getSDIOrDAI();
    }

    @Override
    public IDataParentAdapter toAdapter(TSDI childTSDI) {
        return new SDIAdapter(this, childTSDI);
    }

    @Override
    public AbstractDAIAdapter<?> toAdapter(TDAI childTDAI) {
        return new DAIAdapter(this, childTDAI);
    }

    /**
     * A representation of the model object
     * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.SDIAdapter.DAIAdapter SDIAdapter.DAIAdapter}</b></em>.
     * <p>
     * The following features are supported:
     * </p>
     * <ol>
     *   <li>Adapter</li>
     *    <ul>
     *      <li>{@link DAIAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
     *      <li>{@link DAIAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
     *    </ul>
     *   <li>Principal functions</li>
     *    <ul>
     *      <li>{@link DAIAdapter#addDAI <em>Add <b>TDAI </b> under this object</em>}</li>
     *      <li>{@link DAIAdapter#addSDOI <em>Add <b>TSDI </b> under this object</em>}</li>
     *      <li>{@link DAIAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
     *    </ul>
     * </ol>
     *
     * @see org.lfenergy.compas.scl2007b4.model.TDAI
     */
    public static class DAIAdapter extends AbstractDAIAdapter<SDIAdapter> {

        protected DAIAdapter(SDIAdapter parentAdapter, TDAI currentElem) {
            super(parentAdapter, currentElem);
        }

        @Override
        protected boolean amChildElementRef() {
            return parentAdapter.getCurrentElem().getSDIOrDAI().contains(currentElem);
        }

        @Override
        protected String elementXPath() {
            return String.format("DAI[%s]",
                    Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
        }
    }
}
