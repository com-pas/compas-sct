// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0


package org.lfenergy.compas.sct.commons.scl.ln;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;

import static org.assertj.core.api.Assertions.assertThat;

class LnIdTest {

    @Test
    void from_LN_should_succeed() {
        //Given
        TLN ln = new TLN();
        ln.setInst("1");
        ln.getLnClass().add(TSystemLNGroupEnum.LGOS.value());
        ln.setPrefix("Prefix");
        // When
        LnId lnId = LnId.from(ln);
        // Then
        assertThat(lnId.lnClass()).isEqualTo("LGOS");
        assertThat(lnId.lnInst()).isEqualTo("1");
        assertThat(lnId.prefix()).isEqualTo("Prefix");
    }

    @Test
    void from_LN0_should_succeed() {
        //Given
        LN0 ln0 = new LN0();
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        // When
        LnId lnId = LnId.from(ln0);
        // Then
        assertThat(lnId).isSameAs(LnId.LN0_ID);
    }

    @Test
    void from_TLNode_should_succeed() {
        //Given
        TLNode tlNode = new TLNode();
        tlNode.setLnInst("1");
        tlNode.getLnClass().add(TSystemLNGroupEnum.LGOS.value());
        tlNode.setPrefix("Prefix");
        // When
        LnId lnId = LnId.from(tlNode);
        // Then
        assertThat(lnId.lnClass()).isEqualTo("LGOS");
        assertThat(lnId.lnInst()).isEqualTo("1");
        assertThat(lnId.prefix()).isEqualTo("Prefix");
    }

    @Test
    void from_TLNode_LN0_should_succeed() {
        //Given
        TLNode tlNode = new TLNode();
        tlNode.setLnInst("");
        tlNode.getLnClass().add("LLN0");
        tlNode.setPrefix("");
        // When
        LnId lnId = LnId.from(tlNode);
        // Then
        assertThat(lnId).isSameAs(LnId.LN0_ID);
    }

}
