// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TInputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class InputsAdapterTest {

    @Test
    void constructor_should_succeed() {
        // Given
        TInputs tInputs = new TInputs();
        LN0 ln0 = new LN0();
        ln0.setInputs(tInputs);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);
        // When && Then
        assertThatNoException().isThrownBy(() -> new InputsAdapter(ln0Adapter, tInputs));
    }

    @Test
    void elementXPath_should_succeed() {
        // Given
        TInputs tInputs = new TInputs();
        InputsAdapter inputsAdapter = new InputsAdapter(null, tInputs);
        // When
        String result = inputsAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Inputs");
    }

}
