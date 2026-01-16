// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DataRefTest {

    public static Stream<Arguments> provideDataRef() {
        return Stream.of(
                Arguments.of("Do", "da", new DataRef("Do", null, "da", null)),
                Arguments.of("Do.sdo", "da", new DataRef("Do", List.of("sdo"), "da", null)),
                Arguments.of("Do", "da.bda", new DataRef("Do", null, "da", List.of("bda"))),
                Arguments.of("Do.sdo", "da.bda", new DataRef("Do", List.of("sdo"), "da", List.of("bda"))),
                Arguments.of("Do.sdo1.sdo2.sdo3.sdo4", "da.bda1.bda2.bda3.bda4", new DataRef("Do", List.of("sdo1", "sdo2", "sdo3", "sdo4"), "da", List.of("bda1", "bda2", "bda3", "bda4")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataRef")
    void from_should_parse_DO_and_DA(String doNames, String daNames, DataRef expected) {
        // Given : parameters
        // When
        DataRef result = DataRef.from(doNames, daNames);
        // Then
        assertThat(result).isEqualTo(expected);
    }
}
