// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TControl;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TGSEControl;
import org.lfenergy.compas.scl2007b4.model.TReportControl;
import org.lfenergy.compas.scl2007b4.model.TSampledValueControl;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.sct.commons.dto.ControlBlock;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.dto.GooseControlBlock;
import org.lfenergy.compas.sct.commons.dto.ReportControlBlock;
import org.lfenergy.compas.sct.commons.dto.SMVControlBlock;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

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
    public static LNAdapterBuilder builder(){
        return new LNAdapterBuilder();
    }

    protected abstract Class<T> getElementClassType();
    public abstract String getLNClass() ;
    public abstract String getLNInst();
    public abstract String getPrefix();

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
        if(!signalInfo.isValid()){
            throw new IllegalArgumentException("Invalid or missing attributes in ExtRef signal info");
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
                log.warn("More the one desc for ExtRef [pDO({}),intAddr({})] in {}{}/{}",
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
        boolean isSrcReset = false;
        if(bindingInfo != null && bindingInfo.isValid()){

            extRef.setIedName(bindingInfo.getIedName());
            extRef.setLdInst(bindingInfo.getLdInst());
            extRef.setLnInst(bindingInfo.getLnInst());
            extRef.getLnClass().clear();
            extRef.getLnClass().add(bindingInfo.getLnClass());
            if(bindingInfo.getServiceType() == null && extRefInfo.getSignalInfo() != null){
                bindingInfo.setServiceType(extRefInfo.getSignalInfo().getPServT());
            }
            extRef.setServiceType(bindingInfo.getServiceType());
            extRef.setDaName(null);
            if(bindingInfo.getDaName() != null && !StringUtils.isEmpty(bindingInfo.getDaName().toString())) {
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
            isSrcReset = true;
        }
        //
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if(sourceInfo != null && isSrcReset){
            extRef.setSrcLNInst(sourceInfo.getSrcLNInst());
            if(sourceInfo.getSrcLNClass() != null) {
                extRef.getSrcLNClass().add(sourceInfo.getSrcLNClass());
            }
            extRef.setSrcLDInst(sourceInfo.getSrcLDInst());
            extRef.setSrcPrefix(sourceInfo.getSrcPrefix());
            extRef.setSrcCBName(sourceInfo.getSrcCBName());
        }
    }

    public List<ControlBlock<?>> getControlSetByExtRefInfo(ExtRefInfo extRefInfo) {

        List<TDataSet> tDataSets = this.getDataSet(extRefInfo);
        return getControlBlocks(tDataSets,extRefInfo.getBindingInfo().getServiceType());
    }

    protected List<ControlBlock<?>> getControlBlocks(List<TDataSet> tDataSets, TServiceType serviceType) {
        List<ControlBlock<?>> controlBlocks = new ArrayList<>();
        List<?> tControls;

        for(TDataSet tDataSet : tDataSets){
            if(isLN0() && (serviceType == null || serviceType == TServiceType.GOOSE)) {
                tControls = this.lookUpControlBlocksByDataSetRef(tDataSet.getName(),TGSEControl.class);
                controlBlocks.addAll(
                        tControls.stream()
                        .map(tgseControl -> new GooseControlBlock((TGSEControl)tgseControl))
                        .collect(Collectors.toList())
                );
            }

            if(isLN0() && (serviceType == null || serviceType == TServiceType.SMV)) {

                tControls = this.lookUpControlBlocksByDataSetRef(tDataSet.getName(),TSampledValueControl.class);
                controlBlocks.addAll(
                        tControls.stream()
                        .map(sampledValueControl -> new SMVControlBlock((TSampledValueControl) sampledValueControl))
                        .collect(Collectors.toList())
                );
            }

            if(serviceType == null || serviceType == TServiceType.REPORT) {
                tControls = this.lookUpControlBlocksByDataSetRef(tDataSet.getName(),TReportControl.class);
                controlBlocks.addAll(
                        tControls.stream()
                        .map(reportControl -> new ReportControlBlock((TReportControl) reportControl))
                        .collect(Collectors.toList())
                );
            }
        }

        return controlBlocks;
    }

    protected <T> List<? extends TControl> lookUpControlBlocksByDataSetRef(@NonNull String dataSetRef, Class<T> cls){
        List<? extends TControl> ls = new ArrayList<>();
        if (TGSEControl.class.equals(cls) && isLN0()) {
            ls = ((LN0) currentElem).getGSEControl();
        } else if(TSampledValueControl.class.equals(cls) && isLN0()){
            ls =  ((LN0) currentElem).getSampledValueControl();
        } else if(TReportControl.class.equals(cls)){
            ls = currentElem.getReportControl();
        }

        return ls.stream()
                .map(TControl.class::cast)
                .filter(tControl -> dataSetRef.equals(tControl.getDatSet()))
                .collect(Collectors.toList());
    }

    public static boolean isNull(TFCDA tfcda){
        return Objects.isNull(tfcda.getLdInst()) &&
                tfcda.getLnClass().isEmpty() &&
                Objects.isNull(tfcda.getFc()) ;

    }

    protected List<TDataSet> getDataSet(ExtRefInfo filter){
        if (filter == null || filter.getSignalInfo() == null || filter.getBindingInfo() == null) {
            return currentElem.getDataSet();
        }

        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> tDataSet.getFCDA()
                        .stream()
                        .anyMatch(tfcda -> filter.matchFCDA(tfcda))
                )
                .collect(Collectors.toList());
    }

    public TExtRef updateExtRefSource(ExtRefInfo extRefInfo) throws ScdException {
        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();

        if(signalInfo == null || bindingInfo == null || sourceInfo == null){
            throw new IllegalArgumentException("ExtRef information (signal, binding, source) are missing");
        }
        TExtRef extRef = checkExtRefInfoCoherence(extRefInfo);
        updateExtRefBindingInfo(extRef,extRefInfo);

        return extRef;
    }

    /**
     * Verify the coherence of ExtRef info.
     * This method look up the TExtRef that matches the signal info in this LNode. Then verify the coherence
     * between the ExtRef's binding info and the given binding info. If the ExtRef info contains a source information (
     * ControlBlock info),it verifies that the CB and dataset it points to, exists within and existing binder IED.
     * @param extRefInfo ExtRef information (signal, binding and source info)
     * @return TExtRef that matches the signal info
     * @throws IllegalArgumentException when no given signal info
     * @throws ScdException throws exception if
     *          <ul>
     *              <li> given signal info or it doesn't match any TExtRef in this LNode</li>
     *              <li> the given binding info doesn't match the found TExtRef's binding info.</li>
     *              <li> the given binding info doesn't refer to an exiting IED, LDevice and LNode in the SCL</li>
     *              <li> given source info references unknown control block</li>
     *          </ul>
     */
    public TExtRef checkExtRefInfoCoherence(@NonNull ExtRefInfo extRefInfo) throws ScdException {

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if(signalInfo == null ) {
            log.error("Coherence checking needs at least a signal info");
            throw new IllegalArgumentException("Coherence checking needs at least a signal info");
        }

        String holderIedName = extRefInfo.getHolderIedName(); // parent (IED) of parent (LD) can be used here
        String holderLdInst = extRefInfo.getHolderLdInst(); // parent (LD) can be use here
        List<TExtRef> extRefs = getExtRefs(signalInfo);
        if(extRefs.isEmpty()){
            String msg = String.format("Unknown TExtRef with signal info [pDO(%s),intAddr(%s)] in %s%s/%s%s%s",
                    signalInfo.getPDO(), signalInfo.getIntAddr(), holderIedName, holderLdInst,
                    getPrefix(),getLNClass(),getLNInst());
            log.error(msg);
            throw new ScdException(msg);
        }

        TExtRef extRef = extRefs.get(0); // to be refined : what's criteria for ExtRef's uniqueness
        if(bindingInfo == null){
            return extRef;
        }

        if(!bindingInfo.isWrappedIn(extRef)){
            String msg = String.format("No relation between binding info and the matched TExtRef");
            log.error(msg);
            throw new ScdException(msg);
        }

        String binderIedName = bindingInfo.getIedName();
        String binderLdInst = bindingInfo.getLdInst();
        String binderLnClass = bindingInfo.getLnClass();
        String binderLnInst = bindingInfo.getLnInst();
        String binderLnPrefix = bindingInfo.getPrefix();
        IEDAdapter binderIEDAdapter;
        if(!binderIedName.equals( parentAdapter.getParentAdapter().getName())){ // external binding
            SclRootAdapter sclRootAdapter = parentAdapter.getParentAdapter().getParentAdapter();
            binderIEDAdapter = sclRootAdapter.getIEDAdapter(binderIedName);
        } else {
            binderIEDAdapter = parentAdapter.getParentAdapter();
        }
        LDeviceAdapter binderLDAdapter = binderIEDAdapter.getLDeviceAdapterByLdInst(binderLdInst)
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Unknown LDevice (%s) in IED (%s)", binderLdInst, binderIedName)
                        )
                );
        AbstractLNAdapter<?> anLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(binderLDAdapter)
                .withLnClass(binderLnClass)
                .withLnInst(binderLnInst)
                .withLnPrefix(binderLnPrefix)
                .build();


        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if(sourceInfo == null || sourceInfo.isNull()) { // to be refined : what to do here functionally ?
            return extRef;
        }
        List<ControlBlock<?>> cbs = anLNAdapter.getControlSetByExtRefInfo(extRefInfo);
        boolean isCoherent = !cbs.isEmpty() && cbs.stream()
                .anyMatch(controlBlock -> controlBlock.getName().equals(sourceInfo.getSrcCBName()));
        if(!isCoherent){
            String msg = String.format("Unknown control block %s in %s%s/%s%s%s",
                    sourceInfo.getSrcCBName(), binderIedName,binderLdInst,
                    binderLnPrefix , binderLnClass, binderLnInst);
            log.error(msg);
            throw new ScdException(msg);
        }

        return extRef;
    }
}
