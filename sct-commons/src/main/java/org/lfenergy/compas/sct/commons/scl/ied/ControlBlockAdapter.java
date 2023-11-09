// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlockTarget;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newDurationInMilliSec;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newP;
import static org.lfenergy.compas.sct.commons.util.Utils.xpathAttributeFilter;

/**
 * A representation of the model object
 * <em><b>{@link ControlBlockAdapter ControlBlockAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link ControlBlockAdapter#addTargetIfNotExists <em>Add a ClientLN to ReportControl or IEDName to GSEControl/SampleValueControl, if it
 *      does not already exist.
 *      in this
 *      DataSet</em></li>
 *    </ul>
 * </ol>
 * <br/>
 *  <pre>
 *      <b>ObjectReference</b>: ControlBlock (GSEControl, SampleValueControl or ReportControl)
 *  </pre>
 *
 * @see TGSEControl
 * @see TSampledValueControl
 * @see TReportControl
 * @see AbstractLNAdapter
 */
public class ControlBlockAdapter extends SclElementAdapter<AbstractLNAdapter<? extends TAnyLN>, TControl> {

    private static final long RPT_ENABLED_MAX_DEFAULT = 1L;
    private static final String APPID_P_TYPE = "APPID";
    private static final String MAC_ADDRESS_P_TYPE = "MAC-Address";
    private static final String VLAN_ID_P_TYPE = "VLAN-ID";
    private static final String VLAN_PRIORITY_P_TYPE = "VLAN-PRIORITY";
    private static final int APPID_LENGTH = 4;
    private static final int VLAN_ID_LENGTH = 3;

    public ControlBlockAdapter(AbstractLNAdapter<? extends TAnyLN> parentAdapter, TControl tControl) {
        super(parentAdapter, tControl);
    }

    public ControlBlockEnum getControlBlockEnum() {
        return ControlBlockEnum.from(currentElem.getClass());
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.hasControlBlock(currentElem.getName(), ControlBlockEnum.from(currentElem.getClass()));
    }

    /**
     * Returns local XPath
     *
     * @return XPath for current element (not including parent XPath)
     */
    @Override
    protected String elementXPath() {
        String tag = switch (getControlBlockEnum()) {
            case GSE -> "GSEControl";
            case SAMPLED_VALUE -> "SampledValueControl";
            case REPORT -> "ReportControl";
            case LOG -> "LogControl";
        };
        return String.format("%s[%s]", tag, xpathAttributeFilter("name", currentElem.getName()));
    }

    /**
     * Get the name of this ControlBlock
     * @return name of ControlBlock
     */
    public String getName(){
        return currentElem.getName();
    }

    /**
     * Add a ClientLN to ReportControl or IEDName to GSEControl/SampleValueControl, if it does not already exist.
     * @param targetLn target LN (where the target ExtRef is)
     */
    public void addTargetIfNotExists(AbstractLNAdapter<?> targetLn) {
        ControlBlockTarget controlBlockTarget = new ControlBlockTarget(
            targetLn.getParentLDevice().getAccessPoint().getName(),
            targetLn.getParentIed().getName(),
            targetLn.getParentLDevice().getInst(),
            targetLn.getLNInst(),
            targetLn.getLNClass(),
            targetLn.getPrefix());
        if (currentElem instanceof TControlWithIEDName tControlWithIEDName) {
            if (tControlWithIEDName.getIEDName().stream().noneMatch(controlBlockTarget::equalsIedName)) {
                tControlWithIEDName.getIEDName().add(controlBlockTarget.toIedName());
            }
        } else if (currentElem instanceof TReportControl tReportControl) {
            if (!tReportControl.isSetRptEnabled()) {
                tReportControl.setRptEnabled(new TRptEnabled());
                tReportControl.getRptEnabled().setMax(RPT_ENABLED_MAX_DEFAULT);
            }
            if (tReportControl.getRptEnabled().getClientLN().stream().noneMatch(controlBlockTarget::equalsTClientLn)) {
                tReportControl.getRptEnabled().getClientLN().add(controlBlockTarget.toTClientLn());
            }
        }
    }

    /**
     * Configure the Communication section for this ControlBlock
     *  - Communication/SubNetwork/ConnectedAP/GSE for GSEControl block
     *  - Communication/SubNetwork/ConnectedAP/SMV for SampledValueControl block
     * @param appId value for P type APPID
     * @param macAddress value for P type MAC-Address
     * @param vlanId value for P type VLAN-ID
     * @param vlanPriority value for P type VLAN-PRIORITY
     * @param minTime MinTime Element
     * @param maxTime MaxTime Element
     * @return An empty Optional if network have been configured, else a SclReportItem.
     */
    public Optional<SclReportItem> configureNetwork(long appId, String macAddress, Integer vlanId, Byte vlanPriority, TDurationInMilliSec minTime,
                                                    TDurationInMilliSec maxTime) {
        String accessPointName = getParentLDeviceAdapter().getAccessPoint().getName();

        Optional<ConnectedAPAdapter> optConApAdapter = getSclRootAdapter().findConnectedApAdapter(getParentIedAdapter().getName(), accessPointName);
        if (optConApAdapter.isEmpty()) {
            return Optional.of(buildFatalReportItem("Cannot configure network for ControlBlock because no ConnectAP found for parent AccessPoint"));
        }
        ConnectedAPAdapter connectedAPAdapter = optConApAdapter.get();
        List<TP> listOfPs = new ArrayList<>();
        listOfPs.add(newP(APPID_P_TYPE, Utils.toHex(appId, APPID_LENGTH)));
        listOfPs.add(newP(MAC_ADDRESS_P_TYPE, macAddress));
        if (vlanId != null) {
            listOfPs.add(newP(VLAN_ID_P_TYPE, Utils.toHex(vlanId, VLAN_ID_LENGTH)));
            if (vlanPriority != null) {
                listOfPs.add(newP(VLAN_PRIORITY_P_TYPE, String.valueOf(vlanPriority)));
            }
        }
        switch (getControlBlockEnum()) {
            case GSE -> connectedAPAdapter.updateGseOrCreateIfNotExists(getParentLDeviceAdapter().getInst(), currentElem.getName(), listOfPs, newDurationInMilliSec(minTime), newDurationInMilliSec(maxTime));
            case SAMPLED_VALUE -> connectedAPAdapter.updateSmvOrCreateIfNotExists(getParentLDeviceAdapter().getInst(), currentElem.getName(), listOfPs);
            default -> {
                return Optional.of(buildFatalReportItem("configureNetwork not yet implemented for %s ControlBlocks".formatted(getControlBlockEnum())));
            }
        }
        return Optional.empty();
    }

    /**
     * Get parent LDevice
     * @return ControlBlock's parent lDeviceAdapter
     */
    private LDeviceAdapter getParentLDeviceAdapter() {
        return getParentAdapter().getParentAdapter();
    }

    /**
     * Get parent IED
     * @return ControlBlock's parent IEDAdapter
     */
    public IEDAdapter getParentIedAdapter() {
        return getParentAdapter().getParentIed();
    }

    /**
     * Get SCL Root
     * @return sclRootAdapter
     */
    private SclRootAdapter getSclRootAdapter() {
        return getParentIedAdapter().getParentAdapter();
    }
}
