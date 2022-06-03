// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;

import java.lang.reflect.Field;
import java.util.*;
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

    protected void addControlBlock(ControlBlock<?> controlBlock) throws ScdException {

        switch (controlBlock.getServiceType() ) {
            case REPORT:
                currentElem.getReportControl().add(controlBlock.createControlBlock());
                break;
            case GOOSE:
                if(isLN0()) {
                    ((LN0)currentElem).getGSEControl().add(controlBlock.createControlBlock());
                }
                break;
            case SMV:
                if(isLN0()) {
                    ((LN0)currentElem).getSampledValueControl().add(controlBlock.createControlBlock());
                }
                break;
            default:
                throw new ScdException("Unknown control block type : " + controlBlock.getServiceType());

        }
    }

    public Optional<TDataSet> findDataSetByRef(String dataSetRef)  {
        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> Objects.equals(tDataSet.getName(),dataSetRef))
                .findFirst();
    }

    public DOIAdapter getDOIAdapterByName(String doiName) throws ScdException {
        String iedName = parentAdapter.getParentAdapter().getName();
        String ldInst = parentAdapter.getInst();
        return currentElem.getDOI()
                .stream()
                .filter(tdoi -> tdoi.getName().equals(doiName))
                .findFirst()
                .map(tdoi -> new DOIAdapter(this,tdoi))
                .orElseThrow(
                        () -> new ScdException(
                                String.format( "Unknown DOI(%s) in %s%s/%s%s%s",
                                        doiName, iedName, ldInst, getPrefix(), getLNClass(), getLNInst()
                                )
                        )
                );
    }
    public List<DOIAdapter> getDOIAdapters() {
        return currentElem.getDOI()
                .stream()
                .map(tdoi -> new DOIAdapter(this,tdoi))
                .collect(Collectors.toList());
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

    public void updateExtRefBinders(ExtRefInfo extRefInfo) throws ScdException {

        if(extRefInfo.getBindingInfo() == null || !extRefInfo.getBindingInfo().isValid()){
            throw  new ScdException("ExtRef mandatory binding data are missing");
        }
        String iedName = extRefInfo.getHolderIEDName();
        String ldInst = extRefInfo.getHolderLDInst();

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
            if(bindingInfo.getDaName() != null && bindingInfo.getDaName().isDefined()) {
                extRef.setDaName(bindingInfo.getDaName().toString());
            }

            extRef.setDoName(null);
            if(bindingInfo.getDoName() != null && bindingInfo.getDoName().isDefined()) {
                extRef.setDoName(bindingInfo.getDoName().toString());
            }

            extRef.setPrefix(bindingInfo.getPrefix());
            // invalid source info
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

        LNodeMetaData metaData = LNodeMetaData.from(this);

        for(TDataSet tDataSet : tDataSets){
            if(isLN0() && (serviceType == null || serviceType == TServiceType.GOOSE)) {
                tControls = this.lookUpControlBlocksByDataSetRef(tDataSet.getName(),TGSEControl.class);
                controlBlocks.addAll(
                        tControls.stream()
                        .map(tgseControl -> {
                            var g = new GooseControlBlock((TGSEControl)tgseControl);
                            g.setMetaData(metaData);
                            return g;
                        })
                        .collect(Collectors.toList())
                );
            }

            if(isLN0() && (serviceType == null || serviceType == TServiceType.SMV)) {

                tControls = this.lookUpControlBlocksByDataSetRef(tDataSet.getName(),TSampledValueControl.class);
                controlBlocks.addAll(
                        tControls.stream()
                        .map(sampledValueControl -> {
                            var s = new SMVControlBlock((TSampledValueControl) sampledValueControl);
                            s.setMetaData(metaData);
                            return s;
                        })
                        .collect(Collectors.toList())
                );
            }

            if(serviceType == null || serviceType == TServiceType.REPORT) {
                tControls = this.lookUpControlBlocksByDataSetRef(tDataSet.getName(),TReportControl.class);
                controlBlocks.addAll(
                        tControls.stream()
                        .map(reportControl -> {
                            var r = new ReportControlBlock((TReportControl) reportControl);
                            r.setMetaData(metaData);
                            return r;
                        })
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

    public boolean hasControlBlock(ControlBlock<? extends ControlBlock> controlBlock) {

        switch (controlBlock.getServiceType()){
            case REPORT:
                return currentElem.getReportControl().stream()
                        .anyMatch(control -> control.getName().equals(controlBlock.getName()));
            case GOOSE:
                return isLN0() && ((LN0)currentElem).getGSEControl().stream()
                        .anyMatch(control -> control.getName().equals(controlBlock.getName()));
            case SMV:
                return isLN0() && ((LN0)currentElem).getSampledValueControl().stream()
                        .anyMatch(reportControl -> reportControl.getName().equals(controlBlock.getName()));
            default:
                return false;
        }
    }

    public List<TDataSet> getDataSet(ExtRefInfo filter){
        if (filter == null || filter.getSignalInfo() == null || filter.getBindingInfo() == null) {
            return currentElem.getDataSet();
        }

        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> tDataSet.getFCDA()
                        .stream()
                        .anyMatch(filter::matchFCDA)
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

        String holderIedName = extRefInfo.getHolderIEDName(); // parent (IED) of parent (LD) can be used here
        String holderLdInst = extRefInfo.getHolderLDInst(); // parent (LD) can be use here
        List<TExtRef> extRefs = getExtRefs(signalInfo);
        if(extRefs.isEmpty()){
            String msg = String.format("Unknown TExtRef with signal info [pDO(%s),intAddr(%s)] in %s%s/%s%s%s",
                    signalInfo.getPDO(), signalInfo.getIntAddr(), holderIedName, holderLdInst,
                    getPrefix(),getLNClass(),getLNInst());
            log.error(msg);
            throw new ScdException(msg);
        }

        TExtRef extRef = extRefs.get(0); // to be refined : what's the criteria for ExtRef's uniqueness
        if(bindingInfo == null){
            return extRef;
        }

        if(!bindingInfo.isWrappedIn(extRef)){
            String msg = "No relation between binding info and the matched TExtRef";
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
            binderIEDAdapter = sclRootAdapter.getIEDAdapterByName(binderIedName);
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

    /**
     * Returns a list of resumed DataTypeTemplate for DataAttribute (updatable or not)
     * @param rDtt reference resumed DataTypeTemplate (used as filter)
     * @param updatableOnly true to retrieve only updatable DAI, false to retrieve all DAI
     * @return List of resumed DataTypeTemplate for DataAttribute (updatable or not)
     * @throws ScdException SCD illegal arguments exception
     */
    public List<ResumedDataTemplate> getDAI(ResumedDataTemplate rDtt, boolean updatableOnly) throws ScdException {
        String lnType = currentElem.getLnType();
        if(!StringUtils.isBlank(rDtt.getLnType())){
            lnType = rDtt.getLnType();
        }
        // get resumedDTT from DataTypeTemplate (it might be overridden in the DAI)
        SclRootAdapter sclRootAdapter = parentAdapter.getParentAdapter().getParentAdapter();
        DataTypeTemplateAdapter dttAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(lnType)
            .orElseThrow(
                () -> new ScdException(
                    String.format("Corrupted SCD : lnType missing for LN : %s%s", getLNClass(),getLNInst())
                )
            );
        List<ResumedDataTemplate> resumedDTTs = lNodeTypeAdapter.getResumedDTTs(rDtt);

        resumedDTTs.forEach(this::overrideAttributesFromDAI);

        if (updatableOnly){
            return resumedDTTs.stream().filter(ResumedDataTemplate::isUpdatable).collect(Collectors.toList());
        } else {
            return resumedDTTs;
        }
    }

    protected void overrideAttributesFromDAI(final ResumedDataTemplate rDtt) {
        findMatch(rDtt.getDoName(), rDtt.getDaName())
            .map(iDataAdapter -> (AbstractDAIAdapter<?>) iDataAdapter)
            .map(AbstractDAIAdapter::getCurrentElem)
            .ifPresent(tdai -> {
                rDtt.setDaiValues(tdai.getVal());
                if (rDtt.getDaName().getFc() == TFCEnum.SG || rDtt.getDaName().getFc() == TFCEnum.SE) {
                    boolean isGroup = hasSgGroup(tdai);
                    if (isGroup) {
                        rDtt.setValImport(!Boolean.FALSE.equals(tdai.isValImport()) && iedHasConfSG());
                    } else {
                        rDtt.setValImport(false);
                        log.warn("Inconsistency in the SCD file - DAI {} with fc={} must have a sGroup attribute",
                            rDtt.getObjRef(getCurrentIED().getName(), parentAdapter.getInst()), rDtt.getDaName().getFc());
                    }
                } else if (tdai.isValImport() != null) {
                    rDtt.setValImport(tdai.isValImport());
                }
            });
    }

    private boolean iedHasConfSG() {
        IEDAdapter iedAdapter = getCurrentIED();
        return iedAdapter.isSettingConfig(this.parentAdapter.getInst());
    }

    private IEDAdapter getCurrentIED() {
        LDeviceAdapter lDeviceAdapter = this.parentAdapter;
        return lDeviceAdapter.getParentAdapter();
    }

    private boolean hasSgGroup(TDAI tdai) {
        return tdai.getVal().stream().anyMatch(tVal -> tVal.getSGroup() != null && tVal.getSGroup().intValue() > 0);
    }

    /**
     * Search for DAI that match the given defined-DO (do.sdo1[.sdo2 ...sdo_n]) and defined-DA (da.bda1[.bda2...bda_n])
     * where 'sdo_n' points to a DOType that contains 'da'
     * @param doTypeName defined-DO (do.sdo1[.sdo2 ...sdo_n])
     * @param daTypeName defined-DA (da.bda1[.bda2...bda_n])
     * @return Optional of DAIAdapter for the matched DAI
     */
    protected Optional<IDataAdapter> findMatch(DoTypeName doTypeName, DaTypeName daTypeName){
        DAITracker daiTracker = new DAITracker(this,doTypeName,daTypeName);
        DAITracker.MatchResult matchResult = daiTracker.search();
        if(matchResult != DAITracker.MatchResult.FULL_MATCH){
            return Optional.empty();
        }
        return Optional.of(daiTracker.getBdaiOrDaiAdapter());
    }

    public void updateDAI(@NonNull ResumedDataTemplate rDtt) throws ScdException {

        if(!rDtt.isDoNameDefined() || !rDtt.isDaNameDefined()){
            throw new ScdException("Cannot update undefined DAI");
        }
        DoTypeName doTypeName = rDtt.getDoName();
        DaTypeName daTypeName = rDtt.getDaName();

        DAITracker daiTracker = new DAITracker(this,doTypeName,daTypeName);
        DAITracker.MatchResult matchResult = daiTracker.search();
        AbstractDAIAdapter<?> daiAdapter = null;
        IDataParentAdapter doiOrSdoiAdapter;
        if(matchResult == DAITracker.MatchResult.FULL_MATCH){
            // update
            daiAdapter = (AbstractDAIAdapter) daiTracker.getBdaiOrDaiAdapter();
            if((daiAdapter.isValImport() != null && daiAdapter.isValImport()) ||
                    (daiAdapter.isValImport() == null && rDtt.isUpdatable()) ) {
                daiAdapter.update(daTypeName.getDaiValues());
                return;
            } else{
                throw new ScdException(String.format("DAI (%s -%s) cannot be updated",doTypeName,daTypeName));
            }
        }

        if(rDtt.isUpdatable()) {
            doiOrSdoiAdapter = daiTracker.getDoiOrSdoiAdapter();
            int idx = daiTracker.getIndexDoType();
            int doSz = doTypeName.getStructNames().size();
            if (matchResult == DAITracker.MatchResult.FAILED) {
                doiOrSdoiAdapter = addDOI(doTypeName.getName());
                idx = 0;
            } else if (idx == -1) {
                idx = 0;
            } else if (idx == doSz - 1) {
                idx = doSz;
            }
            for (int i = idx; i < doSz; ++i) {
                String sdoName = doTypeName.getStructNames().get(i);
                doiOrSdoiAdapter = doiOrSdoiAdapter.addSDOI(sdoName);
            }

            IDataParentAdapter daiOrBdaiAdapter = daiTracker.getDoiOrSdoiAdapter();
            idx = daiTracker.getIndexDaType();
            int daSz = daTypeName.getStructNames().size();
            if(idx <= -1 ){
                idx = 0;
            } else if(idx == daSz - 1){
                idx = daSz;
            }
            for(int i = idx; i < daSz; ++i){
                String bdaName = daTypeName.getStructNames().get(i);
                if(idx == 0){
                    daiOrBdaiAdapter = doiOrSdoiAdapter.addSDOI(daTypeName.getName());
                } else if(i == daSz -1){
                    daiAdapter = daiOrBdaiAdapter.addDAI(bdaName, rDtt.isUpdatable());
                } else {
                    daiOrBdaiAdapter = daiOrBdaiAdapter.addSDOI(bdaName);
                }
            }
            if(daiAdapter == null){
               daiAdapter = doiOrSdoiAdapter.addDAI(daTypeName.getName(),rDtt.isUpdatable());
            }

            daiAdapter.update(daTypeName.getDaiValues());
        }
    }

    protected DOIAdapter addDOI(String name) {
        TDOI tdoi = new TDOI();
        tdoi.setName(name);
        currentElem.getDOI().add(tdoi);

        return new DOIAdapter(this,tdoi);
    }

    public String getLNodeName() {
        StringBuilder stringBuilder = new StringBuilder();

        if(TLLN0Enum.LLN_0.value().equals(getLNClass())){
            stringBuilder.append(TLLN0Enum.LLN_0.value());
        } else {
            stringBuilder.append(getPrefix())
                    .append(getLNClass())
                    .append(getLNInst());
        }
        return stringBuilder.toString();
    }

    public boolean matches(ObjectReference objRef) {
        String dataAttribute = objRef.getDataAttributes();
        SclRootAdapter sclRootAdapter = parentAdapter.getParentAdapter().getParentAdapter();
        DataTypeTemplateAdapter dttAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(getLnType())
                .orElseThrow(
                        () -> new AssertionError(
                                String.format("Corrupted  SCD file: Reference to unknown LNodeType(%s)", getLnType())
                        )
                );
        ResumedDataTemplate filter = ResumedDataTemplate.builder()
            .lnInst(getLNInst())
            .lnClass(getLNClass())
            .lnType(currentElem.getLnType()).build();
        List<ResumedDataTemplate> rDtts = lNodeTypeAdapter.getResumedDTTs(filter);

        return matchesDataAttributes(dataAttribute) ||
                rDtts.stream().anyMatch(rDtt -> rDtt.getDataAttributes().startsWith(dataAttribute));
    }

    protected  boolean matchesDataAttributes(String dataAttribute){
        return  currentElem.getDataSet().stream().anyMatch(tDataSet -> tDataSet.getName().equals(dataAttribute)) ||
                currentElem.getReportControl().stream().anyMatch(rptCtl -> rptCtl.getName().equals(dataAttribute));
    }

    public Optional<DataSetInfo> getDataSetByRef(String dataSetRef) {
        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> tDataSet.getName().equals(dataSetRef))
                .map(DataSetInfo::from)
                .findFirst();
    }

    public void addDataSet(DataSetInfo dataSetInfo) {
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName(dataSetInfo.getName());
        tDataSet.getFCDA().addAll(
                dataSetInfo.getFCDAInfos().stream().map(FCDAInfo::getFCDA).collect(Collectors.toList())
        );
        currentElem.getDataSet().add(tDataSet);

    }

    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter.getParentAdapter().getParentAdapter().getDataTypeTemplateAdapter();
    }

    public Map<Long, String> getDAIValues(ResumedDataTemplate rDtt) {
        DAITracker daiTracker = new DAITracker(this,rDtt.getDoName(),rDtt.getDaName());
        DAITracker.MatchResult matchResult = daiTracker.search();
        if(matchResult != DAITracker.MatchResult.FULL_MATCH){
            return new HashMap<>();
        }
        List<TVal> tVals;
        IDataAdapter daiAdapter = daiTracker.getBdaiOrDaiAdapter();
        if(daiAdapter.getClass().equals(SDIAdapter.DAIAdapter.class)){
            tVals =  ((RootSDIAdapter.DAIAdapter)daiAdapter).getCurrentElem().getVal();
        } else if(daiAdapter.getClass().equals(RootSDIAdapter.DAIAdapter.class)){
            tVals =  ((RootSDIAdapter.DAIAdapter)daiAdapter).getCurrentElem().getVal();
        } else {
            tVals =  ((DOIAdapter.DAIAdapter)daiAdapter).getCurrentElem().getVal();
        }

        Map<Long,String> res = new HashMap<>();
        tVals.forEach( tVal -> res.put(tVal.getSGroup(),tVal.getValue()));

        return res;
    }
}
