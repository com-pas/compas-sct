// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TBDA;
import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TProtNs;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A representation of the model object <em><b>{@link org.lfenergy.compas.scl2007b4.model.TDAType DAType}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link DATypeAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *       <li>{@link DATypeAdapter#getBdaAdapterByName <em>Returns the value of the <b>BDAAdapter </b> reference object By <b>BDA</b> name</em>}</li>
 *       <li>{@link DATypeAdapter#getBdaAdapters <em>Returns the value of the <b>BDAAdapters </b>containment reference list</em>}</li>
 *       <li>{@link DATypeAdapter#getDATypeAdapterByBdaName <em>Returns the value of the <b>DATypeAdapter </b>reference object By <b>BDA</b> name/em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link DATypeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link DATypeAdapter#getBDAByName <em>Returns the value of the <b>TBDA </b>reference object By name</em>}</li>
 *      <li>{@link DATypeAdapter#getResumedDTTByDaName <em>Returns List Of <b>ResumedDataTemplate</b> By <b>DaTypeName </b></em>}</li>
 *      <li>{@link DATypeAdapter#getResumedDTTs <em>Returns List Of <b>ResumedDataTemplate </b> By Custom filter</em>}</li>
 *      <li>{@link DATypeAdapter#completeResumedDTT <em>Returns Completed list Of <b>ResumedDataTemplate </b></em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link DATypeAdapter#hasSameContentAs <em>Compare Two TDA</em>}</li>
 *       <li>{@link DATypeAdapter#check <em>Check structData from DaTypeName</em>}</li>
 *       <li>{@link DATypeAdapter#containsStructBdaWithDATypeId <em>Check whether TDA contain TBDA with Struct Btype By Id</em>}</li>
 *       <li>{@link DATypeAdapter#containsBDAWithEnumTypeID <em>Check whether TDAType contain contain TEnumType By Id</em>}</li>
 *    </ul>
 * </ol>
 */
public class DATypeAdapter extends AbstractDataTypeAdapter<TDAType>{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public DATypeAdapter(DataTypeTemplateAdapter parentAdapter, TDAType currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Completes recursively given summarized DataTypeTemplate information from BDAs
     * @param rDtt summarized DataTypeTemplate to complete
     * @return list of completed (updated) summarized DataTypeTemplate
     */
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


    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDAType().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("DAType[%s]",
                Utils.xpathAttributeFilter("id", currentElem.isSetId() ? currentElem.getId() : null));
    }

    /**
     * Gets all BDAs from current DAType
     * @return list of linked BDA as <em>BDAAdapter</em> object
     */
    public List<BDAAdapter> getBdaAdapters(){
        return currentElem.getBDA()
                .stream()
                .map(tbda -> new BDAAdapter(this,tbda))
                .collect(Collectors.toList());
    }

    /**
     * Gets BDA by name
     * @param bdaName BDA name
     * @return Optional <em>TBDA</em> object
     */
    public Optional<TBDA> getBDAByName(String bdaName) {
        for(TBDA tbda : currentElem.getBDA()){
            if(tbda.getName().equals(bdaName)){
                return Optional.of(tbda);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if current DAType contains BDA with specific EnumType
     * @param enumTypeId ID of EnumType in BDA to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsBDAWithEnumTypeID(String enumTypeId) {
        return currentElem.getBDA()
                .stream()
                .anyMatch(
                        bda -> TPredefinedBasicTypeEnum.ENUM.equals(bda.getBType()) &&
                                enumTypeId.equals(bda.getType())
                );
    }

    /**
     * Checks if current DAType contains StructBDA
     * @param daTypeId ID of DAType (which type is Struct)
     * @return <em>Boolean</em> value of check result
     */
    public Boolean containsStructBdaWithDATypeId(String daTypeId) {
        return currentElem.getBDA()
            .stream()
            .anyMatch(
                    bda -> bda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT) &&
                            daTypeId.equals(bda.getType())
            );
    }

    /**
     * Compares current DAType and given DAType
     * @param inputDAType DAType to compare with
     * @return <em>Boolean</em> value of comparison result
     */
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

    /**
     * Check if DaTypeName is correct and coherent with this DATypeAdapter
     * @param daTypeName string containing all BDA/DA names to check
     * @throws ScdException throws when DaTypeName structured names is not well-ordered
     */
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
     * @param filter filter for DO/SDO and DA/BDA
     * @return list of completed Resumed Data Type Templates beginning from this DoType (Do or SDO).
     */
    public List<ResumedDataTemplate> getResumedDTTs(ResumedDataTemplate rootRDTT, ResumedDataTemplate filter) {

        List<ResumedDataTemplate> resultRDTTs = new ArrayList<>();

        for(TBDA bda : currentElem.getBDA()){
            if(filter != null && filter.isDaNameDefined() &&
                    !filter.getBdaNames().contains(bda.getName())){
                continue;
            }
            ResumedDataTemplate currentRDTT = ResumedDataTemplate.copyFrom(rootRDTT);
            currentRDTT.setBType(bda.getBType().value());
            if(bda.getBType() == TPredefinedBasicTypeEnum.STRUCT) {
                currentRDTT.addDaStructName(bda.getName());
                parentAdapter.getDATypeAdapterById(bda.getType()).ifPresent(
                    daTypeAdapter -> resultRDTTs.addAll(daTypeAdapter.getResumedDTTs(currentRDTT, filter)));
            } else {
                currentRDTT.addDaStructName(bda.getName());
                currentRDTT.setType(bda.getType());
                currentRDTT.getDaName().setValImport(bda.isValImport());
                currentRDTT.getDaName().addDaiValues(bda.getVal());
                resultRDTTs.add(currentRDTT);
            }
        }
        return resultRDTTs;
    }

    /**
     * Gets DATypeAdapter by BDA name
     * @param name BDA name
     * @return Optional of <em>DATypeAdapter</em> object
     */
    public Optional<DATypeAdapter> getDATypeAdapterByBdaName(String name)  {
        Optional<TBDA> opBda = getBDAByName(name);
        if(opBda.isPresent()){
            return parentAdapter.getDATypeAdapterById(opBda.get().getType());
        }
        return Optional.empty();
    }

    /**
     * Gets linked DataTypeTemplateAdapter as parent
     * @return <em>DataTypeTemplateAdapter</em> object
     */
    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter;
    }

    /**
     * Gets BDAAdapter by name
     * @param name BDAAdapter name
     * @return Optiobnal of <em>BDAAdapter</em> object
     */
    public Optional<BDAAdapter> getBdaAdapterByName(String name) {
        Optional<TBDA> opBda = getBDAByName(name);
        if(opBda.isPresent()){
            return Optional.of(new BDAAdapter(this,opBda.get()));
        }
        return Optional.empty();
    }

    /**
     * A representation of the model object <em><b>{@link org.lfenergy.compas.scl2007b4.model.TBDA BDA}</b></em>.
     * <p>
     * The following features are supported:
     * </p>
     * <ol>
     *   <li>Adapter</li>
     *   <ul>
     *       <li>{@link BDAAdapter#getDataTypeTemplateAdapter <em>get DataTypeTemplateAdapter</em>}</li>
     *       <li>{@link BDAAdapter#getBdaAdapterByName <em>get BdaAdapter By Name</em>}</li>
     *       <li>{@link BDAAdapter#getBdaAdapters <em>get getBdaAdapters</em>}</li>
     *       <li>{@link BDAAdapter#getDATypeAdapterByBdaName <em>get DATypeAdapter By TBDA Name</em>}</li>
     *    </ul>
     *   <li>Functions</li>
     *    <ul>
     *      <li>{@link BDAAdapter#addPrivate <em>add Private</em>}</li>
     *      <li>{@link BDAAdapter#getBDAByName <em>get TBDA By Name</em>}</li>
     *      <li>{@link BDAAdapter#getResumedDTTByDaName <em>get ResumedDTT By DaTypeName</em>}</li>
     *      <li>{@link BDAAdapter#getResumedDTTs <em>get ResumedDTTs By Custom filter</em>}</li>
     *      <li>{@link BDAAdapter#completeResumedDTT <em>Construct and Complete ResumedDTTs</em>}</li>
     *    </ul>
     *   <li>Check rules</li>
     *    <ul>
     *       <li>{@link BDAAdapter#hasSameContentAs <em>Compare Two TBDA</em>}</li>
     *       <li>{@link BDAAdapter#check <em>Check structData from DaTypeName</em>}</li>
     *       <li>{@link BDAAdapter#containsStructBdaWithDATypeId <em>Check whether TBDA contain TBDA with Struct Btype By Id</em>}</li>
     *       <li>{@link BDAAdapter#containsBDAWithEnumTypeID <em>Check whether TBDA contain contain TEnumType By Id</em>}</li>
     *    </ul>
     * </ol>
     */
    @Getter
    public static class BDAAdapter extends AbstractDataAttributeAdapter<DATypeAdapter, TBDA>{

        /**
         * Constructor
         * @param parentAdapter Parent container reference
         * @param currentElem Current reference
         */
        protected BDAAdapter(DATypeAdapter parentAdapter, TBDA currentElem) {
            super(parentAdapter, currentElem);
        }

        /**
         * Check if node is child of the reference node
         * @return link parent child existence
         */
        @Override
        protected boolean amChildElementRef() {
            return parentAdapter.getCurrentElem().getBDA().contains(currentElem);
        }

        @Override
        protected String elementXPath() {
            return String.format("BDA[%s]",
                    Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
        }

    }
}
