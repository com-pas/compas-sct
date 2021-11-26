// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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
        LNodeDTO lNodeDTO = new LNodeDTO();
        lNodeDTO.nodeType = nodeAdapter.getLnType();
        lNodeDTO.nodeClass = nodeAdapter.getLNClass();
        if(!nodeAdapter.getPrefix().isBlank()){
            lNodeDTO.prefix = nodeAdapter.getPrefix();
        }
        lNodeDTO.inst = nodeAdapter.getLNInst();
        if(options == null) {
            return lNodeDTO;
        }

        if(options.isWithExtRef()) {
            List<TExtRef> extRefList =  nodeAdapter.getExtRefs(null);
            lNodeDTO.extRefs.addAll(
                    extRefList.stream()
                            .map(tExtRef -> {
                                ExtRefInfo extRefInfo = new ExtRefInfo(tExtRef);
                                extRefInfo.setHolderLnClass(lNodeDTO.nodeClass);
                                extRefInfo.setHolderLnInst(lNodeDTO.inst);
                                extRefInfo.setHolderPrefix(lNodeDTO.prefix);
                                LDeviceAdapter lDeviceAdapter = (LDeviceAdapter)nodeAdapter.getParentAdapter();
                                extRefInfo.setHolderIedName(lDeviceAdapter.getParentAdapter().getName());
                                extRefInfo.setHolderLdInst(lDeviceAdapter.getInst());
                                return extRefInfo;
                            })
                            .collect(Collectors.toList())
            );
        }

        /*
        if(options.isWithResumedDtt()){
            ResumedDataTemplate filter = new ResumedDataTemplate();
            filter.setLnInst(nodeAdapter.getLNInst());
            filter.setLnClass(nodeAdapter.getLNClass());
            filter.setLnType(nodeAdapter.getLnType());
            lNodeDTO.extractResumedDTT(nodeAdapter,filter);
            nodeAdapter.toDTO(this, options.isWithExtRef(), options.isWithResumedDtt(), options.isWithCB(), options.isWithDatSet());
        }


        if(options.isWithCB()){
            lNodeDTO.extractControlBlocks(nodeAdapter);
        }

        if(options.isWithDatSet()){

        }*/

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
