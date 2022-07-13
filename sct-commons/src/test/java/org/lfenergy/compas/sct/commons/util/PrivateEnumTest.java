// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TCompasSclFileType;

import javax.xml.bind.JAXBElement;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrivateEnumTest {

    @Test
    void fromClass_should_return_PrivateEnum() {
        // Given
        Class<?> compasClass = TCompasSclFileType.class;
        // When
        PrivateEnum result = PrivateEnum.fromClass(compasClass);
        // Then
        assertThat(result).isEqualTo(PrivateEnum.COMPAS_SCL_FILE_TYPE);
    }

    @Test
    void fromClass_with_unknown_class_should_throw_exception() {
        // Given
        Class<?> classToTest = Object.class;
        // When & Then
        assertThatThrownBy(() -> PrivateEnum.fromClass(classToTest)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void createJaxbElement_should_return_Private() {
        // Given
        TCompasSclFileType compasElement = TCompasSclFileType.SCD;
        // When
        JAXBElement<TCompasSclFileType> result = PrivateEnum.createJaxbElement(compasElement);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo(TCompasSclFileType.SCD);
    }

    @Test
    void createJaxbElement_with_unknown_class_should_throw_exception() {
        // Given
        Object notACompasElement = new Object();
        // When & Then
        assertThatThrownBy(() -> PrivateEnum.createJaxbElement(notACompasElement)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void toString_should_return_private_type() {
        // Given
        PrivateEnum privateEnum = PrivateEnum.COMPAS_SCL_FILE_TYPE;
        // When
        String result = privateEnum.toString();
        // Then
        assertThat(result).isEqualTo("COMPAS-SclFileType");
    }

}
