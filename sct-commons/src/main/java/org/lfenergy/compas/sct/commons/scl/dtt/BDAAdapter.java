package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TBDA;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Objects;

@Slf4j
public class BDAAdapter extends SclElementAdapter<DATypeAdapter, TBDA> implements IDTTComparable<TBDA>{

    public BDAAdapter(DATypeAdapter parentAdapter, TBDA currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getBDA().contains(currentElem);
    }

    @Override
    public boolean hasSameContentAs(TBDA tbda) {
        if(!Objects.equals(currentElem.getName(),tbda.getName())
                || !Objects.equals(currentElem.getBType(),tbda.getBType())
                || !Objects.equals(currentElem.getType(),tbda.getType())
                || !Objects.equals(currentElem.getSAddr(), tbda.getSAddr())
                || !Objects.equals(currentElem.getValKind(), tbda.getValKind())
                || currentElem.isValImport() != tbda.isValImport()){
            return false;
        }

        if(!Objects.equals(currentElem.getCount(),tbda.getCount())){
            // Reset must be done here due to code generated by JAXB
            if(currentElem.getCount().isEmpty()){
                Utils.setField(currentElem,"count",null);
            }
            if(tbda.getCount().isEmpty()){
                Utils.setField(tbda,"count",null);
            }
            return false ;
        } else {
            // Reset must be done here due to code generated by JAXB
            if(currentElem.getCount().isEmpty()){
                Utils.setField(currentElem,"count",null);
            }
            if(tbda.getCount().isEmpty()){
                Utils.setField(tbda,"count",null);
            }
        }

        for(TVal prdVal : tbda.getVal()){
            boolean hasSameVal = currentElem.getVal().stream()
                    .anyMatch(rcvVal -> rcvVal.getValue().equals(prdVal.getValue()) &&
                            Objects.equals(rcvVal.getSGroup(), prdVal.getSGroup()));
            if(!hasSameVal) {
                return false;
            }
        }
        return true;
    }
}
