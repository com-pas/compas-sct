// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ln;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.Objects;

/**
 * Key that define a LN or LN0 inside a LDevice.
 * The main purpose of this record is to compare using only the  LnKey.equals method the 3 attributes (lnInst, lnClass, prefix) on elements that have a reference to a LN
 * (LN, LN0, LNode, ExtRef, FCDA, ClientLN, IEDName).
 * Second purpose is to pass only 1 parameter instead of three to methods that require lnInst, lnClass, prefix.
 * The key is unique for a LN or LN0 inside a LDevice.
 */
public record LnKey(String inst, String lnClass, String prefix) {

    private static final String LN0_LNCLASS = TLLN0Enum.LLN_0.value();

    /**
     * Key of LN0. All LN0 have the same lnClass, lnInst and prefix
     */
    public static final LnKey LN0_KEY = new LnKey("", LN0_LNCLASS, "");

    /**
     * Constructor
     *
     * @param inst    required on LN. Is empty on LN0
     * @param lnClass always required
     * @param prefix  optional. Is set to empty if null because empty is the default value on LN and the majority of the JAXB Element (LN, LNode, FCDA, ClientLN, but not on ExtRef and IEDName unfortunately)
     */
    public LnKey(String inst, String lnClass, String prefix) {
        if (StringUtils.isBlank(lnClass)) {
            throw new ScdException("lnClass is required");
        }
        // TODO: ce check est-il pertinent ?
//        if (StringUtils.isBlank(inst) && !LN0_LNCLASS.equals(lnClass)) {
//            throw new ScdException("lnInst is required for lnClass " + lnClass);
//        }
        this.inst = Objects.requireNonNullElse(inst, "");
        this.lnClass = lnClass;
        this.prefix = Objects.requireNonNullElse(prefix, "");
    }

    /**
     * Alternative constructor with lnClass as a List instead of a String.
     * JAXB classes lnClass attribute are List, even though it can only contain one element.
     * This constructor makes it easier to create LnKey from JAXB classes
     *
     * @param lnInst  LN lnInst
     * @param lnClass one element list containing the lnClass value of the LN
     * @param prefix  LN prefix
     */
    public LnKey(String lnInst, List<String> lnClass, String prefix) {
        this(lnInst, lnClass == null || lnClass.isEmpty() ? null : lnClass.get(0), prefix);
    }

    /**
     * Tell if the key is a key of a LN0 element
     *
     * @return true if the key lnClass is "LLN0", false otherwise.
     */
    public boolean isLn0() {
        return LN0_LNCLASS.equals(lnClass);
    }

    /**
     * Extract key from LN element
     *
     * @param tAnyLN LN element
     * @return key
     */
    public static LnKey from(TAnyLN tAnyLN) {
        if (tAnyLN instanceof TLN0) {
            return LN0_KEY;
        } else if (tAnyLN instanceof TLN tln) {
            return new LnKey(tln.getInst(), tln.getLnClass(), tln.getPrefix());
        } else {
            throw new ScdException("Unexpected class : " + (tAnyLN != null ? tAnyLN.getClass() : null));
        }
    }

    /**
     * Extract key from ExtRef element
     *
     * @param tExtRef ExtRef element
     * @return key
     */
    public static LnKey from(TExtRef tExtRef) {
        if (tExtRef.getLnClass().contains(LN0_LNCLASS)) {
            return LN0_KEY;
        } else {
            return new LnKey(tExtRef.getLnInst(), tExtRef.getLnClass(), tExtRef.getPrefix());
        }
    }

    /**
     * Extract key from LNode element
     *
     * @param tlNode LNode element
     * @return key
     */
    public static LnKey from(TLNode tlNode) {
        if (tlNode.getLnClass().contains(LN0_LNCLASS)) {
            return LN0_KEY;
        } else {
            return new LnKey(tlNode.getLnInst(), tlNode.getLnClass(), tlNode.getPrefix());
        }
    }

    /**
     * Extract key from TClientLN element
     *
     * @param tClientLN TClientLN element
     * @return key
     */
    public static LnKey from(TClientLN tClientLN) {
        if (tClientLN.getLnClass().contains(LN0_LNCLASS)) {
            return LN0_KEY;
        } else {
            return new LnKey(tClientLN.getLnInst(), tClientLN.getLnClass(), tClientLN.getPrefix());
        }
    }

    /**
     * Extract key from TControlWithIEDName.IEDName element
     *
     * @param iedName TControlWithIEDName.IEDName element
     * @return key
     */
    public static LnKey from(TControlWithIEDName.IEDName iedName) {
        if (iedName.getLnClass().contains(LN0_LNCLASS)) {
            return LN0_KEY;
        } else {
            return new LnKey(iedName.getLnInst(), iedName.getLnClass(), iedName.getPrefix());
        }
    }

    /**
     * Extract key from TFCDA element
     *
     * @param tfcda TFCDA element
     * @return key
     */
    public static LnKey from(TFCDA tfcda) {
        if (tfcda.getLnClass().contains(LN0_LNCLASS)) {
            return LN0_KEY;
        } else {
            return new LnKey(tfcda.getLnInst(), tfcda.getLnClass(), tfcda.getPrefix());
        }    }
}
