// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service.scl;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TBDA;
import org.lfenergy.compas.scl.TDA;
import org.lfenergy.compas.scl.TDAType;
import org.lfenergy.compas.scl.TDO;
import org.lfenergy.compas.scl.TDOType;
import org.lfenergy.compas.scl.TDataTypeTemplates;
import org.lfenergy.compas.scl.TEnumType;
import org.lfenergy.compas.scl.TEnumVal;
import org.lfenergy.compas.scl.TIDNaming;
import org.lfenergy.compas.scl.TLNodeType;
import org.lfenergy.compas.scl.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl.TSDO;
import org.lfenergy.compas.scl.TUnNaming;
import org.lfenergy.compas.sct.model.IExtRefDTO;
import org.lfenergy.compas.sct.model.dto.ResumedDataTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Slf4j
public class SclDataTemplateManager {

    private final Map<String, TEnumType> enumTypeToAdd = new HashMap<>();
    private final Map<String, TDAType> daTypeToAdd = new HashMap<>();
    private final Map<String, TDOType> doTypeToAdd = new HashMap<>();
    private final Map<String, TLNodeType> lNodeTypeToAdd = new HashMap<>();
    private final Map<String,String> lNodeTypeTracker = new HashMap<>();


    public SCL importDTT(SCL provider, SCL receiver, String iedName){

        computeImportableDTTFromEnumType(provider,receiver,iedName);
        computeImportableDTTFromDAType(provider,receiver,iedName);
        computeImportableDTTFromDOType(provider,receiver,iedName);
        computeImportableDTTFromLNodeType(provider,receiver,iedName);

        if(receiver.getDataTypeTemplates() == null ){
            receiver.setDataTypeTemplates(new TDataTypeTemplates());
        }
        //add importable enum
        enumTypeToAdd.values().forEach(tEnumType -> receiver.getDataTypeTemplates().getEnumType().add(tEnumType));

        //remove duplicate of enumTypeToAdd in daTypeToAdd

        //add importable DAType
        daTypeToAdd.values().forEach(tdaType -> receiver.getDataTypeTemplates().getDAType().add(tdaType));
        //add importable DOType
        doTypeToAdd.values().forEach(tdoType -> receiver.getDataTypeTemplates().getDOType().add(tdoType));
        //add importable LNodeType
        lNodeTypeToAdd.values().forEach(tlNodeType -> receiver.getDataTypeTemplates().getLNodeType().add(tlNodeType));

        return receiver;
    }

    public <T extends TIDNaming> Boolean isSameID(T rcv, T prd){
        return rcv.getId().equals(prd.getId());
    }

    public boolean isIdentical(TEnumType rcvEnumType,TEnumType prdEnumType, boolean idCheck){
        if(idCheck && !isSameID(rcvEnumType,prdEnumType)){
            return false;
        }
        List<TEnumVal> rcvEnumValList = rcvEnumType.getEnumVal();
        List<TEnumVal> prdEnumValList = prdEnumType.getEnumVal();
        if(rcvEnumValList.size() != prdEnumValList.size()) {
            return false;
        }

        for(TEnumVal prdEnumVal : prdEnumValList){
            boolean isIn = rcvEnumValList
                    .stream()
                    .anyMatch(
                        rcvEnumVal -> rcvEnumVal.getValue().equals(prdEnumVal.getValue()) &&
                        rcvEnumVal.getOrd() == prdEnumVal.getOrd()
                    );
            if(!isIn) {
                return false;
            }
        }
        return true;
    }

    public List<TDAType> findDATypesWhichBdaContainsEnumTypeId(SCL provider,String enumTypeId){
        if(provider.getDataTypeTemplates() == null) {
            return new ArrayList<>();
        }

        List<TDAType> daTypeList = provider.getDataTypeTemplates().getDAType();
        List<TDAType> result = new ArrayList<>();
        for(TDAType tdaType: daTypeList){
            boolean containsEnum = tdaType.getBDA()
                    .stream()
                    .anyMatch(
                        bda -> TPredefinedBasicTypeEnum.ENUM.equals(bda.getBType()) &&
                        enumTypeId.equals(bda.getType())
                    );
            if(containsEnum) {
                result.add(tdaType);
            }
        }
        return result;
    }

    public List<TDOType> findDOTypesWhichDAContainsEnumTypeId(SCL provider,String enumTypeId){

        if(provider.getDataTypeTemplates() == null) {
            return new ArrayList<>();
        }

        List<TDOType> doTypeList = provider.getDataTypeTemplates().getDOType();
        List<TDOType> result = new ArrayList<>();
        for(TDOType tdoType: doTypeList){
            List<TDA> tdaList = new ArrayList<>();
            for( TUnNaming unNaming : tdoType.getSDOOrDA()){
                if(unNaming instanceof TDA) {
                    tdaList.add((TDA)unNaming);
                }
            }
            boolean containsEnum = tdaList.stream()
                    .anyMatch(
                        tda -> tda.getBType().equals(TPredefinedBasicTypeEnum.ENUM) &&
                        tda.getType().equals(enumTypeId)
                    );
            if(containsEnum) {
                result.add(tdoType);
            }
        }
        return result;
    }

    public boolean isIdentical(TDAType rcvDAType,TDAType prdDAType, boolean idCheck){
        if(idCheck && !isSameID(rcvDAType,prdDAType)){
            return false;
        }
        List<TBDA> rcvBdaList = rcvDAType.getBDA();
        List<TBDA> prdBdaList = prdDAType.getBDA();

        if(rcvBdaList.size() != prdBdaList.size()) {
            return false;
        }

        int listSz = rcvBdaList.size();
        for(int i = 0; i < listSz; i++){
            if(!rcvBdaList.get(i).getBType().equals(prdBdaList.get(i).getBType()) ||
                !rcvBdaList.get(i).getName().equals(prdBdaList.get(i).getName())) {
                return false;
            }

            if(rcvBdaList.get(i).getBType().equals(TPredefinedBasicTypeEnum.ENUM) &&
                !rcvBdaList.get(i).getType().equals(prdBdaList.get(i).getType())) {
                return false;
            }
        }
        return true;
    }

    public List<TDAType> findDATypesFromStructBdaWithDATypeId(SCL provider,String daTypeId){

        if(provider.getDataTypeTemplates() == null) {
            return new ArrayList<>();
        }

        List<TDAType> daTypeList = provider.getDataTypeTemplates().getDAType();
        List<TDAType> result = new ArrayList<>();
        for(TDAType tdaType: daTypeList){
            boolean containsEnum = tdaType.getBDA().stream()
                    .anyMatch(
                        bda -> bda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT) &&
                        bda.getType().equals(daTypeId)
                    );
            if(containsEnum) {
                result.add(tdaType);
            }
        }
        return result;
    }

    public List<TDOType> findDOTypesWhichDAContainsStructWithDATypeId(SCL provider,String daTypeId){

        if(provider.getDataTypeTemplates() == null) {
            return new ArrayList<>();
        }

        List<TDOType> doTypeList = provider.getDataTypeTemplates().getDOType();
        List<TDOType> result = new ArrayList<>();
        for(TDOType tdoType: doTypeList){
            List<TDA> daList = new ArrayList<>();
            for( TUnNaming unNaming : tdoType.getSDOOrDA()){
                if(unNaming instanceof TDA) {
                    daList.add((TDA)unNaming);
                }
            }
            boolean containsId = daList.stream()
                    .anyMatch(
                        tda -> TPredefinedBasicTypeEnum.STRUCT.equals(tda.getBType()) &&
                        tda.getType().equals(daTypeId)
                    );
            if(containsId) {
                result.add(tdoType);
            }
        }
        return result;
    }

    public static  <T extends TUnNaming> List<T> retrieveSdoOrDO(List<TUnNaming> sdoOrDoList, Class<T> clz){
        List<T> sdoList = new ArrayList<>();
        for( TUnNaming unNaming : sdoOrDoList){
            if(unNaming.getClass().getName().equalsIgnoreCase(clz.getName())) {
                sdoList.add((T)unNaming);
            }
        }
        return sdoList;
    }

    public boolean isIdentical(TDOType rcvDOType,TDOType prdDOType, boolean idCheck){
        if(idCheck && !isSameID(rcvDOType,prdDOType)) {
            return false;
        }

        List<TDA> rcvTdaList = retrieveSdoOrDO(rcvDOType.getSDOOrDA(),TDA.class);
        List<TDA> prdTdaList = retrieveSdoOrDO(prdDOType.getSDOOrDA(),TDA.class);
        List<TSDO> rcvSdoList = retrieveSdoOrDO(rcvDOType.getSDOOrDA(),TSDO.class);
        List<TSDO> prdSdoList = retrieveSdoOrDO(prdDOType.getSDOOrDA(),TSDO.class);

        if(rcvTdaList.size() != prdTdaList.size() || rcvSdoList.size() != prdSdoList.size()) {
            return false;
        }

        // SDO
        for(int i = 0; i < rcvSdoList.size(); i++){
            if(!rcvSdoList.get(i).getName().equals(prdSdoList.get(i).getName()) ||
                !rcvSdoList.get(i).getType().equals(prdSdoList.get(i).getType())) {
                return false;
            }
        }

        // TDO
        for(int i = 0; i < rcvTdaList.size(); i++){
            if(!rcvTdaList.get(i).getName().equals(prdTdaList.get(i).getName()) ||
                !rcvTdaList.get(i).getBType().equals(prdTdaList.get(i).getBType())) {
                return false;
            }

            if(rcvTdaList.get(i).getBType().equals(TPredefinedBasicTypeEnum.ENUM) &&
                    !rcvTdaList.get(i).getType().equals(prdTdaList.get(i).getType())) {
                return false;
            }
        }

        return true;
    }

    public List<TDOType> findDOTypesFromSDOWithDOTypeId(SCL provider,String doTypeId){

        if(provider.getDataTypeTemplates() == null) {
            return new ArrayList<>();
        }

        List<TDOType> doTypeList = provider.getDataTypeTemplates().getDOType();
        List<TDOType> result = new ArrayList<>();
        for(TDOType tdoType: doTypeList){
            List<TSDO> sdoList = new ArrayList<>();
            for( TUnNaming unNaming : tdoType.getSDOOrDA()){
                if(unNaming.getClass() == TSDO.class) {
                    sdoList.add((TSDO)unNaming);
                }
            }
            boolean containsId = sdoList.stream()
                    .anyMatch(sdo -> sdo.getType().equals(doTypeId));
            if(containsId) {
                result.add(tdoType);
            }
        }
        return result;
    }

    public boolean isIdentical(TLNodeType rcvLNodeType, TLNodeType providerLNodeType, boolean idCheck) {

        if(idCheck && !isSameID(rcvLNodeType,providerLNodeType)) {
            return false;
        }

        List<TDO> rcvTdoList = rcvLNodeType.getDO();
        List<TDO> prdTdoList = providerLNodeType.getDO();
        if(rcvTdoList.size() != prdTdoList.size()) {
            return false;
        }

        for(TDO prdTdo : prdTdoList){
            boolean isIn = rcvLNodeType.getDO().stream()
                    .anyMatch(rcvTdo -> rcvTdo.getType().equals(prdTdo.getType())
                            && rcvTdo.getName().equals(prdTdo.getName()));
            if(!isIn) {
                return false;
            }
        }
        return true;
    }

    public List<TLNodeType> findLNodeTypesFromDoWithDoTypeId(SCL provider,String doTypeId){

        if(provider.getDataTypeTemplates() == null) {
            return new ArrayList<>();
        }

        List<TLNodeType> lNodeTypes = provider.getDataTypeTemplates().getLNodeType();

        List<TLNodeType> result = new ArrayList<>();
        for(TLNodeType lNodeType: lNodeTypes){

            boolean containsId = lNodeType.getDO().stream()
                    .anyMatch(tdo -> tdo.getType().equals(doTypeId));
            if(containsId) {
                result.add(lNodeType);
            }
        }
        return result;
    }

    public String generateDttId(String iedName,String dttId){
        final int MAX_LENGTH = 255;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(iedName).append("_").append(dttId);
        String str = stringBuilder.toString();
        return str.length() <= MAX_LENGTH ? str : str.substring(0,MAX_LENGTH);
    }

    public void computeImportableDTTFromEnumType(SCL provider, SCL receiver, String iedName){

        if(provider.getDataTypeTemplates() == null) {
            return;
        }

        List<TEnumType> prdEnumTypes = provider.getDataTypeTemplates().getEnumType();

        List<TEnumType> rcvEnumTypes = new ArrayList<>();
        if(receiver.getDataTypeTemplates() != null) {
            rcvEnumTypes = receiver.getDataTypeTemplates().getEnumType();
        }

        //import EnumType
        for(TEnumType prdEnumType : prdEnumTypes){
            //search prdEnumType in rcvEnumTypes
            Optional<TEnumType> opRcvEnumType = rcvEnumTypes
                    .stream()
                    .filter(enumType -> isSameID(enumType,prdEnumType)).findFirst();
            // same ID and same content
            if(opRcvEnumType.isPresent() && isIdentical(opRcvEnumType.get(),prdEnumType,false)) {
                continue;
            }

            // not same ID or not same content
            if (opRcvEnumType.isPresent() && isSameID(opRcvEnumType.get(),prdEnumType) ) { // same id and not same content

                List<TDAType> daTypeList = findDATypesWhichBdaContainsEnumTypeId(provider,prdEnumType.getId());
                List<TDOType> doTypeList = findDOTypesWhichDAContainsEnumTypeId(provider,prdEnumType.getId());
                // rename enumType Id
                String newId = generateDttId(iedName,prdEnumType.getId());
                // escalate
                daTypeList.forEach(
                        tdaType -> renameRef(tdaType,TPredefinedBasicTypeEnum.ENUM,prdEnumType.getId(),newId));
                doTypeList.forEach(
                        tdoType -> renameRef(tdoType,TPredefinedBasicTypeEnum.ENUM,prdEnumType.getId(),newId));
                enumTypeToAdd.remove(prdEnumType.getId()); // remove track on old ID if necessary
                prdEnumType.setId(newId);
                enumTypeToAdd.put(prdEnumType.getId(),prdEnumType);
            }
            enumTypeToAdd.put(prdEnumType.getId(),prdEnumType);
        }
    }


    public void computeImportableDTTFromDAType(SCL provider,SCL receiver, String iedName){

        if(provider.getDataTypeTemplates() == null) {
            return;
        }

        List<TDAType> prdDATypes = provider.getDataTypeTemplates().getDAType();

        List<TDAType> rcvDATypes = new ArrayList<>();
        if(receiver.getDataTypeTemplates() != null) {
            rcvDATypes = receiver.getDataTypeTemplates().getDAType();
        }

        for(TDAType prdDAType : prdDATypes) {

            //search prdEnumType in rcvEnumTypes
            Optional<TDAType> opRcvDAType = rcvDATypes
                    .stream()
                    .filter(daType -> isSameID(daType,prdDAType))
                    .findFirst();
            // same ID and same content
            if(opRcvDAType.isPresent() && isIdentical(opRcvDAType.get(),prdDAType,false)) {
                continue;
            }

            // not same ID or not same content
            if (opRcvDAType.isPresent() && isSameID(opRcvDAType.get(),prdDAType) ) {

                List<TDAType> daTypeList = findDATypesFromStructBdaWithDATypeId(provider,prdDAType.getId());
                List<TDOType> doTypeList = findDOTypesWhichDAContainsStructWithDATypeId(provider,prdDAType.getId());

                // rename TDAType Id
                String newId = generateDttId(iedName,prdDAType.getId());
                // escalate
                daTypeList.forEach(
                        tdaType -> renameRef(tdaType,TPredefinedBasicTypeEnum.STRUCT,prdDAType.getId(),newId));
                doTypeList.forEach(
                        tdoType -> renameRef(tdoType,TPredefinedBasicTypeEnum.STRUCT,prdDAType.getId(),newId));

                daTypeToAdd.remove(prdDAType.getId()); // remove track on old ID if necessary
                prdDAType.setId(newId);
            }
            daTypeToAdd.put(prdDAType.getId(),prdDAType);
        }
    }

    public void computeImportableDTTFromDOType(SCL provider,SCL receiver, String iedName){

        if(provider.getDataTypeTemplates() == null) {
            return;
        }

        List<TDOType> prdDOTypes = provider.getDataTypeTemplates().getDOType();

        List<TDOType> rcvDOTypes = new ArrayList<>();
        if(receiver.getDataTypeTemplates() != null) {
            rcvDOTypes = receiver.getDataTypeTemplates().getDOType();
        }
        //Merge DOType
        for(TDOType prdDOType : prdDOTypes) {

            //search prdEnumType in rcvEnumTypes
            Optional<TDOType> opRcvDOType = rcvDOTypes
                    .stream()
                    .filter(daType -> isSameID(daType,prdDOType))
                    .findFirst();
            // same ID and same content
            if(opRcvDOType.isPresent() && isIdentical(opRcvDOType.get(),prdDOType,false)) {
                continue;
            }
            // not same ID or not same content
            if (opRcvDOType.isPresent() && isSameID(opRcvDOType.get(),prdDOType) ) {

                List<TDOType> doTypeList = findDOTypesFromSDOWithDOTypeId(provider,prdDOType.getId());
                List<TLNodeType> lNodeTypeList = findLNodeTypesFromDoWithDoTypeId(provider,prdDOType.getId());
                // rename TDAType Id
                String newId = generateDttId(iedName,prdDOType.getId());
                // escalate
                doTypeList.forEach(tdoType -> renameRef(tdoType,prdDOType.getId(),newId));
                lNodeTypeList.forEach(tlNodeType -> renameRef(tlNodeType,prdDOType.getId(),newId));

                daTypeToAdd.remove(prdDOType.getId()); // remove track on old ID if necessary

                prdDOType.setId(newId);
            }
            doTypeToAdd.put(prdDOType.getId(),prdDOType);
        }
    }

    public void computeImportableDTTFromLNodeType(SCL provider, SCL receiver,String iedName){
        if(provider.getDataTypeTemplates() == null) {
            return;
        }

        List<TLNodeType> prdLNodeTypes = provider.getDataTypeTemplates().getLNodeType();

        List<TLNodeType> rcvLNodeTypes = new ArrayList<>();
        if(receiver.getDataTypeTemplates() != null) {
            rcvLNodeTypes = receiver.getDataTypeTemplates().getLNodeType();
        }
        //Merge DAType

        for(TLNodeType prdLNodeType : prdLNodeTypes) {
            //search prdEnumType in rcvEnumTypes
            Optional<TLNodeType> opRcvLNodeType = rcvLNodeTypes
                    .stream()
                    .filter(daType -> isSameID(daType,prdLNodeType))
                    .findFirst();
            // same ID and same content
            if(opRcvLNodeType.isPresent() && isIdentical(opRcvLNodeType.get(),prdLNodeType,false)) {
                continue;
            }

            if (opRcvLNodeType.isPresent() && isSameID(opRcvLNodeType.get(),prdLNodeType) ) {
                // rename TDAType Id
                String newId = generateDttId(iedName,prdLNodeType.getId());
                lNodeTypeToAdd.remove(prdLNodeType.getId()); // remove track on old ID if necessary
                lNodeTypeTracker.put(prdLNodeType.getId(),newId);
                prdLNodeType.setId(newId);
            }
            lNodeTypeToAdd.put(prdLNodeType.getId(),prdLNodeType);
        }
    }

    public void renameRef(TDOType tdoType, String id, String newId) {
        List<TSDO> sdoList = retrieveSdoOrDO(tdoType.getSDOOrDA(),TSDO.class);
        Optional<TSDO> opSdo = sdoList.stream()
                .filter(sdo ->  sdo.getType().equals(id))
                .findFirst();
        if(opSdo.isPresent()){
            opSdo.get().setType(newId);
        }
    }

    public void renameRef(TLNodeType tlNode, String id, String newId) {
        List<TDO> doList = tlNode.getDO();
        Optional<TDO> opDo = doList.stream()
                .filter(tdo ->  tdo.getType().equals(id))
                .findFirst();
        if(opDo.isPresent()){
            opDo.get().setType(newId);
        }
    }


    public void renameRef(TDAType tdaType, TPredefinedBasicTypeEnum anEnum, String id, String newId) {
        List<TBDA> bdaList = tdaType.getBDA();
        Optional<TBDA> opBda = bdaList.stream()
                .filter(bda -> bda.getBType().equals(anEnum) && bda.getType().equals(id))
                .findFirst();
        if(opBda.isPresent()){
            opBda.get().setType(newId);
        }
    }

    public void renameRef(TDOType tdoType, TPredefinedBasicTypeEnum anEnum, String id, String newId) {
        List<TDA> tdaList = retrieveSdoOrDO(tdoType.getSDOOrDA(),TDA.class);
        Optional<TDA> opDa = tdaList.stream()
                .filter(tda ->  tda.getBType().equals(anEnum) && tda.getType().equals(id))
                .findFirst();
        if(opDa.isPresent()){
            opDa.get().setType(newId);
        }
    }

    public Map<String, TEnumType> getEnumTypeToAdd() {
        return Collections.unmodifiableMap(enumTypeToAdd);
    }

    public Map<String, TDAType> getDaTypeToAdd() {
        return Collections.unmodifiableMap(daTypeToAdd);
    }

    public Map<String, TDOType> getDoTypeToAdd() {
        return Collections.unmodifiableMap(doTypeToAdd);
    }

    public Map<String, TLNodeType> getLNodeTypeToAdd() {
        return Collections.unmodifiableMap(lNodeTypeToAdd);
    }

    public Map<String, String> getLNodeTypeTracker() {
        return Collections.unmodifiableMap(lNodeTypeTracker);
    }


    public static ResumedDataTemplate getResumedDTT(String lnType, IExtRefDTO extRef,
                                                    TDataTypeTemplates dtt) throws ScdException {
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        TLNodeType nodeType = dtt.getLNodeType()
                .stream()
                .filter(tlNodeType -> lnType.equals(tlNodeType.getId()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown LNodeType:" + lnType));

        resumedDataTemplate.setLnType(nodeType.getId());
        if(nodeType.getLnClass().isEmpty()){
            log.error("Mandatory lnClass is missing in DTT. This should not happen SCD DTT must be correct");
            throw new IllegalArgumentException("lnClass is mandatory for LNodeType in DataTemplate:" + lnType);
        }
        resumedDataTemplate.setLnType(nodeType.getId());
        resumedDataTemplate.setLnClass(nodeType.getLnClass().get(0));
        if(extRef.getPDO() == null) {
            return resumedDataTemplate;
        }

        String[] sp = extRef.getPDO().split("\\.");
        String extDoName = sp[0];
        String extSdoName1 = sp.length > 1 ? sp[1] : null;
        String extSdoName2 = sp.length > 2 ? sp[2] : null;

        String doTypeId = nodeType.getDO()
                .stream()
                .filter(tdo -> extDoName.equals(tdo.getName()))
                .map(TDO::getType)
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown doName :" + extRef.getPDO()));

        String finalDoTypeId = doTypeId;
        TDOType tdoType = dtt.getDOType()
                .stream()
                .filter(doType -> doType.getId().equals(finalDoTypeId))
                .findFirst()
                .orElseThrow(() -> new ScdException("No referenced for doName :" + extRef.getPDO()));

        resumedDataTemplate.setDoName(extDoName);
        if(extSdoName1 != null){
            List<TSDO> sdos = SclDataTemplateManager.retrieveSdoOrDO(tdoType.getSDOOrDA(), TSDO.class);
            String sdoTypeId = sdos.stream()
                .filter(tsdo -> extSdoName1.equals(tsdo.getName()))
                .map(TSDO::getType)
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown doName.sdoName :" + extDoName + "." + extSdoName1));

            tdoType = dtt.getDOType()
                .stream()
                .filter(doType -> doType.getId().equals(sdoTypeId))
                .findFirst()
                .orElseThrow(() -> new ScdException("No referenced doName.sdoName :" + extDoName + "." + extSdoName1));

            resumedDataTemplate.setDoName(extDoName + "." + extSdoName1);
        }
        resumedDataTemplate.setCdc(tdoType.getCdc());


        if(extRef.getPDA() == null){
            return resumedDataTemplate;
        }

        sp = extRef.getPDA().split("\\.");
        String extDaName = sp[0];
        String extBdaName1 = sp.length > 1 ? sp[1] : null;
        String extBdaName2 = sp.length > 2 ? sp[2] : null;

        List<TDA> das = SclDataTemplateManager.retrieveSdoOrDO(tdoType.getSDOOrDA(), TDA.class);
        TDA da = das.stream()
                .filter(tda ->extDaName.equals(tda.getName()))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown referenced daName :" + extDaName));

        resumedDataTemplate.setDaName(extDaName);
        resumedDataTemplate.setFc(da.getFc());
        if(TPredefinedBasicTypeEnum.STRUCT.equals(da.getBType()) && extBdaName1 != null){
            String daTypeId = da.getType();
            TDAType tdaType = dtt.getDAType()
                    .stream()
                    .filter(dat -> daTypeId.equals(dat.getId()) )
                    .findFirst()
                    .orElseThrow(() -> new ScdException("Unknown referenced daName.bdaName :" + extDaName + "." + extBdaName1));

            TBDA bda = tdaType.getBDA()
                    .stream()
                    .filter(tbda -> extBdaName1.equals(tbda.getName()))
                    .findFirst()
                    .orElseThrow(() -> new ScdException("Unknown daName.bdaName :" + extDaName + "." + extBdaName1));
            resumedDataTemplate.setDaName(extDaName + "." + extBdaName1);
        }

        return resumedDataTemplate;
    }
}
