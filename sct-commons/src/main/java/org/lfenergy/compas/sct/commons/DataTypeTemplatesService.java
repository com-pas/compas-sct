// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.*;
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
                .map(lNodeType -> doService.findDo(lNodeType, tdo -> MOD_DO_NAME.equals(tdo.getName()))
                        .map(tdo -> doTypeService.findDoType(dtt, doType -> tdo.getType().equals(doType.getId()))
                                .map(doType -> doType.getSDOOrDA().stream()
                                        .filter(sdoOrDa -> sdoOrDa.getClass().equals(TDA.class))
                                        .map(TDA.class::cast)
                                        .anyMatch(tda -> STVAL_DA_NAME.equals(tda.getName())))
                                .orElse(false))
                        .orElse(false))
                .orElse(false);
    }

    @Override
    public Stream<DataAttributeRef> getAllDataObjectsAndDataAttributes(TDataTypeTemplates dtt) {
        return dtt.getLNodeType().stream()
                .flatMap(tlNodeType -> lnodeTypeService.getDataAttributes(dtt, tlNodeType, new DataAttributeRef()));
    }

    @Override
    public Stream<DataAttributeRef> getFilteredDataObjectsAndDataAttributes(TDataTypeTemplates dtt, TAnyLN anyLn, DataAttributeRef filter) {
        return lnodeTypeService.findLnodeType(dtt, tlNodeType -> tlNodeType.getId().equals(anyLn.getLnType()))
                .stream()
                .flatMap(tlNodeType -> lnodeTypeService.getDataAttributes(dtt, tlNodeType, filter));
    }

    @Override
    public Optional<DataAttributeRef> findDataObjectsAndDataAttributesByDataReference(TDataTypeTemplates dtt, String lNodeTypeId, String dataRef) {
        LinkedList<String> dataRefList = new LinkedList<>(Arrays.asList(dataRef.split("\\.")));
        if (dataRefList.size() < 2) return Optional.empty();
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType(lNodeTypeId);
        dataAttributeRef.setDoName(new DoTypeName());
        dataAttributeRef.setDaName(new DaTypeName());

        String doName = dataRefList.remove();
        return lnodeTypeService.findLnodeType(dtt, lNodeType -> lNodeTypeId.equals(lNodeType.getId()))
                .flatMap(lNodeType -> doService.findDo(lNodeType, tdo -> tdo.getName().equals(doName))
                        // Search first DoType from DO (LNodeType)
                        .flatMap(tdo -> doTypeService.findDoType(dtt, doType -> doType.getId().equals(tdo.getType()))
                                .flatMap(tdoType -> {
                                    dataAttributeRef.getDoName().setName(doName);
                                    // Search last DoType from SDO (DOType) > SDO (DOType) > SDO (DOType)
                                    TDOType lastDoType = findDOTypeBySdoName(dtt, tdoType, dataRefList, dataAttributeRef.getDoName().getStructNames());
                                    dataAttributeRef.getDoName().setCdc(tdoType.getCdc());

                                    // Search DA (DOType) > BDA (DAType) > BDA (DAType) > BDA (DAType)
                                    dataAttributeRef.getDaName().setName(dataRefList.get(0));
                                    // Search first DA from last DoType
//                                    Optional<TDA> opDA =
                                    return sdoOrDAService.findSDOOrDA(lastDoType, TDA.class,
                                            tda1 -> tda1.getName().equals(dataAttributeRef.getDaName().getName()))
                                            .flatMap(tda -> {
                                                dataAttributeRef.getDaName().setFc(tda.getFc());
                                                if(tda.getBType() != TPredefinedBasicTypeEnum.STRUCT) {
                                                    DoTypeService.updateFromAbstractDataAttribute(tda, dataAttributeRef.getDaName());
                                                    return Optional.of(dataAttributeRef);
                                                }
                                                // Search first BDA (DaType) from DA (DOType)
                                                return getDATypeByDaNameIfExist(dtt, lastDoType, tda.getName())
                                                        .flatMap(tdaType -> {
                                                            // remove DA name from struct list
                                                            dataRefList.remove();
                                                            // Search last DAType (BDA) > DAType (BDA) > DAType (BDA)
                                                            TDAType lastDAType = findDATypeByBdaName(dtt, tdaType, dataRefList, dataAttributeRef.getDaName().getStructNames());
                                                            // Search BDA in last DAType (should be not STRUCT)
                                                            String lastBdaName = dataAttributeRef.getDaName().getStructNames().get(dataAttributeRef.getDaName().getStructNames().size() - 1);
                                                            return bdaService.findBDA(lastDAType, tbda -> tbda.getName().equals(lastBdaName) && tbda.isSetBType()
                                                                            && tbda.getBType() != TPredefinedBasicTypeEnum.STRUCT && dataRefList.isEmpty())
                                                                    .flatMap(tbda -> {
                                                                        DoTypeService.updateFromAbstractDataAttribute(tbda, dataAttributeRef.getDaName());
                                                                        return Optional.of(dataAttributeRef);
                                                                    });
                                                        });
                                    });
                                })
                        ));
    }

    public List<SclReportItem> isDataObjectsAndDataAttributesExists(TDataTypeTemplates dtt, String lNodeTypeId, String dataRef) {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        LinkedList<String> dataRefList = new LinkedList<>(Arrays.asList(dataRef.split("\\.")));
        if (dataRefList.size() < 2) {
            sclReportItems.add(SclReportItem.error(null, "Invalid data reference %s. At least DO name and DA name are required".formatted(dataRef)));
        }
        String doName = dataRefList.remove();
        return lnodeTypeService.findLnodeType(dtt, lNodeType -> lNodeTypeId.equals(lNodeType.getId()))
                .map(lNodeType -> doService.findDo(lNodeType, tdo -> tdo.getName().equals(doName))
                        .map(tdo -> doTypeService.findDoType(dtt, doType -> doType.getId().equals(tdo.getType()))
                                .map(tdoType -> {
                                    // Search last DoType > DOType > DOType
                                    TDOType lastDoType = findDOTypeBySdoName(dtt, tdoType, dataRefList);
                                    var tdaType = getDATypeByDaNameIfExist0(dtt, lastDoType, dataRefList.get(0));
                                    tdaType.ifPresentOrElse(tdaType1 -> {
                                        // DA can not have BDA, in this case there is no DAType: no error to add
                                        if(tdaType1 instanceof TDOType) return;
                                        // remove DA name from struct list
                                        dataRefList.remove();
                                        // Search last DAType > DAType > DAType
                                        checkDATypeByBdaName(dtt, (TDAType) tdaType1, dataRefList, sclReportItems);
                                    }, ()-> sclReportItems.add(SclReportItem.error(null,
                                            String.format("Unknown Sub Data Object SDO or Data Attribute DA (%s) in DOType.id (%s)"
                                                    .formatted(dataRefList.get(0), lastDoType.getId())))));
                                    return sclReportItems;
                                })
                                .orElseGet(() -> {
                                    sclReportItems.add(SclReportItem.error(null, String.format("DOType.id (%s) for DO.name (%s) not found in DataTypeTemplates".formatted(tdo.getType(), tdo.getName()))));
                                    return sclReportItems;
                                }))
                        .orElseGet(() -> {
                            sclReportItems.add(SclReportItem.error(null, String.format("Unknown DO.name (%s) in DOType.id (%s)".formatted(doName, lNodeTypeId))));
                            return sclReportItems;
                        }))
                .orElseGet(() -> {
                    sclReportItems.add(SclReportItem.error(null, "No Data Attribute found with this reference %s for LNodeType.id (%s)".formatted(dataRef, lNodeTypeId)));
                    return sclReportItems;
                });
    }

    private TDOType findDOTypeBySdoName(TDataTypeTemplates dtt, TDOType tdoType, List<String> sdoNames) {
        if(sdoNames.isEmpty()) return tdoType;
        return sdoOrDAService.findSDOOrDA(tdoType, TSDO.class, tsdo -> tsdo.getName().equals(sdoNames.get(0)))
                .flatMap(sdo1 -> doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId().equals(sdo1.getType()))
                        .stream().findFirst())
                .map(tdoType1 -> {
                    sdoNames.remove(0);
                    return findDOTypeBySdoName(dtt, tdoType1, sdoNames);
                })
                .orElse(tdoType);
    }

    private TDOType findDOTypeBySdoName(TDataTypeTemplates dtt, TDOType tdoType, List<String> sdoNameListToCheck, List<String> checkedSdoNameList) {
        if(sdoNameListToCheck.isEmpty()) return tdoType;
        return sdoOrDAService.findSDOOrDA(tdoType, TSDO.class, tsdo -> tsdo.getName().equals(sdoNameListToCheck.get(0)))
                .flatMap(sdo1 -> doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId().equals(sdo1.getType()))
                        .stream().findFirst())
                .map(tdoType1 -> {
                    checkedSdoNameList.add(sdoNameListToCheck.get(0));
                    sdoNameListToCheck.remove(0);
                    return findDOTypeBySdoName(dtt, tdoType1, sdoNameListToCheck, checkedSdoNameList);
                })
                .orElse(tdoType);
    }

    private void checkDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, List<String> bdaNameList, List<SclReportItem> sclReportItems) {
        if(bdaNameList.isEmpty()) return ;
        getDATypeByBdaName(dtt, tdaType, bdaNameList.get(0))
                .ifPresentOrElse(tdaType1 -> {
                    bdaNameList.remove(0);
                    checkDATypeByBdaName(dtt, tdaType1, bdaNameList, sclReportItems);
                }, () -> sclReportItems.add(SclReportItem.error(null,String.format("Unknown BDA.name (%s) in DAType.id (%s)",
                        bdaNameList.get(0), tdaType.getId()))));
    }

    private TDAType findDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, List<String> bdaNameListToCheck, List<String> checkedBdaNameList) {
        if(bdaNameListToCheck.isEmpty()) return tdaType;
        return getDATypeByBdaName(dtt, tdaType, bdaNameListToCheck.get(0))
                .map(tdaType1 -> {
                    checkedBdaNameList.add(bdaNameListToCheck.get(0));
                    bdaNameListToCheck.remove(0);
                    return findDATypeByBdaName(dtt, tdaType1, bdaNameListToCheck, checkedBdaNameList);
                })
                .orElse(tdaType);
    }

    private <T extends TIDNaming> Optional<T> getDATypeByDaNameIfExist0(TDataTypeTemplates dtt, TDOType tdoType, String daName) {
        Optional<TDA> dai = sdoOrDAService.findSDOOrDA(tdoType, TDA.class, tda -> tda.getName().equals(daName));
        if(dai.isPresent()){
            if(dai.get().isSetBType() && dai.get().getBType().equals(TPredefinedBasicTypeEnum.STRUCT) && dai.get().isSetType()){
                return (Optional<T>) daTypeService.findDaType(dtt, tdaType -> tdaType.getId().equals(dai.get().getType()));
            } else {
                return (Optional<T>) Optional.of(tdoType);
            }
        }
        return Optional.empty();
    }

    private Optional<TDAType> getDATypeByDaNameIfExist(TDataTypeTemplates dtt, TDOType tdoType, String daName) {
        return sdoOrDAService.findSDOOrDA(tdoType, TDA.class, tda -> tda.getName().equals(daName))
                .flatMap(tda -> daTypeService.findDaType(dtt, tdaType -> tda.isSetType() && tdaType.getId().equals(tda.getType())));
    }

    private Optional<TDAType> getDATypeByBdaName(TDataTypeTemplates dtt, TDAType tdaType, String bdaName) {
        if(tdaType == null) return Optional.empty();
        return bdaService.findBDA(tdaType, tbda -> tbda.getName().equals(bdaName))
                .map(tbda ->  {
                    if(tbda.getBType() == TPredefinedBasicTypeEnum.STRUCT) {
                        return daTypeService.findDaType(dtt, tdaType2 -> tdaType2.getId().equals(tbda.getType()))
                                .orElseThrow(() -> new ScdException("BDA.name= = "+tbda + " not found in DAType id = "+tdaType.getId()));
                    }
                    return tdaType;
                })
                .stream().findFirst();
    }

}
