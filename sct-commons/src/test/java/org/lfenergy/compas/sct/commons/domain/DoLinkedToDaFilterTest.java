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

class DoLinkedToDaFilterTest {

    public static Stream<Arguments> provideDoLinkedToDaFilter() {
        return Stream.of(
                Arguments.of(null, null, new DoLinkedToDaFilter(null, null, null, null)),
                Arguments.of("", "", new DoLinkedToDaFilter(null, null, null, null)),
                Arguments.of("Do", "", new DoLinkedToDaFilter("Do", null, null, null)),
                Arguments.of("", "da", new DoLinkedToDaFilter(null, null, "da", null)),
                Arguments.of("Do", "da", new DoLinkedToDaFilter("Do", null, "da", null)),
                Arguments.of("Do.sdo", "da", new DoLinkedToDaFilter("Do", List.of("sdo"), "da", null)),
                Arguments.of("Do", "da.bda", new DoLinkedToDaFilter("Do", null, "da", List.of("bda"))),
                Arguments.of("Do.sdo", "da.bda", new DoLinkedToDaFilter("Do", List.of("sdo"), "da", List.of("bda"))),
                Arguments.of("Do.sdo1.sdo2.sdo3.sdo4", "da.bda1.bda2.bda3.bda4", new DoLinkedToDaFilter("Do", List.of("sdo1", "sdo2", "sdo3", "sdo4"), "da", List.of("bda1", "bda2", "bda3", "bda4")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideDoLinkedToDaFilter")
    void from_should_parse_DO_and_DA(String doNames, String daNames, DoLinkedToDaFilter expected) {
        // Given : parameters
        // When
        DoLinkedToDaFilter result = DoLinkedToDaFilter.from(doNames, daNames);
        // Then
        assertThat(result).isEqualTo(expected);
    }
}
