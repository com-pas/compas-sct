// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TIED IED}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link IEDAdapter#streamLDeviceAdapters LDeviceAdapters <em>Returns the value of the <b>LDeviceAdapter </b>containment reference list</em>}</li>
 *       <li>{@link IEDAdapter#getLDeviceAdapterByLdInst <em>Returns the value of the <b>LDeviceAdapter </b>reference object By LDevice Inst</em>}</li>
 *       <li>{@link IEDAdapter#findLDeviceAdapterByLdInst <em>Returns the value of the <b>LDeviceAdapter </b>reference object By LDevice Inst</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link IEDAdapter#getName <em>Returns the value of the <b>name </b>attribute</em>}</li>
 *      <li>{@link IEDAdapter#getServices Returns the value of the <b>Service </b>object</em>}</li>
 *      <li>{@link IEDAdapter#getPrivateHeader <em>Returns the value of the <b>TPrivate </b>containment reference list</em>}</li>
 *      <li>{@link IEDAdapter#getExtRefBinders <em>Returns the value of the <b>ExtRefBindingInfo </b>containment reference list By <b>ExtRefSignalInfo</b></em>}</li>
 *      <li>{@link IEDAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link IEDAdapter#updateLDeviceNodesType <em>Update <b>Type </b> describing the value of the children <b>TAnyLN</b></em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *      <li>{@link IEDAdapter#isSettingConfig <em>Check whether IIED contain confSG</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.AbstractDAIAdapter
 * @see org.lfenergy.compas.scl2007b4.model.TSettingGroups
 * @see <a href="https://github.com/com-pas/compas-sct/issues/3" target="_blank">Issue !3 (Add new IEDs)</a>
 */
public class IEDAdapter extends SclElementAdapter<SclRootAdapter, TIED> {

    private static final String MESSAGE_LDEVICE_INST_NOT_FOUND = "LDevice.inst '%s' not found in IED '%s'";

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     */
    public IEDAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    public IEDAdapter(SclRootAdapter parentAdapter, TIED currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param iedName       IED name reference
     */
    public IEDAdapter(SclRootAdapter parentAdapter, String iedName) throws ScdException {
        super(parentAdapter);
        TIED ied = parentAdapter.getCurrentElem().getIED()
            .stream()
            .filter(tied -> tied.getName().equals(iedName))
            .findFirst()
            .orElseThrow(() -> new ScdException("Unknown IED name :" + iedName));
        setCurrentElem(ied);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getIED().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("IED[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    /**
     * Sets IED name in current IED
     *
     * @param iedName new name to set
     */
    public void setIEDName(String iedName) {
        currentElem.setName(iedName);
    }

    /**
     * Gets All LDevice from current IED as LDeviceAdapter
     *
     * @return Stream of <em>LDeviceAdapter</em>  object
     */
    public Stream<LDeviceAdapter> streamLDeviceAdapters() {
        return streamLDevices()
                .map(tlDevice -> new LDeviceAdapter(this, tlDevice));
    }

    /**
     * Gets All LDevice from current IED
     *
     * @return Stream of <em>TLDevice</em>  object
     */
    private Stream<TLDevice> streamLDevices() {
        if (!currentElem.isSetAccessPoint()) {
            return Stream.empty();
        }
        return currentElem.getAccessPoint().stream()
                .map(TAccessPoint::getServer)
                .filter(Objects::nonNull)
                .filter(TServer::isSetLDevice)
                .map(TServer::getLDevice)
                .flatMap(List::stream);
    }

    /**
     * Gets LDevice from current IED by ldInst parameter
     *
     * @param ldInst ldInst value of LDevice to get
     * @return <em>LDeviceAdapter</em> object
     * @throws ScdException when LDevice is not found
     */
    public LDeviceAdapter getLDeviceAdapterByLdInst(String ldInst) throws ScdException {
        return findLDeviceAdapterByLdInst(ldInst)
                .orElseThrow(() -> new ScdException(String.format(MESSAGE_LDEVICE_INST_NOT_FOUND, ldInst, getName())));
    }

    /**
     * Gets LDevice from current IED by ldInst parameter
     *
     * @param ldInst ldInst value of LDevice to get
     * @return optional of <em>LDeviceAdapter</em>  object
     */
    public Optional<LDeviceAdapter> findLDeviceAdapterByLdInst(String ldInst) {
        if (StringUtils.isBlank(ldInst)) {
            return Optional.empty();
        }
        return streamLDevices()
                .filter(tlDevice -> ldInst.equals(tlDevice.getInst()))
                .findFirst()
                .map(tlDevice -> new LDeviceAdapter(this, tlDevice));
    }

    /**
     * Updates all LNode type value specified in <em>pairOldNewId</em> as key (oldID), by corresponding value (newID)
     * in all LDevice of the current IED.
     * Update LDevice name by combining IED name and LDevice ldInst value
     *
     * @param pairOldNewId map of old ID and new ID. Old ID to find in LNode Type and replace it with New ID
     * @throws ScdException throws when renaming LDevice and new name has more than 33 caracteres
     */
    public void updateLDeviceNodesType(Map<String, String> pairOldNewId) throws ScdException {
        // renaming ldName
        streamLDeviceAdapters().forEach(lDeviceAdapter -> {
            lDeviceAdapter.updateLDName();
            String lnType = lDeviceAdapter.getCurrentElem().getLN0().getLnType();
            if (pairOldNewId.containsKey(lnType)) {
                lDeviceAdapter.getCurrentElem().getLN0().setLnType(pairOldNewId.get(lnType));
            }
            lDeviceAdapter.getCurrentElem()
                    .getLN()
                    .forEach(tln -> {
                        if (pairOldNewId.containsKey(tln.getLnType())) {
                            tln.setLnType(pairOldNewId.get(tln.getLnType()));
                        }
                    });
        });
    }

    /**
     * Gets Services of current IED
     *
     * @return <em>TServices</em> object
     */
    public TServices getServices() {
        return currentElem.getServices();
    }

    /**
     * Gets name of current IED
     *
     * @return string name
     */
    public String getName() {
        return currentElem.getName();
    }

    /**
     * Checks if given ObjectReference matches with one of current IED LNode
     * (ie having common reference with DataSet, ReportControl or DataTypeTemplate)
     *
     * @param objRef reference to compare with LNodes data's
     * @return <em>Boolean</em> value of check result
     */
    public boolean matches(ObjectReference objRef) {
        if (StringUtils.isBlank(getName())
                || !objRef.getLdName().startsWith(getName())) {
            return false;
        }
        String ldInst = objRef.getLdName().substring(getName().length());
        Optional<LDeviceAdapter> opLD = findLDeviceAdapterByLdInst(ldInst);
        if (opLD.isEmpty()) {
            return false;
        }
        LDeviceAdapter lDeviceAdapter = opLD.get();
        if (TLLN0Enum.LLN_0.value().equals(objRef.getLNodeName())) {
            return lDeviceAdapter.getLN0Adapter().matches(objRef);
        }

        return lDeviceAdapter.getLNAdapters()
                .stream()
                .anyMatch(lnAdapter -> objRef.getLNodeName().equals(lnAdapter.getLNodeName())
                        && lnAdapter.matches(objRef));
    }

    /**
     * Checks existence of Access Point in curent IED by name
     *
     * @param apName AccessPoint name to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean findAccessPointByName(String apName) {
        return currentElem.getAccessPoint()
                .stream()
                .anyMatch(tAccessPoint -> tAccessPoint.getName().equals(apName));
    }

    /**
     * Checks all possible ExtRef in current IED which could be bound to given ExtRef as parameter
     *
     * @param signalInfo ExtRef to bind data
     * @return list of <em>ExtRefBindingInfo</em> object (containing binding data for each LDevice in current IED)
     * @throws ScdException throws when ExtRef contains inconsistency data
     */
    public List<ExtRefBindingInfo> getExtRefBinders(@NonNull ExtRefSignalInfo signalInfo) throws ScdException {
        if (!signalInfo.isValid()) {
            throw new ScdException("Invalid ExtRef signal (pDO,pDA or intAddr))");
        }
        return streamLDeviceAdapters()
                .map(lDeviceAdapter -> lDeviceAdapter.getExtRefBinders(signalInfo)).flatMap(List::stream)
                .toList();
    }

    /**
     * Checks for a given LDevice in current IED if Setting Group is well setted
     *
     * @param ldInst ldInst for LDevice for which Setting Group is checked
     * @return <em>Boolean</em> value of check result
     */
    public boolean isSettingConfig(String ldInst) {
        TAccessPoint accessPoint = currentElem.getAccessPoint().stream()
                .filter(tAccessPoint ->
                        (tAccessPoint.getServer() != null) &&
                                tAccessPoint.getServer().getLDevice().stream()
                                        .anyMatch(tlDevice -> tlDevice.getInst().equals(ldInst))

                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                String.format("LD (%s) is unknown in %s", ldInst, getName())
                        )
                );

        TServices srv = accessPoint.getServices();
        return srv != null && srv.getSettingGroups() != null && srv.getSettingGroups().getConfSG() != null;
    }

    /**
     * Gets IED Private corresponding to specified type in parameter
     *
     * @param privateType Private type to get
     * @return optional of <em>TPrivate</em> object
     */
    public Optional<TPrivate> getPrivateHeader(String privateType) {
        return currentElem.getPrivate()
                .stream()
                .filter(tPrivate -> tPrivate.getType().equals(privateType))
                .findFirst();
    }

    /**
     * Extract private compas:Bay
     *
     * @return value of private compas:Bay if present, empty Optional otherwise
     */
    public Optional<TCompasBay> getPrivateCompasBay() {
        return PrivateService.extractCompasPrivate(currentElem, TCompasBay.class);
    }
}
