// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.Utils.*;

/**
 * A representation of the model object
 * <em><b>{@link DataSetAdapter DataSetAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DataSetAdapter#findFCDA <em>look for a FCDA in this DataSet</em>}</li>
 *      <li>{@link DataSetAdapter#createFCDAIfNotExists <em>create a FCDA in this DataSet</em></li>
 *    </ul>
 * </ol>
 * <br/>
 *  <pre>
 *      <b>ObjectReference</b>: DataSet
 *      <b>LDName</b> = "name" key of the DataSet in the parent LN/LN0
 *  </pre>
 *
 * @see TLN0
 * @see AbstractLNAdapter
 */
public class DataSetAdapter extends SclElementAdapter<AbstractLNAdapter<? extends TAnyLN>, TDataSet> {

    /**
     * IEC 61850 requires FCDA to be oredred inside a DataSet.
     * This is the comparator to sort the FCDA inside a DataSet
     */
    private static final Comparator<TFCDA> fcdaComparator = Comparator
        .comparing(TFCDA::getLdInst, Utils::blanksFirstComparator)
        .thenComparing(TFCDA::getPrefix, Utils::blanksFirstComparator)
        .thenComparing(tfcda -> tfcda.isSetLnClass() ? tfcda.getLnClass().get(0) : null, Utils::blanksFirstComparator)
        .thenComparing(tfcda -> tfcda.getLnInst() != null ? Integer.valueOf(tfcda.getLnInst()) : null, Comparator.nullsFirst(Integer::compareTo))
        .thenComparing(TFCDA::getDoName, Utils::blanksFirstComparator)
        .thenComparing(TFCDA::getDaName, Utils::blanksFirstComparator);

    public DataSetAdapter(AbstractLNAdapter<? extends TAnyLN> parentAdapter, TDataSet dataSet) {
        super(parentAdapter, dataSet);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().isSetDataSet() && parentAdapter.getCurrentElem().getDataSet().stream().anyMatch(dataSet ->
            Objects.equals(currentElem.getName(), dataSet.getName()));
    }

    /**
     * Returns local XPath
     * @return XPath for current element (not including parent XPath)
     */
    @Override
    protected String elementXPath() {
        return String.format("DataSet[%s]", xpathAttributeFilter("name", currentElem.getName()));
    }

    /**
     * Find a FCDA matching all given criteria.
     * @param ldInst FCDA ldInst attribute
     * @param prefix FCDA prefix attribute
     * @param lnClass FCDA lnClass attribute
     * @param lnInst FCDA lnInst attribute
     * @param doName FCDA doName attribute
     * @param daName FCDA daNae attribute
     * @param fc FCDA fc attribute
     * @return Matching FCDA in this DataSet when found, empty Optional otherwise.
     */
    public Optional<TFCDA> findFCDA(String ldInst, String prefix, String lnClass, String lnInst, String doName, String daName, TFCEnum fc) {
        if (!currentElem.isSetFCDA()) {
            return Optional.empty();
        }
        return currentElem.getFCDA().stream()
            .filter(tfcda ->
                Objects.equals(ldInst, tfcda.getLdInst())
                    && equalsOrBothBlank(prefix, tfcda.getPrefix())
                    && Utils.lnClassEquals(tfcda.getLnClass(), lnClass)
                    && equalsOrBothBlank(lnInst, tfcda.getLnInst())
                    && Objects.equals(doName, tfcda.getDoName())
                    && Objects.equals(fc, tfcda.getFc())
                    && equalsOrBothBlank(daName, tfcda.getDaName()))
            .findFirst();
    }

    /**
     * Create a new FCDA in this DataSet.
     * Does nothing if a FCDA with the given attribute already exists in this DataSet.
     * @param ldInst FCDA ldInst attribute
     * @param prefix FCDA prefix attribute
     * @param lnClass FCDA lnClass attribute
     * @param lnInst FCDA lnInst attribute
     * @param doName FCDA doName attribute
     * @param daName FCDA daNae attribute
     * @param fc FCDA fc attribute
     * @return created FCDA, or existing FCDA with the given attributes
     */
    public TFCDA createFCDAIfNotExists(String ldInst, String prefix, String lnClass, String lnInst, String doName, String daName, TFCEnum fc) {
        Objects.requireNonNull(fc); // fc is required by XSD
        Optional<TFCDA> fcda = findFCDA(ldInst, prefix, lnClass, lnInst, doName, daName, fc);
        return fcda
            .orElseGet(() -> {
                TFCDA newFcda = new TFCDA();
                newFcda.setLdInst(nullIfBlank(ldInst));
                newFcda.setPrefix(nullIfBlank(prefix));
                if (StringUtils.isNotBlank(lnClass)) {
                    newFcda.getLnClass().add(lnClass);
                }
                newFcda.setLnInst(nullIfBlank(lnInst));
                newFcda.setDoName(nullIfBlank(doName));
                newFcda.setDaName(nullIfBlank(daName));
                newFcda.setFc(fc);
                currentElem.getFCDA().add(newFcda);
                currentElem.getFCDA().sort(fcdaComparator);
                return newFcda;
            });
    }

}
