/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringLnClassEnumTest {

    @Test
    void value_should_return_string_value() {
        // Given
        // When
        // Then
        assertThat(MonitoringLnClassEnum.LSVS.value()).isEqualTo("LSVS");
        assertThat(MonitoringLnClassEnum.LGOS.value()).isEqualTo("LGOS");
    }

}