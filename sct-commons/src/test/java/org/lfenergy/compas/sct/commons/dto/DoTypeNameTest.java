// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDo;

class DoTypeNameTest {

    @Test
    void testConstructorWithRef() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1.bdo1.bdo2");
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).containsExactly("bdo1", "bdo2");
    }

    @Test
    void testConstructorWithRefWhenEmptyStruct() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1");
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void testConstructorWithRefWithNullName() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName(null);
        // then
        assertThat(doTypeName.getName()).isEqualTo("");
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void testConstructor2WithRef() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1", "bdo1.bdo2");
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).containsExactly("bdo1", "bdo2");
    }

    @Test
    void testConstructor2WithRefWhenEmptyStruct() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1", null);
        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void testConstructor2WithRefWithNullName() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName(null, null);
        // then
        assertThat(doTypeName.getName()).isEqualTo("");
        assertThat(doTypeName.getStructNames()).isNotNull().isEmpty();
    }

    @Test
    void testConstructorWithNameAndStructNames() {
        // given : nothing
        // when
        DoTypeName doTypeName = new DoTypeName("do1", "bdo1.bdo2");

        // then
        assertThat(doTypeName.getName()).isEqualTo("do1");
        assertThat(doTypeName.getStructNames()).containsExactly("bdo1", "bdo2");
    }

    @Test
    void testEquals() {
        // given : nothing
        DoTypeName do1 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        DoTypeName do2 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        // when
        boolean result = do1.equals(do2);
        // then
        assertThat(result).isTrue();
    }

    @Test
    void testNotEquals() {
        // given : nothing
        DoTypeName do1 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        DoTypeName do2 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.ACD);
        // when
        boolean result = do1.equals(do2);
        // then
        assertThat(result).isFalse();
    }

    @Test
    void testFrom() {
        // given : nothing
        DoTypeName do1 = createDo("do1.bdo1.bdo2", TPredefinedCDCEnum.DPS);
        // when
        DoTypeName do2 = DoTypeName.from(do1);
        // then
        assertThat(do2).isEqualTo(do1);
    }
}
