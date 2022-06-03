// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class DOTypeAdapter extends AbstractDataTypeAdapter<TDOType> {

    public DOTypeAdapter(DataTypeTemplateAdapter parentAdapter, TDOType currentElem) {
        super(parentAdapter, currentElem);
    }

    public List<ResumedDataTemplate> getResumedDTTByDoName(DoTypeName doTypeName, int idx, ResumedDataTemplate rDtt) {

        int sz = doTypeName.getStructNames().size();
        if(!doTypeName.isDefined()) {
            return new ArrayList<>();
        }
        List<ResumedDataTemplate> result = new ArrayList<>();
        if(sz - idx > 0){
            String sdoName = doTypeName.getStructNames().get(idx);
            TSDO tsdo = getSDOByName(sdoName).orElse(null);
            if(tsdo == null) {
                return new ArrayList<>();
            }
            ResumedDataTemplate copyRDtt = ResumedDataTemplate.copyFrom(rDtt);
            copyRDtt.getDoName().setCdc(getCdc());
            copyRDtt.getDoName().addStructName(sdoName);
            DOTypeAdapter doTypeAdapter = getDataTypeTemplateAdapter().getDOTypeAdapterById(tsdo.getType())
                    .orElseThrow();
            result.addAll( doTypeAdapter.getResumedDTTByDoName(doTypeName,idx+1,copyRDtt));

        } else {
            for(TDA tda : getSdoOrDAs(TDA.class)){
                ResumedDataTemplate copyRDtt = ResumedDataTemplate.copyFrom(rDtt);
                DAAdapter daAdapter = new DAAdapter(this,tda);
                copyRDtt.getDaName().setName(daAdapter.getName());
                copyRDtt.getDaName().setType(daAdapter.getType());
                copyRDtt.getDaName().setBType(daAdapter.getBType());
                copyRDtt.getDaName().setValImport(daAdapter.isValImport());
                if(!daAdapter.isTail()){
                    DATypeAdapter daTypeAdapter = daAdapter.getDATypeAdapter()
                            .orElseThrow(
                                    () -> new  AssertionError(
                                            String.format(
                                                    "TDA(%s) references unknown DAType id(%s)",
                                                    tda.getName(), tda.getType()
                                            )
                                    )
                            );
                    result.addAll( daTypeAdapter.completeResumedDTT(copyRDtt));
                } else {
                    copyRDtt.getDaName().addDaiValues(tda.getVal());
                    result.add(copyRDtt);
                }
            }
        }
        return result;
    }

    public Optional<ResumedDataTemplate> getResumedDTTByDaName(DaTypeName daTypeName,
                                                               ResumedDataTemplate rDtt) throws ScdException {
        if(!rDtt.getDoName().isDefined()) {
            return Optional.empty();
        }
        DoTypeName doTypeName = rDtt.getDoName();

        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass().equals(TSDO.class)) {
                TSDO sdo = (TSDO) tUnNaming;
                DOTypeAdapter doTypeAdapter = getDOTypeAdapterBySdoName(sdo.getName()).orElseThrow();
                doTypeName.addStructName(sdo.getName());
                doTypeName.setCdc(getCdc());
                Optional<ResumedDataTemplate> opRDtt = doTypeAdapter.getResumedDTTByDaName(daTypeName,rDtt);
                if (opRDtt.isPresent()) {
                    return opRDtt;
                }
            } else {
                TDA tda = (TDA)tUnNaming;
                rDtt.getDaName().setValImport(tda.isValImport());
                rDtt.getDaName().setFc(tda.getFc());
                rDtt.getDaName().setType(tda.getType());
                rDtt.getDaName().setBType(tda.getBType());
                rDtt.getDaName().setName(tda.getName());
                rDtt.setDaiValues(tda.getVal());
                if(tda.getBType() != TPredefinedBasicTypeEnum.STRUCT &&
                        tda.getName().equals(daTypeName.getName()) &&
                        daTypeName.getStructNames().isEmpty()  ){
                    return Optional.of(rDtt);
                }

                if(tda.getBType() == TPredefinedBasicTypeEnum.STRUCT &&
                        tda.getName().equals(daTypeName.getName()) &&
                        !daTypeName.getStructNames().isEmpty() ){
                    DAAdapter daAdapter = getDAAdapterByName(tda.getName())
                            .orElseThrow(() -> new AssertionError(""));

                    DATypeAdapter daTypeAdapter = daAdapter.getDATypeAdapter()
                            .orElseThrow(() -> new AssertionError(""));
                    Optional<ResumedDataTemplate> opRDtt = daTypeAdapter.getResumedDTTByDaName(daTypeName,0,rDtt);
                    if (opRDtt.isPresent()) {
                        return opRDtt;
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDOType().contains(currentElem);
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    public boolean containsDAWithDAName(String da){
        return currentElem.getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class))
                .map(TDA.class::cast)
                .anyMatch(
                    tda -> tda.getName().equals(da)
                );
    }
    public boolean containsDAWithEnumTypeId(String enumTypeId) {
        return currentElem.getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class))
                .map(TDA.class::cast)
                .anyMatch(
                        tda -> tda.getBType().equals(TPredefinedBasicTypeEnum.ENUM) &&
                                tda.getType().equals(enumTypeId)
                );
    }

    /**
     * Find path from a DoType to DA (defined by name)
     * @param daName DA for which to find a path
     * @return pair of DO/SDO and DoType. DO/SDO references the DOType
     * @throws ScdException when inconsistency are found in th SCL's
     *                     DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
    Pair<String,DOTypeAdapter> findPathDoType2DA(String daName) throws ScdException {
        if(containsDAWithDAName(daName)){
            // Attention : Do this check before calling this function
            // It is not interesting to no have the DO/SDO that references this DoType
            return Pair.of("",this);
        }
        DOTypeAdapter doTypeAdapter = this;
        List<TSDO> sdoTypes = doTypeAdapter.getSdoOrDAs(TSDO.class);

        Queue<TSDO> doTypeIdQueue = new LinkedList<>();
        doTypeIdQueue.addAll(sdoTypes);
        TSDO currSDO;
        while( (currSDO = doTypeIdQueue.poll()) != null){
            doTypeAdapter = parentAdapter.getDOTypeAdapterById(currSDO.getType()).orElse(null);
            if(doTypeAdapter != null && doTypeAdapter.containsDAWithDAName(daName)){
                doTypeIdQueue.clear();
                break;
            }
            if(doTypeAdapter != null) {
                // add all SDO
                doTypeIdQueue.addAll(doTypeAdapter.getSdoOrDAs(TSDO.class));
            }
        }
        if(currSDO == null || doTypeAdapter == null){
            throw new ScdException(
                    String.format("No coherence or path between DOType(%s) and DA(%s)", currentElem.getId(),daName)
            );
        }
        return Pair.of(currSDO.getName(),doTypeAdapter);
    }


    Pair<String,DOTypeAdapter> findPathSDO2DA(String sdoName, String daName) throws ScdException {
        String errMsg = String.format("No coherence or path between DO/SDO(%s) and DA(%s)", sdoName,daName);
        Optional<TSDO> opSdo = getSDOByName(sdoName);
        if(opSdo.isEmpty()) {
            throw new ScdException(errMsg);
        }

        DOTypeAdapter doTypeAdapter = parentAdapter.getDOTypeAdapterById(opSdo.get().getType()).orElse(null);
        if(doTypeAdapter == null) {
            throw new ScdException(errMsg);
        }
        if(doTypeAdapter.containsDAWithDAName(daName)){
            return Pair.of(opSdo.get().getName(),doTypeAdapter);
        }
        return doTypeAdapter.findPathDoType2DA(daName);
    }

    public boolean containsDAStructWithDATypeId(String daTypeId) {
        return currentElem.getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class))
                .map(TDA.class::cast)
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
        DAAdapter daAdapter = new DAAdapter(this,thisTDA);
        return daAdapter.hasSameContentAs(inTDA);
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

    public Optional<Pair<String,DOTypeAdapter>> checkAndCompleteStructData(DoTypeName doTypeName) throws ScdException {
        int sz = doTypeName.getStructNames().size();
        if(sz == 0){
            return Optional.empty();
        }

        DOTypeAdapter doTypeAdapter = this;
        for(int i = 0; i < sz; ++i){
            String sdoName = doTypeName.getStructNames().get(i);
            doTypeAdapter = doTypeAdapter.getDOTypeAdapterBySdoName(sdoName)
                    .orElseThrow(
                            () -> new ScdException(
                                    String.format(
                                            "Invalid SDO(%s) in the SDO's chain (%s)", sdoName,doTypeName
                                    )
                            )
                    );
            doTypeName.setCdc(doTypeAdapter.getCdc());
        }
        return Optional.of(Pair.of(doTypeName.getStructNames().get(sz-1),doTypeAdapter));
    }

    public Optional<TSDO> getSDOByName(String sdoName) {
        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TSDO.class && ((TSDO)tUnNaming).getName().equals(sdoName)){
                return Optional.of((TSDO)tUnNaming);
            }
        }
        return Optional.empty();
    }

    public Optional<TDA> getDAByName(String name) {
        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TDA.class && ((TDA)tUnNaming).getName().equals(name)){
                return Optional.of((TDA)tUnNaming);
            }
        }
        return Optional.empty();
    }

    public TPredefinedCDCEnum getCdc() {
        return currentElem.getCdc();
    }

    /**
     * return a list of Resumed Data Type Templates beginning from this DoType (Do or SDO).
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     * @param rootRDTT reference Resumed Data Type Template used to build the list
     * @param filter filter for DO/SDO and DA/BDA
     * @return list of Resumed Data Type Templates beginning from this DoType (Do or SDO)
     */
    public List<ResumedDataTemplate> getResumedDTTs(ResumedDataTemplate rootRDTT, ResumedDataTemplate filter) {
        List<ResumedDataTemplate> resultRDTTs = new ArrayList<>();
        for(TUnNaming tUnNaming: currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TDA.class){
                TDA tda = (TDA)tUnNaming;
                resultRDTTs.addAll(getResumedDTTsOfDA(rootRDTT, filter, tda));
            } else {
                TSDO tsdo = (TSDO)tUnNaming;
                if(excludedByFilter(filter, tsdo)){
                    continue;
                }
                ResumedDataTemplate currentRDTT = ResumedDataTemplate.copyFrom(rootRDTT);
                currentRDTT.addDoStructName(tsdo.getName());
                parentAdapter.getDOTypeAdapterById(tsdo.getType()).ifPresent(
                    doTypeAdapter ->
                        resultRDTTs.addAll(doTypeAdapter.getResumedDTTs(currentRDTT, filter)));
            }
        }
        return resultRDTTs;
    }

    private List<ResumedDataTemplate> getResumedDTTsOfDA(ResumedDataTemplate rootRDTT, ResumedDataTemplate filter, TDA da){
        if(excludedByFilter(filter, da)){
            return Collections.emptyList();
        }
        ResumedDataTemplate currentRDTT = ResumedDataTemplate.copyFrom(rootRDTT);
        currentRDTT.getDaName().setName(da.getName());
        currentRDTT.getDaName().setFc(da.getFc());
        currentRDTT.getDaName().setBType(da.getBType());
        if(da.getBType() == TPredefinedBasicTypeEnum.STRUCT){
            return parentAdapter.getDATypeAdapterById(da.getType())
                .map(daTypeAdapter -> daTypeAdapter.getResumedDTTs(currentRDTT, filter))
                .orElse(Collections.emptyList());
        } else {
            currentRDTT.getDaName().setType(da.getType());
            currentRDTT.getDaName().setValImport(da.isValImport());
            currentRDTT.setDaiValues(da.getVal());
            return List.of(currentRDTT);
        }
    }

    private boolean excludedByFilter(ResumedDataTemplate filter, TDA da) {
        return filter != null && filter.isDaNameDefined() &&
            !filter.getDaName().getName().equals(da.getName());
    }

    private boolean excludedByFilter(ResumedDataTemplate filter, TSDO tsdo) {
        return filter != null &&
            !filter.getSdoNames().isEmpty() &&
            !filter.getSdoNames().contains(tsdo.getName());
    }

    public Optional<DOTypeAdapter> getDOTypeAdapterBySdoName(String name) {
        Optional<TSDO> opSdo = getSDOByName(name);
        if(!opSdo.isPresent()){
            return Optional.empty();
        }
       return parentAdapter.getDOTypeAdapterById(opSdo.get().getType());
    }

    public Optional<DAAdapter> getDAAdapterByName(String name){
        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TDA.class && ((TDA)tUnNaming).getName().equals(name)){
                return Optional.of(new DAAdapter(this,(TDA)tUnNaming));
            }
        }
        return  Optional.empty();
    }

    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter;
    }
    public <T extends TUnNaming> List<T > getSdoOrDAs(Class<T> cls) {
        return currentElem.getSDOOrDA()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(cls))
                .map(cls::cast)
                .collect(Collectors.toList());
    }


}
