// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;

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
    public Stream<DataAttributeRef> getAllDOAndDA(TDataTypeTemplates dtt) {
        return dtt.getLNodeType().stream()
                .flatMap(tlNodeType -> lnodeTypeService.getAllDOAndDA(dtt, tlNodeType.getId()));
    }

    @Override
    public Stream<DataAttributeRef> getFilteredDOAndDA(TDataTypeTemplates dtt, TAnyLN anyLn, DataAttributeRef filter) {
        return lnodeTypeService.findLnodeType(dtt, tlNodeType -> tlNodeType.getId().equals(anyLn.getLnType()))
                .stream()
                .flatMap(tlNodeType -> lnodeTypeService.getFilteredDOAndDA(dtt, filter));
    }

    @Override
    public Optional<DataAttributeRef> findDOAndDA(TDataTypeTemplates dtt, String lNodeTypeId, DataAttributeRef dataAttributeRef) {
        LinkedList<String> dataRefList = new LinkedList<>(dataAttributeRef.getSdoNames());
        dataAttributeRef.getBdaNames().forEach(dataRefList::addLast);

        return lnodeTypeService.findLnodeType(dtt, lNodeType -> lNodeTypeId.equals(lNodeType.getId()))
                .flatMap(lNodeType -> doService.findDo(lNodeType, tdo -> tdo.getName().equals(dataAttributeRef.getDoName().getName()))
                        // Search DoType for each DO
                        .flatMap(tdo -> doTypeService.findDoType(dtt, doType -> doType.getId().equals(tdo.getType()))
                                .flatMap(tdoType -> {
                                    // Search last DoType from DOType (SDO) > DOType (SDO)
                                    TDOType lastDoType = findDOTypeBySdoName(dtt, tdoType, dataRefList);
                                    // Search first DA from last DoType
                                    return sdoOrDAService.findDA(lastDoType, tda1 -> tda1.getName().equals(dataAttributeRef.getDaName().getName()))
                                            .flatMap(tda -> {
                                                // Check if first DA is STRUCT or not
                                                if(!tda.getBType().equals(TPredefinedBasicTypeEnum.STRUCT)) {
                                                    return Optional.of(dataAttributeRef);
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
                                                                    .flatMap(tbda -> Optional.of(dataAttributeRef));
                                                        });
                                    });
                                })
                        ));
    }

    private Optional<TDAType> getDATypeByDaName(TDataTypeTemplates dtt, TDOType tdoType, String daName) {
        return sdoOrDAService.findDA(tdoType, tda -> tda.getName().equals(daName))
                .flatMap(tda -> daTypeService.findDaType(dtt, tdaType -> tda.isSetType() && tdaType.getId().equals(tda.getType())));
    }

    private TDOType findDOTypeBySdoName(TDataTypeTemplates dtt, TDOType tdoType, List<String> sdoNames) {
        if(sdoNames.isEmpty()) return tdoType;
        return sdoOrDAService.findSDO(tdoType, tsdo -> tsdo.getName().equals(sdoNames.get(0)))
                .flatMap(tsdo -> doTypeService.findDoType(dtt, tdoType2 -> tdoType2.getId().equals(tsdo.getType())))
                .map(tdoType2 -> {
                    sdoNames.remove(0);
                    return findDOTypeBySdoName(dtt, tdoType2, sdoNames);
                }).orElse(tdoType);
    }

    private TDAType findDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, Predicate<TBDA> tbdaPredicate, List<String> bdaNames) {
        if(bdaNames.isEmpty()) return tdaType;
        return bdaService.getFilteredBDAs(tdaType, tbdaPredicate)
                .findFirst()
                .flatMap(tbda -> daTypeService.findDaType(dtt, tdaType2 -> tdaType2.getId().equals(tbda.getType())))
                .map(tdaType2 -> {
                    bdaNames.remove(0);
                    return findDATypeByBdaName(dtt, tdaType2,tbdaPredicate, bdaNames);
                }).orElse(tdaType);
    }

}
