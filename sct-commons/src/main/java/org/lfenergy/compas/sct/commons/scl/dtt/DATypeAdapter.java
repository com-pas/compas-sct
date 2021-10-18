package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TBDA;
import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TProtNs;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class DATypeAdapter
        extends SclElementAdapter<DataTypeTemplateAdapter, TDAType>
        implements IDTTComparable<TDAType>{
    public DATypeAdapter(DataTypeTemplateAdapter parentAdapter, TDAType currentElem) {
        super(parentAdapter, currentElem);
    }


    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDAType().contains(currentElem);
    }

    public List<BDAAdapter> getBdaAdapters(){
        return currentElem.getBDA()
                .stream()
                .map(tbda -> new BDAAdapter(this,tbda))
                .collect(Collectors.toList());
    }

    public boolean containsBDAWithEnumTypeID(String enumTypeId) {
        return currentElem.getBDA()
                .stream()
                .anyMatch(
                        bda -> TPredefinedBasicTypeEnum.ENUM.equals(bda.getBType()) &&
                                enumTypeId.equals(bda.getType())
                );
    }

    public Boolean containsStructBdaWithDATypeId(String daTypeId) {
        return currentElem.getBDA()
            .stream()
            .anyMatch(
                    bda -> bda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT) &&
                            daTypeId.equals(bda.getType())
            );
    }

    @Override
    public boolean hasSameContentAs(TDAType inputDAType) {
        if(!DataTypeTemplateAdapter.hasSamePrivates(currentElem,inputDAType) ||
                currentElem.getProtNs().size() != inputDAType.getProtNs().size() ||
                currentElem.getBDA().size() != inputDAType.getBDA().size()){
            return false;
        }
        List<TBDA> thisBDAs = currentElem.getBDA();
        List<TBDA> inputBDAs = inputDAType.getBDA();

        for(int i = 0; i < thisBDAs.size(); i++){
            // The order in which BDAs appear matters
            BDAAdapter bdaAdapter = new BDAAdapter(this,thisBDAs.get(i));
            if (!bdaAdapter.hasSameContentAs(inputBDAs.get(i))){
                return false;
            }
        }

        List<TProtNs> thisProtNs = currentElem.getProtNs();
        List<TProtNs> inputProtNs = inputDAType.getProtNs();
        for(int i = 0; i < thisProtNs.size(); i++){
            // The order in which ProtNs appear matters
            if(!Objects.equals(thisProtNs.get(i).getValue(),inputProtNs.get(i).getValue()) ||
                    !Objects.equals(thisProtNs.get(i).getType(),inputProtNs.get(i).getType())){
                return false;
            }
        }
        return true;
    }
}
