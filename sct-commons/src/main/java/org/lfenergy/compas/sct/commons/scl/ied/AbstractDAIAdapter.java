// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
 *    <ul>
 *      <li>{@link AbstractDAIAdapter#isValImport <em>Check value Of <b>valImport </b> attribute</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDAI
 * @see <a href="https://github.com/com-pas/compas-sct/issues/70" target="_blank">Issue !70</a>
 */
public abstract class AbstractDAIAdapter<P extends SclElementAdapter> extends SclElementAdapter<P, TDAI> implements IDataAdapter{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    protected AbstractDAIAdapter(P parentAdapter, TDAI currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Gets SDI from DAI by name
     * @param sName SDI name
     * @return <em>IDataAdapter</em> related object
     * @param <S> expected class type
     * @throws ScdException throws when specified SDI not present in DAI
     */
    public <S extends IDataAdapter> S getStructuredDataAdapterByName(String sName) throws ScdException {
        throw new UnsupportedOperationException("DAI doesn't have any SDI");
    }

    /**
     * Gets DataAdapter by DAI
     * @param sName DAI name
     * @return <em>IDataAdapter</em> related object
     * @param <S> expected class type
     * @throws ScdException throws when specified DAI unknown
     */
    public  <S extends IDataAdapter> S getDataAdapterByName(String sName) throws ScdException {
        throw new UnsupportedOperationException("DAI doesn't have any DAI");
    }

    /**
     * Sets <em>ValImport</em> value
     * @param b value
     */
    public void setValImport(boolean b){
        currentElem.setValImport(b);
    }

    /**
     * Cheks <em>ValImport</em> boolean value
     * @return <em>Boolean</em> value of <em>ValImport</em> if define or <em>null</em>
     */
    public Boolean isValImport(){
        return currentElem.isSetValImport() ? currentElem.isValImport() : null;
    }

    public AbstractDAIAdapter<? extends SclElementAdapter> update(Map<Long, String> daiValues) throws ScdException {
        if(daiValues.size() > 1 && daiValues.containsKey(0L)){
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
     * @param sGroup SGroup to update
     * @param val value
     * @throws ScdException throws when DAI for which SGroup should be updated is not updatable
     */
    public void update(Long sGroup, String val) throws ScdException {
        if(currentElem.isSetValImport() && !currentElem.isValImport()){
            String msg = String.format(
                    "DAI(%s) cannot be updated : valImport(false)",currentElem.getName()
            );
            throw new ScdException(msg);
        }
        Stream<TVal> tValStream = currentElem.getVal().stream();
        if (sGroup != null && sGroup != 0) {
            Optional<TVal> tVal = tValStream.filter(tValElem -> tValElem.isSetSGroup() &&
                    sGroup.equals(tValElem.getSGroup()))
                    .findFirst();
            if(tVal.isPresent()){
                tVal.get().setValue(val);
            } else {
                TVal newTVal = new TVal();
                newTVal.setValue(val);
                newTVal.setSGroup(sGroup);
                currentElem.getVal().add(newTVal);
            }
        } else {
            Optional<TVal> tVal = tValStream.findFirst();
            if(tVal.isPresent()){
                tVal.get().setValue(val);
            }else {
                TVal newTVal = new TVal();
                newTVal.setValue(val);
                currentElem.getVal().add(newTVal);
            }
        }
    }

    public IDataAdapter addDAI(String name){
        throw new UnsupportedOperationException("DAI cannot contain an SDI");
    }

    public IDataAdapter addSDOI(String sdoName){
        throw new UnsupportedOperationException("DAI cannot contain an DAI");
    }
}
