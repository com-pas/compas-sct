// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ReflectionSupport;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonConstantsTest {

    @Test
    void constructor_should_throw_exception() {
        // Given
        // When & Then
        assertThatThrownBy(() -> ReflectionSupport.newInstance(Utils.class))
            .isInstanceOf(UnsupportedOperationException.class);
    }

}
