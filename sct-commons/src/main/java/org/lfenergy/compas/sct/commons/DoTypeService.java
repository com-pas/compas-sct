// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.SerializationUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.DaDomain;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DoTypeService {

    final DaTypeService daTypeService =  new DaTypeService();
    final SDOOrDAService sdoOrDAService =  new SDOOrDAService();
    final BDAService bdaService =  new BDAService();

    public Stream<TDOType> getDoTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getDOType().stream();
    }

    public Stream<TDOType> getFilteredDoTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TDOType> tdoTypePredicate) {
        return getDoTypes(tDataTypeTemplates).filter(tdoTypePredicate);
    }

    public Optional<TDOType> findDoType(TDataTypeTemplates tDataTypeTemplates, Predicate<TDOType> tdoTypePredicate) {
        return getFilteredDoTypes(tDataTypeTemplates, tdoTypePredicate).findFirst();
    }

    public List<DataAttributeRef> getAllSDOAndDA(TDataTypeTemplates dtt, TDOType tdoType, DataAttributeRef filter) {
        List<DataAttributeRef> result = new ArrayList<>();
        // DA -> BDA -> BDA..
        sdoOrDAService.getDAs(tdoType).forEach(tda -> {
            DataAttributeRef newDataObjectRef = DataAttributeRef.copyFrom(filter);
            newDataObjectRef.getDaName().setName(tda.getName());
            if(tda.isSetFc()) {
                newDataObjectRef.getDaName().setFc(tda.getFc());
            }

            // STRUCT type (BType=STRUCT) refer to BDA, otherwise it is DA
            if(tda.isSetType() && tda.isSetBType() && tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                daTypeService.findDaType(dtt, tdaType -> tdaType.getId().equals(tda.getType()))
                        .ifPresent(nextDaType -> result.addAll(getDataAttributesFromBDA(dtt, nextDaType, newDataObjectRef)));
            } else {
                newDataObjectRef.setDaName(updateDaNameFromDaOrBda(tda, newDataObjectRef.getDaName()));
                result.add(newDataObjectRef);
            }
        });
        // SDO -> SDO -> SDO..
        sdoOrDAService.getSDOs(tdoType)
                .forEach(tsdo -> findDoType(dtt, tdoType1 -> tsdo.isSetType() && tdoType1.getId().equals(tsdo.getType()))
                        .ifPresent(nextDoType -> {
                            DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(filter);
                            newDataAttributeRef.getDoName().getStructNames().add(tsdo.getName());
                            if(nextDoType.isSetCdc()) {
                                newDataAttributeRef.getDoName().setCdc(nextDoType.getCdc());
                            }
                            result.addAll(getAllSDOAndDA(dtt, nextDoType, newDataAttributeRef));
                        }));
        return result;
    }

    public List<DoLinkedToDa> getAllSDOLinkedToDa(TDataTypeTemplates dtt, TDOType tdoType, DoLinkedToDa filtredDoLinkedToDa) {
        List<DoLinkedToDa> result = new ArrayList<>();
        // DA -> BDA -> BDA..
        sdoOrDAService.getDAs(tdoType).forEach(tda -> {
            DoLinkedToDa doLinkedToDa = SerializationUtils.clone(filtredDoLinkedToDa);
            doLinkedToDa.getDaDomain().setDaName(tda.getName());
            if(tda.isSetFc()) {
                doLinkedToDa.getDaDomain().setFc(tda.getFc());
            }

            // STRUCT type (BType=STRUCT) refer to BDA, otherwise it is DA
            if(tda.isSetType() && tda.isSetBType() && tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                daTypeService.findDaType(dtt, tdaType -> tdaType.getId().equals(tda.getType()))
                        .ifPresent(nextDaType -> result.addAll(getDaLinkedToBDA(dtt, nextDaType, doLinkedToDa)));
            } else {
                doLinkedToDa.setDaDomain(updateDaNameFromDaOrBda(tda, doLinkedToDa.getDaDomain()));
                result.add(doLinkedToDa);
            }
        });
        // SDO -> SDO -> SDO..
        sdoOrDAService.getSDOs(tdoType)
                .forEach(tsdo -> findDoType(dtt, tdoType1 -> tsdo.isSetType() && tdoType1.getId().equals(tsdo.getType()))
                        .ifPresent(nextDoType -> {
                            DoLinkedToDa newDoLinkedToDa = SerializationUtils.clone(filtredDoLinkedToDa);
                            newDoLinkedToDa.getDoDomain().getSdoNames().add(tsdo.getName());
                            if(nextDoType.isSetCdc()) {
                                newDoLinkedToDa.getDoDomain().setCdc(nextDoType.getCdc());
                            }
                            result.addAll(getAllSDOLinkedToDa(dtt, nextDoType, newDoLinkedToDa));
                        }));
        return result;
    }
    private List<DoLinkedToDa> getDaLinkedToBDA(TDataTypeTemplates dtt, TDAType tdaType1, DoLinkedToDa filtredDoLinkedToDa) {
        List<DoLinkedToDa> result = new ArrayList<>();
        // BDA -> BDA -> BDA..
        bdaService.getBDAs(tdaType1).forEach(tbda -> {
            DoLinkedToDa doLinkedToDa = SerializationUtils.clone(filtredDoLinkedToDa);
            doLinkedToDa.getDaDomain().getBdaNames().add(tbda.getName());

            // STRUCT type (BType=STRUCT) refer to complex BDA object, otherwise it is kind of DA object
            if(tbda.isSetType() && tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)){
                daTypeService.findDaType(dtt, tdaType -> tdaType.getId().equals(tbda.getType()))
                        .ifPresent(nextDaType -> result.addAll(getDaLinkedToBDA(dtt, nextDaType, doLinkedToDa)));
            } else {
                doLinkedToDa.setDaDomain(updateDaNameFromDaOrBda(tbda, doLinkedToDa.getDaDomain()));
                result.add(doLinkedToDa);
            }
        });
        return result;
    }

    private List<DataAttributeRef> getDataAttributesFromBDA(TDataTypeTemplates dtt, TDAType tdaType1, DataAttributeRef dataAttributeRef) {
        List<DataAttributeRef> result = new ArrayList<>();
        // BDA -> BDA -> BDA..
        bdaService.getBDAs(tdaType1).forEach(tbda -> {
            DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(dataAttributeRef);
            newDataAttributeRef.getDaName().getStructNames().add(tbda.getName());

            // STRUCT type (BType=STRUCT) refer to complex BDA object, otherwise it is kind of DA object
            if(tbda.isSetType() && tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)){
                daTypeService.findDaType(dtt, tdaType -> tdaType.getId().equals(tbda.getType()))
                        .ifPresent(nextDaType -> result.addAll(getDataAttributesFromBDA(dtt, nextDaType, newDataAttributeRef)));
            } else {
                newDataAttributeRef.setDaName(updateDaNameFromDaOrBda(tbda, newDataAttributeRef.getDaName()));
                result.add(newDataAttributeRef);
            }
        });
        return result;
    }

    private DaTypeName updateDaNameFromDaOrBda(TAbstractDataAttribute daOrBda, DaTypeName daTypeName) {
        if (daOrBda.isSetType()) daTypeName.setType(daOrBda.getType());
        if (daOrBda.isSetBType()) daTypeName.setBType(daOrBda.getBType());
        if (daOrBda.isSetValImport()) daTypeName.setValImport(daOrBda.isValImport());
        if (daOrBda.isSetVal()) daTypeName.addDaiValues(daOrBda.getVal());
        return daTypeName;
    }

    private DaDomain updateDaNameFromDaOrBda(TAbstractDataAttribute daOrBda, DaDomain daDomain) {
        if (daOrBda.isSetType()) daDomain.setType(daOrBda.getType());
        if (daOrBda.isSetBType()) daDomain.setBType(daOrBda.getBType());
        if (daOrBda.isSetValImport()) daDomain.setValImport(daOrBda.isValImport());
        if (daOrBda.isSetVal()) daDomain.addDaiValues(daOrBda.getVal());
        return daDomain;
    }

}