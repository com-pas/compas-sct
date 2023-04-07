// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.CommonConstants;

import java.util.Map;

import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newVal;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.AbstractDAIAdapter AbstractDAIAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Principal functions</li>
 *    <ul>
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
     * Sets <em>ValImport</em> value
     *
     * @param b value
     */
    public void setValImport(boolean b) {
        currentElem.setValImport(b);
    }

    private boolean isDOModDAstVal() {
        if (parentAdapter.getCurrentElem() instanceof final TDOI tdoi) {
            return currentElem.getName().equals(CommonConstants.STVAL_DA_NAME) && tdoi.getName().equals(CommonConstants.MOD_DO_NAME);
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
     * Updates DAI SGroup value. This method checks if DAI is updatable.
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
            setSGroupVal(sGroup, val);
        } else {
            setVal(val);
        }
    }

    private void setSGroupVal(Long sGroup, String value) {
        currentElem.getVal().stream()
                .filter(tValElem -> tValElem.isSetSGroup() && sGroup.equals(tValElem.getSGroup()))
                .findFirst()
                .ifPresentOrElse(
                        tVal -> tVal.setValue(value),
                        () -> currentElem.getVal().add(newVal(value, sGroup)));
    }

    /**
     * Set Val (without sGroup) for DAI without checking if DAI is updatable
     * @param value new value
     */
    public void setVal(String value) {
        currentElem.getVal().stream().findFirst()
                .ifPresentOrElse(
                        tVal -> tVal.setValue(value),
                        () -> currentElem.getVal().add(newVal(value)));

    }

}
