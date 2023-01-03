// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TLNodeType LNodeType}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *       <li>{@link LNodeTypeAdapter#getDataTypeTemplateAdapter() <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *       <li>{@link LNodeTypeAdapter#getDOAdapterByName <em>Returns the value of the <b>DOAdapter </b> by <b>DO </b> name </em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LNodeTypeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getDOTypeId <em>Returns the value of the <b>type </b>attribute By DOType Id</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getId() <em>Returns the value of the <b>id </b>attribute</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getLNClass <em>Returns the value of the <b>lnClass </b>attribute</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getResumedDTTs <em>Returns <b>ResumedDataTemplate </b> list</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link LNodeTypeAdapter#hasSameContentAs <em>Compare Two TLNodeType</em>}</li>
 *       <li>{@link LNodeTypeAdapter#containsDOWithDOTypeId <em>Check whether TLNodeType contain TDO By Id</em>}</li>
 *    </ul>
 * </ol>
 */
@Slf4j
public class LNodeTypeAdapter
        extends SclElementAdapter <DataTypeTemplateAdapter, TLNodeType>
        implements IDataTemplate,IDTTComparable<TLNodeType> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public LNodeTypeAdapter(DataTypeTemplateAdapter parentAdapter, TLNodeType currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getLNodeType().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("LNodeType[%s and %s]",
                Utils.xpathAttributeFilter("id", currentElem.isSetId() ? currentElem.getId() : null),
                Utils.xpathAttributeFilter("lnClass", currentElem.isSetLnClass() ? currentElem.getLnClass() : null));
    }

    /**
     * Compares current LNodeType and given LNodeType
     * @param tlNodeType LNodeType to compare with
     * @return <em>Boolean</em> value of comparison result
     */
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

    /**
     * Checks if current LNodeType contains DO with specific DOTYpe ID
     * @param doTypeId ID of DOType in DO to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsDOWithDOTypeId(String doTypeId) {
        return currentElem.getDO().stream()
                .anyMatch(tdo -> tdo.getType().equals(doTypeId));
    }

    /**
     * Gets LnClass value
     * @return LnClass Value
     */
    public String getLNClass() {
        if(!currentElem.getLnClass().isEmpty()){
            return currentElem.getLnClass().get(0);
        }
        return null;
    }

    /**
     * Gets DOType ID from current LNodeType
     * @param doName name of DO for which ID is search
     * @return optional of <em>Boolean</em> value
     */
    public Optional<String> getDOTypeId(String doName){
        return currentElem.getDO()
                .stream()
                .filter(tdo -> doName.equals(Utils.removeTrailingDigits(tdo.getName())))
                .map(TDO::getType)
                .findFirst();
    }

    /**
     * return a list of summarized Resumed Data Type Templates beginning from given this LNodeType.
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     * @param filter filter for LNodeType
     * @return list of completed Resumed Data Type Templates beginning from this LNodeType.
     */
    public List<ResumedDataTemplate> getResumedDTTs(@NonNull ResumedDataTemplate filter)  {

        List<ResumedDataTemplate> resumedDataTemplates = new ArrayList<>();
        if(filter.isDaNameDefined()) {
            try {
                check(filter.getDoName(),filter.getDaName());
            } catch (ScdException e){
                log.error(e.getMessage());
                return resumedDataTemplates;
            }
        }
        ResumedDataTemplate rootRDTT = new ResumedDataTemplate();
        rootRDTT.setLnType(currentElem.getId());
        rootRDTT.setLnClass(filter.getLnClass());
        rootRDTT.setLnInst(filter.getLnInst());
        rootRDTT.setPrefix(filter.getPrefix());

        for(TDO tdo : currentElem.getDO()){
            if(filter.isDoNameDefined() &&
                    !filter.getDoName().getName().equals(tdo.getName())){
                continue;
            }

            parentAdapter.getDOTypeAdapterById(tdo.getType()).ifPresent(
                doTypeAdapter -> {
                    ResumedDataTemplate currentRDTT = ResumedDataTemplate.copyFrom(rootRDTT);
                    currentRDTT.getDoName().setName(tdo.getName());
                    currentRDTT.getDoName().setCdc(doTypeAdapter.getCdc());
                    resumedDataTemplates.addAll(doTypeAdapter.getResumedDTTs(currentRDTT, filter));
                }
            ); // else this should never happen or the scd won't be built in the first place and we'd never be here
        }
        return resumedDataTemplates;
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
     * Gets DO from current LNodeType
     * @param name name of DO to find
     * @return optional of <em>DOAdapter</em> adapter
     */
    public Optional<DOAdapter> getDOAdapterByName(String name) {
        for(TDO tdo : currentElem.getDO()){
            if(tdo.getName().equals(name)){
                return Optional.of(new DOAdapter(this,tdo));
            }
        }
        return Optional.empty();
    }

    /**
     * Find path from a DO to DA (defined by names)
     * @param doName DO from which find a path
     * @param daName DA for which find a path to
     * @return pair of DO name and  DOType.
     * @throws ScdException when inconsistency are found in th SCL's
     *                     DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
    Pair<String,DOTypeAdapter> findPathFromDo2DA(String doName, String daName) throws ScdException {
        DOAdapter doAdapter = getDOAdapterByName(doName).orElseThrow();
        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
        if(doTypeAdapter.containsDAWithDAName(doName)){
            return Pair.of(doName,doTypeAdapter);
        }
        return doTypeAdapter.findPathDoTypeToDA(daName);
    }


    /**
     * Check if DoTypeName and DaTypeName are correct and coherent with this LNodeTypeAdapter
     * @param doTypeName DO/SDO to check
     * @param daTypeName DA/BDA to check
     * @throws ScdException when inconsistency are found in th SCL's
     *                     DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
    public void check(@NonNull DoTypeName doTypeName, @NonNull DaTypeName daTypeName) throws ScdException {
        if(!doTypeName.isDefined() || !daTypeName.isDefined() ){
            throw new ScdException("Invalid Data: data attributes information are missing");
        }
        // check Data Object information
        DOAdapter doAdapter = this.getDOAdapterByName(doTypeName.getName()).orElseThrow(
            () -> new ScdException(
                String.format("Unknown DO(%s) in LNodeType(%s)",doTypeName.getName(), currentElem.getId())
            )
        );

        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow(
            () -> new ScdException("Corrupted SCL DataTypeTemplate, Unknown DOType id: " + doAdapter.getType())
        );

        Pair<String, DOTypeAdapter> adapterPair = doTypeAdapter.checkAndCompleteStructData(doTypeName)
                .orElse(null);

        // check coherence between Data Object and Data Attributes information
        DOTypeAdapter lastDoTypeAdapter;
        if(adapterPair == null){
            adapterPair = findPathFromDo2DA(doTypeName.getName(),daTypeName.getName());
            lastDoTypeAdapter = adapterPair.getValue();
        } else {
            if(adapterPair.getRight().containsDAWithDAName(daTypeName.getName())){
                lastDoTypeAdapter = adapterPair.getValue();
            } else {
                adapterPair = adapterPair.getRight().findPathDoTypeToDA(daTypeName.getName());
                lastDoTypeAdapter = adapterPair.getValue();
            }
        }

        DAAdapter daAdapter = lastDoTypeAdapter.getDAAdapterByName(daTypeName.getName())
            .orElseThrow(
                ()-> new ScdException(
                    String.format("Unknown DA (%s) in DOType (%s) ", daTypeName.getName(), "leafSdoId")
                )
            );

        // check Data Attributes
        if(!daTypeName.getStructNames().isEmpty() && daAdapter.getBType() != TPredefinedBasicTypeEnum.STRUCT){
            throw new ScdException("Invalid DA chain" +  daTypeName);
        }

        if(daTypeName.getStructNames().isEmpty()){
            daAdapter.check(daTypeName);
        } else {
            daTypeName.setFc(daAdapter.getCurrentElem().getFc());
            DATypeAdapter daTypeAdapter = parentAdapter.getDATypeAdapterById(daAdapter.getType()).orElseThrow(
                () -> new ScdException(
                        String.format("Unknown DAType (%s) referenced by DA(%s)", daAdapter.getType(), daAdapter.getName())
                )
            );
            daTypeAdapter.check(daTypeName);
        }
    }

    /**
     * Gets list of summarized data type template from DaTypeName
     * @param daTypeName DaTypeName from which summarized data type templates are created
     * @return list of <em>ResumedDataTemplate</em> object
     */
    public List<ResumedDataTemplate> getResumedDTTByDaName(DaTypeName daTypeName) throws ScdException {
        Optional<ResumedDataTemplate> opRDtt;
        List<ResumedDataTemplate> rDtts = new ArrayList<>();
        for(TDO tdo : currentElem.getDO()){
            DOAdapter doAdapter = new DOAdapter(this,tdo);
            DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
            ResumedDataTemplate rDtt = new ResumedDataTemplate();
            rDtt.setLnType(currentElem.getId());
            rDtt.getDoName().setName(doAdapter.getCurrentElem().getName());

            opRDtt = doTypeAdapter.getResumedDTTByDaName(daTypeName, rDtt);
            opRDtt.ifPresent(rDtts::add);
        }
        return rDtts;
    }

    /**
     * Gets list of summarized data type template from DoTypeName
     * @param doTypeName DoTypeName from which summarized data type templates are created
     * @return list of <em>ResumedDataTemplate</em> object
     */
    public List<ResumedDataTemplate> getResumedDTTByDoName(DoTypeName doTypeName) {


        DOAdapter doAdapter = getDOAdapterByName(doTypeName.getName()).orElseThrow();
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        rDtt.getDoName().setName(doTypeName.getName());
        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
        return doTypeAdapter.getResumedDTTByDoName(doTypeName,0,rDtt);

    }

    /**
     * Gets current LNodeType ID
     * @return LNodeType ID
     */
    public String getId() {
        return currentElem.getId();
    }

    /**
     * Find binded DOType info
     * @param signalInfo extRef signal info for binding
     * @return DOType info as object contening name, id and adapter
     * @throws ScdException throws when DO unknown
     */
    public DataTypeTemplateAdapter.DOTypeInfo findMatchingDOType(ExtRefSignalInfo signalInfo)  throws ScdException{
        DoTypeName doName = new DoTypeName(signalInfo.getPDO());
        String extDoName = Utils.removeTrailingDigits(doName.getName());
        String doTypeId = getDOTypeId(extDoName).orElseThrow(() ->
                new IllegalArgumentException("Unknown doName :" + signalInfo.getPDO()));
        DOTypeAdapter doTypeAdapter = this.getParentAdapter().getDOTypeAdapterById(doTypeId).orElseThrow(() ->
                new IllegalArgumentException(String.format("%s: No referenced to DO id : %s, scl file not valid", doName, doTypeId)));
        doTypeAdapter.checkAndCompleteStructData(doName);
        return new DataTypeTemplateAdapter.DOTypeInfo(doName, doTypeId, doTypeAdapter);
    }
}
