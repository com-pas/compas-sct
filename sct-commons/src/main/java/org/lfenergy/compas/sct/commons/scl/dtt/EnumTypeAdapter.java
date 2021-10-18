package org.lfenergy.compas.sct.commons.scl.dtt;


import org.lfenergy.compas.scl2007b4.model.TEnumType;
import org.lfenergy.compas.scl2007b4.model.TEnumVal;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.Objects;

public class EnumTypeAdapter
        extends SclElementAdapter<DataTypeTemplateAdapter, TEnumType>
        implements IDTTComparable<TEnumType>{

    public EnumTypeAdapter(DataTypeTemplateAdapter parentAdapter, TEnumType currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getEnumType().contains(currentElem);
    }

    public boolean hasSameContentAs(TEnumType tEnumType) {

        if(!DataTypeTemplateAdapter.hasSamePrivates(currentElem, tEnumType)) {
            return false;
        }

        List<TEnumVal> rcvEnumValList = currentElem.getEnumVal();
        List<TEnumVal> prdEnumValList = tEnumType.getEnumVal();
        if(rcvEnumValList.size() != prdEnumValList.size()) {
            return false;
        }
        for(TEnumVal tEnumVal : tEnumType.getEnumVal()){
            boolean hasSameVal = currentElem.getEnumVal().stream()
                    .anyMatch(rcvVal -> rcvVal.getValue().equals(tEnumVal.getValue()) &&
                            Objects.equals(rcvVal.getOrd(), tEnumVal.getOrd()));
            if(!hasSameVal) {
                return false;
            }
        }
        return true;
    }
}
