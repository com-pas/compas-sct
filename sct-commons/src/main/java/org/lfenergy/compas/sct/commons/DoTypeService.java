// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;

import java.sql.SQLData;
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

    public List<DataAttributeRef> getAllSDOAndDA(TDataTypeTemplates dtt, TDOType tdoType, DataAttributeRef dataRef) {
        List<DataAttributeRef> result = new ArrayList<>();
        // DA -> BDA -> BDA..
        sdoOrDAService.getSDOOrDAs(tdoType, TDA.class).forEach(tda -> {
            DataAttributeRef newDataObjectRef = DataAttributeRef.copyFrom(dataRef);
            newDataObjectRef.getDaName().setName(tda.getName());
            if(tda.isSetFc()) newDataObjectRef.getDaName().setFc(tda.getFc());

            // STRUCT type (BType=STRUCT) refer to BDA, otherwise it is DA
            if(tda.isSetBType() && tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                daTypeService.findDaType(dtt, tdaType -> tda.isSetType() && tdaType.getId().equals(tda.getType()))
                        .ifPresent(nextDaType -> result.addAll(getDataAttributesFromBDA(dtt, nextDaType, newDataObjectRef)));
            } else {
                updateFromAbstractDataAttribute(tda, newDataObjectRef.getDaName());
                result.add(newDataObjectRef);
            }
        });
        // SDO -> SDO -> SDO..
        sdoOrDAService.getSDOOrDAs(tdoType, TSDO.class)
                .forEach(tsdo -> findDoType(dtt, tdoType1 -> tsdo.isSetType() && tdoType1.getId().equals(tsdo.getType()))
                        .ifPresent(nextDoType -> {
                            DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(dataRef);
                            newDataAttributeRef.getDoName().getStructNames().add(tsdo.getName());
                            if(nextDoType.isSetCdc()) newDataAttributeRef.getDoName().setCdc(nextDoType.getCdc());

                            result.addAll(getAllSDOAndDA(dtt, nextDoType, newDataAttributeRef));
                        }));
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
                updateFromAbstractDataAttribute(tbda, newDataAttributeRef.getDaName());
                result.add(newDataAttributeRef);
            }
        });
        return result;
    }

    public List<DataAttributeRef> getFilteredSDOAndDA(TDataTypeTemplates dtt, TDOType tdoType, DataAttributeRef dataObjectRef) {

        List<DataAttributeRef> result = new ArrayList<>();

        tdoType.getSDOOrDA()
                .forEach(tUnNaming -> {
                    // Filter SDO -> SDO -> SDO..
                    if(tUnNaming.getClass().equals(TSDO.class)){
                        TSDO tsdo0 = (TSDO)tUnNaming ;
                        List<DataAttributeRef> filteredSDO = getFilteredSDO(dtt, tdoType, dataObjectRef,
                                tsdo -> dataObjectRef.getDoName().getStructNames().contains(tsdo0.getName())
                        );
//                                .peek(dataAttributeRef -> {
//                                    System.out.println(" Before [getFilteredSDO] doRef : "+dataAttributeRef.getDoRef());
//                                    System.out.println(" Before [getFilteredSDO] daRef : "+dataAttributeRef.getDaRef());
//                                    System.out.println(" ====== ");
//                                })
                        result.addAll(filteredSDO);
                    }
                    if(tUnNaming.getClass().equals(TDA.class)){
                        TDA tda0 = (TDA)tUnNaming ;
                        DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(dataObjectRef);
//                        newDataAttributeRef.getDoName().getStructNames().add(tda0.getName());
                        List<DataAttributeRef> filteredDABDA = getFilteredDA(dtt, tdoType, newDataAttributeRef,
                                tda -> newDataAttributeRef.getDaName().getStructNames().isEmpty())
                                .toList();
                        result.addAll(filteredDABDA);
                    }
                });
        return result;
    }

    public List<DataAttributeRef> getFilteredSDO(TDataTypeTemplates dtt, TDOType tdoType,
                                                 DataAttributeRef dataObjectRef, Predicate<TSDO> tsdoPredicate) {
        List<DataAttributeRef> result = new ArrayList<>();

        // SDO -> SDO -> SDO..
        sdoOrDAService.getFilteredSDOOrDAs(tdoType, TSDO.class, tsdoPredicate)
                .findFirst()
                .ifPresentOrElse(tsdo -> {
                    findDoType(dtt, tdoType1 -> tsdo.isSetType() && tdoType1.getId().equals(tsdo.getType()))
                            .ifPresent(nextDoType -> {
                                DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(dataObjectRef);
//                                newDataAttributeRef.getDoName().getStructNames().add(tsdo.getName());
                                if (!newDataAttributeRef.getDoName().getStructNames().contains(tsdo.getName()))
                                    newDataAttributeRef.getDoName().getStructNames().add(tsdo.getName());
                                if (nextDoType.isSetCdc()) newDataAttributeRef.getDoName().setCdc(nextDoType.getCdc());
                                result.addAll(
                                        getFilteredSDOAndDA(dtt, nextDoType, newDataAttributeRef).stream()
                                                .toList()

                                );
                            });
                }, ()-> {
                    sdoOrDAService.getSDOOrDAs(tdoType, TSDO.class)
                            .forEach(tsdo -> findDoType(dtt, tdoType1 -> tsdo.isSetType() && tdoType1.getId().equals(tsdo.getType()))
                                    .ifPresent(nextDoType -> {
                                        DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(dataObjectRef);
                                        if (!newDataAttributeRef.getDoName().getStructNames().contains(tsdo.getName()))
                                            newDataAttributeRef.getDoName().getStructNames().add(tsdo.getName());
//                                        newDataAttributeRef.getDoName().getStructNames().add(tsdo.getName());
                                        if(nextDoType.isSetCdc()) newDataAttributeRef.getDoName().setCdc(nextDoType.getCdc());
                                        result.addAll(
                                                //TODO
                                                getAllSDOAndDA(dtt, nextDoType, newDataAttributeRef)
                                        );
                                    }));
                });
        return result;
    }

    public Stream<DataAttributeRef> getFilteredDA(TDataTypeTemplates dtt, TDOType tdoType,
                                                   DataAttributeRef dataObjectRef, Predicate<TDA> tdaPredicate) {
        List<DataAttributeRef> result = new ArrayList<>();
        // DA -> BDA -> BDA..
       return sdoOrDAService.getFilteredSDOOrDAs(tdoType, TDA.class, tdaPredicate)
               .flatMap(tda -> {
                   System.out.println(" [da] : "+tda.getName());
                   System.out.println(" [da] getStructNames : "+dataObjectRef.getDaName().getStructNames());
                   DataAttributeRef newDataObjectRef = DataAttributeRef.copyFrom(dataObjectRef);
                   newDataObjectRef.getDaName().setName(tda.getName());
                   if(tda.isSetFc()) newDataObjectRef.getDaName().setFc(tda.getFc());

                    // STRUCT type (BType=STRUCT) refer to BDA, otherwise it is DA
                   if(tda.isSetBType() && tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                       return daTypeService.getFilteredDaTypes(dtt, tdaType -> tda.isSetType() && tdaType.getId().equals(tda.getType()))
                               .flatMap(nextDaType -> {
                                   return getFilteredDataAttributesFromBDA(dtt,
                                           nextDaType,
                                           newDataObjectRef,
                                            tbda -> newDataObjectRef.getDaName()
                                                    .getStructNames().contains(tbda.getName()));
                                   });
                    } else {
                        System.out.println(" [da] SAMPLE : "+tda.getName());
                        updateFromAbstractDataAttribute(tda, newDataObjectRef.getDaName());
                        result.add(newDataObjectRef);
                        return result.stream();
                    }
        });
    }

    private Stream<DataAttributeRef> getFilteredDataAttributesFromBDA(TDataTypeTemplates dtt, TDAType tdaType1,
                                                                    DataAttributeRef dataAttributeRef,
                                                                    Predicate<TBDA> tbdaPredicate) {
        List<DataAttributeRef> result = new ArrayList<>();
        // BDA -> BDA -> BDA..
       return bdaService.getFilteredBDAs(tdaType1, tbdaPredicate)
               .flatMap(tbda -> {
                    DataAttributeRef newDataAttributeRef = DataAttributeRef.copyFrom(dataAttributeRef);
                    newDataAttributeRef.getDaName().getStructNames().add(tbda.getName());
                    // STRUCT type (BType=STRUCT) refer to complex BDA object, otherwise it is kind of DA object
                    if(tbda.isSetType() && tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)){
                        return daTypeService.getFilteredDaTypes(dtt, tdaType -> tdaType.getId().equals(tbda.getType()))
                                .flatMap(nextDaType ->
                                        getFilteredDataAttributesFromBDA(dtt, nextDaType, newDataAttributeRef, tbdaPredicate)
                                );
                    }
                    else {
                        updateFromAbstractDataAttribute(tbda, newDataAttributeRef.getDaName());
                        result.add(newDataAttributeRef);
                        return result.stream();
                    }
               });
    }

    private <T extends TAbstractDataAttribute> void updateFromAbstractDataAttribute(T daOrBda, DaTypeName daTypeName) {
        if(daOrBda.isSetType()) daTypeName.setType(daOrBda.getType());
        if(daOrBda.isSetBType()) daTypeName.setBType(daOrBda.getBType());
        if(daOrBda.isSetValImport()) daTypeName.setValImport(daOrBda.isValImport());
        if(daOrBda.isSetVal()) daTypeName.addDaiValues(daOrBda.getVal());
    }
}