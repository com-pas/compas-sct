// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ExtrefTarget;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    protected static final String DA_NAME_SET_SRC_REF = "setSrcRef";
    protected static final String DA_NAME_SET_SRC_CB = "setSrcCB";
    protected static final String DA_NAME_SET_TST_REF = "setTstRef";
    protected static final String DA_NAME_SET_TST_CB = "setTstCB";

    private static final Comparator<TExtRef> EXTREF_DESC_SUFFIX_COMPARATOR = Comparator.comparingInt(extRef -> extractDescSuffix(extRef.getDesc()));

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    protected DOIAdapter(AbstractLNAdapter<? extends TAnyLN> parentAdapter, TDOI currentElem) {
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

    /**
     * Gets SDI by name from current DOI
     *
     * @param sName name of SDI to get
     * @return <em>RootSDIAdapter</em> object
     * @throws ScdException throws when specified name of SDI not present in current DOI
     */
    @Override
    public RootSDIAdapter getStructuredDataAdapterByName(String sName) throws ScdException {
        return currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sName))
                .map(tsdi -> new RootSDIAdapter(this, tsdi))
                .findFirst()
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Unknown SDI (%s) in DOI (%s)", sName, currentElem.getName())
                        )
                );
    }

    /**
     * Gets DAI from current DOI
     *
     * @param daName name of DAI to get
     * @return <em>DAIAdapter</em> object
     * @throws ScdException throws when specified name of DAI not present in current DOI
     */
    @Override
    public DAIAdapter getDataAdapterByName(String daName) throws ScdException {
        return findDataAdapterByName(daName)
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Unknown DAI (%s) in DOI (%s)", daName, currentElem.getName())
                        )
                );
    }

    /**
     * Given a DAI[name], returns an associated DAIAdapter
     *
     * @param daName name of DAI to get
     * @return an Optional <em>DAIAdapter</em> if found, Optional.empty() otherwise
     */
    public Optional<DAIAdapter> findDataAdapterByName(String daName) {
        return currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals(daName))
                .map(tdai -> new DAIAdapter(this, tdai))
                .findFirst();
    }

    /**
     * Adds DAI to current DOI
     *
     * @param name        name of DAI to add
     * @param isUpdatable updatability state of DAI
     * @return <em>DAIAdapter</em> object as added DAI
     */
    @Override
    public DAIAdapter addDAI(String name, boolean isUpdatable) {
        TDAI tdai = new TDAI();
        tdai.setName(name);
        tdai.setValImport(isUpdatable);
        currentElem.getSDIOrDAI().add(tdai);
        return new DAIAdapter(this, tdai);
    }

    /**
     * Adds SDOI to SDI in current DOI
     *
     * @param sdoName name of SDOI to add
     * @return <em>RootSDIAdapter</em> object as added SDOI
     */
    @Override
    public RootSDIAdapter addSDOI(String sdoName) {
        TSDI tsdi = new TSDI();
        tsdi.setName(sdoName);
        currentElem.getSDIOrDAI().add(tsdi);
        return new RootSDIAdapter(this, tsdi);
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
    public Optional<SclReportItem> updateDaiFromExtRef(List<TExtRef> tExtRefs) {
        Optional<TExtRef> tExtRefMinOptional = tExtRefs.stream().min(EXTREF_DESC_SUFFIX_COMPARATOR);

        if (tExtRefMinOptional.isPresent() && extractDescSuffix(tExtRefMinOptional.get().getDesc()) == 1) {
            TExtRef tExtRefMin = tExtRefMinOptional.get();
            findDataAdapterByName(DA_NAME_SET_SRC_REF)
                    .orElse(addDAI(DA_NAME_SET_SRC_REF, true))
                    .updateVal(createInRefValNominalString(tExtRefMin));
            if (tExtRefMin.isSetSrcCBName()) {
                findDataAdapterByName(DA_NAME_SET_SRC_CB)
                        .orElse(addDAI(DA_NAME_SET_SRC_CB, true))
                        .updateVal(createInRefValTestString(tExtRefMin));
            }

            Optional<TExtRef> tExtRefMaxOptional = tExtRefs.stream().max(EXTREF_DESC_SUFFIX_COMPARATOR);
            if (tExtRefMaxOptional.isPresent() && extractDescSuffix(tExtRefMaxOptional.get().getDesc()) > 1) {
                TExtRef tExtRefMax = tExtRefMaxOptional.get();
                findDataAdapterByName(DA_NAME_SET_TST_REF)
                        .orElse(addDAI(DA_NAME_SET_TST_REF, true))
                        .updateVal(createInRefValNominalString(tExtRefMax));
                if (tExtRefMax.isSetSrcCBName()) {
                    findDataAdapterByName(DA_NAME_SET_TST_CB)
                            .orElse(addDAI(DA_NAME_SET_TST_CB, true))
                            .updateVal(createInRefValTestString(tExtRefMax));
                }
            }
        } else {
            return Optional.of(SclReportItem.warning(getXPath(), "The DOI %s can't be bound with an ExtRef".formatted(getXPath())));
        }

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
