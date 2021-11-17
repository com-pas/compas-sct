// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LNodeTypeAdapter
        extends SclElementAdapter<DataTypeTemplateAdapter, TLNodeType>
        implements IDTTComparable<TLNodeType> {

    public LNodeTypeAdapter(DataTypeTemplateAdapter parentAdapter, TLNodeType currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getLNodeType().contains(currentElem);
    }


    @Override
    public boolean hasSameContentAs(TLNodeType tlNodeType) {
        if(!DataTypeTemplateAdapter.hasSamePrivates(currentElem,tlNodeType)){
            return false;
        }

        if(Objects.equals(
                currentElem.getLnClass().toArray(new String[0]),
                tlNodeType.getLnClass().toArray(new String[0])
            ) || !Objects.equals(currentElem.getIedType(),tlNodeType.getIedType())){
            return false;
        }

        List<TDO> thisTDOs = currentElem.getDO();
        List<TDO> inTDOs = tlNodeType.getDO();
        if(thisTDOs.size() != inTDOs.size()) {
            return false;
        }
        for(int i = 0; i < inTDOs.size(); i++){
            // the order in which DOs appears matter
            TDO inTDO = inTDOs.get(i);
            TDO thisTDO = thisTDOs.get(i);
            if(!thisTDO.getType().equals(inTDO.getType())
                    || !thisTDO.getName().equals(inTDO.getName())
                    || thisTDO.isTransient() != inTDO.isTransient()
                    || !Objects.equals(thisTDO.getAccessControl(), inTDO.getAccessControl())){
                return false;
            }
        }
        return true;
    }

    public boolean containsDOWithDOTypeId(String doTypeId) {
        return currentElem.getDO().stream()
                .anyMatch(tdo -> tdo.getType().equals(doTypeId));
    }

    public String getLNClass() {
        if(!currentElem.getLnClass().isEmpty()){
            return currentElem.getLnClass().get(0);
        }
        return null;
    }

    public Optional<String> getD0TypeId(String doName){
        return currentElem.getDO()
                .stream()
                .filter(tdo -> doName.equals(tdo.getName()))
                .map(TDO::getType)
                .findFirst();
    }
}