// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDo;

class DoTypeNameTest {

    @Test
    void constructor_whenCalledWithDoName_shouldFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1.bdo1.bdo2");
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).containsExactly("bdo1", "bdo2");
    }

    @Test
    void constructor_whenCalledWithDoNameWithDotSeparatedValues_shouldFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1");
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNullDoName_shouldNotFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName(null);
        // then
        assertThat(doTypeName.getName()).isEmpty();
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithDoNameAndSdoNames_shouldFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1", "bdo1.bdo2");
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).containsExactly("bdo1", "bdo2");
    }

    @Test
    void constructor_whenCalledWithNullSdoNames_shouldNotFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1", null);
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNullDoNameAndNullSdoNames_shouldNotFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName(null, null);
        // then
        assertThat(doTypeName.getName()).isEmpty();
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void constructor_whenCalledWithNameAndSdoNames_shouldFillValues() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1", "bdo1.bdo2");

        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).containsExactly("bdo1", "bdo2");
    }

    @Test
    void equals_whenCalled_shouldReturnTrue() {
        // given : nothing
        DoTypeName do1 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        DoTypeName do2 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        // when
        boolean result = do1.equals(do2);
        // then
        assertThat(result).isTrue();
    }

    @Test
    void equals_whenCalled_shouldReturnFalse() {
        // given : nothing
        DoTypeName do1 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        DoTypeName do2 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.ACD);
        // when
        boolean result = do1.equals(do2);
        // then
        assertThat(result).isFalse();
    }

    @Test
    void from_whenCalledWithDoTypeName_shouldNotFillValues() {
        // given : nothing
        DoTypeName do1 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        // when
        DoTypeName do2 = DoTypeName.from(do1);
        // then
        assertThat(do2).isEqualTo(do1);
    }

    @ParameterizedTest
    @CsvSource({"do1,do", "do,do", "do1.sdo1,do.sdo1", "do.sdo1,do.sdo1"})
    void toStringWithoutInst_when_instance_is_present_should_return_without_instance_number(String input, String expected) {
        // given : nothing
        DoTypeName do1 = createDo(input, TPredefinedCDCEnum.DPS);
        // when
        String result = do1.toStringWithoutInst();
        // then
        assertThat(result).isEqualTo(expected);
    }

}
