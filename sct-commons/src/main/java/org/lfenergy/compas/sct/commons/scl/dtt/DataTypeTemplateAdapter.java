// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.*;
import java.util.stream.Collectors;


/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates DataTypeTemplates}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link DataTypeTemplateAdapter#getLNodeTypeAdapterById <em>Returns the value of the <b>LNodeTypeAdapter </b>reference object By Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getLNodeTypeAdapters <em>Returns the value of the <b>LNodeTypeAdapter </b>containment reference list</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getDOTypeAdapterById <em>Returns the value of the <b>DOTypeAdapter </b>reference object By Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getDOTypeAdapters <em>Returns the value of the <b>DOTypeAdapters </b>containment reference list</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getDATypeAdapterById <em>Returns the value of the <b>DATypeAdapter </b>reference object By Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getDATypeAdapters <em>Returns the value of the <b>DATypeAdapter </b>containment reference list</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getEnumTypeAdapterById <em>Returns the value of the <b>EnumTypeAdapter </b>reference object By Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#getEnumTypeAdapters <em>Returns the value of the <b>EnumTypeAdapters </b>containment reference list</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DataTypeTemplateAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#importDTT <em>Add <b>TDataTypeTemplates </b>
 *      describing the childrens <b>TLNodeType,TDOType,TDAType,TEnumType </b> that can be created under this object</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#importEnumType <em>Add <b>TDataTypeTemplates </b> describing the children <b>TEnumType </b> that can be created under this object</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#importLNodeType <em>Add <b>TDataTypeTemplates </b> describing the children <b>TLNodeType </b> that can be created under this object</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#importDOType <em>Add <b>TDataTypeTemplates </b> describing the children <b>TDOType </b> that can be created under this object</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#importDAType <em>Add <b>TDataTypeTemplates </b> describing the children <b>TDAType </b> that can be created under this object</em>}</li>
 *
 *      <li>{@link DataTypeTemplateAdapter#findLNodeTypesFromDoWithDoTypeId <em>Returns <b>LNodeTypeAdapter </b> containment reference list that match <b>DO </b> object And DOType Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#findDOTypesFromSDOWithDOTypeId <em>Returns <b>DOTypeAdapter </b> object Of Type <b>SDO </b> By DOType Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#findDOTypesWhichDAContainsEnumTypeId <em>Returns <b>DOTypeAdapter </b> containment reference list that match <b>DA </b> object Of Type <b>Enum </b> And EnumType Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#findDATypesWhichBdaContainsEnumTypeId <em>Returns <b>DATypeAdapter </b> containment reference list that match <b>BDA </b> object Of Type <b>Enum </b> And EnumType Id</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#findDATypesFromStructBdaWithDATypeId <em>Returns <b>DATypeAdapter </b> object that match <b>BDA </b> object Of Type <b>Struct </b> By DAType Id</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *      <li>{@link DataTypeTemplateAdapter#hasSameID <em>Compare Two TIDNaming</em>}</li>
 *      <li>{@link DataTypeTemplateAdapter#hasSamePrivates <em>Compare Two TDataTypeTemplateAdapter's By these Private</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TLNodeType
 * @see org.lfenergy.compas.scl2007b4.model.TDOType
 * @see org.lfenergy.compas.scl2007b4.model.TDAType
 * @see org.lfenergy.compas.scl2007b4.model.TEnumType
 * @see org.lfenergy.compas.scl2007b4.model.TDA
 * @see org.lfenergy.compas.scl2007b4.model.TBDA
 * @see org.lfenergy.compas.scl2007b4.model.TSDO
 * @see <a href="https://github.com/com-pas/compas-sct/issues/5" target="_blank">General rules to define if two DTT are different</a>
 */
@Slf4j
public class DataTypeTemplateAdapter extends SclElementAdapter<SclRootAdapter, TDataTypeTemplates> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param dataTypeTemplate Current reference
     */
    public DataTypeTemplateAdapter(SclRootAdapter parentAdapter, TDataTypeTemplates dataTypeTemplate) {
        super(parentAdapter,dataTypeTemplate);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getDataTypeTemplates();
    }

    @Override
    protected String elementXPath() {
        return "DataTypeTemplates";
    }


    /**
     * Gets LNodeType from DataTypeTemplate by ID in LNodeTypeAdapter
     * @param id LNodeType ID
     * @return Optional LNodeTypeAdapter object
     */
    public Optional<LNodeTypeAdapter> getLNodeTypeAdapterById(String id) {
        for(TLNodeType tlNodeType : currentElem.getLNodeType()){
            if(tlNodeType.getId().equals(id)) {
                return Optional.of(new LNodeTypeAdapter(this, tlNodeType));
            }
        }
       return Optional.empty();

    }

    /**
     * Gets all LNodeTypes from DataTypeTemplate in list of LNodeTypeAdapter
     * @return list LNodeTypeAdapter objects
     */
    public List<LNodeTypeAdapter> getLNodeTypeAdapters(){

        return currentElem.getLNodeType()
                .stream()
                .map(tlNodeType -> new LNodeTypeAdapter(this,tlNodeType))
                .collect(Collectors.toList());
    }

    /**
     * Gets DOType from DataTypeTemplate by ID in DOTypeAdapter
     * @param id DO Type ID
     * @return Optional DOTypeAdapter object
     */
    public Optional<DOTypeAdapter> getDOTypeAdapterById(String id)  {
        for(TDOType tdoType : currentElem.getDOType()){
            if(tdoType.getId().equals(id)) {
                return Optional.of(new DOTypeAdapter(this, tdoType));
            }
        }
        return Optional.empty();
    }

    /**
     * Gets all DOTypes from DataTypeTemplate in list of DOTypeAdapter
     * @return list DOTypeAdapter objects
     */
    public List<DOTypeAdapter> getDOTypeAdapters(){
        return currentElem.getDOType()
                .stream()
                .map(tdoType -> new DOTypeAdapter(this,tdoType))
                .collect(Collectors.toList());
    }

    /**
     * Gets DAType from DataTypeTemplate by ID in DATypeAdapter
     * @param id DA Type ID
     * @return Optional DATypeAdapter object
     */
    public Optional<DATypeAdapter> getDATypeAdapterById(String id) {
        for(TDAType tdaType : currentElem.getDAType()){
            if(tdaType.getId().equals(id)) {
                return Optional.of(new DATypeAdapter(this,tdaType));
            }
        }
        return Optional.empty();
    }

    /**
     * Gets all DATypes from DataTypeTemplate in list of DATypeAdapter
     * @return list DATypeAdapter objects
     */
    public List<DATypeAdapter> getDATypeAdapters(){
        return currentElem.getDAType()
                .stream()
                .map(tdaType -> new DATypeAdapter(this,tdaType))
                .collect(Collectors.toList());
    }

    /**
     * Gets EnumType from DataTypeTemplate by ID in EnumTypeAdapter
     * @param id DA Type ID
     * @return Optional EnumTypeAdapter object
     */
    public Optional<EnumTypeAdapter> getEnumTypeAdapterById(String id)  {
        for(TEnumType tEnumType : currentElem.getEnumType()){
            if(tEnumType.getId().equals(id)) {
                return Optional.of(new EnumTypeAdapter(this, tEnumType));
            }
        }
        return Optional.empty();
    }

    /**
     * Gets all EnumTypes from DataTypeTemplate in list of EnumTypeAdapter
     * @return list EnumTypeAdapter objects
     */
    public List<EnumTypeAdapter> getEnumTypeAdapters(){
        return currentElem.getEnumType()
                .stream()
                .map(enumType -> new EnumTypeAdapter(this,enumType))
                .collect(Collectors.toList());
    }

    /**
     * Import enum type from this DataTypeTemplate adapter from provider DataTypeTemplate adapter
     * @param thisIEDName IED name (in which DO Type is localized)
     * @param prvDttAdapter Adapter of the Data Type template that provides its DataTypeTemplate
     */
    public void importEnumType(String thisIEDName, DataTypeTemplateAdapter prvDttAdapter){

        Map<String,String> pairOldAndNewEnumId = new HashMap<>();
        List<EnumTypeAdapter> prvEnumTypeAdapters = prvDttAdapter.getEnumTypeAdapters();

        for(EnumTypeAdapter prvEnumTypeAdapter : prvEnumTypeAdapters){

            TEnumType prvEnumType = prvEnumTypeAdapter.getCurrentElem();
            String oldEnumId = prvEnumType.getId();
            String newEnumId = prvEnumType.getId();
            Optional<EnumTypeAdapter> opRcvEnumTypeAdapter = this.getEnumTypeAdapterById(oldEnumId);

            boolean isImportable = opRcvEnumTypeAdapter.isEmpty() || !opRcvEnumTypeAdapter.get().hasSameContentAs(prvEnumType);

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

        // escalate on this DTT and update all element linked to added EnumType
        pairOldAndNewEnumId.forEach((oldId, newId) -> {
            List<DATypeAdapter> daTypeAdapters = prvDttAdapter.findDATypesWhichBdaContainsEnumTypeId(oldId);
            List<TBDA> bdas = daTypeAdapters.stream()
                    .map(DATypeAdapter::getBdaAdapters)
                    .flatMap(Collection::stream)
                    .map(DATypeAdapter.BDAAdapter::getCurrentElem)
                    .filter(bda -> TPredefinedBasicTypeEnum.ENUM == bda.getBType()
                            && Objects.equals(bda.getType(),oldId))
                    .collect(Collectors.toList());
            bdas.forEach(tbda -> tbda.setType(newId));
            List<DOTypeAdapter> doTypeAdapters = prvDttAdapter.findDOTypesWhichDAContainsEnumTypeId(oldId);
            List<TDA> tdas = doTypeAdapters.stream()
                    .map(doTypeAdapter -> retrieveSdoOrDA(doTypeAdapter.getCurrentElem().getSDOOrDA(), TDA.class))
                    .flatMap(Collection::stream)
                    .filter(tda -> TPredefinedBasicTypeEnum.ENUM == tda.getBType()
                            && Objects.equals(tda.getType(),oldId))
                    .collect(Collectors.toList());
            tdas.forEach(tda -> tda.setType(newId));
        });
    }

    /**
     * Import DataTypeTemplate from IEDName and received DataTypeTemplate
     * @param thisIEDName IED name (in which DO Type is localized)
     * @param rcvDttAdapter Adapter of the Data Type template that receives its DataTypeTemplate
     * @return map of (old enumId, new enumId)
     */
    public Map<String,String> importDTT(String thisIEDName, DataTypeTemplateAdapter rcvDttAdapter) {

        this.importEnumType(thisIEDName,rcvDttAdapter);

        this.importDAType(thisIEDName,rcvDttAdapter);

        this.importDOType(thisIEDName,rcvDttAdapter);

        return importLNodeType(thisIEDName,rcvDttAdapter);
    }

    /**
     * Import LNodeType from IEDName and received DataTypeTemplate
     * @param thisIEDName IED name (in which DO Type is localized)
     * @param prvDttAdapter Adapter of the Data Type template that provides its DataTypeTemplate
     * @return map of (old enumId, new enumId)
     */
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
                //import this LNodeType
                currentElem.getLNodeType().add(prvLNodeType);
                if(!Objects.equals(oldId,newId)) {
                    pairOldAndNewId.put(oldId,newId);
                }
            }
        }
        return pairOldAndNewId;
    }

    /**
     * Import DOType from IEDName and received DataTypeTemplate
     * @param thisIEDName IED name (in which DO Type is localized)
     * @param prvDttAdapter Adapter of the Data Type template that provides its DataTypeTemplate
     */
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
                //import this DOType
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

    /**
     * Import DOType from IEDName and received DataTypeTemplate
     * @param thisIEDName IED name (in which DO Type is localized)
     * @param prvDttAdapter Adapter of the Data Type template that provides its DataTypeTemplate
     */
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
                //import this DAType
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

    /**
     * Checks DO/DA ID equality
     * @param rcv input
     * @param prd input
     * @return Equality ckeck result
     * @param <T> Objects' type
     */
    public static <T extends TIDNaming> boolean hasSameID(T rcv, T prd){
        return rcv.getId().equals(prd.getId());
    }

    /**
     * Checks DO/DA contains same private elements
     * @param rcv input
     * @param prd input
     * @return Comparison result
     * @param <T>
     */
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

    /**
     * Finds DA Types for BDA containing specified Enum Type ID
     * @param enumTypeId input
     * @return list DATypeAdapter object
     */
    protected List<DATypeAdapter> findDATypesWhichBdaContainsEnumTypeId(String enumTypeId){

        List<DATypeAdapter> result = new ArrayList<>();
        for(DATypeAdapter daTypeAdapter : getDATypeAdapters()){
            if(daTypeAdapter.containsBDAWithEnumTypeID(enumTypeId)) {
                result.add(daTypeAdapter);
            }
        }
        return result;
    }

    /**
     * Finds DA Types for BDA containing specified Enum Type ID
     * @param enumTypeId input
     * @return list DOTypeAdapter object
     */
    protected List<DOTypeAdapter> findDOTypesWhichDAContainsEnumTypeId(String enumTypeId){

        List<DOTypeAdapter> result = new ArrayList<>();
        for(DOTypeAdapter doTypeAdapter : getDOTypeAdapters()){
            if(doTypeAdapter.containsDAWithEnumTypeId(enumTypeId)) {
                result.add(doTypeAdapter);
            }
        }
        return result;
    }

    /**
     * Finds DA Types from BDA struct with specified ID
     * @param daTypeId input
     * @return list DATypeAdapter object
     */
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

    /**
     * Collects DO/SDO/DA/SDA into list cooresponding to specified class type from list
     * @param sdoOrDoList input list
     * @param clz classe type
     * @return list of DO/SDO/DA/SDA object
     * @param <T> class type of returned object
     */
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

    /**
     * Generates formatted DataTypeTemplate ID from
     * @param iedName IED name
     * @param dttId DataTypeTemplate ID
     * @return formatted DataTypeTemplate ID
     */
    protected String generateDttId(String iedName,String dttId){
        final int MAX_LENGTH = 255;
        String str = iedName + "_" + dttId;
        return str.length() <= MAX_LENGTH ? str : str.substring(0,MAX_LENGTH);
    }

    /**
     * Gets binding information for ExtRefs
     * @param lnType NodeType ID
     * @param signalInfo ExtRef signal information
     * @return ExtRef binding information in <em>ExtRefBindingInfo</em> object
     * @throws ScdException
     */
    public ExtRefBindingInfo getBinderResumedDTT(String lnType, ExtRefSignalInfo signalInfo) throws ScdException {
        ExtRefBindingInfo binder = new ExtRefBindingInfo();
        // LNodeType check
        LNodeTypeAdapter lNodeTypeAdapter = getLNodeTypeAdapterById(lnType)
                .orElseThrow(() -> new ScdException("Unknown LNodeType:" + lnType));
        if(lNodeTypeAdapter.getLNClass() == null){
            log.error("Mandatory lnClass is missing in DTT. This should not happen for valid SCD");
            throw new IllegalArgumentException("lnClass is mandatory for LNodeType in DataTemplate : " + lnType);
        }
        binder.setLnType(lnType);
        binder.setLnClass(lNodeTypeAdapter.getLNClass());
        if (signalInfo.getPDO() == null) {
            return binder;
        }
        // DoType check
        DOTypeInfo doTypeInfo = lNodeTypeAdapter.findMatchingDOType(signalInfo);
        binder.setDoName(new DoTypeName(signalInfo.getPDO()));
        if (signalInfo.getPDA() == null) {
            return binder;
        }
        // DaType check
        binder.updateDAInfos(signalInfo, doTypeInfo);
        return binder;
    }

    @RequiredArgsConstructor
    @Getter
    public static class DOTypeInfo {
        private final DoTypeName doTypeName;
        private final String doTypeId;
        private final DOTypeAdapter doTypeAdapter;
    }
}
