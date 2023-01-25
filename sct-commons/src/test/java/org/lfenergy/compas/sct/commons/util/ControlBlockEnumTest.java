// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;

import java.util.stream.Stream;

class ControlBlockEnumTest {

    @ParameterizedTest
    @MethodSource("provideFromTServiceType")
    void from_should_map_ServiceType_to_ControlBlockEnumTest(TServiceType tServiceType, ControlBlockEnum expected) {
        //Given : parameter
        //When
        ControlBlockEnum result = ControlBlockEnum.from(tServiceType);
        //Then
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void from_POOL_should_throw_exception() {
        //Given
        TServiceType pollServiceType = TServiceType.POLL;
        //When & Then
        Assertions.assertThatThrownBy(() -> ControlBlockEnum.from(pollServiceType))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("provideFromTControlClass")
    void from_should_map_Class_to_ControlBlockEnumTest(Class<? extends TControl> tControlClass, ControlBlockEnum expected) {
        //Given : parameter
        //When
        ControlBlockEnum result = ControlBlockEnum.from(tControlClass);
        //Then
        Assertions.assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> provideFromTControlClass() {
        return Stream.of(
            Arguments.of(TGSEControl.class, ControlBlockEnum.GSE),
            Arguments.of(TSampledValueControl.class, ControlBlockEnum.SAMPLED_VALUE),
            Arguments.of(TReportControl.class, ControlBlockEnum.REPORT),
            Arguments.of(TLogControl.class, ControlBlockEnum.LOG)
        );
    }

    public static Stream<Arguments> provideFromTServiceType() {
        return Stream.of(
            Arguments.of(TServiceType.GOOSE, ControlBlockEnum.GSE),
            Arguments.of(TServiceType.SMV, ControlBlockEnum.SAMPLED_VALUE),
            Arguments.of(TServiceType.REPORT, ControlBlockEnum.REPORT)
        );
    }

}
