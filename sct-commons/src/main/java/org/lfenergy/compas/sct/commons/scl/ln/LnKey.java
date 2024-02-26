// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.ln;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;

@Getter
@EqualsAndHashCode
public class LnKey {

    private final String inst;
    @EqualsAndHashCode.Exclude
    private final String lnType;
    private final String lnClass;
    /** empty for LN0 */
    private final String prefix;

    public LnKey(LN0 ln0) {
        this.inst = ln0.getInst();
        this.lnType = ln0.getLnType();
        this.lnClass = ln0.getLnClass().get(0);
        this.prefix = StringUtils.EMPTY;
    }

    public LnKey(TLN tln) {
        this.inst = tln.getInst();
        this.lnType = tln.getLnType();
        this.lnClass = tln.getLnClass().get(0);
        this.prefix = tln.getPrefix();
    }

}

