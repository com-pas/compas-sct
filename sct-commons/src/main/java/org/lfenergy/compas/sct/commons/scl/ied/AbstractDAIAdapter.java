// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.CommonConstants;

import java.util.Map;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.AbstractDAIAdapter AbstractDAIAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link AbstractDAIAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
 *      <li>{@link AbstractDAIAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link AbstractDAIAdapter#addDAI(String) <em>Add <b>TDAI </b> under this object</em>}</li>
 *      <li>{@link AbstractDAIAdapter#addSDOI(String) <em>Add <b>TSDI </b> under this object</em>}</li>
 *      <li>{@link AbstractDAIAdapter#update(Long, String) <em>Update <b>TDAI</b> (sGroup, value)</em>}</li>
 *      <li>{@link AbstractDAIAdapter#update(Map) <em>Update Many <b>TDAI</b> (sGroup, value)</em>}</li>
 *      <li>{@link AbstractDAIAdapter#addPrivate(TPrivate) <em>Add <b>TPrivate </b> under this object</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDAI
 * @see <a href="https://github.com/com-pas/compas-sct/issues/70" target="_blank">Issue !70</a>
 */
public abstract class AbstractDAIAdapter<P extends SclElementAdapter> extends SclElementAdapter<P, TDAI> implements IDataAdapter {

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    protected AbstractDAIAdapter(P parentAdapter, TDAI currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Gets SDI from DAI by name
     *
     * @param sName SDI name
     * @param <S>   expected class type
     * @return <em>IDataAdapter</em> related object
     * @throws ScdException throws when specified SDI not present in DAI
     */
    public <S extends IDataAdapter> S getStructuredDataAdapterByName(String sName) throws ScdException {
        throw new UnsupportedOperationException("DAI doesn't have any SDI");
    }

    /**
     * Gets DataAdapter by DAI
     *
     * @param sName DAI name
     * @param <S>   expected class type
     * @return <em>IDataAdapter</em> related object
     * @throws ScdException throws when specified DAI unknown
     */
    public <S extends IDataAdapter> S getDataAdapterByName(String sName) throws ScdException {
        throw new UnsupportedOperationException("DAI doesn't have any DAI");
    }

    /**
     * Sets <em>ValImport</em> value
     *
     * @param b value
     */
    public void setValImport(boolean b) {
        currentElem.setValImport(b);
    }

    private boolean isDOModDAstVal() {
        if (parentAdapter.getCurrentElem() instanceof final TDOI tdoi) {
            return currentElem.getName().equals(CommonConstants.STVAL) && tdoi.getName().equals(CommonConstants.MOD_DO_NAME);
        }
        return false;
    }

    public AbstractDAIAdapter<? extends SclElementAdapter> update(Map<Long, String> daiValues) throws ScdException {
        if (daiValues.size() > 1 && daiValues.containsKey(0L)) {
            update(0L, daiValues.get(0L)); // to be refined (with COMPAS TEAMS)
        } else {
            for (Map.Entry<Long, String> mapVal : daiValues.entrySet()) {
                update(mapVal.getKey(), mapVal.getValue());
            }
        }
        return this;
    }

    /**
     * Updates DAI SGroup value
     *
     * @param sGroup SGroup to update
     * @param val    value
     * @throws ScdException throws when DAI for which SGroup should be updated is not updatable
     */
    public void update(Long sGroup, String val) throws ScdException {
        if (!isDOModDAstVal() && currentElem.isSetValImport() && !currentElem.isValImport()) {
            String msg = String.format(
                    "DAI(%s) cannot be updated : valImport(false) %s", currentElem.getName(), getXPath()
            );
            throw new ScdException(msg);
        }
        if (sGroup != null && sGroup != 0) {
            updateSGroupVal(sGroup, val);
        } else {
            updateVal(val);
        }
    }

    private void updateSGroupVal(Long sGroup, String val) {
        currentElem.getVal().stream()
                .filter(tValElem -> tValElem.isSetSGroup() && sGroup.equals(tValElem.getSGroup()))
                .findFirst()
                .orElseGet(
                        () -> {
                            TVal newTVal = new TVal();
                            newTVal.setSGroup(sGroup);
                            currentElem.getVal().add(newTVal);
                            return newTVal;
                        })
                .setValue(val);
    }

    public void updateVal(String s) {
        currentElem.getVal().stream().findFirst()
                .orElseGet(
                        () -> {
                            TVal newTVal = new TVal();
                            currentElem.getVal().add(newTVal);
                            return newTVal;
                        })
                .setValue(s);
    }

    public IDataAdapter addDAI(String name) {
        throw new UnsupportedOperationException("DAI cannot contain an SDI");
    }

    public IDataAdapter addSDOI(String sdoName) {
        throw new UnsupportedOperationException("DAI cannot contain an DAI");
    }
}
