// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.model.cbcom.TCBType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.from;

class ControlBlockEnumTest {

    @ParameterizedTest
    @MethodSource("provideFromTServiceType")
    void from_TServiceType_should_map_ServiceType_to_ControlBlockEnumTest(TServiceType tServiceType, ControlBlockEnum expected) {
        //Given : parameter
        //When
        ControlBlockEnum result = from(tServiceType);
        //Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void from_TServiceType_POOL_should_throw_exception() {
        //Given
        TServiceType pollServiceType = TServiceType.POLL;
        //When & Then
        assertThatThrownBy(() -> from(pollServiceType))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("provideFromTControlClass")
    void from_TControl_should_map_Class_to_ControlBlockEnumTest(Class<? extends TControl> tControlClass, ControlBlockEnum expected) {
        //Given : parameter
        //When
        ControlBlockEnum result = from(tControlClass);
        //Then
        assertThat(result).isEqualTo(expected);
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

    @ParameterizedTest
    @MethodSource("provideFromTCBType")
    public void from_TCBType_when_GOOSE_or_SV_should_succeed(TCBType tcbType, ControlBlockEnum expectedControlBlockEnum){
        // Given : parameter
        // When
        ControlBlockEnum result = from(tcbType);
        // Then
        assertThat(result).isEqualTo(expectedControlBlockEnum);
    }

    @ParameterizedTest
    @EnumSource(value = TCBType.class, mode = EnumSource.Mode.EXCLUDE, names = {"GOOSE", "SV"})
    public void from_TCBType_when_unsupported_TCBType_should_throw_exception(TCBType tcbType){
        // Given : parameter
        // When & Then
        assertThatThrownBy(() -> from(tcbType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported TCBType: " + tcbType.name());
    }

    public static Stream<Arguments> provideFromTCBType() {
        return Stream.of(
                Arguments.of(TCBType.GOOSE, ControlBlockEnum.GSE),
                Arguments.of(TCBType.SV, ControlBlockEnum.SAMPLED_VALUE)
        );
    }
}
