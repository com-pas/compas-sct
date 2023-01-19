// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlockTarget;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

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

}
