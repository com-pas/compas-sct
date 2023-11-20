// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.Objects;

/**
 * Key that define a LN inside a LDevice.
 * The key is unique inside a LDevice.
 * LnKey attributes can also be found on ExtRef, FCDA, ClientLN, IEDName and LNode elements to point towards a LN.
 *
 * @param lnClass always required
 * @param lnInst  required on LN. Is empty on LN0
 * @param prefix  is set to empty if null (this is the default value for prefix on LN, LNode, FCDA, ClientLN, but not on ExtRef, IEDName)
 */
public record LnKey(String lnClass, String lnInst, String prefix) {

    /**
     * Constructor
     */
    public LnKey(String lnClass, String lnInst, String prefix) {
        if (StringUtils.isBlank(lnClass)) {
            throw new ScdException("lnClass is required");
        }
        if (StringUtils.isBlank(lnInst) && !TLLN0Enum.LLN_0.value().equals(lnClass)) {
            throw new ScdException("lnInst is required for lnClass " + lnClass);
        }
        this.lnClass = lnClass;
        this.lnInst = Objects.requireNonNullElse(lnInst, "");
        this.prefix = Objects.requireNonNullElse(prefix, "");

    }

    /**
     * Constructor with lnClass as a List.
     * xjc plugin generates lnClass attribute as a List, even though it can only contain one element.
     *
     * @param lnClass one element list containing the lnClass value of the LN
     * @param lnInst  LN lnInst
     * @param prefix  LN prefix
     */
    public LnKey(List<String> lnClass, String lnInst, String prefix) {
        this(lnClass == null || lnClass.isEmpty() ? null : lnClass.get(0), lnInst, prefix);
    }

    private static final List<String> LN0_LNCLASS = List.of(TLLN0Enum.LLN_0.value());

    /**
     * Key of LN0. All LN0 have the same lnClass, lnInst and prefix in SCD
     */
    public static final LnKey LN0_KEY = new LnKey(LN0_LNCLASS, "", "");

    /**
     * Tell if the key is a key of a LN0 element
     *
     * @return true if the key lnClass is "LLN0", false otherwise.
     */
    public boolean isLn0() {
        return Objects.equals(lnClass, TLLN0Enum.LLN_0.value());
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
            return new LnKey(tln.getLnClass(), tln.getInst(), tln.getPrefix());
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
        if (LN0_LNCLASS.equals(tExtRef.getLnClass())) {
            return LN0_KEY;
        } else {
            return new LnKey(tExtRef.getLnClass(), tExtRef.getLnInst(), tExtRef.getPrefix());
        }
    }

    /**
     * Extract key from LNode element
     *
     * @param tlNode LNode element
     * @return key
     */
    public static LnKey from(TLNode tlNode) {
        if (LN0_LNCLASS.equals(tlNode.getLnClass())) {
            return LN0_KEY;
        } else {
            return new LnKey(tlNode.getLnClass(), tlNode.getLnInst(), tlNode.getPrefix());
        }
    }
}
