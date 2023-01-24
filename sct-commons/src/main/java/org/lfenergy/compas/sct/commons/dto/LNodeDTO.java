// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
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
 *  <li>{@link LNodeDTO#getResumedDataTemplates <em>Refers To DataTemplates Objects</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TLNode
 */

@Slf4j
@Getter
@NoArgsConstructor
public class LNodeDTO {
    private String inst;
    private String nodeClass;
    private String nodeType;
    private String prefix;
    private Set<ExtRefInfo> extRefs = new HashSet<>();
    private Set<GooseControlBlock> gooseControlBlocks = new HashSet<>();
    private Set<SMVControlBlock> smvControlBlocks = new HashSet<>();
    private Set<ReportControlBlock> reportControlBlocks = new HashSet<>();
    private Set<DataSetInfo> datSets = new HashSet<>();
    private Set<ResumedDataTemplate> resumedDataTemplates = new HashSet<>();

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
            lNodeDTO.datSets = DataSetInfo.getDataSets(nodeAdapter);
        }

        if(options.isWithResumedDtt()) {
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
            ResumedDataTemplate filter = ResumedDataTemplate.builder()
                .lnInst(nodeAdapter.getLNInst())
                .lnClass(nodeAdapter.getLNClass())
                .prefix(nodeAdapter.getPrefix())
                .lnType(nodeAdapter.getLnType()).build();
            List<ResumedDataTemplate> resumedDataTemplateList = lNodeTypeAdapter.getResumedDTTs(filter);
            lNodeDTO.addAllResumedDataTemplate(resumedDataTemplateList);
        }

        if(options.isWithCB()) {
            //TODO
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
     * @param dtt input
     */
    public void addResumedDataTemplate(ResumedDataTemplate dtt) {
        resumedDataTemplates.add(dtt);
    }

    /**
     * Adds list of DataTypeTemplate's sumarised data
     * @param dtt input
     */
    public void addAllResumedDataTemplate(List<ResumedDataTemplate> dtt) {
        this.resumedDataTemplates.addAll(dtt);
    }

    /**
     * Gets DataTypeTemplate's sumarised data
     * @return Set of ResumedDataTemplate object
     */
    public Set<ResumedDataTemplate> getResumedDataTemplates(){
        return Set.of(resumedDataTemplates.toArray(new ResumedDataTemplate[0]));
    }

    /**
     * Adds DataSet information to LNode
     * @param dataSetInfo input
     */
    public void addDataSet(DataSetInfo dataSetInfo) {
        this.datSets.add(dataSetInfo);
    }
}
