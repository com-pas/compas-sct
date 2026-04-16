// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Optional;


/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.DOIAdapter DOIAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link DOIAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
 *      <li>{@link DOIAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DOIAdapter#addDAI <em>Add <b>TDAI </b> under this object</em>}</li>
 *      <li>{@link DOIAdapter#addSDOI <em>Add <b>TSDI </b> under this object</em>}</li>
 *      <li>{@link DOIAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *    </ul>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDAI
 * @see org.lfenergy.compas.scl2007b4.model.TSDI
 */
public class DOIAdapter extends SclElementAdapter<AbstractLNAdapter<? extends TAnyLN>, TDOI> implements IDataParentAdapter {

    protected static final String DAI_NOT_UPDATABLE_MESSAGE = "The DAI %s cannot be updated";

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    public DOIAdapter(AbstractLNAdapter<? extends TAnyLN> parentAdapter, TDOI currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDOI().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("DOI[%s]",
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
        return new RootSDIAdapter(this, childTSDI);
    }

    @Override
    public AbstractDAIAdapter<?> toAdapter(TDAI childTDAI) {
        return new DAIAdapter(this, childTDAI);
    }

    /**
     * Check and update DAI identified by name with given value
     *
     * @param daName name of DAI to update
     * @param value  new value
     * @return warning message when DAI not updatable, otherwise return empty and update DAI with value
     */
    public Optional<SclReportItem> updateDAI(String daName, String value) {
        DataAttributeRef daiFilterSrcRef = new DataAttributeRef(getParentAdapter(), new DoTypeName(getName()), new DaTypeName(daName));
        Optional<DataAttributeRef> foundDais = getParentAdapter().getDAI(daiFilterSrcRef, true).stream().findFirst();
        if (foundDais.isEmpty()) {
            return Optional.of(SclReportItem.warning(getXPath() + "/DAI@name=\"" + daName + "\"/Val", DAI_NOT_UPDATABLE_MESSAGE.formatted(daName)));
        }
        DataAttributeRef filterForUpdate = foundDais.get();
        filterForUpdate.setVal(value);
        getParentAdapter().updateDAI(filterForUpdate);
        return Optional.empty();

    }

    /**
     * A representation of the model object
     * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.DOIAdapter.DAIAdapter DOIAdapter.DAIAdapter}</b></em>.
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
     * @see org.lfenergy.compas.scl2007b4.model.TSDI
     */
    public static class DAIAdapter extends AbstractDAIAdapter<DOIAdapter> {

        protected DAIAdapter(DOIAdapter parentAdapter, TDAI currentElem) {
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
