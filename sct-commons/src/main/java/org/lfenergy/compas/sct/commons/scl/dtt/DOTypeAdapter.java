// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
public class DOTypeAdapter
        extends SclElementAdapter<DataTypeTemplateAdapter, TDOType>
        implements IDTTComparable<TDOType> {

    public DOTypeAdapter(DataTypeTemplateAdapter parentAdapter, TDOType currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDOType().contains(currentElem);
    }

    public boolean containsDAWithEnumTypeId(String enumTypeId) {
        return currentElem.getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class))
                .map(tUnNaming -> (TDA)tUnNaming)
                .anyMatch(
                        tda -> tda.getBType().equals(TPredefinedBasicTypeEnum.ENUM) &&
                                tda.getType().equals(enumTypeId)
                );
    }

    public boolean containsDAStructWithDATypeId(String daTypeId) {
        return currentElem.getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class))
                .map(tUnNaming -> (TDA)tUnNaming)
                .anyMatch(
                        tda -> TPredefinedBasicTypeEnum.STRUCT.equals(tda.getBType()) &&
                                tda.getType().equals(daTypeId)
                );
    }

    @Override
    public boolean hasSameContentAs(TDOType tdoType) {
        if(!DataTypeTemplateAdapter.hasSamePrivates(currentElem,tdoType)){
            return false;
        }

        if(!currentElem.getCdc().equals(tdoType.getCdc())
                || !currentElem.getIedType().equals(tdoType.getIedType())){
            return false;
        }

        if(currentElem.getSDOOrDA().size() != tdoType.getSDOOrDA().size() ) {
            return false;
        }

        for(int i = 0; i < tdoType.getSDOOrDA().size(); i++){
            TUnNaming inSdo = tdoType.getSDOOrDA().get(i);
            TUnNaming  thisSdo = currentElem.getSDOOrDA().get(i);
            if(inSdo.getClass() != thisSdo.getClass()) return false;
            boolean hasSameContent;
            if(inSdo.getClass() == TSDO.class) {
                hasSameContent = this.hasSameContent((TSDO) thisSdo, (TSDO) inSdo);
            } else {
                hasSameContent = this.hasSameContent((TDA) thisSdo, (TDA) inSdo);
            }
            if(!hasSameContent) return false;
        }
        return true;

    }

    protected boolean hasSameContent(TSDO thisSdo, TSDO inSdo) {
        return Objects.equals(thisSdo.getName(),inSdo.getName())
                && Objects.equals(thisSdo.getType(),inSdo.getType())
                && Objects.equals(thisSdo.getCount(),inSdo.getCount());

    }

    protected boolean hasSameContent(TDA thisTDA, TDA inTDA) {
        if(!Objects.equals(thisTDA.getName(),inTDA.getName())
                || !Objects.equals(thisTDA.getBType(),inTDA.getBType())
                || !Objects.equals(thisTDA.getType(),inTDA.getType())
                || !Objects.equals(thisTDA.getFc(),inTDA.getFc())
                || !Objects.equals(thisTDA.getSAddr(), inTDA.getSAddr())
                || !Objects.equals(thisTDA.getValKind(), inTDA.getValKind())
                || thisTDA.isDchg() != inTDA.isDchg()
                || thisTDA.isDupd() != inTDA.isDupd()
                || thisTDA.isQchg() != inTDA.isQchg()
                || thisTDA.isValImport() != inTDA.isValImport()){
            return false;
        }
        if(!Objects.equals(thisTDA.getCount(),inTDA.getCount())){
            if(thisTDA.getCount().isEmpty()){
                Utils.setField(thisTDA,"count",null);
            }
            if(inTDA.getCount().isEmpty()){
                Utils.setField(inTDA,"count",null);
            }
            return false ;
        } else if(thisTDA.getCount().isEmpty()){
            Utils.setField(thisTDA,"count",null);
            Utils.setField(inTDA,"count",null);
        }

        if((thisTDA.getBType() == TPredefinedBasicTypeEnum.ENUM ||
                thisTDA.getBType() == TPredefinedBasicTypeEnum.STRUCT)
                && !Objects.equals(thisTDA.getType(),inTDA.getType())) {
            return false;
        }

        for(TVal prdVal : inTDA.getVal()){
            boolean hasSameVal = thisTDA.getVal().stream()
                    .anyMatch(rcvVal -> rcvVal.getValue().equals(prdVal.getValue()) &&
                            Objects.equals(rcvVal.getSGroup(), prdVal.getSGroup()));
            if(!hasSameVal) {
                return false;
            }
        }

        for(TProtNs prdProtNs : inTDA.getProtNs()){
            boolean hasSameVal = thisTDA.getProtNs().stream()
                    .anyMatch(rcvProtNs -> rcvProtNs.getValue().equals(prdProtNs.getValue()) &&
                            Objects.equals(rcvProtNs.getType(), prdProtNs.getType()));
            if(!hasSameVal) {
                return false;
            }
        }

        return true;
    }

    public boolean containsSDOWithDOTypeId(String doTypeId) {
        List<TSDO> sdoList = new ArrayList<>();
        for( TUnNaming unNaming : currentElem.getSDOOrDA()){
            if(unNaming.getClass() == TSDO.class) {
                sdoList.add((TSDO)unNaming);
            }
        }
        return sdoList.stream().anyMatch(sdo -> sdo.getType().equals(doTypeId));
    }
}
