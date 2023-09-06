// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;

class DaTypeNameTest {

    @Test
    void constructor_whenCalledWithDaName_shouldFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName("da1.bda1.bda2");
        // then
        assertThat(daTypeName.getName()).isEqualTo("da1");
        assertThat(daTypeName.getStructNames()).containsExactly("bda1", "bda2");
        assertThat(daTypeName.getDaiValues()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithDaNameWithDotSeparatedValues_shouldFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName("da1");
        // then
        assertThat(daTypeName.getName()).isEqualTo("da1");
        assertThat(daTypeName.getStructNames()).isNotNull().isEmpty();
        assertThat(daTypeName.getDaiValues()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNullDaName_shouldNotFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName(null);
        // then
        assertThat(daTypeName.getName()).isEmpty();
        assertThat(daTypeName.getStructNames()).isNotNull().isEmpty();
        assertThat(daTypeName.getDaiValues()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithDaNameAndStructNames_shouldFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName("da1", "bda1.bda2");
        // then
        assertThat(daTypeName.getName()).isEqualTo("da1");
        assertThat(daTypeName.getStructNames()).containsExactly("bda1", "bda2");
        assertThat(daTypeName.getDaiValues()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNullStructNames_shouldNotFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName("da1", null);
        // then
        assertThat(daTypeName.getName()).isEqualTo("da1");
        assertThat(daTypeName.getStructNames()).isNotNull().isEmpty();
        assertThat(daTypeName.getDaiValues()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNullDaNameAndNullStructNames_shouldNotFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName(null, null);
        // then
        assertThat(daTypeName.getName()).isEmpty();
        assertThat(daTypeName.getStructNames()).isNotNull().isEmpty();
        assertThat(daTypeName.getDaiValues()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNameAndStructNames_shouldFillValues() {
        // given : nothing
        // when
        DaTypeName daTypeName = new DaTypeName("da1", "bda1.bda2");

        // then
        assertThat(daTypeName.getName()).isEqualTo("da1");
        assertThat(daTypeName.getStructNames()).containsExactly("bda1", "bda2");
    }

    @Test
    void equals_whenCalled_shouldReturnTrue() {
        // given : nothing
        DaTypeName da1 = createDa("da1.bda1.bda2", TFCEnum.CF, true, Map.of(0L, "value"));
        DaTypeName da2 = createDa("da1.bda1.bda2", TFCEnum.CF, true, Map.of(0L, "value"));
        // when
        boolean result = da1.equals(da2);
        // then
        assertThat(result).isTrue();
    }

    @Test
    void equals_whenCalled_shouldReturnFalse() {
        // given : nothing
        DaTypeName da1 = createDa("da1.bda1.bda2", TFCEnum.CF, true, Map.of(0L, "value"));
        DaTypeName da2 = createDa("da1.bda1.bda2", TFCEnum.DC, true, Map.of(0L, "value"));
        // when
        boolean result = da1.equals(da2);
        // then
        assertThat(result).isFalse();
    }

    @Test
    void from_whenCalledWithDaTypeName_shouldNotFillValues() {
        // given : nothing
        DaTypeName da1 = createDa("da1.bda1.bda2", TFCEnum.CF, true, Map.of(0L, "value"));
        // when
        DaTypeName da2 = DaTypeName.from(da1);
        // then
        assertThat(da2).isEqualTo(da1);
    }

    /**
     * ListFcEnum = CF, DC, SG, SP, ST , SE
     * Fc value should be in ListFcEnum to be considered as known
     */
    @Test
    void isUpdatable_whenFcKnown_And_valImportIsTrue_shouldReturnTrue() {
        // given
        DaTypeName da1 = createDa("da1.bda1.bda2",  TFCEnum.CF, true, Map.of(0L, "value"));
        // when
        boolean isDAUpdatable = da1.isUpdatable();
        // then
        assertThat(isDAUpdatable).isTrue();
    }

    @ParameterizedTest
    @MethodSource("daParametersProvider")
    void isUpdatable(String testName, TFCEnum fc, boolean valImport) {
        // given
        DaTypeName da1 = createDa("da1.bda1.bda2", fc, valImport, Map.of(0L, "value"));
        // when
        boolean isDAUpdatable = da1.isUpdatable();
        // then
        assertThat(isDAUpdatable).isFalse();
    }

    private static Stream<Arguments> daParametersProvider() {
        return Stream.of(
                Arguments.of("should return false when ", TFCEnum.MX, true),
                Arguments.of("should return false when ",TFCEnum.CF, false)
        );
    }

}
