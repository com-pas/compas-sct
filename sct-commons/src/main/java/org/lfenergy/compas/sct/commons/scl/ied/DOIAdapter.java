// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;


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

    private static final Comparator<TExtRef> EXTREF_DESC_SUFFIX_COMPARATOR = Comparator.comparingInt(extRef -> extractDescSuffix(extRef.getDesc()));

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
     * Update the DAI/Val according to the ExtRef attributes.
     * If the ExtRef.desc start with DAI[name='purpose']/Val and end with "_1" (nominal case):
     *  - DAI[name='setSrcRef']/Val is updated with ExtRef attributes concatenation
     *  - DAI[name='setSrcCB']/Val is updated with ExtRef attributes concatenation if ExtRef.srcCBName is present
     * If the ExtRef.desc start with DAI[name='purpose']/Val and end with "_2" or greater (test case):
     *  - DAI[name='setTstRef']/Val is updated with ExtRef attributes concatenation
     *  - DAI[name='setTstCB']/Val is updated with ExtRef attributes concatenation if ExtRef.srcCBName is present
     *
     * @param tExtRefs all the ExtRefs contained in the current LDevice/LLN0
     * @return a filled SclReportItem if an error occurs, empty SclReportItem otherwise
     */
    public List<SclReportItem> updateDaiFromExtRef(List<TExtRef> tExtRefs) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        Optional<TExtRef> tExtRefMinOptional = tExtRefs.stream().min(EXTREF_DESC_SUFFIX_COMPARATOR);
        if (tExtRefMinOptional.isPresent() && extractDescSuffix(tExtRefMinOptional.get().getDesc()) == 1) {
            TExtRef tExtRefMin = tExtRefMinOptional.get();
            String valueSrcRef = createInRefValNominalString(tExtRefMin);
            updateDAI(SETSRCREF_DA_NAME, valueSrcRef).ifPresent(sclReportItems::add);
            if (tExtRefMin.isSetSrcCBName()) {
                String valueSrcCb = createInRefValTestString(tExtRefMin);
                updateDAI(SETSRCCB_DA_NAME, valueSrcCb).ifPresent(sclReportItems::add);
            }

            Optional<TExtRef> tExtRefMaxOptional = tExtRefs.stream().max(EXTREF_DESC_SUFFIX_COMPARATOR);
            if (tExtRefMaxOptional.isPresent() && extractDescSuffix(tExtRefMaxOptional.get().getDesc()) > 1) {
                TExtRef tExtRefMax = tExtRefMaxOptional.get();
                String valueTstRef = createInRefValNominalString(tExtRefMax);
                updateDAI(SETTSTREF_DA_NAME, valueTstRef).ifPresent(sclReportItems::add);
                if (tExtRefMax.isSetSrcCBName()) {
                    String valueTstCb = createInRefValTestString(tExtRefMax);
                    updateDAI(SETTSTCB_DA_NAME, valueTstCb).ifPresent(sclReportItems::add);
                }
            }
        } else {
            sclReportItems.add(SclReportItem.warning(getXPath(), "The DOI %s can't be bound with an ExtRef".formatted(getXPath())));
        }

        return sclReportItems;
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

    private static int extractDescSuffix(String desc) throws NumberFormatException {
        return Integer.parseInt(Objects.requireNonNull(Utils.extractField(desc, "_", -1)));
    }

    private String createInRefValNominalString(TExtRef extRef) {
        return new ObjectReference(extRef, ExtrefTarget.SRC_REF).getReference();
    }

    private String createInRefValTestString(TExtRef extRef) {
        return new ObjectReference(extRef, ExtrefTarget.SRC_CB).getReference();
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
