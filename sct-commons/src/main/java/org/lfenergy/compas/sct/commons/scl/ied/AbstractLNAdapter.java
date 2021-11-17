// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.dto.ReportControlBlock;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Slf4j
public abstract class AbstractLNAdapter<T extends TAnyLN> extends SclElementAdapter<LDeviceAdapter, T> {


    protected AbstractLNAdapter(LDeviceAdapter parentAdapter, T currentElem) {
        super(parentAdapter, currentElem);
    }

    protected abstract Class<T> getElementClassType();

    public abstract String getLNClass() ;
    public abstract String getLNInst();
    public abstract String getPrefix() ;

    public static LNAdapterBuilder builder(){
        return new LNAdapterBuilder();
    }

    protected void addControlBlock(ReportControlBlock controlBlock) {
        currentElem.getReportControl().add(controlBlock.createControlBlock());
    }

    public Optional<TDataSet> findDataSetByRef(String dataSetRef)  {
        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> Objects.equals(tDataSet.getName(),dataSetRef))
                .findFirst();
    }

    boolean isLN0(){
        return getElementClassType() == LN0.class;
    }


    public String getLnType(){
        return currentElem.getLnType();
    }

    public List<TExtRef> getExtRefs(ExtRefSignalInfo filter) {
        if(!hasInputs()){
            return new ArrayList<>();
        }

        if (filter == null) {
            return currentElem.getInputs().getExtRef();
        }
        return currentElem.getInputs().getExtRef()
                .stream()
                .filter(tExtRef ->
                        ((filter.getDesc() == null && tExtRef.getDesc().isEmpty())
                                || Objects.equals(filter.getDesc(),tExtRef.getDesc()) ) &&
                            Objects.equals(filter.getPDO(),tExtRef.getPDO()) &&
                            Objects.equals(filter.getPDA(),tExtRef.getPDA()) &&
                            Objects.equals(filter.getIntAddr(),tExtRef.getIntAddr()) &&
                            Objects.equals(filter.getPServT(),tExtRef.getPServT()))
                .collect(Collectors.toList());
    }

    public boolean hasInputs() {
        return currentElem.getInputs() != null;
    }

    public List<TExtRef> getExtRefsBySignalInfo(ExtRefSignalInfo signalInfo) {
        if (currentElem.getInputs() == null) {
            return new ArrayList<>();
        }

        if (signalInfo == null) {
            return currentElem.getInputs().getExtRef();
        }

        return currentElem.getInputs().getExtRef()
                .stream()
                .filter(tExtRef ->  Objects.equals(signalInfo.getDesc(), tExtRef.getDesc()) &&
                            Objects.equals(tExtRef.getPDO(), signalInfo.getPDO()) &&
                            Objects.equals(signalInfo.getIntAddr(), tExtRef.getIntAddr()) &&
                            Objects.equals(signalInfo.getPServT(), tExtRef.getPServT())
                )
                .collect(Collectors.toList());
    }

    public void updateExtRefBinders(Set<ExtRefInfo> extRefInfos) throws ScdException {
        boolean missingData =  extRefInfos.stream()
                .anyMatch(extRefInfo -> {
                    ExtRefBindingInfo extRefBindingInfo = extRefInfo.getBindingInfo();
                    return !extRefBindingInfo.isValid();
                });
        if(missingData){
            throw  new ScdException("ExtRef mandatory binding data are missing");
        }
        String iedName = parentAdapter.getParentAdapter().getName();
        String ldInst = parentAdapter.getInst();

        for(ExtRefInfo extRefInfo : extRefInfos){
            ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
            List<TExtRef> tExtRefs = this.getExtRefs(signalInfo);
            if(tExtRefs.isEmpty()){
                String msg = String.format("Unknown ExtRef [pDO(%s),intAddr(%s)] in %s/%s.%s",
                        signalInfo.getPDO(), signalInfo.getIntAddr(), iedName, ldInst,getLNClass());
                throw new ScdException(msg);
            }
            if(tExtRefs.size() != 1){
                log.warn("More the one desc for ExtRef [pDO({}),intAddr({})] in {}/{}.{}",
                        signalInfo.getPDO(), signalInfo.getIntAddr(), iedName, ldInst,getLNClass());
            }
            TExtRef extRef = tExtRefs.get(0);
            // update ExtRef with binding info
            updateExtRefBindingInfo(extRef, extRefInfo);
        }
    }

    protected void updateExtRefBindingInfo(TExtRef extRef, ExtRefInfo extRefInfo) {
        //update binding info
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if(bindingInfo != null && bindingInfo.isValid()){

            extRef.setIedName(bindingInfo.getIedName());
            extRef.setLdInst(bindingInfo.getLdInst());
            extRef.setLnInst(bindingInfo.getLnInst());
            extRef.getLnClass().clear();
            extRef.getLnClass().add(bindingInfo.getLnClass());
            extRef.setDoName(bindingInfo.getDoName().toString());
            if(bindingInfo.getServiceType() == null && extRefInfo.getSignalInfo() != null){
                bindingInfo.setServiceType(extRefInfo.getSignalInfo().getPServT());
            }
            extRef.setServiceType(bindingInfo.getServiceType());
            extRef.setDaName(null);
            if(!StringUtils.isEmpty(bindingInfo.getDaName().toString())) {
                extRef.setDaName(bindingInfo.getDaName().toString());
            }
            extRef.setPrefix(bindingInfo.getPrefix());

            // invalid source info
            extRef.setServiceType(null);
            extRef.setSrcCBName(null);
            extRef.setSrcLDInst(null);
            extRef.setSrcPrefix(null);
            extRef.setSrcLNInst(null);
            // the JAXB don't provide setter for srcLNClass
            // SCL XSD doesn't accept empty srcLNClass list
            // No choice here but to do reflection
            try {
                Field f = extRef.getClass().getDeclaredField("srcLNClass");
                f.setAccessible(true);
                f.set(extRef,null);
            } catch ( Exception e) {
                log.error("Cannot nullify srcLNClass:", e);
            }
        }
        //
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if(sourceInfo != null){
            extRef.setSrcLNInst(sourceInfo.getSrcLNInst());
            if(sourceInfo.getSrcLNClass() != null) {
                extRef.getSrcLNClass().add(sourceInfo.getSrcLNClass());
            }
            extRef.setSrcLDInst(sourceInfo.getSrcLDInst());
            extRef.setSrcPrefix(sourceInfo.getSrcPrefix());
            extRef.setSrcCBName(sourceInfo.getSrcCBName());
        }
    }
}
