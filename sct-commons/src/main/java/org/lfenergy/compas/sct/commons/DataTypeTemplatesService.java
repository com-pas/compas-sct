// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.RequiredArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DataObject;
import org.lfenergy.compas.sct.commons.domain.DataRef;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum.STRUCT;

@RequiredArgsConstructor
public class DataTypeTemplatesService implements DataTypeTemplateReader {

    /**
     * Create a new instance without passing dependency.
     * This will create a new instance of each dependency
     */
    public DataTypeTemplatesService() {
        this(new LnodeTypeService(), new DoTypeService(new DaTypeService(), new DaService(), new SdoService(), new BDAService()), new DaTypeService(), new DoService(), new DaService(), new SdoService(), new BDAService());
    }

    final LnodeTypeService lnodeTypeService;
    final DoTypeService doTypeService;
    final DaTypeService daTypeService;
    final DoService doService;
    final DaService daService;
    final SdoService sdoService;
    final BDAService bdaService;

    @Override
    public Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt) {
        return lnodeTypeService.getLnodeTypes(dtt)
                .flatMap(tlNodeType -> tlNodeType.getDO()
                        .stream()
                        .flatMap(tdo -> getAllDoLinkedToDa(dtt, tdo)));
    }

    @Override
    public Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId) {
        // Filter on LNodeType.Id
        return lnodeTypeService.findLnodeType(dtt, lNodeTypeId)
                .stream()
                .flatMap(tlNodeType -> doService.getDos(tlNodeType)
                        .flatMap(tdo -> getAllDoLinkedToDa(dtt, tdo)));
    }

    @Override
    public Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, String doName) {
        // Filter on LNodeType.Id
        return lnodeTypeService.findLnodeType(dtt, lNodeTypeId)
                .stream()
                // Filter on DO.name
                .flatMap(tlNodeType -> doService.getFilteredDos(tlNodeType, tdo -> tdo.getName().equals(doName)))
                .flatMap(tdo -> getAllDoLinkedToDa(dtt, tdo));
    }

    private Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt, TDO tdo) {
        DataObject dataObject = new DataObject();
        dataObject.setDoName(tdo.getName());
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, new DataAttribute());
        return doTypeService.findDoType(dtt, tdo.getType())
                .stream()
                .flatMap(tdoType -> {
                    doLinkedToDa.dataObject().setCdc(tdoType.getCdc());
                    return doTypeService.getAllSDOLinkedToDa(dtt, tdoType, doLinkedToDa).stream();
                });
    }

    @Override
    public Optional<DoLinkedToDa> findDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, DataRef dataRef) {
        return lnodeTypeService.findLnodeType(dtt, lNodeTypeId)
                .flatMap(lNodeType -> doService.findDo(lNodeType, dataRef.doName())
                        // Search DoType for each DO
                        .flatMap(tdo -> doTypeService.findDoType(dtt, tdo.getType())
                                .flatMap(tdoType -> {
                                    // Search last DoType from DOType (SDO) > DOType (SDO)
                                    Optional<TDOType> optLastDoType = findDOTypeBySdoName(dtt, tdoType, dataRef.sdoNames());
                                    if (optLastDoType.isEmpty()) {
                                        // dataRef.sdoNames() is not empty and we didn't find one of the SDO
                                        return Optional.empty();
                                    }
                                    // Prepare DataObject
                                    DataObject dataObject = new DataObject(tdo.getName(), tdoType.getCdc(), dataRef.sdoNames());
                                    // Search first DA from last DoType
                                    return daService.findDA(optLastDoType.get(), dataRef.daName())
                                            .flatMap(tda -> {
                                                // Prepare DataAttribute
                                                DataAttribute dataAttribute = new DataAttribute();
                                                dataAttribute.setDaName(tda.getName());
                                                dataAttribute.setFc(tda.getFc());
                                                // case dataRef.bdaNames() is empty
                                                if (dataRef.bdaNames().isEmpty()) {
                                                    if (tda.getBType() != STRUCT) {
                                                        // Check that DA is not STRUCT
                                                        dataAttribute.addDaVal(tda.getVal());
                                                        dataAttribute.setBType(tda.getBType());
                                                        dataAttribute.setType(tda.getType());
                                                        dataAttribute.setValImport(tda.isValImport());
                                                        dataAttribute.setValKind(tda.getValKind());
                                                        return Optional.of(new DoLinkedToDa(dataObject, dataAttribute));
                                                    }
                                                    return Optional.empty(); // the DA has a bType STRUCT so it has BDA
                                                }
                                                // case dataRef.bdaNames() is not empty
                                                if (tda.getBType() != STRUCT) {
                                                    return Optional.empty();
                                                }
                                                // Search first DaType from DOType (from last DOType where DA is STRUCT)
                                                return getDATypeByDaName(dtt, optLastDoType.get(), tda.getName())
                                                        .flatMap(tdaType -> {
                                                            // Search last STRUCT DAType
                                                            Optional<TDAType> optLastStructDAType = findDATypeByBdaName(dtt, tdaType, tbda -> tbda.isSetBType()
                                                                                                                                              && tbda.getBType() == STRUCT, dataRef.bdaNames().subList(0, dataRef.bdaNames().size() - 1));
                                                            if (optLastStructDAType.isEmpty()) {
                                                                return Optional.empty(); // One of the BDA is missing
                                                            }
                                                            String lastBdaName = dataRef.bdaNames().getLast();
                                                            return bdaService.findBDA(optLastStructDAType.get(), tbda -> tbda.getName().equals(lastBdaName) && tbda.getBType() != STRUCT)
                                                                    .flatMap(tbda -> {
                                                                        dataAttribute.getBdaNames().addAll(dataRef.bdaNames());
                                                                        dataAttribute.setBType(tbda.getBType());
                                                                        dataAttribute.setType(tbda.getType());
                                                                        dataAttribute.setValImport(tbda.isValImport());
                                                                        dataAttribute.setValKind(tbda.getValKind());
                                                                        dataAttribute.addDaVal(tbda.getVal());
                                                                        return Optional.of(new DoLinkedToDa(dataObject, dataAttribute));
                                                                    });
                                                        });
                                            });
                                })
                        ));
    }

    @Override
    public List<String> getEnumValues(TDataTypeTemplates dataTypeTemplates, String enumId) {
        return dataTypeTemplates.getEnumType().stream()
                .filter(tEnumType -> tEnumType.getId().equals(enumId))
                .findFirst()
                .stream()
                .flatMap(tEnumType -> tEnumType.getEnumVal().stream())
                .map(TEnumVal::getValue)
                .toList();
    }

    private Optional<TDAType> getDATypeByDaName(TDataTypeTemplates dtt, TDOType tdoType, String daName) {
        return daService.findDA(tdoType, daName)
                .flatMap(tda -> daTypeService.findDaType(dtt, tda.getType()));
    }

    private Optional<TDOType> findDOTypeBySdoName(TDataTypeTemplates dtt, TDOType tdoType, List<String> sdoNames) {
        if (sdoNames.isEmpty()) {
            return Optional.of(tdoType);
        }
        return sdoService.findSDO(tdoType, sdoNames.getFirst())
                .flatMap(tsdo -> doTypeService.findDoType(dtt, tsdo.getType()))
                .flatMap(tdoType2 -> findDOTypeBySdoName(dtt, tdoType2, sdoNames.subList(1, sdoNames.size())));
    }

    private Optional<TDAType> findDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, Predicate<TBDA> tbdaPredicate, List<String> bdaNames) {
        if (bdaNames.isEmpty()) {
            return Optional.of(tdaType);
        }
        return bdaService.getFilteredBDAs(tdaType, tbdaPredicate)
                .findFirst()
                .flatMap(tbda -> daTypeService.findDaType(dtt, tbda.getType()))
                .flatMap(tdaType2 -> findDATypeByBdaName(dtt, tdaType2, tbdaPredicate, bdaNames.subList(1, bdaNames.size())));
    }

}
