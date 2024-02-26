// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0


package org.lfenergy.compas.sct.commons.scl.ln;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.scl2007b4.model.TSystemLNGroupEnum;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;

import static org.assertj.core.api.Assertions.assertThat;

class LnKeyTest {

    @Test
    void test_updateDataRef_withLN() {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        TLN ln = new TLN();
        ln.setLnType("LnTypeAny");
        ln.setInst("1");
        ln.getLnClass().add(TSystemLNGroupEnum.LGOS.value());
        // When
        DataAttributeRef result = LnKey.updateDataRef(ln, dataAttributeRef);
        // Then
        assertThat(result).extracting(DataAttributeRef::getLnType,
                DataAttributeRef::getPrefix,
                DataAttributeRef::getLnClass,
                DataAttributeRef::getLnInst)
                .containsExactly("LnTypeAny", "", "LGOS", "1");
    }

    @Test
    void test_updateDataRef_withLN0() {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        LN0 ln0 = new LN0();
        ln0.setLnType("LnType0");
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        // When
        DataAttributeRef result = LnKey.updateDataRef(ln0, dataAttributeRef);
        // Then
        assertThat(result).extracting(DataAttributeRef::getLnType,
                        DataAttributeRef::getPrefix,
                        DataAttributeRef::getLnClass,
                        DataAttributeRef::getLnInst)
                .containsExactly("LnType0", "", "LLN0", null);
    }

}