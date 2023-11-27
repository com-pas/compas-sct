// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TDOType DOType}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link DOTypeAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *       <li>{@link DOTypeAdapter#getDAAdapterByName <em>Returns the value of the <b>DAAdapter </b>reference object By <b>DA </b> name </em>}</li>
 *       <li>{@link DOTypeAdapter#getDOTypeAdapterBySdoName <em>Returns the value of the <b>DOTypeAdapter </b>reference object By <b>SDO </b> name </em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DOTypeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link DOTypeAdapter#getDAByName <em>Returns the value of the <b>TDA </b>reference object By name</em>}</li>
 *      <li>{@link DOTypeAdapter#getDataAttributeRefByDaName <em>Returns <b>DataAttributeRef</b> By <b>DaTypeName </b> </em>}</li>
 *      <li>{@link DOTypeAdapter#getDataAttributeRefByDoName <em>Returns <b>DataAttributeRef</b> By <b>DoTypeName </b></em>}</li>
 *      <li>{@link DOTypeAdapter#getDataAttributeRefs <em>Returns List Of <b>DataAttributeRef</b> By Custom filter</em>}</li>
 *      <li>{@link DOTypeAdapter#getDataAttributeRefsOfDA <em>Returns List Of <b>DataAttributeRef</b> By <b>DA </b> Object</em>}</li>
 *      <li>{@link DOTypeAdapter#getSdoOrDAs <em>Returns List of <b>TSDO or TDA</b> </em>}</li>
 *      <li>{@link DOTypeAdapter#getCdc <em>Returns the value of the <b>cdc </b>attribute</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link DOTypeAdapter#hasSameContent <em>Compare two TSDO or Two TDA</em>}</li>
 *       <li>{@link DOTypeAdapter#hasSameContentAs <em>Compare Two TDOType</em>}</li>
 *       <li>{@link DOTypeAdapter#checkAndCompleteStructData <em>Check and Complete structData from DoTypeName</em>}</li>
 *       <li>{@link DOTypeAdapter#containsDAStructWithDATypeId <em>Check whether TDOType contain TDA with Struct Btype By Id</em>}</li>
 *       <li>{@link DOTypeAdapter#containsSDOWithDOTypeId <em>Check whether TDOType contain TSDO By Id</em>}</li>
 *       <li>{@link DOTypeAdapter#containsDAWithDAName <em>Check whether TDOType contain TDA By Name</em>}</li>
 *       <li>{@link DOTypeAdapter#containsDAWithEnumTypeId <em>Check whether TDOType contain TEnumType By Id</em>}</li>
 *    </ul>
 * </ol>
 */
public class DOTypeAdapter extends AbstractDataTypeAdapter<TDOType> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public DOTypeAdapter(DataTypeTemplateAdapter parentAdapter, TDOType currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * complete the input Data Attribute Reference from DataTypeTemplate filtered out by the given DoTypeName
     * @param doTypeName the Data object, eventually with DOs
     * @param idx index of the DOs in the given DoTypeName
     * @param dataAttributeRef Data Attribute Reference to complete
     * @return completed Data Attribute Reference or null if the filter constrains are not met
     */
    public List<DataAttributeRef> getDataAttributeRefByDoName(DoTypeName doTypeName, int idx, DataAttributeRef dataAttributeRef) {

        int sz = doTypeName.getStructNames().size();
        if(!doTypeName.isDefined()) {
            return new ArrayList<>();
        }
        List<DataAttributeRef> result = new ArrayList<>();
        if(sz - idx > 0){
            String sdoName = doTypeName.getStructNames().get(idx);
            TSDO tsdo = getSDOByName(sdoName).orElse(null);
            if(tsdo == null) {
                return new ArrayList<>();
            }
            DataAttributeRef copyDataAttributeRef = DataAttributeRef.copyFrom(dataAttributeRef);
            copyDataAttributeRef.getDoName().setCdc(getCdc());
            copyDataAttributeRef.getDoName().addStructName(sdoName);
            DOTypeAdapter doTypeAdapter = getDataTypeTemplateAdapter().getDOTypeAdapterById(tsdo.getType())
                    .orElseThrow();
            result.addAll( doTypeAdapter.getDataAttributeRefByDoName(doTypeName,idx+1,copyDataAttributeRef));

        } else {
            for(TDA tda : getSdoOrDAs(TDA.class)){
                DataAttributeRef copyDataAttributeRef = DataAttributeRef.copyFrom(dataAttributeRef);
                DAAdapter daAdapter = new DAAdapter(this,tda);
                copyDataAttributeRef.getDaName().setName(daAdapter.getName());
                copyDataAttributeRef.getDaName().setType(daAdapter.getType());
                copyDataAttributeRef.getDaName().setBType(daAdapter.getBType());
                copyDataAttributeRef.getDaName().setValImport(daAdapter.isValImport());
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
                    result.addAll( daTypeAdapter.completeDataAttributeRef(copyDataAttributeRef));
                } else {
                    copyDataAttributeRef.getDaName().addDaiValues(tda.getVal());
                    result.add(copyDataAttributeRef);
                }
            }
        }
        return result;
    }

    /**
     * Completes recursively summarize Data Type Templates (dataAttributeRef) from DOType to specified DaTypeName.
     * @param daTypeName the Data object, eventually with DA
     * @param dataAttributeRef reference Data Attribute Reference to complete
     * @return optional of <em>DataAttributeRef</em> object
     * @throws ScdException
     */
    public Optional<DataAttributeRef> getDataAttributeRefByDaName(DaTypeName daTypeName,
                                                            DataAttributeRef dataAttributeRef) throws ScdException {
        if(!dataAttributeRef.getDoName().isDefined()) {
            return Optional.empty();
        }
        DoTypeName doTypeName = dataAttributeRef.getDoName();

        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass().equals(TSDO.class)) {
                TSDO sdo = (TSDO) tUnNaming;
                DOTypeAdapter doTypeAdapter = getDOTypeAdapterBySdoName(sdo.getName()).orElseThrow();
                doTypeName.addStructName(sdo.getName());
                doTypeName.setCdc(getCdc());
                Optional<DataAttributeRef> opDataAttributeRef = doTypeAdapter.getDataAttributeRefByDaName(daTypeName,dataAttributeRef);
                if (opDataAttributeRef.isPresent()) {
                    return opDataAttributeRef;
                }
            } else {
                TDA tda = (TDA)tUnNaming;
                dataAttributeRef.getDaName().setValImport(tda.isValImport());
                dataAttributeRef.getDaName().setFc(tda.getFc());
                dataAttributeRef.getDaName().setType(tda.getType());
                dataAttributeRef.getDaName().setBType(tda.getBType());
                dataAttributeRef.getDaName().setName(tda.getName());
                dataAttributeRef.setDaiValues(tda.getVal());
                if(tda.getBType() != TPredefinedBasicTypeEnum.STRUCT &&
                        tda.getName().equals(daTypeName.getName()) &&
                        daTypeName.getStructNames().isEmpty()  ){
                    return Optional.of(dataAttributeRef);
                }

                if(tda.getBType() == TPredefinedBasicTypeEnum.STRUCT &&
                        tda.getName().equals(daTypeName.getName()) &&
                        !daTypeName.getStructNames().isEmpty() ){
                    DAAdapter daAdapter = getDAAdapterByName(tda.getName())
                            .orElseThrow(() -> new AssertionError(""));

                    DATypeAdapter daTypeAdapter = daAdapter.getDATypeAdapter()
                            .orElseThrow(() -> new AssertionError(""));
                    Optional<DataAttributeRef> opDataAttributeRef = daTypeAdapter.getDataAttributeRefByDaName(daTypeName,0,dataAttributeRef);
                    if (opDataAttributeRef.isPresent()) {
                        return opDataAttributeRef;
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDOType().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("DOType[%s]",
                Utils.xpathAttributeFilter("id", currentElem.isSetId() ? currentElem.getId() : null));
    }


    /**
     * Checks if current DOType contains DA
     * @param da DA name
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsDAWithDAName(String da){
        return getTdaStream().anyMatch(tda -> tda.getName().equals(da));
    }

    /**
     * Checks if current DOType contains DA with specific EnumType
     * @param enumTypeId ID of EnumType in DA to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsDAWithEnumTypeId(String enumTypeId) {
        return getTdaStream()
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
    public Pair<String,DOTypeAdapter> findPathDoTypeToDA(String daName) throws ScdException {
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

    /**
     * Find path from a SDO to DA (defined by names)
     *
     * @param sdoName SDO from which find a path
     * @param daName  DA for which find a path to
     * @return pair of DO/SDO and DoType. DO/SDO references the DOType
     * @throws ScdException when inconsistency are found in th SCL's
     *                      DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
    public Pair<String, DOTypeAdapter> findPathSDOToDA(String sdoName, String daName) throws ScdException {
        String errMsg = String.format("No coherence or path between DO/SDO(%s) and DA(%s)", sdoName, daName);
        Optional<TSDO> opSdo = getSDOByName(sdoName);
        if (opSdo.isEmpty()) {
            throw new ScdException(errMsg);
        }

        DOTypeAdapter doTypeAdapter = parentAdapter.getDOTypeAdapterById(opSdo.get().getType()).orElse(null);
        if (doTypeAdapter == null) {
            throw new ScdException(errMsg);
        }
        if(doTypeAdapter.containsDAWithDAName(daName)){
            return Pair.of(opSdo.get().getName(),doTypeAdapter);
        }
        return doTypeAdapter.findPathDoTypeToDA(daName);
    }

    /**
     * Checks if current DOType contains DAStruct
     * @param daTypeId ID of DAType (which type is Struct)
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsDAStructWithDATypeId(String daTypeId) {
        return getTdaStream()
                .anyMatch(
                        tda -> TPredefinedBasicTypeEnum.STRUCT.equals(tda.getBType()) &&
                                tda.getType().equals(daTypeId)
                );
    }

    /**
     * Compares current DOType and given DOType
     * @param tdoType DOType to compare with
     * @return <em>Boolean</em> value of comparison result
     */
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

    /**
     * Compares two SDOs
     * @param thisSdo SDO to compare
     * @param inSdo SDO to compare
     * @return <em>Boolean</em> value of comparison result
     */
    protected boolean hasSameContent(TSDO thisSdo, TSDO inSdo) {
        return Objects.equals(thisSdo.getName(),inSdo.getName())
                && Objects.equals(thisSdo.getType(),inSdo.getType())
                && Objects.equals(thisSdo.getCount(),inSdo.getCount());

    }

    /**
     * Compares DA from current DOType and given DA
     * @param thisTDA DA from current DOType to compare
     * @param inTDA DA to compare with
     * @return <em>Boolean</em> value of comparison result
     */
    protected boolean hasSameContent(TDA thisTDA, TDA inTDA) {
        DAAdapter daAdapter = new DAAdapter(this,thisTDA);
        return daAdapter.hasSameContentAs(inTDA);
    }

    /**
     * Checks if current DOType contains SDO with specific DOTYpe ID
     * @param doTypeId ID of DOType in SDO to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsSDOWithDOTypeId(String doTypeId) {
        List<TSDO> sdoList = new ArrayList<>();
        for( TUnNaming unNaming : currentElem.getSDOOrDA()){
            if(unNaming.getClass() == TSDO.class) {
                sdoList.add((TSDO)unNaming);
            }
        }
        return sdoList.stream().anyMatch(sdo -> sdo.getType().equals(doTypeId));
    }

    /**
     * Checks current DOType structure coherence and completes given DoTypeName with CDC value
     * @param doTypeName DoTypeName to check and complete
     * @return pair of last DO name in DOType. current DOTypeAdapter
     * @throws ScdException when inconsistency are found in th SCL's
     *                     DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
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

    /**
     * Gets from current DOType specific SDO
     * @param sdoName name of SDO to return
     * @return optional of <em>TSDO</em> object or empty if unknown SDO name
     */
    public Optional<TSDO> getSDOByName(String sdoName) {
        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TSDO.class && ((TSDO)tUnNaming).getName().equals(sdoName)){
                return Optional.of((TSDO)tUnNaming);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets from current DOType specific DA
     * @param name name of DA to return
     * @return optional of <em>TDA</em> object or empty if unknown DA name
     */
    public Optional<TDA> getDAByName(String name) {
        TDOType tdoType = currentElem;
        if (!containsDAWithDAName(name)) {
            tdoType = findPathDoTypeToDA(name)
                    .getValue()
                    .getCurrentElem();
        }
        return tdoType.getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class)
                        && ((TDA)unNaming).getName().equals(name))
                .map(TDA.class::cast)
                .findFirst();
    }

    /**
     * Retrieves all TDA in DOType
     * @return stream of TDA
     */
    private Stream<TDA> getTdaStream() {
        return getCurrentElem()
                .getSDOOrDA()
                .stream()
                .filter(unNaming -> unNaming.getClass().equals(TDA.class))
                .map(TDA.class::cast);
    }

    /**
     * Gets from current DOType CDC enum value
     * @return <em>TPredefinedCDCEnum</em> corresponding value
     */
    public TPredefinedCDCEnum getCdc() {
        return currentElem.getCdc();
    }

    /**
     * return a list of Data Attribute References beginning from this DoType (Do or SDO).
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     * @param rootDataAttributeRef reference Data Attribute Reference used to build the list
     * @param filter filter for DO/SDO and DA/BDA
     * @return list of Data Attribute References beginning from this DoType (Do or SDO)
     */
    public List<DataAttributeRef> getDataAttributeRefs(DataAttributeRef rootDataAttributeRef, DataAttributeRef filter) {
        List<DataAttributeRef> resultDataAttributeRefs = new ArrayList<>();
        for(TUnNaming tUnNaming: currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TDA.class){
                TDA tda = (TDA)tUnNaming;
                resultDataAttributeRefs.addAll(getDataAttributeRefsOfDA(rootDataAttributeRef, filter, tda));
            } else {
                TSDO tsdo = (TSDO)tUnNaming;
                if(excludedByFilter(filter, tsdo)){
                    continue;
                }
                DataAttributeRef currentDataAttributeRef = DataAttributeRef.copyFrom(rootDataAttributeRef);
                currentDataAttributeRef.addDoStructName(tsdo.getName());
                parentAdapter.getDOTypeAdapterById(tsdo.getType())
                        .ifPresent(doTypeAdapter -> resultDataAttributeRefs.addAll(doTypeAdapter.getDataAttributeRefs(currentDataAttributeRef, filter)));
            }
        }
        return resultDataAttributeRefs;
    }

    /**
     * return a list of summarized Data Attribute References beginning from given DA/BDA.
     * <ul>
     *     <li> If DA the list will contain only one summarized Data Attribute References  </li>
     *     <li> If BDA list will contain all summarized Data Attribute References for each DA in BDA </li>
     * </ul>
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     * @param rootDataAttributeRef reference Data Attribute Reference used to build the list
     * @param filter filter for DA/BDA
     * @param da DA containing information to summarize
     * @return list of completed Data Attribute References beginning from this DoType.
     */
    private List<DataAttributeRef> getDataAttributeRefsOfDA(DataAttributeRef rootDataAttributeRef, DataAttributeRef filter, TDA da) {
        if (excludedByFilter(filter, da)) {
            return Collections.emptyList();
        }
        DataAttributeRef currentDataAttributeRef = DataAttributeRef.copyFrom(rootDataAttributeRef);
        currentDataAttributeRef.getDaName().setName(da.getName());
        currentDataAttributeRef.getDaName().setFc(da.getFc());
        currentDataAttributeRef.getDaName().setBType(da.getBType());
        if(da.getBType() == TPredefinedBasicTypeEnum.STRUCT){
            return parentAdapter.getDATypeAdapterById(da.getType())
                    .map(daTypeAdapter -> daTypeAdapter.getDataAttributeRefs(currentDataAttributeRef, filter))
                    .orElse(Collections.emptyList());
        } else {
            currentDataAttributeRef.getDaName().setType(da.getType());
            currentDataAttributeRef.getDaName().setValImport(da.isValImport());
            currentDataAttributeRef.setDaiValues(da.getVal());
            return List.of(currentDataAttributeRef);
        }
    }

    /**
     * Checks if given Data Attribute Reference contains specified DA
     * @param filter Data Attribute Reference to check contain
     * @param da SDO to checked
     * @return <em>Boolean</em> exclusion result
     */
    private boolean excludedByFilter(DataAttributeRef filter, TDA da) {
        return filter != null && filter.isDaNameDefined() &&
            !filter.getDaName().getName().equals(da.getName());
    }

    /**
     * Checks if given Data Attribute Reference contains specified SDO
     * @param filter Data Attribute Reference to check contain
     * @param tsdo SDO to checked
     * @return <em>Boolean</em> exclusion result
     */
    private boolean excludedByFilter(DataAttributeRef filter, TSDO tsdo) {
        return filter != null &&
            !filter.getSdoNames().isEmpty() &&
            !filter.getSdoNames().contains(tsdo.getName());
    }

    /**
     * Gets DOType from SDO
     * @param name name of SDO linked to DOType
     * @return optional of <em>DOTypeAdapter</em> object
     */
    public Optional<DOTypeAdapter> getDOTypeAdapterBySdoName(String name) {
        Optional<TSDO> opSdo = getSDOByName(name);
        if(!opSdo.isPresent()){
            return Optional.empty();
        }
       return parentAdapter.getDOTypeAdapterById(opSdo.get().getType());
    }

    /**
     * Gets DA from current DOType
     * @param name name of DA to find
     * @return optional of <em>DAAdapter</em> adapter
     */
    public Optional<DAAdapter> getDAAdapterByName(String name){
        for(TUnNaming tUnNaming : currentElem.getSDOOrDA()){
            if(tUnNaming.getClass() == TDA.class && ((TDA)tUnNaming).getName().equals(name)){
                return Optional.of(new DAAdapter(this,(TDA)tUnNaming));
            }
        }
        return  Optional.empty();
    }

    /**
     * Gets Data Type Template linked to this DOType as parent reference
     * @return <em>DataTypeTemplateAdapter</em> object
     */
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
