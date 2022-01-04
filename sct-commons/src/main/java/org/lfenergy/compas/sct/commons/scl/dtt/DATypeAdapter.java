// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TBDA;

import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TProtNs;

import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DATypeAdapter extends AbstractDataTypeAdapter<TDAType>{

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

    public Optional<TBDA> getBDAByName(String sdoName) {
        for(TBDA tbda : currentElem.getBDA()){
            if(tbda.getName().equals(sdoName)){
                return Optional.of(tbda);
            }
        }
        return Optional.empty();
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

    public void check(DaTypeName daTypeName) throws ScdException {
        int sz= daTypeName.getStructNames().size();
        String strBDAs = StringUtils.join(daTypeName.getStructNames());
        if(sz == 0)  return;
        DATypeAdapter daTypeAdapter = this;
        for (int i = 0; i < sz - 1; ++i) {
            String bdaName = daTypeName.getStructNames().get(i);
            daTypeAdapter = daTypeAdapter.getDATypeAdapterByBdaName(bdaName)
                    .orElseThrow(
                            () -> new ScdException(String.format("Invalid BDA(%s) in '%s'",bdaName,strBDAs  ))
                    );
        }
        String lastBda = daTypeName.getStructNames().get(sz - 1);
        BDAAdapter bdaAdapter = daTypeAdapter.getBdaAdapterByName(lastBda)
                .orElseThrow();
        if(!bdaAdapter.isTail()){
            throw new ScdException(
                    String.format("Last BDA(%s) in '%s' cannot be of type STRUCT", lastBda, strBDAs)
            );
        }
        bdaAdapter.check(daTypeName);
    }

    /**
     * return a list of completed Resumed Data Type Templates beginning from this DoType (Do or SDO).
     * Each Resumed Data Type Template is instantiated from a reference resumed Data Type.
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     * @param rootRDTT reference Resumed Data Type Template used to build the list
     * @param visitedBDA a cache to stored visited SDO
     * @param filter filter for DO/SDO and DA/BDA
     * @return list of completed Resumed Data Type Templates beginning from this DoType (Do or SDO).
     */
    public List<ResumedDataTemplate> getResumedDTTs(ResumedDataTemplate rootRDTT,
                                                    Set<String> visitedBDA, ResumedDataTemplate filter) {

        List<ResumedDataTemplate> resumedDataTemplates = new ArrayList<>();

        for(TBDA bda : currentElem.getBDA()){
            if(filter.isDaNameDefined() &&
                    !filter.getBdaNames().contains(bda.getName())){
                continue;
            }
            rootRDTT.setBType(bda.getBType().value());
            if(bda.getBType() == TPredefinedBasicTypeEnum.STRUCT) {
                if(visitedBDA.contains(bda.getType())) {
                    continue;
                }

                DATypeAdapter daTypeAdapter = parentAdapter.getDATypeAdapterById(bda.getType()).orElse(null);
                visitedBDA.add(bda.getType());
                rootRDTT.addStructName(bda.getName(),DaTypeName.class);
                if(daTypeAdapter != null){
                    List<ResumedDataTemplate> resumedDataTemplateList = daTypeAdapter.getResumedDTTs(
                            rootRDTT,visitedBDA,filter
                    );
                    resumedDataTemplates.addAll(resumedDataTemplateList);
                }
            } else {
                ResumedDataTemplate resumedDataTemplate = ResumedDataTemplate.copyFrom(rootRDTT);
                resumedDataTemplate.addStructName(bda.getName(),DaTypeName.class);
                resumedDataTemplate.setType(bda.getType());
                resumedDataTemplate.getDaName().setValImport(bda.isValImport());
                resumedDataTemplates.add(resumedDataTemplate);
            }
        }
        return resumedDataTemplates;
    }

    public Optional<DATypeAdapter> getDATypeAdapterByBdaName(String name)  {
        Optional<TBDA> opBda = getBDAByName(name);
        if(opBda.isPresent()){
            return parentAdapter.getDATypeAdapterById(opBda.get().getType());
        }
        return Optional.empty();
    }


    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter;
    }


    public Optional<BDAAdapter> getBdaAdapterByName(String name) {
        Optional<TBDA> opBda = getBDAByName(name);
        if(opBda.isPresent()){
            return Optional.of(new BDAAdapter(this,opBda.get()));
        }
        return Optional.empty();
    }

    @Getter
    public static class BDAAdapter extends AbstractDataAttributeAdapter<DATypeAdapter, TBDA>{

        protected BDAAdapter(DATypeAdapter parentAdapter, TBDA currentElem) {
            super(parentAdapter, currentElem);
        }

        @Override
        protected boolean amChildElementRef() {
            return parentAdapter.getCurrentElem().getBDA().contains(currentElem);
        }
    }
}
