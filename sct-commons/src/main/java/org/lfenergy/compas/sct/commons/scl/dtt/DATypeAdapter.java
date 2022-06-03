// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DATypeAdapter extends AbstractDataTypeAdapter<TDAType>{

    public DATypeAdapter(DataTypeTemplateAdapter parentAdapter, TDAType currentElem) {
        super(parentAdapter, currentElem);
    }

    public List<ResumedDataTemplate> completeResumedDTT(ResumedDataTemplate rDtt) {
        List<ResumedDataTemplate> result = new ArrayList<>();
        for(BDAAdapter bdaAdapter : getBdaAdapters()){
            ResumedDataTemplate copyRDtt = ResumedDataTemplate.copyFrom(rDtt);
            copyRDtt.getDaName().addStructName(bdaAdapter.getName());
            copyRDtt.getDaName().setType(bdaAdapter.getType());
            copyRDtt.getDaName().setBType(bdaAdapter.getBType());
            copyRDtt.getDaName().setValImport(bdaAdapter.isValImport());
            if(bdaAdapter.isTail()){
                copyRDtt.getDaName().addDaiValues(bdaAdapter.getCurrentElem().getVal());
            } else {
                DATypeAdapter daTypeAdapter = bdaAdapter.getDATypeAdapter()
                        .orElseThrow(
                                () -> new AssertionError(
                                        String.format(
                                                "BDA(%s) references unknown DAType id(%s)",
                                                bdaAdapter.getName(),bdaAdapter.getType()
                                        )
                                )
                        );
                result.addAll(daTypeAdapter.completeResumedDTT(copyRDtt));
            }
            result.add(copyRDtt);
        }

        return result;
    }

    /**
     * complete the input resumed data type template from DataTypeTemplate filtered out by the given DaTypeName
     * @param daTypeName the Data attributes, eventually with BDAs
     * @param idx index of the BDAs list in the given DaTypeName
     * @param rDtt Resumed Data Template to complete
     * @return completed Resumed Data Template or null if the filter constrains are not met
     * @throws ScdException if last BDA is of type STRUCT or intermediate BDA is not of type STRUCT
     */
    public Optional<ResumedDataTemplate> getResumedDTTByDaName(DaTypeName daTypeName,int idx,ResumedDataTemplate rDtt) throws ScdException {
        int sz= daTypeName.getStructNames().size();
        String strBDAs = StringUtils.join(daTypeName.getStructNames());
        if(sz - idx <= 0)  {
            return Optional.of(rDtt);
        }
        DaTypeName typeName = rDtt.getDaName();
        DATypeAdapter daTypeAdapter = this;

        String bdaName = daTypeName.getStructNames().get(idx);
        BDAAdapter bdaAdapter = getBdaAdapterByName(bdaName).orElse(null);

        if(bdaAdapter == null) {
            return Optional.empty();
        }
        typeName.setValImport(bdaAdapter.isValImport());
        typeName.setType(bdaAdapter.getType());
        typeName.setBType(bdaAdapter.getBType());
        typeName.getStructNames().add(bdaName);
        if( idx == sz - 1 ){
            if(!bdaAdapter.isTail()) {
                throw new ScdException(
                        String.format("Last BDA(%s) in '%s' cannot be of type STRUCT", bdaName, strBDAs)
                );
            }
            return Optional.of(rDtt);
        }
        daTypeAdapter = daTypeAdapter.getDATypeAdapterByBdaName(bdaName)
                .orElseThrow(
                        () -> new ScdException(String.format("Invalid BDA(%s) in '%s'",bdaName,strBDAs  ))
                );

        Optional<ResumedDataTemplate> opRDtt = daTypeAdapter.getResumedDTTByDaName(daTypeName,idx+1,rDtt);
        if(opRDtt.isPresent()){
            return opRDtt;
        }

        return Optional.empty();
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
            if(filter != null && filter.isDaNameDefined() &&
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
                resumedDataTemplate.getDaName().addDaiValues(bda.getVal());
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

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
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

        @Override
        protected void addPrivate(TPrivate tPrivate) {
            currentElem.getPrivate().add(tPrivate);
        }
    }
}
