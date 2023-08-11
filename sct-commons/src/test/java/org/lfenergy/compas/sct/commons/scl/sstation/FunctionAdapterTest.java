// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FunctionAdapterTest {

    private final BayAdapter bayAdapter = mock(BayAdapter.class);
    private FunctionAdapter functionAdapter;

    @BeforeEach
    void setUp() {
        TBay tBay = new TBay();
        TFunction tFunction = new TFunction();
        tFunction.setName("functionName");
        tBay.getFunction().add(tFunction);
        when(bayAdapter.getCurrentElem()).thenReturn(tBay);
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
    void amChildElementRef_when_parent_does_not_contain_function_shouldReturnFalse() {
        // Given : setUp
        functionAdapter.getParentAdapter().getCurrentElem().getFunction().clear();
        // When
        boolean result = functionAdapter.amChildElementRef();
        // Then
        assertThat(result).isFalse();
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given : setUp
        // When
        String result = functionAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Function[@name=\"functionName\"]");
    }

}
