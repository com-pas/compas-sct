// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import javax.xml.bind.JAXBElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrivateServiceTest {

    private TPrivate privateSCD;
    private TPrivate privateICD;
    private ObjectFactory objectFactory;

    @BeforeEach
    void setUp() {
        objectFactory = new ObjectFactory();
        privateSCD = objectFactory.createTPrivate();
        privateSCD.setType(PrivateEnum.COMPAS_SCL_FILE_TYPE.getPrivateType());
        privateSCD.getContent().add(objectFactory.createSclFileType(TCompasSclFileType.SCD));
        privateICD = objectFactory.createTPrivate();
        privateICD.setType(PrivateEnum.COMPAS_SCL_FILE_TYPE.getPrivateType());
        privateICD.getContent().add(objectFactory.createSclFileType(TCompasSclFileType.ICD));
    }

    @Test
    void class_should_not_be_instantiable() {
        // Given
        Constructor<?>[] constructors = PrivateService.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        // When & Then
        assertThatThrownBy(constructor::newInstance)
            .isInstanceOf(InvocationTargetException.class)
            .getCause().isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getCompasPrivates_should_return_privates_value() {
        // Given : setUp
        // When
        List<TCompasSclFileType> result = PrivateService.getCompasPrivates(List.of(privateSCD, privateICD),
            TCompasSclFileType.class);
        //Then
        assertThat(result)
            .hasSize(2)
            .containsExactly(TCompasSclFileType.SCD, TCompasSclFileType.ICD);
    }

    @Test
    void getCompasPrivates_when_no_privates_match_class_should_return_empty_list() {
        // Given : setUp
        // When
        List<TCompasICDHeader> result = PrivateService.getCompasPrivates(List.of(privateSCD), TCompasICDHeader.class);
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCompasPrivates_when_class_is_not_compas_class_should_throw_exception() {
        // Given : setUp
        List<TPrivate> privates = List.of(privateSCD);
        // When & Then
        assertThatThrownBy(() -> PrivateService.getCompasPrivates(privates, Object.class)).isInstanceOf(
            NoSuchElementException.class);
    }

    @Test
    void getCompasPrivates_when_content_not_match_private_type_should_throw_exception() {
        // Given : setUp
        privateSCD.setType(PrivateEnum.COMPAS_BAY.getPrivateType());
        Class<?> compasClass = PrivateEnum.COMPAS_BAY.getCompasClass();
        List<TPrivate> privates = List.of(privateSCD);
        // When & Then
        assertThatThrownBy(() -> PrivateService.getCompasPrivates(privates, compasClass)).isInstanceOf(
            ScdException.class);
    }

    @Test
    void getCompasPrivates_on_base_element_should_return_privates_value() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        baseElement.getPrivate().add(privateICD);
        // When
        List<TCompasSclFileType> result = PrivateService.getCompasPrivates(baseElement, TCompasSclFileType.class);
        // Then
        assertThat(result)
            .hasSize(2)
            .containsExactly(TCompasSclFileType.SCD, TCompasSclFileType.ICD);
    }

    @Test
    void getCompasPrivates_on_base_element_should_not_set_private() {
        // Given
        TBaseElement baseElement = new SCL();
        // When
        PrivateService.getCompasPrivates(baseElement, TCompasSclFileType.class);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void getCompasPrivate_should_return_private_value() {
        // Given : setUp
        // When
        Optional<TCompasSclFileType> result = PrivateService.getCompasPrivate(privateSCD,
            TCompasSclFileType.class);
        //Then
        assertThat(result).isPresent()
            .hasValue(TCompasSclFileType.SCD);
    }

    @Test
    void getCompasPrivate_should_return_empty() {
        // Given : setUp
        // When
        Optional<TCompasBay> result = PrivateService.getCompasPrivate(privateSCD,
            TCompasBay.class);
        //Then
        assertThat(result).isNotPresent();
    }

    @Test
    void getCompasPrivate_should_throw_exception() {
        // Given : setUp
        privateSCD.getContent().add(objectFactory.createSclFileType(TCompasSclFileType.ICD));
        Class<?> compasClass = PrivateEnum.COMPAS_SCL_FILE_TYPE.getCompasClass();
        // When & Then
        assertThatThrownBy(() -> PrivateService.getCompasPrivate(privateSCD, compasClass)).isInstanceOf(
            ScdException.class);
    }

    @Test
    void getCompasICDHeader_should_return_private_value() {
        // Given
        privateSCD = objectFactory.createTPrivate();
        privateSCD.setType(PrivateEnum.COMPAS_ICDHEADER.getPrivateType());
        TCompasICDHeader tCompasICDHeader = objectFactory.createTCompasICDHeader();
        privateSCD.getContent().add(objectFactory.createICDHeader(tCompasICDHeader));
        // When
        Optional<TCompasICDHeader> optionalResult = PrivateService.getCompasICDHeader(privateSCD);
        //Then
        assertThat(optionalResult).isPresent().get().matches(result -> result == tCompasICDHeader);
    }

    @Test
    void createPrivate_should_return_private_new_private() {
        // Given
        // When
        TPrivate result = PrivateService.createPrivate(TCompasSclFileType.SCD);
        //Then
        assertThat(result).isNotNull()
            .hasFieldOrPropertyWithValue("type", PrivateEnum.COMPAS_SCL_FILE_TYPE.getPrivateType());
        assertThat(result.getContent()).hasSize(1).first().satisfies(content -> assertThat(content).isInstanceOf(JAXBElement.class));
        JAXBElement<?> content = (JAXBElement<?>) result.getContent().get(0);
        assertThat(content.isNil()).isFalse();
        assertThat(content.getValue()).isNotNull().isInstanceOf(TCompasSclFileType.class)
            .isEqualTo(TCompasSclFileType.SCD);
    }

    @Test
    void removePrivates_should_remove_privates() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_SCL_FILE_TYPE);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void removePrivates_should_do_nothing_when_no_private_match_type() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.getPrivate()).hasSize(1);
    }

    @Test
    void removePrivates_should_remove_privates_of_given_type() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        TCompasICDHeader tCompasICDHeader = objectFactory.createTCompasICDHeader();
        baseElement.getPrivate().add(PrivateService.createPrivate(tCompasICDHeader));
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.getPrivate()).hasSize(1);
        TPrivate tPrivate = baseElement.getPrivate().get(0);
        assertThat(tPrivate.getType()).isEqualTo(privateSCD.getType());
        assertThat(tPrivate.getContent()).hasSize(1).first().isInstanceOf(JAXBElement.class);
        JAXBElement<?> jaxbElement = (JAXBElement<?>) tPrivate.getContent().get(0);
        assertThat(jaxbElement.isNil()).isFalse();
        assertThat(jaxbElement.getValue()).isEqualTo(TCompasSclFileType.SCD);
    }

    @Test
    void removePrivates_should_not_set_private() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.unsetPrivate();
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

}
