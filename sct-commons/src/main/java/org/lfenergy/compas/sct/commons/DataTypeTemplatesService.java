// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DataObject;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDaFilter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;

public class DataTypeTemplatesService implements DataTypeTemplateReader {

    final LnodeTypeService lnodeTypeService = new LnodeTypeService();
    final DoTypeService doTypeService = new DoTypeService();
    final DaTypeService daTypeService = new DaTypeService();
    final DoService doService = new DoService();
    final SDOOrDAService sdoOrDAService = new SDOOrDAService();
    final BDAService bdaService = new BDAService();

    /**
     * verify if DO(name=Mod)/DA(name=stVal) exists in DataTypeTemplate
     * @param dtt TDataTypeTemplates where Data object and Data attribute exists
     * @param lNodeTypeId LNode Type ID where Data object exists
     *  DataTypeTemplates model :
     * <DataTypeTemplates>
     *     <LNodeType lnClass="LNodeTypeClass" id="LNodeTypeID">
     *         <DO name="Mod" type="DOModTypeID" ../>
     *     </LNodeType>
     *     ...
     *     <DOType cdc="DOTypeCDC" id="DOModTypeID">
     *         <DA name="stVal" ../>
     *     </DOType>
     * </DataTypeTemplates>
     * @return true if the Data Object (Mod) and Data attribute (stVal) present, false otherwise
     */
    public boolean isDoModAndDaStValExist(TDataTypeTemplates dtt, String lNodeTypeId) {
        return lnodeTypeService.findLnodeType(dtt, lNodeType -> lNodeTypeId.equals(lNodeType.getId()))
                .flatMap(lNodeType -> doService.findDo(lNodeType, tdo -> MOD_DO_NAME.equals(tdo.getName()))
                        .flatMap(tdo -> doTypeService.findDoType(dtt, doType -> tdo.getType().equals(doType.getId()))
                                .map(doType -> sdoOrDAService.findDA(doType, tda -> STVAL_DA_NAME.equals(tda.getName())).isPresent())))
                .orElse(false);
    }

    @Override
    public Stream<DoLinkedToDa> getAllDoLinkedToDa(TDataTypeTemplates dtt) {
            return lnodeTypeService.getLnodeTypes(dtt)
                .flatMap(tlNodeType -> {
                    DoLinkedToDa doLinkedToDa = new DoLinkedToDa();
                    doLinkedToDa.setDataObject(new DataObject());
                    doLinkedToDa.setDataAttribute(new DataAttribute());
                    return tlNodeType.getDO()
                            .stream()
                            .map(tdo -> doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()))
                                    .map(doType -> {
                                        doLinkedToDa.getDataObject().setDoName(tdo.getName());
                                        return doTypeService.getAllSDOLinkedToDa(dtt, doType, doLinkedToDa).stream();
                                    }))
                            .filter(Optional::isPresent)
                            .flatMap(Optional::orElseThrow);
                });
    }

    @Override
    public Stream<DoLinkedToDa> getFilteredDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, DoLinkedToDaFilter doLinkedToDaFilter) {
        return lnodeTypeService.findLnodeType(dtt, tlNodeType -> tlNodeType.getId().equals(lNodeTypeId))
                .stream()
                .flatMap(tlNodeType -> doService.getFilteredDos(tlNodeType, tdo -> StringUtils.isBlank(doLinkedToDaFilter.doName())
                                || doLinkedToDaFilter.doName().equals(tdo.getName()))
                        .flatMap(tdo -> {
                            DoLinkedToDa doLinkedToDa = new DoLinkedToDa();
                            DataObject dataObject = new DataObject();
                            dataObject.setDoName(tdo.getName());
                            doLinkedToDa.setDataObject(dataObject);
                            doLinkedToDa.setDataAttribute(new DataAttribute());
                            return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()))
                                    .stream()
                                    .flatMap(tdoType -> {
                                        doLinkedToDa.getDataObject().setCdc(tdoType.getCdc());
                                        return doTypeService.getAllSDOLinkedToDa(dtt, tdoType, doLinkedToDa).stream()
                                                .filter(doLinkedToDa1 -> StringUtils.isBlank(doLinkedToDaFilter.doName())
                                                        || (doLinkedToDa1.getDoRef().startsWith(doLinkedToDaFilter.getDoRef()) && StringUtils.isBlank(doLinkedToDaFilter.daName()))
                                                        || doLinkedToDa1.getDaRef().startsWith(doLinkedToDaFilter.getDaRef()));
                                    });
                }));
    }

    @Override
    public Optional<DoLinkedToDa> findDoLinkedToDa(TDataTypeTemplates dtt, String lNodeTypeId, DoLinkedToDa doLinkedToDa) {
        List<String> dataRefList = new ArrayList<>(doLinkedToDa.getDataObject().getSdoNames());
        dataRefList.addAll(doLinkedToDa.getDataAttribute().getBdaNames());

        return lnodeTypeService.findLnodeType(dtt, lNodeType -> lNodeTypeId.equals(lNodeType.getId()))
                .flatMap(lNodeType -> doService.findDo(lNodeType, tdo -> tdo.getName().equals(doLinkedToDa.getDataObject().getDoName()))
                        // Search DoType for each DO
                        .flatMap(tdo -> doTypeService.findDoType(dtt, doType -> doType.getId().equals(tdo.getType()))
                                .flatMap(tdoType -> {
                                    // Search last DoType from DOType (SDO) > DOType (SDO)
                                    TDOType lastDoType = findDOTypeBySdoName(dtt, tdoType, dataRefList);
                                    // Search first DA from last DoType
                                    return sdoOrDAService.findDA(lastDoType, tda1 -> tda1.getName().equals(doLinkedToDa.getDataAttribute().getDaName()))
                                            .flatMap(tda -> {
                                                // Check if first DA is STRUCT or not
                                                if(!tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                                                    return Optional.of(doLinkedToDa);
                                                }
                                                // Search first DaType from DOType (from last DOType where DA is STRUCT)
                                                return getDATypeByDaName(dtt, lastDoType, tda.getName())
                                                        .flatMap(tdaType -> {
                                                            // Search last DAType from first DAType
                                                            TDAType lastDAType = findDATypeByBdaName(dtt, tdaType, tbda -> tbda.isSetBType()
                                                                    && tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT), dataRefList);

                                                            // last DAType should contain BDA not STRUCT
                                                            if(dataRefList.size() != 1) return Optional.empty();
                                                            String lastBdaName = dataRefList.getFirst();
                                                            return bdaService.findBDA(lastDAType, tbda -> tbda.getName().equals(lastBdaName)
                                                                            && !tbda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT))
                                                                    .flatMap(tbda -> Optional.of(doLinkedToDa));
                                                        });
                                            });
                                })
                        ));
    }

    private Optional<TDAType> getDATypeByDaName(TDataTypeTemplates dtt, TDOType tdoType, String daName) {
        return sdoOrDAService.findDA(tdoType, tda -> tda.getName().equals(daName))
                .flatMap(tda -> daTypeService.findDaType(dtt, tda.getType()));
    }

    private TDOType findDOTypeBySdoName(TDataTypeTemplates dtt, TDOType tdoType, List<String> sdoNames) {
        if(sdoNames.isEmpty()) return tdoType;
        return sdoOrDAService.findSDO(tdoType, tsdo -> tsdo.getName().equals(sdoNames.getFirst()))
                .flatMap(tsdo -> doTypeService.findDoType(dtt, tdoType2 -> tdoType2.getId().equals(tsdo.getType())))
                .map(tdoType2 -> {
                    sdoNames.removeFirst();
                    return findDOTypeBySdoName(dtt, tdoType2, sdoNames);
                }).orElse(tdoType);
    }

    private TDAType findDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, Predicate<TBDA> tbdaPredicate, List<String> bdaNames) {
        if(bdaNames.isEmpty()) return tdaType;
        return bdaService.getFilteredBDAs(tdaType, tbdaPredicate)
                .findFirst()
                .flatMap(tbda -> daTypeService.findDaType(dtt, tbda.getType()))
                .map(tdaType2 -> {
                    bdaNames.removeFirst();
                    return findDATypeByBdaName(dtt, tdaType2, tbdaPredicate, bdaNames);
                }).orElse(tdaType);
    }

}
