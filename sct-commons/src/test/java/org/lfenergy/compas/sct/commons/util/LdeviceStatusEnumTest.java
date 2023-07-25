// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LdeviceStatusEnumTest {

    @ParameterizedTest
    @CsvSource({"on,ON", "off,OFF"})
    void fromValue(String ldeviceStatus, String expected) {
        //Given
        //When
        LdeviceStatus ldeviceStatusEnum = LdeviceStatus.fromValue(ldeviceStatus);
        //Then
        assertThat(ldeviceStatusEnum).isEqualTo(LdeviceStatus.valueOf(expected));
    }

    @Test
    void fromValue() {
        //Given
        String ldeviceStatus = "patate";
        //When
        //Then
        assertThatCode(() -> LdeviceStatus.fromValue(ldeviceStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The Ldevice status patate does not exist. It should be among [on, off]");
    }
}