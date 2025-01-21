// SPDX-FileCopyrightText: 2025 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.ln;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.Objects;

public record LnId(String lnClass, String lnInst, String prefix) {
    private static final String LN0_LNCLASS = TLLN0Enum.LLN_0.value();

    /**
     * Id of LN0. All LN0 have the same lnClass, lnInst and prefix in SCD
     */
    public static final LnId LN0_ID = new LnId(LN0_LNCLASS, "", "");

    /**
     * Constructor
     *
     * @param lnClass always required
     * @param lnInst  optional. It should be empty for LN0, and should be filled for LN. No verification is done because some Lnode GAPC do not have a lninst.
     * @param prefix  optional. Is set to empty if null because empty is the default value on LN and the majority of the JAXB Element (LN, LNode, FCDA, ClientLN, but not on ExtRef and IEDName unfortunately)
     */
    public LnId(String lnClass, String lnInst, String prefix) {
        if (StringUtils.isBlank(lnClass)) {
            throw new ScdException("lnClass is required");
        }
        this.lnClass = lnClass;
        this.lnInst = Objects.requireNonNullElse(lnInst, "");
        this.prefix = Objects.requireNonNullElse(prefix, "");
    }

    /**
     * Alternative constructor with lnClass as a List instead of a String.
     * JAXB classes lnClass attribute are List, even though it can only contain one element.
     * This constructor makes it easier to create LnId from JAXB classes
     *
     * @param lnClass one element list containing the lnClass value of the LN
     * @param lnInst  LN lnInst
     * @param prefix  LN prefix
     */
    public LnId(List<String> lnClass, String lnInst, String prefix) {
        this(lnClass == null || lnClass.isEmpty() ? null : lnClass.getFirst(), lnInst, prefix);
    }

    /**
     * Extract id from LN element
     *
     * @param tAnyLN LN element
     * @return id
     */
    public static LnId from(TAnyLN tAnyLN) {
        if (tAnyLN instanceof TLN0) {
            return LN0_ID;
        } else if (tAnyLN instanceof TLN tln) {
            return new LnId(tln.getLnClass(), tln.getInst(), tln.getPrefix());
        } else {
            throw new ScdException("Unexpected class : " + (tAnyLN != null ? tAnyLN.getClass() : null));
        }
    }

    /**
     * Extract id from LNode element
     *
     * @param tlNode LNode element
     * @return id
     */
    public static LnId from(TLNode tlNode) {
        if (tlNode.getLnClass().contains(LN0_LNCLASS)) {
            return LN0_ID;
        } else {
            return new LnId(tlNode.getLnClass(), tlNode.getLnInst(), tlNode.getPrefix());
        }
    }


}
