// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TFunction;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionAdapterTest {

    private FunctionAdapter functionAdapter;

    @BeforeEach
    void setUp() {
        TBay tBay = new TBay();
        BayAdapter bayAdapter = new BayAdapter(null, tBay);
        TFunction tFunction = new TFunction();
        tFunction.setName("functionName");
        tBay.getFunction().add(tFunction);
        functionAdapter = new FunctionAdapter(bayAdapter, tFunction);
    }

    @Test
    void amChildElementRef_should_succeed() {
        // Given : setUp
        // When
        boolean result = functionAdapter.amChildElementRef();
        // Then
        assertThat(result).isTrue();
    }

    @Test
    void amChildElementRef_when_parent_does_not_contain_function_should_be_false() {
        // Given : setUp
        functionAdapter.getParentAdapter().getCurrentElem().getFunction().clear();
        // When
        boolean result = functionAdapter.amChildElementRef();
        // Then
        assertThat(result).isFalse();
    }

    @Test
    void elementXPath() {
        // Given : setUp
        // When
        String result = functionAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Function[@name=\"functionName\"]");
    }

}
