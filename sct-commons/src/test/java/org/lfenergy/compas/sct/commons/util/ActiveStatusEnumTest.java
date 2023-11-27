// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ActiveStatusEnumTest {

    @ParameterizedTest
    @CsvSource({"on,ON", "off,OFF"})
    void fromValue_withKnownStatus_shouldNotThrowException(String ldeviceStatus, String expected) {
        //Given
        //When
        ActiveStatus activeStatusEnum = ActiveStatus.fromValue(ldeviceStatus);
        //Then
        assertThat(activeStatusEnum).isEqualTo(ActiveStatus.valueOf(expected));
    }

    @Test
    void fromValue_withUnknownStatus_shouldThrowException() {
        //Given
        String ldeviceStatus = "patate";
        //When
        //Then
        assertThatCode(() -> ActiveStatus.fromValue(ldeviceStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The status patate does not exist. It should be among [on, off]");
    }
}