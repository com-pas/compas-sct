// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class LNodeTypeAdapter
        extends SclElementAdapter <DataTypeTemplateAdapter, TLNodeType>
        implements IDataTemplate,IDTTComparable<TLNodeType> {

    public LNodeTypeAdapter(DataTypeTemplateAdapter parentAdapter, TLNodeType currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getLNodeType().contains(currentElem);
    }

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

    public boolean containsDOWithDOTypeId(String doTypeId) {
        return currentElem.getDO().stream()
                .anyMatch(tdo -> tdo.getType().equals(doTypeId));
    }

    public String getLNClass() {
        if(!currentElem.getLnClass().isEmpty()){
            return currentElem.getLnClass().get(0);
        }
        return null;
    }
    public Optional<String> getDOTypeId(String doName){
        return currentElem.getDO()
                .stream()
                .filter(tdo -> doName.equals(tdo.getName()))
                .map(TDO::getType)
                .findFirst();
    }

    public List<ResumedDataTemplate> getResumedDTTs(ResumedDataTemplate filter) throws ScdException {

        List<ResumedDataTemplate> resumedDataTemplates = new ArrayList<>();
        if(filter.isDaNameDefined()) {
            check(filter.getDoName(),filter.getDaName());
        }
        ResumedDataTemplate rootResumedRTT = new ResumedDataTemplate();
        rootResumedRTT.setLnType(currentElem.getId());
        if(filter != null) {
            rootResumedRTT.setLnClass(filter.getLnClass());
            rootResumedRTT.setLnInst(filter.getLnInst());
            rootResumedRTT.setPrefix(filter.getPrefix());
        }

        for(TDO tdo : currentElem.getDO()){
            if(filter != null && filter.isDoNameDefined() &&
                    !filter.getDoName().getName().equals(tdo.getName())){
                continue;
            }

            rootResumedRTT.setDoName(tdo.getName());
            DOTypeAdapter doTypeAdapter = parentAdapter.getDOTypeAdapterById(tdo.getType()).orElse(null);
            if(doTypeAdapter != null){
                rootResumedRTT.getDoName().setCdc(doTypeAdapter.getCdc());
                List<ResumedDataTemplate> rDTTList = doTypeAdapter.getResumedDTTs(rootResumedRTT,new HashSet<>(), filter);
                resumedDataTemplates.addAll(rDTTList);
            } // else this should never happen or the scd won't be built in the first place and we'd never be here
            // may be use an assert here to enforce constrain
        }
        return resumedDataTemplates;
    }


    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter;
    }

    public Optional<DOAdapter> getDOAdapterByName(String name) {
        for(TDO tdo : currentElem.getDO()){
            if(tdo.getName().equals(name)){
                return Optional.of(new DOAdapter(this,tdo));
            }
        }
        return Optional.empty();
    }

    Pair<String,DOTypeAdapter> findPathFromDo2DA(String doName, String daName) throws ScdException {
        DOAdapter doAdapter = getDOAdapterByName(doName).orElseThrow();
        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
        if(doTypeAdapter.containsDAWithDAName(daName)){
            return Pair.of(daName,doTypeAdapter);
        }
        return doTypeAdapter.findPathDoType2DA(daName);
    }


    public void check(DoTypeName doTypeName, DaTypeName daTypeName) throws ScdException {
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
        String lastSdo;
        if(adapterPair == null){
            adapterPair = findPathFromDo2DA(doTypeName.getName(),daTypeName.getName());
            lastDoTypeAdapter = adapterPair.getValue();
            lastSdo = adapterPair.getKey();
        } else {
            lastSdo = adapterPair.getKey();
            if(adapterPair.getRight().containsDAWithDAName(daTypeName.getName())){
                lastDoTypeAdapter = adapterPair.getValue();
            } else {
                adapterPair = adapterPair.getRight().findPathDoType2DA(lastSdo);
                lastDoTypeAdapter = adapterPair.getValue();
                lastSdo = adapterPair.getKey();
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
            DATypeAdapter daTypeAdapter = parentAdapter.getDATypeAdapterById(daAdapter.getType()).orElseThrow(
                () -> new ScdException(
                        String.format("Unknown DAType (%s) referenced by DA(%s)", daAdapter.getType(), daAdapter.getName())
                )
            );
            daTypeAdapter.check(daTypeName);
        }
    }
}
