// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ldevice;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TLDevice LDevice}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link LDeviceAdapter#getLN0Adapter <em>Returns the value of the <b>LN0Adapter </b>containment reference list</em>}</li>
 *      <li>{@link LDeviceAdapter#getLNAdapter <em>Returns the value of the <b>LNAdapter </b>reference object By  LNClass, inst and prefix</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LDeviceAdapter#getInst <em>Returns the value of the <b>inst </b>attribute</em>}</li>
 *      <li>{@link LDeviceAdapter#getLdName <em>Returns the value of the <b>ldName </b>attribute</em>}</li>
 *      <li>{@link LDeviceAdapter#getExtRefInfo em>Returns the value of the <b>ExtRefInfo </b>containment reference</em>}</li>
 *      <li>{@link LDeviceAdapter#getExtRefBinders <em>Returns the value of the <b>ExtRefBindingInfo </b>containment reference list By <b>ExtRefSignalInfo</b></em>}</li>
 *      <li>{@link LDeviceAdapter#getDAI <em>Returns the value of the <b>DataAttributeRef </b>containment reference By filter</em>}</li>
 *      <li>{@link LDeviceAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see LNAdapter
 * @see LN0Adapter
 * @see org.lfenergy.compas.scl2007b4.model.TLDevice
 * @see org.lfenergy.compas.scl2007b4.model.TDOI
 * @see org.lfenergy.compas.scl2007b4.model.TDAI
 * @see <a href="https://github.com/com-pas/compas-sct/issues/32" target="_blank">Issue !32</a>
 */
@Slf4j
public class LDeviceAdapter extends SclElementAdapter<IEDAdapter, TLDevice> {

    private static final String DA_SETSRCREF = "setSrcRef";

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    public LDeviceAdapter(IEDAdapter parentAdapter, TLDevice currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getAccessPoint()
                .stream()
                .map(TAccessPoint::getServer)
                .filter(Objects::nonNull)
                .filter(TServer::isSetLDevice)
                .map(TServer::getLDevice)
                .flatMap(Collection::stream)
                .anyMatch(tlDevice -> currentElem.getInst().equals(tlDevice.getInst()));
    }

    public TAccessPoint getAccessPoint() {
        return parentAdapter.getCurrentElem().getAccessPoint()
                .stream()
                .filter(accessPoint ->
                        Optional.ofNullable(accessPoint.getServer())
                                .filter(TServer::isSetLDevice)
                                .map(TServer::getLDevice)
                                .stream()
                                .flatMap(List::stream)
                                .map(TLDevice::getInst)
                                .anyMatch(inst -> inst.equals(currentElem.getInst()))
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("LDevice.inst='%s' not found in parent IED.name='%s'", currentElem.getInst(),
                        parentAdapter.getName())));
    }

    @Override
    protected String elementXPath() {
        return String.format("LDevice[%s]", Utils.xpathAttributeFilter("inst", currentElem.isSetInst() ? currentElem.getInst() : null));
    }

    @Override
    public String getXPath() {
        if (parentAdapter != null) {
            return parentAdapter.getXPath() + "/AccessPoint/Server/" + elementXPath();
        } else {
            return super.getXPath();
        }
    }

    /**
     * Updates LDevice name by combining IED name and LDevice ldInst value
     */
    public void updateLDName() {
        String newLdName = parentAdapter.getCurrentElem().getName() + currentElem.getInst();
        // renaming ldName; Carefull because the maximum ldevice name length is 64 based on xsd
        currentElem.setLdName(newLdName);
    }

    /**
     * Gets current LDevice Inst parameter value
     *
     * @return Inst parameter value
     */
    public String getInst() {
        return currentElem.getInst();
    }

    /**
     * Gets current LDevice name
     *
     * @return LDevice name
     */
    public String getLdName() {
        return currentElem.getLdName();
    }

    /**
     * Gets current LDevice LNode LN0
     *
     * @return <em>LN0Adapter</em>
     */
    public LN0Adapter getLN0Adapter() {
        return new LN0Adapter(this, currentElem.getLN0());
    }

    /**
     * Checks if LDevice has an LN0 node
     *
     * @return true if lDevice has a LN0 node, false otherwise
     */
    public boolean hasLN0() {
        return currentElem.isSetLN0();
    }

    /**
     * Gets current LDevice LNodes (except LN0)
     *
     * @return list of <em>LNAdapter</em> object
     */
    public List<LNAdapter> getLNAdapters() {
        return currentElem.getLN()
                .stream()
                .map(tln -> new LNAdapter(this, tln))
                .toList();
    }

    /**
     * Gets specific LNode from current LDevice
     *
     * @param lnClass LNode lnClass value
     * @param lnInst  LNode lnInst value
     * @param prefix  LNode prefix value
     * @return <em>LNAdapter</em> object
     * @throws ScdException thros when specified LNode not found in current IED
     */
    public LNAdapter getLNAdapter(String lnClass, String lnInst, String prefix) throws ScdException {
        return findLnAdapter(lnClass, lnInst, prefix)
                .orElseThrow(
                        () -> new ScdException(
                                String.format(
                                        "LDevice [%s] has no LN [%s,%s,%s]", currentElem.getInst(), lnClass, lnInst, prefix)
                        )
                );

    }

    /**
     * Find a specific LN from current LDevice
     *
     * @param lnClass LNode lnClass value
     * @param lnInst  LNode lnInst value
     * @param prefix  LNode prefix value
     * @return <em>LNAdapter</em> object
     * @throws ScdException thros when specified LNode not found in current IED
     */
    public Optional<LNAdapter> findLnAdapter(String lnClass, String lnInst, String prefix) {
        if (!currentElem.isSetLN()) {
            return Optional.empty();
        }
        return currentElem.getLN()
                .stream()
                .filter(tln -> Utils.lnClassEquals(tln.getLnClass(), lnClass)
                        && tln.getInst().equals(lnInst)
                        && Utils.equalsOrBothBlank(prefix, tln.getPrefix()))
                .map(tln -> new LNAdapter(this, tln))
                .findFirst();
    }

    /**
     * Checks all possible ExtRef in current LDevice which could be bound to given ExtRef as parameter
     *
     * @param signalInfo ExtRef to bind data
     * @return list of <em>ExtRefBindingInfo</em> object (containing binding data for each LDNode in current LDevice
     * related to given ExtRef)
     */
    public List<ExtRefBindingInfo> getExtRefBinders(ExtRefSignalInfo signalInfo) {
        DataTypeTemplateAdapter dttAdapter = parentAdapter.getParentAdapter().getDataTypeTemplateAdapter();
        return getLNAdaptersIncludingLN0().stream()
                .filter(abstractLNAdapter -> StringUtils.isBlank(signalInfo.getPLN()) || abstractLNAdapter.getLNClass().equals(signalInfo.getPLN()))
                .map(lnAdapter -> {
                    String lnType = lnAdapter.getLnType();
                    ExtRefBindingInfo extRefBindingInfo = dttAdapter.getBinderDataAttribute(lnType, signalInfo);
                    extRefBindingInfo.setIedName(parentAdapter.getName());
                    extRefBindingInfo.setLdInst(currentElem.getInst());
                    extRefBindingInfo.setLnClass(lnAdapter.getLNClass());
                    extRefBindingInfo.setLnInst(lnAdapter.getLNInst());
                    extRefBindingInfo.setPrefix(lnAdapter.getPrefix());
                    return extRefBindingInfo;
                })
                .toList();
    }

    /**
     * Gets all ExtRef of all LNodes of current LDevice
     *
     * @return list of <em>ExtRefInfo</em> object (containing binding data for each LDNode in current LDevice)
     */
    public List<ExtRefInfo> getExtRefInfo() {
        List<ExtRefInfo> extRefInfos = new ArrayList<>();
        List<AbstractLNAdapter<?>> lnAdapters = getLNAdaptersIncludingLN0();
        LogicalNodeOptions logicalNodeOptions = new LogicalNodeOptions();
        logicalNodeOptions.setWithExtRef(true);
        for (AbstractLNAdapter<?> lnAdapter : lnAdapters) {
            LNodeDTO lNodeDTO = LNodeDTO.from(lnAdapter, logicalNodeOptions);
            extRefInfos.addAll(lNodeDTO.getExtRefs());
        }
        return extRefInfos;
    }

    /**
     * Gets a list of summarized DataTypeTemplate for DataAttribute DAIs (updatableOnly or not)
     *
     * @param dataAttributeRef          Data Attribute Reference (used as filter)
     * @param updatableOnly true to retrieve only updatableOnly DAIs, false to retrieve all DAIs
     * @return Set of <em>DataAttributeRef</em> (updatableOnly or not)
     * @throws ScdException SCD illegal arguments exception
     */
    public Set<DataAttributeRef> getDAI(DataAttributeRef dataAttributeRef, boolean updatableOnly) throws ScdException {
        List<? extends AbstractLNAdapter<?>> lnAdapters;
        if (StringUtils.isBlank(dataAttributeRef.getLnClass())) {
            lnAdapters = getLNAdaptersIncludingLN0();
        } else if (dataAttributeRef.getLnClass().equals(TLLN0Enum.LLN_0.value())) {
            lnAdapters = hasLN0() ? Collections.singletonList(getLN0Adapter()) : Collections.emptyList();
        } else {
            lnAdapters = findLnAdapter(dataAttributeRef.getLnClass(), dataAttributeRef.getLnInst(), dataAttributeRef.getPrefix()).stream().toList();
        }

        Set<DataAttributeRef> dataAttributeRefSet = new HashSet<>();
        for (AbstractLNAdapter<?> lnAdapter : lnAdapters) {
            DataAttributeRef filter = DataAttributeRef.copyFrom(dataAttributeRef);
            filter.setLnClass(lnAdapter.getLNClass());
            filter.setLnInst(lnAdapter.getLNInst());
            filter.setPrefix(lnAdapter.getPrefix());
            filter.setLnType(lnAdapter.getLnType());
            dataAttributeRefSet.addAll(lnAdapter.getDAI(filter, updatableOnly));
        }
        return dataAttributeRefSet;

    }

    public Optional<String> getLDeviceStatus() {
        if (!hasLN0()) {
            return Optional.empty();
        }
        return getLN0Adapter().getDaiModStValValue();
    }

    /**
     * Gets all LN of LDevice including LN0
     *
     * @return list of all LN of LDevice
     */
    public List<AbstractLNAdapter<?>> getLNAdaptersIncludingLN0() {
        List<AbstractLNAdapter<?>> aLNAdapters = new ArrayList<>();
        aLNAdapters.add(getLN0Adapter());
        aLNAdapters.addAll(getLNAdapters());
        return aLNAdapters;
    }

    public List<SclReportItem> createDataSetAndControlBlocks(List<org.lfenergy.compas.sct.commons.model.da_comm.TFCDA> allowedFcdas) {
        LN0Adapter ln0Adapter = getLN0Adapter();
        if (!ln0Adapter.hasInputs()) {
            return Collections.emptyList();
        }
        return ln0Adapter.getInputsAdapter()
                .updateAllSourceDataSetsAndControlBlocks(allowedFcdas);
    }

    public Set<DataAttributeRef> findSourceDA(TExtRef extRef) {
        String extRefLnClass = extRef.getLnClass().stream().findFirst().orElse("");
        DataAttributeRef filter = DataAttributeRef.builder()
                .lnClass(extRefLnClass)
                .prefix(extRef.getPrefix())
                .lnInst(extRef.getLnInst())
                .doName(new DoTypeName(extRef.getDoName()))
                .daName(new DaTypeName(extRef.getDaName()))
                .build();
        return getDAI(filter, false);
    }

    /**
     * Checks if parent AccessPoint has DataSet creation capability
     *
     * @param controlBlockEnum the type of DataSet we want to check for creation capability.
     * @return true if parent AccessPoint has the capability, false otherwise
     */
    public boolean hasDataSetCreationCapability(ControlBlockEnum controlBlockEnum) {
        Objects.requireNonNull(controlBlockEnum);
        TAccessPoint accessPoint = getAccessPoint();
        if (!accessPoint.isSetServices()) {
            return false;
        }
        TServices services = accessPoint.getServices();
        return switch (controlBlockEnum) {
            case REPORT -> services.isSetReportSettings() && hasDatSetConfOrDyn(services.getReportSettings());
            case GSE -> services.isSetGSESettings() && hasDatSetConfOrDyn(services.getGSESettings());
            case SAMPLED_VALUE -> services.isSetSMVSettings() && hasDatSetConfOrDyn(services.getSMVSettings());
            case LOG -> services.isSetLogSettings() && hasDatSetConfOrDyn(services.getLogSettings());
        };
    }

    private boolean hasDatSetConfOrDyn(TServiceSettings tServiceSettings) {
        return tServiceSettings.isSetDatSet()
                && (TServiceSettingsEnum.CONF.equals(tServiceSettings.getDatSet()) || TServiceSettingsEnum.DYN.equals(tServiceSettings.getDatSet()));
    }

    /**
     * Checks if parent AccessPoint has ControlBlock creation capability
     *
     * @param controlBlockEnum the type of ControlBlock we want to check for creation capability.
     * @return true if parent AccessPoint has the capability, false otherwise
     */
    public boolean hasControlBlockCreationCapability(ControlBlockEnum controlBlockEnum) {
        Objects.requireNonNull(controlBlockEnum);
        TAccessPoint accessPoint = getAccessPoint();
        if (!accessPoint.isSetServices()) {
            return false;
        }
        TServices services = accessPoint.getServices();
        return switch (controlBlockEnum) {
            case REPORT -> services.isSetReportSettings() && hasCBNameConf(services.getReportSettings());
            case GSE -> services.isSetGSESettings() && hasCBNameConf(services.getGSESettings());
            case SAMPLED_VALUE -> services.isSetSMVSettings() && hasCBNameConf(services.getSMVSettings());
            case LOG -> services.isSetLogSettings() && hasCBNameConf(services.getLogSettings());
        };
    }

    private boolean hasCBNameConf(TServiceSettings tServiceSettings) {
        return tServiceSettings.isSetCbName()
                && (TServiceSettingsNoDynEnum.CONF.equals(tServiceSettings.getCbName()));
    }

}
