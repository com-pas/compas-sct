// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.ln;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.DataSetService;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;


/**
 * A representation of the model object
 * <em><b>{@link AbstractLNAdapter AbstractLNAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link AbstractLNAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link AbstractLNAdapter#getLNInst <em>Returns the value of the <b>inst </b>attribute</em>}</li>
 *      <li>{@link AbstractLNAdapter#getLNClass <em>Returns the value of the <b>lnClass </b>attribute</em>}</li>
 *      <li>{@link AbstractLNAdapter#getLnType <em>Returns the value of the <b>lnType </b>attribute</em>}</li>
 *      <li>{@link AbstractLNAdapter#getLNodeName <em>Returns the logical node name <b>LNName = prefix + lnClass + lnInst</b></em>}</li>
 *
 *      <li>{@link AbstractLNAdapter#getExtRefs() <em>Returns the value of the <b>TExtRef </b>containment reference list</em>}</li>
 *      <li>{@link AbstractLNAdapter#getExtRefs(ExtRefSignalInfo) <em>Returns the value of the <b>TExtRef </b>containment reference list By <b>ExtRefSignalInfo <b></b></b></em>}</li>
 *
 *      <li>{@link AbstractLNAdapter#getDAI <em>Returns the value of the <b>DataAttributeRef </b> containment reference By filter</em>}</li>
 *      <li>{@link AbstractLNAdapter#getDAIValues(DataAttributeRef) <em>Returns <b>DAI (sGroup, value) </b> containment reference list By <b>DataAttributeRef </b> filter</em>}</li>
 *
 *      <li>{@link AbstractLNAdapter#getDataSetByName(String) <em>Returns the value of the <b>TDataSet </b>object reference By the value of the <b>name </b>attribute </em>}</li>
 *
 *      <li>{@link AbstractLNAdapter#getControlBlocks(List, TServiceType) <em>Returns the value of the <b>ControlBlock </b>containment reference list that match <b>datSet </b> value of given <b>TDataSet</b> </em>}</li>
 *      <li>{@link AbstractLNAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link AbstractLNAdapter#removeAllControlBlocksAndDatasets() <em>Remove all <b>ControlBlock</b></em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *      <li>{@link AbstractLNAdapter#matches(ObjectReference) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LNAdapter DataName</em>}</li>
 *      <li>{@link AbstractLNAdapter#matchesDataAttributes(String) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LNAdapter DataName Excluding DataName from DataTypeTemplat</em>}</li>
 *    </ul>
 * </ol>
 * <br/>
 * <pre>
 *    <b>ObjectReference</b>: LDName/LNName.DataName[.DataName[…]].DataAttributeName[.DAComponentName[ ….]]
 *    <b>LDName</b> = "name" attribute of IEDName element + "inst" attribute of LDevice element
 *    <b>LNName</b> = "prefix" + "lnClass" + "lnInst"
 * </pre>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TAnyLN
 */
@Getter
@Slf4j
public abstract class AbstractLNAdapter<T extends TAnyLN> extends SclElementAdapter<LDeviceAdapter, T> {


    public static final DoTypeName MOD_DO_TYPE_NAME = new DoTypeName(MOD_DO_NAME);
    public static final DaTypeName STVAL_DA_TYPE_NAME = new DaTypeName(STVAL_DA_NAME);
    private static final String DAI_MOD_STVAL_VALUE_ON = "on";

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    protected AbstractLNAdapter(LDeviceAdapter parentAdapter, T currentElem) {
        super(parentAdapter, currentElem);
    }

    public static LNAdapterBuilder builder() {
        return new LNAdapterBuilder();
    }

    protected abstract Class<T> getElementClassType();

    public abstract String getLNClass();

    public abstract String getLNInst();

    public abstract String getPrefix();

    /**
     * Returns sets of enum value for given DataAttributeRef object
     *
     * @param enumType enum Type
     * @return Enum value list
     */
    public Set<String> getEnumValues(String enumType) {
        Optional<EnumTypeAdapter> enumTypeAdapter = getDataTypeTemplateAdapter()
                .getEnumTypeAdapterById(enumType);

        if (enumTypeAdapter.isEmpty()) {
            return Collections.emptySet();
        }
        return enumTypeAdapter.get().getCurrentElem().getEnumVal()
                .stream().map(TEnumVal::getValue)
                .collect(Collectors.toSet());
    }

    public Optional<DataSetAdapter> findDataSetByName(String dataSetName) {
        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> Objects.equals(tDataSet.getName(), dataSetName))
                .findFirst()
                .map(dataSet -> new DataSetAdapter(this, dataSet));
    }

    /**
     * Find DOI in this LN/LN0 by name
     *
     * @param doiName name of the DOI to look for
     * @return DOIAdapter when found or else empty Optional.
     */
    public Optional<DOIAdapter> findDoiAdapterByName(String doiName) {
        return currentElem.getDOI()
                .stream()
                .filter(tdoi -> tdoi.getName().equals(doiName))
                .findFirst()
                .map(tdoi -> new DOIAdapter(this, tdoi));
    }

    /**
     * Get DOI in this LN/LN0 by name
     *
     * @param doiName name of the DOI to look for
     * @return DOIAdapter when found or else empty Optional.
     * @throws ScdException when DOI not found
     */
    public DOIAdapter getDOIAdapterByName(String doiName) throws ScdException {
        return findDoiAdapterByName(doiName)
                .orElseThrow(() -> new ScdException(String.format("Unknown DOI(%s) in %s", doiName, getXPath())));
    }

    public List<DOIAdapter> getDOIAdapters() {
        return currentElem.getDOI()
                .stream()
                .map(tdoi -> new DOIAdapter(this, tdoi))
                .toList();
    }

    boolean isLN0() {
        return getElementClassType() == LN0.class;
    }

    public String getLnType() {
        return currentElem.getLnType();
    }

    /**
     * Gets all ExtRefs
     *
     * @return list of LNode ExtRefs elements
     */
    public List<TExtRef> getExtRefs() {
        return getExtRefs(null);
    }

    /**
     * Gets all ExtRefs matches specified ExtRef info
     *
     * @param filter ExtRef filter value
     * @return list of <em>TExtRef</em>
     */
    public List<TExtRef> getExtRefs(ExtRefSignalInfo filter) {
        if (!hasInputs()) {
            return new ArrayList<>();
        }

        if (filter == null) {
            return currentElem.getInputs().getExtRef();
        }
        return currentElem.getInputs().getExtRef()
                .stream()
                .filter(tExtRef ->
                        ((filter.getDesc() == null && tExtRef.getDesc().isEmpty())
                                || Objects.equals(filter.getDesc(), tExtRef.getDesc())) &&
                                Objects.equals(filter.getPDO(), tExtRef.getPDO()) &&
                                Objects.equals(filter.getPDA(), tExtRef.getPDA()) &&
                                Objects.equals(filter.getIntAddr(), tExtRef.getIntAddr()) &&
                                Objects.equals(filter.getPServT(), tExtRef.getPServT()))
                .toList();
    }

    /**
     * Check whether the LN has an Inputs node
     *
     * @return true if the LN has an Inputs node
     */
    public boolean hasInputs() {
        return currentElem.isSetInputs();
    }

    /**
     * Checks for ExtRef signal existence in target LN
     *
     * @param signalInfo ExtRef signal data to check
     */
    public void isExtRefExist(ExtRefSignalInfo signalInfo) {
        if (currentElem.getInputs() == null || signalInfo == null) {
            throw new ScdException("No Inputs for LN or no ExtRef signal to check");
        }
        if (!signalInfo.isValid()) {
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        boolean extRefExist = currentElem.getInputs().getExtRef()
                .stream()
                .anyMatch(tExtRef -> Objects.equals(signalInfo.getDesc(), tExtRef.getDesc()) &&
                        Objects.equals(tExtRef.getPDO(), signalInfo.getPDO()) &&
                        Objects.equals(signalInfo.getIntAddr(), tExtRef.getIntAddr()) &&
                        Objects.equals(signalInfo.getPServT(), tExtRef.getPServT()));
        if (!extRefExist) {
            throw new ScdException("ExtRef signal does not exist in target LN");
        }
    }

    /**
     * Update LNode ExtRefs data with ExtRefInfo data
     *
     * @param extRefInfo contains new data for LNode ExtREf update
     * @throws ScdException throws when mandatory data are missing
     */
    public void updateExtRefBinders(ExtRefInfo extRefInfo) throws ScdException {

        if (extRefInfo.getBindingInfo() == null || !extRefInfo.getBindingInfo().isValid()) {
            throw new ScdException("ExtRef mandatory binding data are missing");
        }
        String iedName = extRefInfo.getHolderIEDName();
        String ldInst = extRefInfo.getHolderLDInst();

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        List<TExtRef> tExtRefs = this.getExtRefs(signalInfo);
        if (tExtRefs.isEmpty()) {
            String msg = String.format("Unknown ExtRef [pDO(%s),intAddr(%s)] in %s/%s.%s",
                    signalInfo.getPDO(), signalInfo.getIntAddr(), iedName, ldInst, getLNClass());
            throw new ScdException(msg);
        }
        if (tExtRefs.size() != 1) {
            log.warn("More the one desc for ExtRef [pDO({}),intAddr({})] in {}{}/{}",
                    signalInfo.getPDO(), signalInfo.getIntAddr(), iedName, ldInst, getLNClass());
        }
        TExtRef extRef = tExtRefs.get(0);
        // update ExtRef with binding info
        updateExtRefBindingInfo(extRef, extRefInfo);
    }

    /**
     * Updates ExtRef with data from ExtRefInfo
     *
     * @param extRef     ExtRef to update
     * @param extRefInfo contains new data for LNode ExtREf update
     */
    protected void updateExtRefBindingInfo(TExtRef extRef, ExtRefInfo extRefInfo) {
        //update binding info
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        boolean isSrcReset = false;
        if (bindingInfo != null && bindingInfo.isValid()) {

            extRef.setIedName(bindingInfo.getIedName());
            extRef.setLdInst(bindingInfo.getLdInst());
            extRef.setLnInst(bindingInfo.getLnInst());
            extRef.getLnClass().clear();
            extRef.getLnClass().add(bindingInfo.getLnClass());
            if (bindingInfo.getServiceType() == null && extRefInfo.getSignalInfo() != null) {
                bindingInfo.setServiceType(extRefInfo.getSignalInfo().getPServT());
            }
            extRef.setServiceType(bindingInfo.getServiceType());
            extRef.setDaName(null);
            if (bindingInfo.getDaName() != null && bindingInfo.getDaName().isDefined()) {
                extRef.setDaName(bindingInfo.getDaName().toString());
            }

            extRef.setDoName(null);
            if (bindingInfo.getDoName() != null && bindingInfo.getDoName().isDefined()) {
                extRef.setDoName(bindingInfo.getDoName().toString());
            }

            extRef.setPrefix(bindingInfo.getPrefix());
            // invalid source info
            removeExtRefSourceBinding(extRef);
            isSrcReset = true;
        }
        //
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        if (sourceInfo != null && isSrcReset) {
            extRef.setSrcLNInst(sourceInfo.getSrcLNInst());
            if (sourceInfo.getSrcLNClass() != null) {
                extRef.getSrcLNClass().add(sourceInfo.getSrcLNClass());
            }
            extRef.setSrcLDInst(sourceInfo.getSrcLDInst());
            extRef.setSrcPrefix(sourceInfo.getSrcPrefix());
            extRef.setSrcCBName(sourceInfo.getSrcCBName());
        }
    }

    /**
     * Gets Control Blocks matching FCDA compatible with specified in <em>extRefInfo</em>
     *
     * @param extRefInfo ExtRef signal data for which Control Blocks should be found (contain binding info to match with FCDA)
     * @return list of <em>ControlBlock</em> object as ControlBlocks of LNode matching FCDA and ExtRef
     */
    public Stream<ControlBlock> getControlBlocksForMatchingFCDA(@NonNull ExtRefInfo extRefInfo) {
        return getDataSetMatchingExtRefInfo(extRefInfo)
                .map(TDataSet::getName)
                .flatMap(dataSetName -> getControlBlocksByDataSetRef(dataSetName, extRefInfo.getBindingInfo().getServiceType()));
    }

    /**
     * finds all Control Blocks by dataSetRef and a Service Type (GOOSE, SampleValue, Report) or all Service Types.
     *
     * @param dataSetRef  DatSet value searched
     * @param serviceType service type to be filtered
     * @return all Control Blocks matching dataSetRef and a Service Type or all Service Types
     */
    public Stream<ControlBlock> getControlBlocksByDataSetRef(String dataSetRef, TServiceType serviceType) {
        Stream<ControlBlock> streamGSEControl = Stream.empty();
        Stream<ControlBlock> streamSMVControl = Stream.empty();
        Stream<ControlBlock> streamReportControl = Stream.empty();
        LNodeMetaData metaData = LNodeMetaData.from(this);

        if (isLN0()) {
            LN0 ln0 = (LN0) currentElem;
            if (serviceType == null || serviceType == TServiceType.GOOSE) {
                streamGSEControl = ln0.getGSEControl()
                        .stream()
                        .filter(tControl -> dataSetRef.equals(tControl.getDatSet()))
                        .map(tgseControl -> {
                            GooseControlBlock gseCbl = new GooseControlBlock(tgseControl);
                            gseCbl.setMetaData(metaData);
                            return gseCbl;
                        });

            }
            if (serviceType == null || serviceType == TServiceType.SMV) {
                streamSMVControl = ln0.getSampledValueControl()
                        .stream()
                        .filter(tControl -> dataSetRef.equals(tControl.getDatSet()))
                        .map(sampledValueControl -> {
                            SMVControlBlock smvCbl = new SMVControlBlock(sampledValueControl);
                            smvCbl.setMetaData(metaData);
                            return smvCbl;
                        });
            }
        }
        //REPORT
        if (serviceType == null || serviceType == TServiceType.REPORT) {
            streamReportControl = currentElem.getReportControl()
                    .stream()
                    .filter(tControl -> dataSetRef.equals(tControl.getDatSet()))
                    .map(reportControl -> {
                        ReportControlBlock rptCbl = new ReportControlBlock(reportControl);
                        rptCbl.setMetaData(metaData);
                        return rptCbl;
                    });
        }
        return Stream.concat(Stream.concat(streamGSEControl, streamSMVControl), streamReportControl);
    }

    /**
     * finds all Control Blocks by Service Type (GOOSE, SampleValue, Report).
     *
     * @param cls Type of Control Block
     * @param <V> inference parameter for Type of Control Block to find
     * @return all Control Blocks matching a Service Type
     */
    public <V extends TControl> List<V> getTControlsByType(Class<V> cls) {
        if (TGSEControl.class.equals(cls) && isLN0()) {
            return (List<V>) ((LN0) currentElem).getGSEControl();
        } else if (TSampledValueControl.class.equals(cls) && isLN0()) {
            return (List<V>) ((LN0) currentElem).getSampledValueControl();
        } else if (TReportControl.class.equals(cls)) {
            return (List<V>) currentElem.getReportControl();
        }
        throw new IllegalArgumentException("Unsupported ControlBlock "+cls.getSimpleName()+" for Lnode");
    }

    /**
     * Checks if LN contains a Control block with specified name
     *
     * @param cbName name of Control Block searched
     * @return true if there is at one control Block matches, otherwise false
     */
    private boolean isCBKnown(String cbName) {
        return isLN0()
                && (((LN0) currentElem).getGSEControl().stream().anyMatch(tgse -> tgse.getName().equals(cbName)) ||
                ((LN0) currentElem).getSampledValueControl().stream().anyMatch(tsmv -> tsmv.getName().equals(cbName)))
                || currentElem.getReportControl().stream().anyMatch(trpt -> trpt.getName().equals(cbName));
    }

    /**
     * Checks if specified Control Block is present in LN
     *
     * @param cbName           name of the control block to look for
     * @param controlBlockEnum type of control block to look for
     * @return true if a ControlBlock of type controlBlockEnum named cbName exists in LN, else false
     */
    public boolean hasControlBlock(String cbName, ControlBlockEnum controlBlockEnum) {
        return getTControlsByType(controlBlockEnum.getControlBlockClass()).stream()
                .anyMatch(control -> control.getName().equals(cbName));
    }

    /**
     * retrieves all DataSets for which at least one FCDA matches with data given in ExtRefInfo for external binding
     *
     * @param extRefInfo contains data for external binding which should match with FCDAs values
     * @return list of Data for which at least one FCDA matches with filter datas
     */
    public Stream<TDataSet> getDataSetMatchingExtRefInfo(@NonNull ExtRefInfo extRefInfo) {
        return currentElem.getDataSet()
                .stream()
                .filter(tDataSet -> tDataSet.getFCDA().stream().anyMatch(extRefInfo::checkMatchingFCDA));
    }

    /**
     * Updates ExtRef source binding data's based on given data in <em>extRefInfo</em>
     *
     * @param extRefInfo new data for ExtRef source binding data
     * @return <em>TExtRef</em> object as update ExtRef with new source binding data
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    public TExtRef updateExtRefSource(ExtRefInfo extRefInfo) throws ScdException {
        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();

        if (signalInfo == null || bindingInfo == null || sourceInfo == null || StringUtils.isBlank(sourceInfo.getSrcCBName())) {
            throw new IllegalArgumentException("ExtRef information (signal, binding, source) are missing");
        }

        checkExtRefInfoCoherence(extRefInfo);

        TExtRef extRef = extractExtRefFromExtRefInfo(extRefInfo);

        updateExtRefBindingInfo(extRef, extRefInfo);

        return extRef;
    }

    /**
     * Verify the coherence of ExtRef info.
     * This method look up the TExtRef that matches the signal info in this LNode. Then verify the coherence
     * between the ExtRef's binding info and the given binding info. If the ExtRef info contains a source information (
     * ControlBlock info),it verifies that the CB and dataset it points to, exists within and existing binder IED.
     *
     * @param extRefInfo ExtRef information (signal, binding and source info)
     * @throws ScdException throws exception if
     *                      <ul>
     *                          <li> the given binding info doesn't match the found TExtRef's binding info.</li>
     *                          <li> the given binding info doesn't refer to an existing IED, LDevice and LNode in the SCL</li>
     *                          <li> given source info references unknown control block</li>
     *                      </ul>
     */
    public void checkExtRefInfoCoherence(ExtRefInfo extRefInfo) {

        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();

        String binderIedName = bindingInfo.getIedName();
        String binderLdInst = bindingInfo.getLdInst();
        String binderLnClass = bindingInfo.getLnClass();
        String binderLnInst = bindingInfo.getLnInst();
        String binderLnPrefix = bindingInfo.getPrefix();

        IEDAdapter binderIEDAdapter;

        if (!binderIedName.equals(getParentIed().getName())) {
            binderIEDAdapter = getCurrentScd().getIEDAdapterByName(binderIedName);
        } else {
            binderIEDAdapter = getParentIed();
        }
        LDeviceAdapter binderLDeviceAdapter = binderIEDAdapter.findLDeviceAdapterByLdInst(binderLdInst)
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Unknown LDevice (%s) in IED (%s)", binderLdInst, binderIedName)
                        )
                );

        ExtRefSourceInfo sourceInfo = extRefInfo.getSourceInfo();
        List<AbstractLNAdapter<?>> aLNAdapters = getPossibleSourceLNAdapters(binderLDeviceAdapter, sourceInfo);

        boolean isCBKnown = aLNAdapters.stream()
                .anyMatch(abstractLNAdapter -> abstractLNAdapter.isCBKnown(extRefInfo.getSourceInfo().getSrcCBName()));

        if (!isCBKnown) {
            String msg = String.format("Unknown control block %s in Ied: %s / LdInst: %s / LnPrefix: %s LnClass: %s LnInst: %s",
                    extRefInfo.getSourceInfo().getSrcCBName(), binderIedName, binderLdInst, binderLnPrefix, binderLnClass, binderLnInst);
            log.error(msg);
            throw new ScdException(msg);

        }
    }

    /**
     * Gets all possible LNs for given source info
     *
     * @param binderLDeviceAdapter LDevice in which LNs are searched
     * @param sourceInfo           binding LN info
     * @return list of LNAdapters matching SourceInfo
     */
    private List<AbstractLNAdapter<?>> getPossibleSourceLNAdapters(LDeviceAdapter binderLDeviceAdapter, ExtRefSourceInfo sourceInfo) {
        List<AbstractLNAdapter<?>> aLNAdapters = new ArrayList<>();
        if (StringUtils.isBlank(sourceInfo.getSrcLNClass())) {
            aLNAdapters.addAll(binderLDeviceAdapter.getLNAdaptersIncludingLN0());
        } else {
            if (TLLN0Enum.LLN_0.value().equals(sourceInfo.getSrcLNClass())) {
                aLNAdapters.add(binderLDeviceAdapter.getLN0Adapter());
            } else {
                aLNAdapters.add(binderLDeviceAdapter.getLNAdapter(sourceInfo.getSrcLNClass(), sourceInfo.getSrcLNInst(), sourceInfo.getSrcPrefix()));
            }
        }
        return aLNAdapters;
    }

    /**
     * Finds ExtRef from LN
     *
     * @param extRefInfo ExtRef information (signal, binding and source info)
     * @return TExtRef matching given
     */
    public TExtRef extractExtRefFromExtRefInfo(@NonNull ExtRefInfo extRefInfo) {

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        List<TExtRef> extRefs = getExtRefs(signalInfo);

        if (extRefs.isEmpty()) {
            String holderIedName = extRefInfo.getHolderIEDName(); // parent (IED) of parent (LD) can be used here
            String holderLdInst = extRefInfo.getHolderLDInst(); // parent (LD) can be use here
            String msg = String.format("Unknown TExtRef with signal info [pDO(%s),intAddr(%s)] in %s%s/%s%s%s",
                    signalInfo.getPDO(), signalInfo.getIntAddr(), holderIedName, holderLdInst,
                    getPrefix(), getLNClass(), getLNInst());
            log.error(msg);
            throw new ScdException(msg);
        }
        TExtRef extRef = extRefs.get(0);// to be refined : what's the criteria for ExtRef's uniqueness
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if (!bindingInfo.isWrappedIn(extRef)) {
            String msg = "No relation between binding info and the matched TExtRef";
            log.error(msg);
            throw new ScdException(msg);
        }
        return extRef;
    }

    /**
     * Returns a list of Data Attribute Reference for DataAttribute (updatable or not)
     *
     * @param dataAttributeRef          Data Attribute Reference (used as filter)
     * @param updatableOnly true to retrieve DataTypeTemplate's related to only updatable DAI, false to retrieve all
     * @return List of Data Attribute Reference (updatable or not)
     * @throws ScdException SCD illegal arguments exception
     */
    public List<DataAttributeRef> getDAI(DataAttributeRef dataAttributeRef, boolean updatableOnly) throws ScdException {
        String lnType = currentElem.getLnType();
        if (!StringUtils.isBlank(dataAttributeRef.getLnType())) {
            lnType = dataAttributeRef.getLnType();
        }
        // get dataAttributeRef from DataTypeTemplate (it might be overridden in the DAI)
        DataTypeTemplateAdapter dttAdapter = getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(lnType)
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Corrupted SCD : lnType missing for LN : %s%s", getLNClass(), getLNInst())
                        )
                );
        List<DataAttributeRef> dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs(dataAttributeRef);

        dataAttributeRefs.forEach(this::overrideAttributesFromDAI);

        if (updatableOnly) {
            return dataAttributeRefs.stream().filter(DataAttributeRef::isUpdatable).toList();
        } else {
            return dataAttributeRefs;
        }
    }

    /**
     * Update given DataAttributeRef DAI datas from LNode
     *
     * @param dataAttributeRef summarized Data Type Template object to update DAI datas
     */
    protected void overrideAttributesFromDAI(final DataAttributeRef dataAttributeRef) {
        findMatch(dataAttributeRef.getDoName(), dataAttributeRef.getDaName())
                .map(iDataAdapter -> (AbstractDAIAdapter<?>) iDataAdapter)
                .map(AbstractDAIAdapter::getCurrentElem)
                .ifPresent(tdai -> {
                    dataAttributeRef.setDaiValues(tdai.getVal());
                    if (dataAttributeRef.getDaName().getFc() == TFCEnum.SG || dataAttributeRef.getDaName().getFc() == TFCEnum.SE) {
                        boolean isGroup = hasSgGroup(tdai);
                        if (isGroup) {
                            dataAttributeRef.setValImport((!tdai.isSetValImport() || tdai.isValImport()) && iedHasConfSG());
                        } else {
                            dataAttributeRef.setValImport(false);
                            log.warn("Inconsistency in the SCD file - DAI {} with fc={} must have a sGroup attribute",
                                    dataAttributeRef.getObjRef(getParentIed().getName(), parentAdapter.getInst()), dataAttributeRef.getDaName().getFc());
                        }
                    } else if (tdai.isSetValImport()) {
                        dataAttributeRef.setValImport(tdai.isValImport());
                    }
                });
    }

    /**
     * Checks if linked IED as parent has Setting Group set
     *
     * @return <em>Boolean</em> value of check result
     */
    private boolean iedHasConfSG() {
        IEDAdapter iedAdapter = getParentIed();
        return iedAdapter.isSettingConfig(this.parentAdapter.getInst());
    }

    /**
     * Gets linked LDevice as parent
     *
     * @return <em>IEDAdapter</em> object
     */
    public LDeviceAdapter getParentLDevice() {
        return this.parentAdapter;
    }

    /**
     * Gets linked IED as parent
     *
     * @return <em>IEDAdapter</em> object
     */
    public IEDAdapter getParentIed() {
        return getParentLDevice().getParentAdapter();
    }

    /**
     * Gets root Scd
     *
     * @return <em>SclRootAdapter</em> object
     */
    protected SclRootAdapter getCurrentScd() {
        return getParentIed().getParentAdapter();
    }

    /**
     * Gets SCL DataTypeTemplate
     *
     * @return <em>DataTypeTemplateAdapter</em> object
     */
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return getCurrentScd().getDataTypeTemplateAdapter();
    }


    /**
     * Checks from DAI if Setting Group value is set correctly
     *
     * @param tdai DAI for which check is done
     * @return <em>Boolean</em> value of check result
     */
    private boolean hasSgGroup(TDAI tdai) {
        return tdai.getVal().stream().anyMatch(tVal -> tVal.isSetSGroup() && tVal.getSGroup() > 0);
    }

    /**
     * Search for DAI that match the given defined-DO (do.sdo1[.sdo2 ...sdo_n]) and defined-DA (da.bda1[.bda2...bda_n])
     * where 'sdo_n' points to a DOType that contains 'da'
     *
     * @param doTypeName defined-DO (do.sdo1[.sdo2 ...sdo_n])
     * @param daTypeName defined-DA (da.bda1[.bda2...bda_n])
     * @return Optional of DAIAdapter for the matched DAI
     */
    protected Optional<IDataAdapter> findMatch(DoTypeName doTypeName, DaTypeName daTypeName) {
        DAITracker daiTracker = new DAITracker(this, doTypeName, daTypeName);
        DAITracker.MatchResult matchResult = daiTracker.search();
        if (matchResult != DAITracker.MatchResult.FULL_MATCH) {
            return Optional.empty();
        }
        return Optional.of(daiTracker.getBdaiOrDaiAdapter());
    }

    /**
     * Updates DAI in LN/LN0 section.
     * It will create the missing DOI/SDI/DAI in this LN/LN0 if needed.
     * Be careful, this method does not check that the given dataAttributeRef is allowed by the lnType of this LN/LN0.
     * It does not even check if dataAttributeRef exists in DataTypeTemplate section.
     * That means that it will create the missing DOI/SDI/DAI, even if it is not consistent with DataTypeTemplate section.
     * It is the caller responsibility to ensure the consistency between the given dataAttributeRef and the lnType of this LN/LN0 (which points to DataTypeTemplate section).
     * See 9.3.5 "LN0 and other Logical Nodes" of IEC 61850-6.
     * If given dataAttributeRef.isUpdatable() is false, the method does nothing (it will not check if DA/BDA is updatable in DataTypeTemplate section).
     * This method will not remove any Val, it will only create new Val or replace existing Val (for example if dataAttributeRef.getDaName().getDaiValues() is empty, it does nothing)
     *
     * @param dataAttributeRef summarized Data Type Temple containing new DA val
     * @throws ScdException when given dataAttributeRef is missing DoName or DaName
     */
    public void updateDAI(@NonNull DataAttributeRef dataAttributeRef) throws ScdException {

        if (!dataAttributeRef.isDoNameDefined() || !dataAttributeRef.isDaNameDefined()) {
            throw new ScdException("Cannot update undefined DAI");
        }
        if (!dataAttributeRef.isUpdatable() || dataAttributeRef.getDaName().getDaiValues().isEmpty()) {
            return;
        }

        AbstractDAIAdapter<?> daiAdapter = (AbstractDAIAdapter<?>) createDoiSdiDaiChainIfNotExists(dataAttributeRef.getDataAttributes(), dataAttributeRef.isUpdatable());
        daiAdapter.update(dataAttributeRef.getDaName().getDaiValues());
    }

    /**
     * Return the DAI (in DOI/SDI/DAI chain) matching the given data reference name
     * If it does not exist, create the missing DOI/SDI/DAI elements in this LN/LN0 if needed, based on the given parameters
     * DOI is the equivalent of the DO
     * SDI is the equivalent of a type with bType="Struct". It can be a SDO, DA or BDA.
     * DOI is the equivalent of the final leaf : DA or BDA with bType != "Struct".
     * Be careful, this method does not check that the given data type is allowed by the lnType of this LN/LN0.
     * It does not even check if the data type exists in DataTypeTemplate section.
     * That means that it will create the missing DOI/SDI/DAI, even if it is not consistent with DataTypeTemplate section.
     * It is the caller responsibility to ensure the consistency between the given data type and the lnType of this LN/LN0 (which refer to DataTypeTemplate section).
     * See 9.3.5 "LN0 and other Logical Nodes" of IEC 61850-6.
     *
     * @param dataTypeRef          Reference name of data : DO/SDO/DA/BDA names, in order from parent to child, separated by a period
     *                             (Ex: "Do1.da1", "Do2.sdoA.sdoB.da2.bdaA.bdaB")
     * @param setValImportOnCreate when this method creates the DAI, it will set DAI.valImport attribute with this parameter
     * @return adapter for existing DAI or created DAI.
     */
    public Object createDoiSdiDaiChainIfNotExists(String dataTypeRef, boolean setValImportOnCreate) {
        String[] names = dataTypeRef.split("\\.");
        if (names.length < 2 || Arrays.stream(names).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("dataTypeRef must be valid with at least a DO and a DA, but got: " + dataTypeRef);
        }
        String doiName = names[0];
        String daiName = names[names.length - 1];
        ListIterator<String> sdiNames = Arrays.asList(names).subList(1, names.length - 1).listIterator();
        IDataParentAdapter parentDoiOrSdi = findDoiAdapterByName(doiName).orElseGet(() -> addDOI(doiName));
        while (sdiNames.hasNext()) {
            String currenSdiName = sdiNames.next();
            final IDataParentAdapter parent = parentDoiOrSdi;
            parentDoiOrSdi = parentDoiOrSdi.findStructuredDataAdapterByName(currenSdiName).orElseGet(() -> parent.addSDOI(currenSdiName));
        }
        final IDataParentAdapter lastParent = parentDoiOrSdi;
        return lastParent.findDataAdapterByName(daiName).orElseGet(() -> lastParent.addDAI(daiName, setValImportOnCreate));
    }

    /**
     * Adds DO in LNode
     *
     * @param name DOI name
     * @return added <em>DOIAdapter</em> object
     */
    protected DOIAdapter addDOI(String name) {
        TDOI tdoi = new TDOI();
        tdoi.setName(name);
        currentElem.getDOI().add(tdoi);

        return new DOIAdapter(this, tdoi);
    }

    public String getLNodeName() {
        StringBuilder stringBuilder = new StringBuilder();

        if (TLLN0Enum.LLN_0.value().equals(getLNClass())) {
            stringBuilder.append(TLLN0Enum.LLN_0.value());
        } else {
            stringBuilder.append(getPrefix())
                    .append(getLNClass())
                    .append(getLNInst());
        }
        return stringBuilder.toString();
    }

    /**
     * Checks given reference matches with DataSet or ReportControl or DataTypeTemplate element for calling LNode
     * in SCL file
     *
     * @param objRef reference to compare with LNode datas
     * @return <em>Boolean</em> value of check result
     */
    public boolean matches(ObjectReference objRef) {
        String dataAttribute = objRef.getDataAttributes();
        DataTypeTemplateAdapter dttAdapter = getDataTypeTemplateAdapter();
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById(getLnType())
                .orElseThrow(
                        () -> new AssertionError(
                                String.format("Corrupted  SCD file: Reference to unknown LNodeType(%s)", getLnType())
                        )
                );
        DataAttributeRef filter = DataAttributeRef.builder()
                .lnInst(getLNInst())
                .lnClass(getLNClass())
                .lnType(currentElem.getLnType()).build();
        List<DataAttributeRef> dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs(filter);

        return matchesDataAttributes(dataAttribute) ||
                dataAttributeRefs.stream().anyMatch(dataAttributeRef -> dataAttributeRef.getDataAttributes().startsWith(dataAttribute));
    }

    /**
     * Checks if given attibrute corresponds to DataSet or ReportControl in LNode
     *
     * @param dataAttribute attribute to check
     * @return <em>Boolean</em> value of check result
     */
    protected boolean matchesDataAttributes(String dataAttribute) {
        return currentElem.getDataSet().stream().anyMatch(tDataSet -> tDataSet.getName().equals(dataAttribute)) ||
                currentElem.getReportControl().stream().anyMatch(rptCtl -> rptCtl.getName().equals(dataAttribute));
    }

    /**
     * Gets DAI values for specified DA in summaraized Data Type Template
     *
     * @param dataAttributeRef summaraized Data Type Template containing DA datas
     * @return map of Setting Group and it's VAL
     */
    public Map<Long, String> getDAIValues(DataAttributeRef dataAttributeRef) {
        DAITracker daiTracker = new DAITracker(this, dataAttributeRef.getDoName(), dataAttributeRef.getDaName());
        DAITracker.MatchResult matchResult = daiTracker.search();
        if (matchResult != DAITracker.MatchResult.FULL_MATCH) {
            return new HashMap<>();
        }
        List<TVal> tVals;
        IDataAdapter daiAdapter = daiTracker.getBdaiOrDaiAdapter();
        if (daiAdapter.getClass().equals(SDIAdapter.DAIAdapter.class)) {
            tVals = ((RootSDIAdapter.DAIAdapter) daiAdapter).getCurrentElem().getVal();
        } else if (daiAdapter.getClass().equals(RootSDIAdapter.DAIAdapter.class)) {
            tVals = ((RootSDIAdapter.DAIAdapter) daiAdapter).getCurrentElem().getVal();
        } else {
            tVals = ((DOIAdapter.DAIAdapter) daiAdapter).getCurrentElem().getVal();
        }

        Map<Long, String> res = new HashMap<>();
        tVals.forEach(tVal -> res.put(
                tVal.isSetSGroup() ? tVal.getSGroup() : 0L, tVal.getValue())
        );

        return res;
    }

    /**
     * Removes all ControlBlocks and DataSets from current LN
     */
    public void removeAllControlBlocksAndDatasets() {
        currentElem.unsetReportControl();
        currentElem.unsetLogControl();
        currentElem.unsetDataSet();
    }

    /**
     * Removes all ExtRefs source binding data's
     */
    public void removeAllExtRefSourceBindings() {
        getExtRefs().forEach(this::removeExtRefSourceBinding);
    }

    /**
     * Removes specified ExtRef Source binding data's
     *
     * @param tExtRef ExtRef Source for which binding data's should be removed
     */
    private void removeExtRefSourceBinding(final TExtRef tExtRef) {
        tExtRef.setSrcCBName(null);
        tExtRef.setSrcLDInst(null);
        tExtRef.setSrcPrefix(null);
        tExtRef.setSrcLNInst(null);
        tExtRef.unsetSrcLNClass();
    }

    /**
     * Adds DataSet in specified LNode in current IED/AccessPoint.
     * The AccessPoint must have DataSet creation capabilities
     *
     * @param dataSetName      Name of the dataSet
     * @param controlBlockEnum GOOSE, SMV or REPORT service type
     * @throws ScdException throws when IED does not have DataSet creation capabilities
     * @see LDeviceAdapter#hasDataSetCreationCapability
     */
    public DataSetAdapter createDataSetIfNotExists(String dataSetName, ControlBlockEnum controlBlockEnum) {
        return findDataSetByName(dataSetName)
                .orElseGet(() -> {
                    if (!getParentLDevice().hasDataSetCreationCapability(controlBlockEnum)) {
                        throw new ScdException("IED/AccessPoint does not have capability to create DataSet of type " + controlBlockEnum + " in " + getXPath());
                    }
                    TDataSet newDataSet = new TDataSet();
                    newDataSet.setName(dataSetName);
                    getCurrentElem().getDataSet().add(newDataSet);
                    return new DataSetAdapter(this, newDataSet);
                });
    }

    /**
     * Creates Control Block in specified LNode in current IED
     *
     * @param controlBlock Control Block data to add to this LN
     * @return created <em>ControlBlockAdapter</em> object
     * @throws ScdException throws when inconsistency between given ControlBlock and IED configuration
     */
    public ControlBlockAdapter addControlBlock(ControlBlock controlBlock) {

        controlBlock.validateCB();
        controlBlock.validateSecurityEnabledValue(getParentIed());

        if (!getParentLDevice().hasControlBlockCreationCapability(controlBlock.getControlBlockEnum())) {
            throw new ScdException("Cannot create ControlBlock %s %s because IED/AccessPoint does not have capability to create ControlBlock of type %s in %s"
                    .formatted(controlBlock.getClass().getSimpleName(), controlBlock.getName(), controlBlock.getControlBlockEnum(), getXPath()));
        }

        if (hasControlBlock(controlBlock.getName(), controlBlock.getControlBlockEnum())) {
            throw new ScdException("Cannot create ControlBlock %s %s because it already exists in %s"
                    .formatted(controlBlock.getClass().getSimpleName(), controlBlock.getName(), getXPath())
            );
        }

        DataSetService dataSetService = new DataSetService();
        if (dataSetService.findDataSet(currentElem, tDataSet -> tDataSet.getName().equals(controlBlock.getDataSetRef())).isEmpty()) {
            throw new ScdException("Cannot create ControlBlock %s %s because target DataSet %s does not exists in %s"
                    .formatted(controlBlock.getClass().getSimpleName(), controlBlock.getName(), controlBlock.getDataSetRef(), getXPath())
            );
        }

        TControl tControl = controlBlock.addToLN(this.currentElem);
        return new ControlBlockAdapter(this, tControl);
    }

    public Optional<ControlBlockAdapter> findControlBlock(String name, ControlBlockEnum controlBlockEnum) {
        return getTControlsByType(controlBlockEnum.getControlBlockClass()).stream()
                .filter(tReportControl -> name.equals(tReportControl.getName()))
                .findFirst()
                .map(tControl -> new ControlBlockAdapter(this, tControl));
    }

    public Stream<ControlBlockAdapter> streamControlBlocks(ControlBlockEnum controlBlockEnum) {
        return getTControlsByType(controlBlockEnum.getControlBlockClass()).stream()
                .map(tControl -> new ControlBlockAdapter(this, tControl));
    }

    /**
     * Create ControlBlock if there is no ControlBlock of the same type (controlBlockEnum) and with the same cbName in this LN/LN0.
     * When the controlBlock already exists, the id and datSet attributes are NOT updated with the given values.
     *
     * @param cbName           cbName of the controlBlock to look for. When not found, the cbName of the controlBlock to create.
     * @param id               When controlBlock not found, the id of the controlBlock to create
     * @param datSet           the datSet of the controlBlock to create
     * @param controlBlockEnum the type of ControlBlock to create
     * @return existing controlBlock if a controlBlock of the same type and with same cbName was found in this LN/LN0, otherwise the created ControlBlock.
     * The returned ControlBlock is always a child of this LN/LN0.
     */
    public ControlBlockAdapter createControlBlockIfNotExists(String cbName, String id, String datSet, ControlBlockEnum controlBlockEnum) {
        return findControlBlock(cbName, controlBlockEnum)
                .orElseGet(() -> addControlBlock(
                                switch (controlBlockEnum) {
                                    case GSE -> new GooseControlBlock(cbName, id, datSet);
                                    case SAMPLED_VALUE -> new SMVControlBlock(cbName, id, datSet);
                                    case REPORT -> new ReportControlBlock(cbName, id, datSet);
                                    default -> throw new IllegalArgumentException("Unsupported ControlBlock Type " + controlBlockEnum);
                                }
                        )
                );
    }

    /**
     * Generate a ControlBlock Id based on the current LN and the given ldName (ldName can be different from the parent LD.name)
     *
     * @param ldName LD name to use for generating the id
     * @param cbName name of the ControlBlock
     * @return "ldName/LnPrefixLnClassLnInst.cbName". Blank values are omitted (e.g "IEDNAME1LD1/LLN0.CBNAME1")
     */
    public String generateControlBlockId(String ldName, String cbName) {
        String s = getLNInst();
        String s1 = getPrefix();
        return StringUtils.trimToEmpty(ldName)
                + "/"
                + StringUtils.trimToEmpty(s1)
                + StringUtils.defaultString(getLNClass(), "")
                + StringUtils.trimToEmpty(s)
                + "."
                + StringUtils.trimToEmpty(cbName);
    }

    /**
     * Finds all FCDAs in DataSet of Control Block feeding ExtRef
     *
     * @param tExtRef Fed ExtRef
     * @return list of all FCDA in DataSet of Control Block
     */
    public List<TFCDA> getFCDAs(TExtRef tExtRef) {
        TControl tControl = getTControlsByType(ControlBlockEnum.from(tExtRef.getServiceType()).getControlBlockClass()).stream()
                .filter(tCtrl -> tExtRef.getSrcCBName() != null && tExtRef.getSrcCBName().equals(tCtrl.getName()))
                .findFirst().orElseThrow(() ->
                        new ScdException(String.format("Control Block %s not found in %s", tExtRef.getSrcCBName(), getXPath())));
        return getCurrentElem().getDataSet().stream()
                .filter(tDataSet -> tDataSet.getName().equals(tControl.getDatSet()))
                .map(TDataSet::getFCDA)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Get the value of "Mod.stVal" of the current LN
     *
     * @return Mod.stVal value if present, else empty Optional
     */
    public Optional<String> getDaiModStValValue() {
        return getDaiModStVal()
                .flatMap(DataAttributeRef::findFirstValue);
    }

    protected Optional<DataAttributeRef> getDaiModStVal() {
        DataAttributeRef daiModFilter = new DataAttributeRef(this, MOD_DO_TYPE_NAME, STVAL_DA_TYPE_NAME);
        return getDAI(daiModFilter, false).stream()
                .findFirst();
    }
}
