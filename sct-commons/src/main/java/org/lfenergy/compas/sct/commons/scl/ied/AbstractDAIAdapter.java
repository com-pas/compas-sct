// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractDAIAdapter<P extends SclElementAdapter> extends SclElementAdapter<P, TDAI> implements IDataAdapter{

    protected AbstractDAIAdapter(P parentAdapter, TDAI currentElem) {
        super(parentAdapter, currentElem);
    }

    public <S extends IDataAdapter> S getStructuredDataAdapterByName(String sName) throws ScdException {
        throw new UnsupportedOperationException("DAI doesn't have any SDI");
    }

    public  <S extends IDataAdapter> S getDataAdapterByName(String sName) throws ScdException {
        throw new UnsupportedOperationException("DAI doesn't have any DAI");
    }

    public void setValImport(boolean b){
        currentElem.setValImport(b);
    }
    public Boolean isValImport(){
        return currentElem.isValImport();
    }

    public AbstractDAIAdapter update(Map<Long, String> daiValues) throws ScdException {
        if(daiValues.size() > 1 && daiValues.containsKey(0L)){
            update(0L, daiValues.get(0L)); // to be refined (with COMPAS TEAMS)
        } else {
            for (Map.Entry<Long, String> mapVal : daiValues.entrySet()) {
                update(mapVal.getKey(), mapVal.getValue());
            }
        }
        return this;
    }

    public boolean matches(ObjectReference dataAttributes){
        return false;
    }

    public void update(Long sGroup, String val) throws ScdException {
        if(currentElem.isValImport() != null && !currentElem.isValImport().booleanValue()){
            String msg = String.format(
                    "DAI(%s) cannot be updated : valImport(false)",currentElem.getName()
            );
            throw new ScdException(msg);
        }
        Stream<TVal> tValStream = currentElem.getVal().stream() ;
        if(sGroup != 0){
            Optional<TVal> tVal = tValStream.filter(tValElem -> tValElem.getSGroup() != null &&
                    tValElem.getSGroup().equals(sGroup))
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
