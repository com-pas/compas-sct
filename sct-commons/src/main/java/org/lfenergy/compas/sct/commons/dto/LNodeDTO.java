// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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

    public LNodeDTO(String inst, String lnClass, String lnPrefix, String lnType) {
        this.inst = inst;
        this.nodeClass = lnClass;
        this.nodeType = lnType;
        this.prefix = lnPrefix;
    }

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

    public void setInst(String inst){
        this.inst = inst;
    }

    public void setNodeClass(String lnClass){
        this.nodeClass = lnClass;
    }

    public void setNodeType(String lnType){
        this.nodeType = lnType;
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }


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

    public void addExtRefInfo(ExtRefInfo extRef) {
        extRefs.add(extRef);
    }

    public void addAllExtRefInfo(List<ExtRefInfo> extRefs) {
        this.extRefs.addAll(extRefs);
    }

    public <T extends ControlBlock> void addControlBlock(ControlBlock<T> controlBlock) {
        if (GooseControlBlock.class.equals(controlBlock.getClassType())) {
            gooseControlBlocks.add((GooseControlBlock) controlBlock);
        }

        if (SMVControlBlock.class.equals(controlBlock.getClassType())) {
            smvControlBlocks.add((SMVControlBlock) controlBlock);
        }

        if (ReportControlBlock.class.equals(controlBlock.getClassType())) {
            reportControlBlocks.add((ReportControlBlock) controlBlock);
        }
    }

    public <T extends ControlBlock> void addAllControlBlocks(List< ControlBlock<T> > controlBlockInfoList){
        controlBlockInfoList.forEach(this::addControlBlock);
    }

    public void addAllDatSets(List<DataSetInfo> dataSetList) {
        this.datSets.addAll(dataSetList);
    }

    public void addResumedDataTemplate(ResumedDataTemplate dtt) {
        resumedDataTemplates.add(dtt);
    }

    public void addAllResumedDataTemplate(List<ResumedDataTemplate> dtt) {
        this.resumedDataTemplates.addAll(dtt);
    }

    public Set<ResumedDataTemplate> getResumedDataTemplates(){
        return Set.of(resumedDataTemplates.toArray(new ResumedDataTemplate[0]));
    }

    public void addDataSet(DataSetInfo dataSetInfo) {
        this.datSets.add(dataSetInfo);
    }
}
