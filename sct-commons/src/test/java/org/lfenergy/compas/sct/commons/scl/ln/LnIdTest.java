// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0


package org.lfenergy.compas.sct.commons.scl.ln;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.scl2007b4.model.TSystemLNGroupEnum;

import static org.assertj.core.api.Assertions.assertThat;

class LnIdTest {

    @Test
    void test_LnKey_withLN() {
        //Given
        TLN ln = new TLN();
        ln.setLnType("LnTypeAny");
        ln.setInst("1");
        ln.getLnClass().add(TSystemLNGroupEnum.LGOS.value());
        // When
        LnId lnId = LnId.from(ln);
        // Then
        assertThat(lnId.lnClass()).isEqualTo("LGOS");
        assertThat(lnId.lnInst()).isEqualTo("1");
        assertThat(lnId.prefix()).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void test_LnKey_withLN0() {
        //Given
        LN0 ln0 = new LN0();
        ln0.setLnType("LnType0");
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        // When
        LnId lnId = LnId.from(ln0);
        // Then
        assertThat(lnId.lnClass()).isEqualTo("LLN0");
        assertThat(lnId.lnInst()).isEqualTo("");
        assertThat(lnId.prefix()).isEqualTo(StringUtils.EMPTY);
    }

}
