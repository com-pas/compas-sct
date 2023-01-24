// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TClientLN;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Objects;

import static org.lfenergy.compas.scl2007b4.model.TControlWithIEDName.IEDName;
import static org.lfenergy.compas.sct.commons.util.Utils.emptyIfBlank;
import static org.lfenergy.compas.sct.commons.util.Utils.nullIfBlank;

/**
 * Record that hold the data for TClientLN and TControlWithIEDName.IEDName which have the same fields.
 * It provides mapping methods "to" and "from" TClientLN and IEDName.
 * It also provides comparison methods with TClientLN and IEDName.
 *
 * @param apRef   apRef
 * @param iedName iedName : this field is "value" on TControlWithIEDName.IEDName
 * @param ldInst  ldInst
 * @param lnInst  lnInst
 * @param lnClass lnClass
 * @param prefix  prefix
 * @param desc    desc : this field is only available on TClientLN
 */
public record ControlBlockTarget(String apRef, String iedName, String ldInst, String lnInst, String lnClass, String prefix, String desc) {

    /**
     * "desc" field is only available on TClientLN, not on TControlWithIEDName.IEDName,
     * so we provide a constructor without "desc" parameter
     */
    public ControlBlockTarget(String apRef, String iedName, String ldInst, String lnInst, String lnClass, String prefix) {
        this(apRef, iedName, ldInst, lnInst, lnClass, prefix, null);
    }

    /**
     * Map instance to TClientLN
     * @return new instance of TClientLN
     */
    public TClientLN toTClientLn() {
        TClientLN newTClientLn = new TClientLN();
        newTClientLn.setApRef(apRef);
        newTClientLn.setIedName(iedName);
        newTClientLn.setLdInst(ldInst);
        if (StringUtils.isNotBlank(lnClass)) {
            newTClientLn.getLnClass().add(lnClass);
        }
        // LnInst is required on TClientLN, so it cannot be null, but it can be empty
        newTClientLn.setLnInst(emptyIfBlank(lnInst));
        newTClientLn.setPrefix(nullIfBlank(prefix));
        newTClientLn.setDesc(desc);
        return newTClientLn;
    }

    /**
     * Map instance to TControlWithIEDName.IEDName
     * @return new instance of TControlWithIEDName.IEDName
     */
    public IEDName toIedName() {
        IEDName newIedName = new IEDName();
        newIedName.setApRef(apRef);
        newIedName.setValue(iedName);
        newIedName.setLdInst(ldInst);
        if (StringUtils.isNotBlank(lnClass)) {
            newIedName.getLnClass().add(lnClass);
        }
        // LnInst is optional on IEDName, so it can be null, but cannot be empty
        newIedName.setLnInst(nullIfBlank(lnInst));
        newIedName.setPrefix(nullIfBlank(prefix));
        return newIedName;
    }

    /**
     * Map TClientLN to ControlBlockTarget
     * @param tClientLN tClientLN to map
     * @return new instance of ControlBlockTarget
     */
    public static ControlBlockTarget from(TClientLN tClientLN) {
        return new ControlBlockTarget(
            tClientLN.getApRef(),
            tClientLN.getIedName(),
            tClientLN.getLdInst(),
            tClientLN.getLnInst(),
            tClientLN.isSetLnClass() && StringUtils.isNotBlank(tClientLN.getLnClass().get(0)) ? tClientLN.getLnClass().get(0) : null,
            tClientLN.getPrefix(),
            tClientLN.getDesc());
    }

    /**
     * Map TControlWithIEDName.IEDName to ControlBlockTarget
     * @param iedName iedName to map
     * @return new instance of ControlBlockTarget
     */
    public static ControlBlockTarget from(IEDName iedName) {
        return new ControlBlockTarget(
            iedName.getApRef(),
            iedName.getValue(),
            iedName.getLdInst(),
            iedName.getLnInst(),
            iedName.isSetLnClass() && StringUtils.isNotBlank(iedName.getLnClass().get(0)) ? iedName.getLnClass().get(0) : null,
            iedName.getPrefix());
    }

    /**
     * Compare instance to a TControlWithIEDName.IEDName.
     * desc value is ignored since TControlWithIEDName.IEDName does not have a desc attribute.
     * Blank lnInst are considered equals.
     * Blank prefix are considered equals.
     * @param iedName iedName to compare
     * @return true if all attributes of this instance (except desc) are equal to the attributes of TControlWithIEDName.IEDName
     * (lnInst and prefix are compared using Utils#equalsOrBothBlank).
     * @see Utils#equalsOrBothBlank(String, String)
     */
    public boolean equalsIedName(IEDName iedName) {
        return Objects.equals(iedName.getApRef(), apRef)
            && Objects.equals(iedName.getValue(), this.iedName)
            && Objects.equals(iedName.getLdInst(), ldInst)
            && Utils.equalsOrBothBlank(iedName.getLnInst(), lnInst)
            && Utils.lnClassEquals(iedName.getLnClass(), lnClass)
            && Utils.equalsOrBothBlank(iedName.getPrefix(), prefix);
    }

    /**
     * Compare instance to a TClientLN.
     * Blank lnInst are considered equals.
     * Blank prefix are considered equals.
     * Blank desc are considered equals.
     * @param tClientLn tClientLn to compare
     * @return true if all attributes of this instance are equal to the attributes of TControlWithIEDName.IEDName
     * (lnInst, prefix and desc are compared using Utils#equalsOrBothBlank).
     * @see Utils#equalsOrBothBlank(String, String)
     */
    public boolean equalsTClientLn(TClientLN tClientLn) {
        return Objects.equals(tClientLn.getApRef(), apRef)
            && Objects.equals(tClientLn.getIedName(), iedName)
            && Objects.equals(tClientLn.getLdInst(), ldInst)
            && Utils.equalsOrBothBlank(tClientLn.getLnInst(), lnInst)
            && Utils.lnClassEquals(tClientLn.getLnClass(), lnClass)
            && Utils.equalsOrBothBlank(tClientLn.getPrefix(), prefix)
            && Utils.equalsOrBothBlank(tClientLn.getDesc(), desc);
    }
}
