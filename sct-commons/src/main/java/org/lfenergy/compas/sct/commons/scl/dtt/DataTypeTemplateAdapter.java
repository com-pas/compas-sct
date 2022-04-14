// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class DataTypeTemplateAdapter extends SclElementAdapter<SclRootAdapter, TDataTypeTemplates> {


    public DataTypeTemplateAdapter(SclRootAdapter parentAdapter, TDataTypeTemplates dataTypeTemplate) {
        super(parentAdapter,dataTypeTemplate);
    }

    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getDataTypeTemplates();
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        throw new IllegalArgumentException("Private is not Allowed here");
    }

    public Optional<LNodeTypeAdapter> getLNodeTypeAdapterById(String id) {
        for(TLNodeType tlNodeType : currentElem.getLNodeType()){
            if(tlNodeType.getId().equals(id)) {
                return Optional.of(new LNodeTypeAdapter(this, tlNodeType));
            }
        }
       return Optional.empty();

    }

    public List<LNodeTypeAdapter> getLNodeTypeAdapters(){

        return currentElem.getLNodeType()
                .stream()
                .map(tlNodeType -> new LNodeTypeAdapter(this,tlNodeType))
                .collect(Collectors.toList());
    }

    public Optional<DOTypeAdapter> getDOTypeAdapterById(String id)  {
        for(TDOType tdoType : currentElem.getDOType()){
            if(tdoType.getId().equals(id)) {
                return Optional.of(new DOTypeAdapter(this, tdoType));
            }
        }
        return Optional.empty();
    }

    public List<DOTypeAdapter> getDOTypeAdapters(){
        return currentElem.getDOType()
                .stream()
                .map(tdoType -> new DOTypeAdapter(this,tdoType))
                .collect(Collectors.toList());
    }

    public Optional<DATypeAdapter> getDATypeAdapterById(String id) {
        for(TDAType tdaType : currentElem.getDAType()){
            if(tdaType.getId().equals(id)) {
                return Optional.of(new DATypeAdapter(this,tdaType));
            }
        }
        return Optional.empty();
    }

    public List<DATypeAdapter> getDATypeAdapters(){
        return currentElem.getDAType()
                .stream()
                .map(tdaType -> new DATypeAdapter(this,tdaType))
                .collect(Collectors.toList());
    }

    public Optional<EnumTypeAdapter> getEnumTypeAdapterById(String id)  {
        for(TEnumType tEnumType : currentElem.getEnumType()){
            if(tEnumType.getId().equals(id)) {
                return Optional.of(new EnumTypeAdapter(this, tEnumType));
            }
        }
        return Optional.empty();
    }
    public List<EnumTypeAdapter> getEnumTypeAdapters(){
        return currentElem.getEnumType()
                .stream()
                .map(enumType -> new EnumTypeAdapter(this,enumType))
                .collect(Collectors.toList());
    }

    /**
     * import enum type from this DTT adapter from provider DTT adapter
     * @param prvDttAdapter Adapter of the Data Type template that provides its DTT
     * @return map of (old enumId, new enumId)
     */
    public void importEnumType(String thisIEDName, DataTypeTemplateAdapter prvDttAdapter){

        Map<String,String> pairOldAndNewEnumId = new HashMap<>();
        List<EnumTypeAdapter> prvEnumTypeAdapters = prvDttAdapter.getEnumTypeAdapters();

        for(EnumTypeAdapter prvEnumTypeAdapter : prvEnumTypeAdapters){

            TEnumType prvEnumType = prvEnumTypeAdapter.getCurrentElem();
            String oldEnumId = prvEnumType.getId();
            String newEnumId = prvEnumType.getId();
            Optional<EnumTypeAdapter> opRcvEnumTypeAdapter = this.getEnumTypeAdapterById(oldEnumId);

            boolean isImportable = false;
            if(!opRcvEnumTypeAdapter.isPresent() || !opRcvEnumTypeAdapter.get().hasSameContentAs(prvEnumType)) {
                isImportable = true;
            }

            if(isImportable && opRcvEnumTypeAdapter.isPresent()){
                // same ID, different content
                // rename enumType Id
                newEnumId = generateDttId(thisIEDName,prvEnumType.getId());
                prvEnumType.setId(newEnumId);
            }

            if(isImportable) {
                //import this enumType
                currentElem.getEnumType().add(prvEnumType);
                if(!Objects.equals(oldEnumId,newEnumId)) {
                    pairOldAndNewEnumId.put(oldEnumId,newEnumId);
                }
            }
        }

        // escalate on this DTT
        pairOldAndNewEnumId.forEach((oldId, newId) -> {
            List<DATypeAdapter> daTypeAdapters = prvDttAdapter.findDATypesWhichBdaContainsEnumTypeId(oldId);
            var bdas = daTypeAdapters.stream()
                    .map(DATypeAdapter::getBdaAdapters)
                    .flatMap(Collection::stream)
                    .map(DATypeAdapter.BDAAdapter::getCurrentElem)
                    .filter(bda -> TPredefinedBasicTypeEnum.ENUM == bda.getBType()
                            && Objects.equals(bda.getType(),oldId))
                    .collect(Collectors.toList());
            bdas.forEach(tbda -> tbda.setType(newId));
            List<DOTypeAdapter> doTypeAdapters = prvDttAdapter.findDOTypesWhichDAContainsEnumTypeId(oldId);
            var tdas = doTypeAdapters.stream()
                    .map(doTypeAdapter -> retrieveSdoOrDA(doTypeAdapter.getCurrentElem().getSDOOrDA(), TDA.class))
                    .flatMap(Collection::stream)
                    .filter(tda -> TPredefinedBasicTypeEnum.ENUM == tda.getBType()
                            && Objects.equals(tda.getType(),oldId))
                    .collect(Collectors.toList());
            tdas.forEach(tda -> tda.setType(newId));
        });
    }

    public Map<String,String> importDTT(String thisIEDName, DataTypeTemplateAdapter rcvDttAdapter) {

        this.importEnumType(thisIEDName,rcvDttAdapter);

        this.importDAType(thisIEDName,rcvDttAdapter);

        this.importDOType(thisIEDName,rcvDttAdapter);

        return importLNodeType(thisIEDName,rcvDttAdapter);
    }

    protected Map<String, String> importLNodeType(String thisIEDName, DataTypeTemplateAdapter prvDttAdapter) {
        Map<String,String> pairOldAndNewId = new HashMap<>();
        List<LNodeTypeAdapter> prvLNodeTypeAdapters = prvDttAdapter.getLNodeTypeAdapters();
        for(LNodeTypeAdapter prvLNodeTypeAdapter : prvLNodeTypeAdapters){

            TLNodeType prvLNodeType = prvLNodeTypeAdapter.getCurrentElem();
            String oldId = prvLNodeType.getId();
            String newId = prvLNodeType.getId();
            Optional<LNodeTypeAdapter> opRcvLNodeTypeAdapter = this.getLNodeTypeAdapterById(oldId);

            boolean isImportable = false;
            if(!opRcvLNodeTypeAdapter.isPresent() || !opRcvLNodeTypeAdapter.get().hasSameContentAs(prvLNodeType)) {
                isImportable = true;
            }

            if(isImportable && opRcvLNodeTypeAdapter.isPresent()){
                // same ID, different content
                // rename enumType Id
                newId = generateDttId(thisIEDName,prvLNodeType.getId());
                prvLNodeType.setId(newId);
            }

            if(isImportable) {
                //import this enumType
                currentElem.getLNodeType().add(prvLNodeType);
                if(!Objects.equals(oldId,newId)) {
                    pairOldAndNewId.put(oldId,newId);
                }
            }
        }
        return pairOldAndNewId;
    }

    protected void importDOType(String thisIEDName, DataTypeTemplateAdapter prvDttAdapter) {
        Map<String,String> pairOldAndNewDOTyYpeId = new HashMap<>();
        List<DOTypeAdapter> prvDOTypeAdapters = prvDttAdapter.getDOTypeAdapters();

        for(DOTypeAdapter prvDOTypeAdapter : prvDOTypeAdapters){

            TDOType prvDOType = prvDOTypeAdapter.getCurrentElem();
            String oldId = prvDOType.getId();
            String newId = prvDOType.getId();
            Optional<DOTypeAdapter> opRcvDOTypeAdapter = this.getDOTypeAdapterById(oldId);

            boolean isImportable = false;
            if(!opRcvDOTypeAdapter.isPresent() || !opRcvDOTypeAdapter.get().hasSameContentAs(prvDOType)) {
                isImportable = true;
            }

            if(isImportable && opRcvDOTypeAdapter.isPresent()){
                // same ID, different content
                // rename enumType Id
                newId = generateDttId(thisIEDName,prvDOType.getId());
                prvDOType.setId(newId);
            }

            if(isImportable) {
                //import this enumType
                currentElem.getDOType().add(prvDOType);
                if(!Objects.equals(oldId,newId)) {
                    pairOldAndNewDOTyYpeId.put(oldId,newId);
                }
            }
        }

        // escalate on provider DTT
        pairOldAndNewDOTyYpeId.forEach((oldId, newId) -> {
            List<DOTypeAdapter> doTypeAdapters = prvDttAdapter.findDOTypesFromSDOWithDOTypeId(oldId);
            var tsdos = doTypeAdapters.stream()
                    .map(doTypeAdapter -> retrieveSdoOrDA(doTypeAdapter.getCurrentElem().getSDOOrDA(),TSDO.class) )
                    .flatMap(Collection::stream)
                    .filter(bda ->  Objects.equals(bda.getType(),oldId))
                    .collect(Collectors.toList());
            tsdos.forEach(tbda -> tbda.setType(newId));
            List<LNodeTypeAdapter> lNodeTypeAdapters = prvDttAdapter.findLNodeTypesFromDoWithDoTypeId(oldId);
            var tdos = lNodeTypeAdapters.stream()
                    .map(lNodeTypeAdapter ->  lNodeTypeAdapter.getCurrentElem().getDO())
                    .flatMap(Collection::stream)
                    .filter(tda -> Objects.equals(tda.getType(),oldId))
                    .collect(Collectors.toList());
            tdos.forEach(tda -> tda.setType(newId));
        });
    }

    protected void importDAType(String thisIEDName, DataTypeTemplateAdapter prvDttAdapter) {

        Map<String,String> pairOldAndNewEnumId = new HashMap<>();
        List<DATypeAdapter> prvDATypeAdapters = prvDttAdapter.getDATypeAdapters();

        for(DATypeAdapter prvDATypeAdapter : prvDATypeAdapters){

            TDAType prvDAType = prvDATypeAdapter.getCurrentElem();
            String oldId = prvDAType.getId();
            String newId = prvDAType.getId();
            Optional<DATypeAdapter> opRcvDATypeAdapter = this.getDATypeAdapterById(oldId);

            boolean isImportable = false;
            if(!opRcvDATypeAdapter.isPresent() || !opRcvDATypeAdapter.get().hasSameContentAs(prvDAType)) {
                isImportable = true;
            }

            if(isImportable && opRcvDATypeAdapter.isPresent()){
                // same ID, different content
                // rename enumType Id
                newId = generateDttId(thisIEDName,prvDAType.getId());
                prvDAType.setId(newId);
            }

            if(isImportable) {
                //import this enumType
                currentElem.getDAType().add(prvDAType);
                if(!Objects.equals(oldId,newId)) {
                    pairOldAndNewEnumId.put(oldId,newId);
                }
            }
        }

        // escalate on this DTT
        pairOldAndNewEnumId.forEach((oldId, newId) -> {
            List<DATypeAdapter> daTypeAdapters = prvDttAdapter.findDATypesFromStructBdaWithDATypeId(oldId);
            var tbdas = daTypeAdapters.stream()
                    .map(DATypeAdapter::getBdaAdapters)
                    .flatMap(Collection::stream)
                    .map(DATypeAdapter.BDAAdapter::getCurrentElem)
                    .filter(bda -> TPredefinedBasicTypeEnum.STRUCT == bda.getBType()
                            && Objects.equals(bda.getType(),oldId))
                    .collect(Collectors.toList());
            tbdas.forEach(tbda -> tbda.setType(newId));
            List<DOTypeAdapter> doTypeAdapters = prvDttAdapter.findDOTypesWhichDAContainsStructWithDATypeId(oldId);
            var tdas = doTypeAdapters.stream()
                    .map(doTypeAdapter -> retrieveSdoOrDA(doTypeAdapter.getCurrentElem().getSDOOrDA(), TDA.class))
                    .flatMap(Collection::stream)
                    .filter(tda -> TPredefinedBasicTypeEnum.STRUCT == tda.getBType()
                            && Objects.equals(tda.getType(),oldId))
                    .collect(Collectors.toList());
            tdas.forEach(tda -> tda.setType(newId));
        });

    }

    public static <T extends TIDNaming> boolean hasSameID(T rcv, T prd){
        return rcv.getId().equals(prd.getId());
    }

    public static <T extends TIDNaming> boolean hasSamePrivates(T rcv, T prd){
        if(prd.getPrivate().size() != rcv.getPrivate().size()) {
            return false;
        }

        for(TPrivate prdTPrivate : prd.getPrivate()){
            boolean isPrivateIn = rcv.getPrivate()
                .stream()
                .anyMatch(rcvTPrivate ->  Objects.equals(rcvTPrivate.getType(),prdTPrivate.getType())
                    && Objects.equals(rcvTPrivate.getSource(),prdTPrivate.getSource())
                );
            if(!isPrivateIn) {
                return false;
            }
        }
        return true ;
    }

    protected List<DATypeAdapter> findDATypesWhichBdaContainsEnumTypeId(String enumTypeId){

        List<DATypeAdapter> result = new ArrayList<>();
        for(DATypeAdapter daTypeAdapter : getDATypeAdapters()){
            if(daTypeAdapter.containsBDAWithEnumTypeID(enumTypeId)) {
                result.add(daTypeAdapter);
            }
        }
        return result;
    }

    protected List<DOTypeAdapter> findDOTypesWhichDAContainsEnumTypeId(String enumTypeId){

        List<DOTypeAdapter> result = new ArrayList<>();
        for(DOTypeAdapter doTypeAdapter : getDOTypeAdapters()){
            if(doTypeAdapter.containsDAWithEnumTypeId(enumTypeId)) {
                result.add(doTypeAdapter);
            }
        }
        return result;
    }

    protected List<DATypeAdapter> findDATypesFromStructBdaWithDATypeId(String daTypeId){
        return getDATypeAdapters().stream()
                .filter(daTypeAdapter -> daTypeAdapter.containsStructBdaWithDATypeId(daTypeId))
                .collect(Collectors.toList());
    }

    protected List<DOTypeAdapter> findDOTypesWhichDAContainsStructWithDATypeId(String daTypeId){
        return  getDOTypeAdapters().stream()
                .filter(doTypeAdapter -> doTypeAdapter.containsDAStructWithDATypeId(daTypeId))
                .collect(Collectors.toList());
    }

    protected List<DOTypeAdapter> findDOTypesFromSDOWithDOTypeId(String doTypeId){
        return getDOTypeAdapters().stream()
                .filter(doTypeAdapter -> doTypeAdapter.containsSDOWithDOTypeId(doTypeId))
                .collect(Collectors.toList());
    }

    public static  <T extends TUnNaming> List<T> retrieveSdoOrDA(List<TUnNaming> sdoOrDoList, Class<T> clz){
        return sdoOrDoList.stream()
                .filter(tUnNaming -> tUnNaming.getClass().isAssignableFrom(clz))
                .map(tUnNaming -> (T)tUnNaming)
                .collect(Collectors.toList());
    }

    protected List<LNodeTypeAdapter> findLNodeTypesFromDoWithDoTypeId(String doTypeId){
        return getLNodeTypeAdapters().stream()
                .filter(lNodeTypeAdapter -> lNodeTypeAdapter.containsDOWithDOTypeId(doTypeId))
                .collect(Collectors.toList());
    }

    protected String generateDttId(String iedName,String dttId){
        final int MAX_LENGTH = 255;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(iedName).append("_").append(dttId);
        String str = stringBuilder.toString();
        return str.length() <= MAX_LENGTH ? str : str.substring(0,MAX_LENGTH);
    }

    public ExtRefBindingInfo getBinderResumedDTT(String lnType, ExtRefSignalInfo signalInfo) throws ScdException {

        ExtRefBindingInfo binder = new ExtRefBindingInfo();

        // LNodeType check
        LNodeTypeAdapter lNodeTypeAdapter = getLNodeTypeAdapterById(lnType)
                .orElseThrow(()-> new ScdException("Unknown LNodeType:" + lnType));
        if(lNodeTypeAdapter.getLNClass() == null){
            log.error("Mandatory lnClass is missing in DTT. This should not happen for valid SCD");
            throw new IllegalArgumentException("lnClass is mandatory for LNodeType in DataTemplate:" + lnType);
        }

        binder.setLnType(lnType);
        binder.setLnClass(lNodeTypeAdapter.getLNClass());

        if(signalInfo.getPDO() == null) {
            return binder;
        }
        // DoType check
        DoTypeName doName = new DoTypeName(signalInfo.getPDO());
        String extDoName = doName.getName();
        String doTypeId = lNodeTypeAdapter.getDOTypeId(extDoName)
                .orElseThrow(() ->new ScdException("Unknown doName :" + signalInfo.getPDO()));

        DOTypeAdapter doTypeAdapter = getDOTypeAdapterById(doTypeId)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format("%s: No referenced to DO id : %s", doName, doTypeId)
                )
            );

        //doTypeAdapter.completeStructuredData(doName);
        doTypeAdapter.checkAndCompleteStructData(doName);
        binder.setDoName(doName);

        if(signalInfo.getPDA() == null){
            return binder;
        }

        // DaType check
        DaTypeName daName = new DaTypeName(signalInfo.getPDA());
        String extDaName = daName.getName();
        DOTypeAdapter lastDoTypeAdapter = doTypeAdapter;
        if(!doTypeAdapter.containsDAWithDAName(extDaName)){
            var pair = doTypeAdapter.findPathDoType2DA(extDaName);
            lastDoTypeAdapter = pair.getValue();
        }

        TDA da = lastDoTypeAdapter.getDAByName(extDaName)
                .orElseThrow(
                        ()-> new ScdException(
                                String.format("%s: Unknown DA (%s) in DOType (%s) ", doName, extDaName, doTypeId)
                        )
                );
        if(da.getBType() != TPredefinedBasicTypeEnum.STRUCT && !daName.getStructNames().isEmpty() ){
            throw new ScdException(
                    String.format(
                            "Invalid ExtRef signal: no coherence between pDO(%s) and pDA(%s)",
                        signalInfo.getPDO(),signalInfo.getPDA()
                    )
            );
        }

        if(da.getBType() == TPredefinedBasicTypeEnum.STRUCT && !daName.getStructNames().isEmpty()){
            String daTypeId = da.getType();
            DATypeAdapter daTypeAdapter = getDATypeAdapterById(daTypeId)
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    String.format("%s: Unknown DA (%s), or no reference to its type", daName, extDaName)
                            )
                    );
            daTypeAdapter.check(daName);
            daName.setFc(da.getFc());
            binder.setDaName(daName);
        }
        return binder;
    }
}
