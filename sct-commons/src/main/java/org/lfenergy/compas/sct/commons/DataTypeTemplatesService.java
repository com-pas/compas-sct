// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DataObject;
import org.lfenergy.compas.sct.commons.domain.DataRef;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DataTypeTemplatesService implements DataTypeTemplateReader {

    final LnodeTypeService lnodeTypeService = new LnodeTypeService();
    final DoTypeService doTypeService = new DoTypeService();
    final DaTypeService daTypeService = new DaTypeService();
    final DoService doService = new DoService();
    final DaService daService = new DaService();
    final SdoService sdoService = new SdoService();
    final BDAService bdaService = new BDAService();

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
        List<String> dataRefList = new ArrayList<>(dataRef.sdoNames());
        dataRefList.addAll(dataRef.bdaNames());

        return lnodeTypeService.findLnodeType(dtt, lNodeTypeId)
                .flatMap(lNodeType -> doService.findDo(lNodeType, dataRef.doName())
                        // Search DoType for each DO
                        .flatMap(tdo -> doTypeService.findDoType(dtt, tdo.getType())
                                .flatMap(tdoType -> {
                                    // Search last DoType from DOType (SDO) > DOType (SDO)
                                    TDOType lastDoType = findDOTypeBySdoName(dtt, tdoType, dataRefList);
                                    // Prepare DataObject
                                    DataObject dataObject = new DataObject(tdo.getName(), tdoType.getCdc(), dataRef.sdoNames());
                                    // Search first DA from last DoType
                                    return daService.findDA(lastDoType, dataRef.daName())
                                            .flatMap(tda -> {
                                                // Prepare DataAttribute
                                                DataAttribute dataAttribute = new DataAttribute();
                                                dataAttribute.setDaName(tda.getName());
                                                dataAttribute.setFc(tda.getFc());
                                                // Check if first DA is STRUCT or not
                                                if (!tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                                                    dataAttribute.addDaVal(tda.getVal());
                                                    dataAttribute.setBType(tda.getBType());
                                                    dataAttribute.setType(tda.getType());
                                                    dataAttribute.setValImport(tda.isValImport());
                                                    dataAttribute.setValKind(tda.getValKind());
                                                    return Optional.of(new DoLinkedToDa(dataObject, dataAttribute));
                                                }
                                                // Search first DaType from DOType (from last DOType where DA is STRUCT)
                                                return getDATypeByDaName(dtt, lastDoType, tda.getName())
                                                        .flatMap(tdaType -> {
                                                            // Search last DAType from first DAType
                                                            TDAType lastDAType = findDATypeByBdaName(dtt, tdaType, tbda -> tbda.isSetBType()
                                                                                                                           && tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT), dataRefList);

                                                            // last DAType should contain BDA not STRUCT
                                                            if (dataRefList.size() != 1) return Optional.empty();
                                                            String lastBdaName = dataRefList.getFirst();
                                                            return bdaService.findBDA(lastDAType, tbda -> tbda.getName().equals(lastBdaName)
                                                                                                          && !tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT))
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

    private TDOType findDOTypeBySdoName(TDataTypeTemplates dtt, TDOType tdoType, List<String> sdoNames) {
        if (sdoNames.isEmpty()) return tdoType;
        return sdoService.findSDO(tdoType, sdoNames.getFirst())
                .flatMap(tsdo -> doTypeService.findDoType(dtt, tsdo.getType()))
                .map(tdoType2 -> {
                    sdoNames.removeFirst();
                    return findDOTypeBySdoName(dtt, tdoType2, sdoNames);
                }).orElse(tdoType);
    }

    private TDAType findDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, Predicate<TBDA> tbdaPredicate, List<String> bdaNames) {
        if (bdaNames.isEmpty()) return tdaType;
        return bdaService.getFilteredBDAs(tdaType, tbdaPredicate)
                .findFirst()
                .flatMap(tbda -> daTypeService.findDaType(dtt, tbda.getType()))
                .map(tdaType2 -> {
                    bdaNames.removeFirst();
                    return findDATypeByBdaName(dtt, tdaType2, tbdaPredicate, bdaNames);
                }).orElse(tdaType);
    }

}
