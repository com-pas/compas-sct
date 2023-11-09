// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.DataSetService;
import org.lfenergy.compas.sct.commons.ExtRefReaderService;
import org.lfenergy.compas.sct.commons.LnodeTypeService;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LnKey;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A representation of the model object <em><b>LNode</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link LNodeDTO#getNodeClass <em>Ln Class</em>}</li>
 *   <li>{@link LNodeDTO#getInst <em>Ln Inst</em>}</li>
 *   <li>{@link LNodeDTO#getNodeType <em>Ln Type</em>}</li>
 *   <li>{@link LNodeDTO#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link LNodeDTO#getExtRefs <em>Refers To ExtRef</em>}</li>
 *   <li>{@link LNodeDTO#getGooseControlBlocks <em>Refers To GooseControl Blocks</em>}</li>
 *   <li>{@link LNodeDTO#getSmvControlBlocks <em>Refers To SmvControl Blocks</em>}</li>
 *   <li>{@link LNodeDTO#getReportControlBlocks <em>Refers To ReportControl Blocks</em>}</li>
 *  <li>{@link LNodeDTO#getDataAttributeRefs <em>Refers To DataTemplates Objects</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TLNode
 */
@Slf4j
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LNodeDTO {
    //TODO this is a DTO object; it's meant to be used for carry information; he must be created be the one responsible for carying the info
    @EqualsAndHashCode.Include
    private String inst;
    @EqualsAndHashCode.Include
    private String nodeClass;
    private String nodeType;
    @EqualsAndHashCode.Include
    private String prefix;
    private final Set<ExtRefInfo> extRefs = new HashSet<>();
    private final Set<GooseControlBlock> gooseControlBlocks = new HashSet<>();
    private final Set<SMVControlBlock> smvControlBlocks = new HashSet<>();
    private final Set<ReportControlBlock> reportControlBlocks = new HashSet<>();
    private Set<DataSetInfo> datSets = new HashSet<>();
    private final Set<DataAttributeRef> dataAttributeRefs = new HashSet<>();

    /**
     * Constructor
     * @param inst input
     * @param lnClass input
     * @param lnPrefix input
     * @param lnType input
     */
    public LNodeDTO(String inst, String lnClass, String lnPrefix, String lnType) {
        this.inst = inst;
        this.nodeClass = lnClass;
        this.nodeType = lnType;
        this.prefix = lnPrefix;
    }

    /**
     * Initialize LN
     * @param nodeAdapter input
     * @param options input
     * @return LNodeDTO object
     * @param <T> LNode type (LLN0 or other LN's)
     */
    public static <T extends TAnyLN> LNodeDTO from(AbstractLNAdapter<T> nodeAdapter, LogicalNodeOptions options) {
        log.info(Utils.entering());
        LNodeDTO lNodeDTO = new LNodeDTO();
        if(nodeAdapter == null) return lNodeDTO;

        lNodeDTO.nodeType = nodeAdapter.getLnType();
        lNodeDTO.nodeClass = nodeAdapter.getLNClass();
        if(!nodeAdapter.getPrefix().isBlank()){
            lNodeDTO.prefix = nodeAdapter.getPrefix();
        }
        lNodeDTO.inst = nodeAdapter.getLNInst();
        if(options == null) {
            log.info(Utils.leaving());
            return lNodeDTO;
        }

        if(options.isWithExtRef()) {
            List<TExtRef> extRefList =  nodeAdapter.getExtRefs(null);
            LDeviceAdapter lDeviceAdapter = nodeAdapter.getParentAdapter();
            String holderIedName = lDeviceAdapter.getParentAdapter().getName();
            String holderLDInst = lDeviceAdapter.getInst();
            lNodeDTO.extRefs.addAll(
                    extRefList.stream()
                            .map(tExtRef ->
                                ExtRefInfo.from(tExtRef,holderIedName,holderLDInst,lNodeDTO.nodeClass,
                                        lNodeDTO.inst,lNodeDTO.prefix
                                ))
                            .collect(Collectors.toList())
            );
        }

        if(options.isWithDatSet()) {
            DataSetService dataSetService = new DataSetService();
            lNodeDTO.datSets = dataSetService.getDataSets(nodeAdapter.getCurrentElem()).map(DataSetInfo::new).collect(Collectors.toSet());
        }

        if(options.isWithDataAttributeRef()) {
            DataTypeTemplateAdapter dttAdapter = nodeAdapter.getDataTypeTemplateAdapter();
            LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(nodeAdapter.getLnType())
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    String.format(
                                            "Corrupted SCD file: reference to unknown lnType(%s)",
                                            nodeAdapter.getLnType()
                                    )
                            )
                    );
            DataAttributeRef filter = DataAttributeRef.builder()
                .lnInst(nodeAdapter.getLNInst())
                .lnClass(nodeAdapter.getLNClass())
                .prefix(nodeAdapter.getPrefix())
                .lnType(nodeAdapter.getLnType()).build();
            List<DataAttributeRef> dataAttributeRefList = lNodeTypeAdapter.getDataAttributeRefs(filter);
            lNodeDTO.addAllDataAttributeRef(dataAttributeRefList);
        }

        if(options.isWithCB()) {
            //TODO
        }
        log.info(Utils.leaving());
        return lNodeDTO;
    }

    public static LNodeDTO from(TAnyLN tAnyLN, LogicalNodeOptions options, String iedName, String ldInst, SCL scl) {
        log.info(Utils.entering());
        LnKey lnKey = switch (tAnyLN) {
            case LN0 ln0 -> new LnKey(ln0);
            case TLN tln -> new LnKey(tln);
            default -> throw new IllegalStateException("Unexpected value: " + tAnyLN);
        };
        String inst = lnKey.getInst();
        String lnClass = lnKey.getLnClass();
        String prefix = lnKey.getPrefix();
        String lnType = tAnyLN.getLnType();
        LNodeDTO lNodeDTO = new LNodeDTO(inst, lnClass, prefix, lnType);
        if (options.isWithExtRef()) {
            List<ExtRefInfo> extRefInfos = new ExtRefReaderService().getExtRefs(tAnyLN)
                    .map(extRef -> ExtRefInfo.from(extRef, iedName, ldInst, lnClass, inst, prefix))
                    .toList();
            lNodeDTO.addAllExtRefInfo(extRefInfos);
        }
        if (options.isWithDatSet()) {
            List<DataSetInfo> dataSetInfos = new DataSetService().getDataSets(tAnyLN)
                    .map(DataSetInfo::new)
                    .distinct()
                    .toList();
            lNodeDTO.addAllDatSets(dataSetInfos);
        }

        if (options.isWithDataAttributeRef()) {
            DataAttributeRef filter = DataAttributeRef.builder()
                    .lnInst(inst)
                    .lnClass(lnClass)
                    .prefix(prefix)
                    .lnType(lnType)
                    .build();

            TLNodeType lnodeType = new LnodeTypeService().findLnodeType(scl.getDataTypeTemplates(), lnodeType1 -> lnodeType1.getId().equals(lnType))
                    .orElseThrow(() -> new IllegalArgumentException("Corrupted SCD file: reference to unknown lnType(" + lnType + ")"));
            List<DataAttributeRef> dataAttributeRefList = new LNodeTypeAdapter(new DataTypeTemplateAdapter(new SclRootAdapter(scl), scl.getDataTypeTemplates()), lnodeType)
                    .getDataAttributeRefs(filter);
            lNodeDTO.addAllDataAttributeRef(dataAttributeRefList);
        }
        log.info(Utils.leaving());
        return lNodeDTO;
    }

    /**
     * Sets LNode Inst value
     * @param inst input
     */
    public void setInst(String inst){
        this.inst = inst;
    }

    /**
     * Sets LNode Class value
     * @param lnClass input
     */
    public void setNodeClass(String lnClass){
        this.nodeClass = lnClass;
    }

    /**
     *  Sets LNode Type
     * @param lnType
     */
    public void setNodeType(String lnType){
        this.nodeType = lnType;
    }

    /**
     * Sets LNode Prefix value
     * @param prefix input
     */
    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    /**
     * Extracts LNode ExtRef informations
     * @param lnAdapter input
     * @return LNodeDTO object
     */
    public static LNodeDTO extractExtRefInfo(LNAdapter lnAdapter) {
        String lnClass = lnAdapter.getLNClass() == null ? "" : lnAdapter.getLNClass();
        LNodeDTO lNodeDTO = new LNodeDTO(lnAdapter.getLNInst(),lnClass,lnAdapter.getPrefix(), lnAdapter.getLnType());
        if(lnAdapter.hasInputs()){
            lnAdapter.getExtRefs(null).forEach(tExtRef -> {
                ExtRefInfo extRefInfo = new ExtRefInfo(tExtRef);
                lNodeDTO.addExtRefInfo(extRefInfo);
            });
        }
        return lNodeDTO;
    }

    /**
     * Adds ExtRef Info to LNode ExtRefs
     * @param extRef input
     */
    public void addExtRefInfo(ExtRefInfo extRef) {
        extRefs.add(extRef);
    }

    /**
     * Adds list of ExtRef Info into LNode
     * @param extRefs input
     */
    public void addAllExtRefInfo(List<ExtRefInfo> extRefs) {
        this.extRefs.addAll(extRefs);
    }

    /**
     * Adds Control Block to LNode Control Blocks
     * @param controlBlock input
     */
    public void addControlBlock(ControlBlock controlBlock) {
        if (controlBlock instanceof GooseControlBlock gooseControlBlock) {
            gooseControlBlocks.add(gooseControlBlock);
        }

        if (controlBlock instanceof SMVControlBlock smvControlBlock) {
            smvControlBlocks.add(smvControlBlock);
        }

        if (controlBlock instanceof ReportControlBlock reportControlBlock) {
            reportControlBlocks.add(reportControlBlock);
        }
    }

    /**
     * Adds lis of Control Block to LNode Control Blocks
     * @param controlBlockInfoList input
     */
    public void addAllControlBlocks(List<ControlBlock> controlBlockInfoList){
        controlBlockInfoList.forEach(this::addControlBlock);
    }

    /**
     * Adds list of DataSet to LNode
     * @param dataSetList input
     */
    public void addAllDatSets(List<DataSetInfo> dataSetList) {
        this.datSets.addAll(dataSetList);
    }

    /**
     * Adds DataTypeTemplate's sumarised data
     * @param dataAttributeRef input
     */
    public void addDataAttributeRef(DataAttributeRef dataAttributeRef) {
        dataAttributeRefs.add(dataAttributeRef);
    }

    /**
     * Adds list of DataTypeTemplate's sumarised data
     * @param dataAttributeRefs input
     */
    public void addAllDataAttributeRef(List<DataAttributeRef> dataAttributeRefs) {
        this.dataAttributeRefs.addAll(dataAttributeRefs);
    }

    /**
     * Gets DataTypeTemplate's sumarised data
     * @return Set of DataAttributeRef object
     */
    public Set<DataAttributeRef> getDataAttributeRefs(){
        return Set.of(dataAttributeRefs.toArray(new DataAttributeRef[0]));
    }

    /**
     * Adds DataSet information to LNode
     * @param dataSetInfo input
     */
    public void addDataSet(DataSetInfo dataSetInfo) {
        this.datSets.add(dataSetInfo);
    }
}
