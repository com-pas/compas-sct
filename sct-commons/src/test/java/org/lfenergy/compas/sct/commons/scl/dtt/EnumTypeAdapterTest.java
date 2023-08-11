// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnumTypeAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenDataTypeTemplatesAndEnumType_shouldThrowException() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(new TDataTypeTemplates());
        TEnumType enumType = new TEnumType();
        //When Then
        assertThatCode(() -> new EnumTypeAdapter(dataTemplateAdapter, enumType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenDataTypeTemplatesAndEnumType_shouldNotThrowException() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TEnumType enumType = new TEnumType();
        dataTemplate.getEnumType().add(enumType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        //When Then
        assertThatCode(() -> new EnumTypeAdapter(dataTemplateAdapter, enumType)).doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    void testHasSameContentAs(){
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TEnumType enumType = new TEnumType();
        dataTemplate.getEnumType().add(enumType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        EnumTypeAdapter enumTypeAdapter = new EnumTypeAdapter(dataTemplateAdapter, enumType);
        TEnumVal sameEnumVal = new TEnumVal();
        sameEnumVal.setValue("sameValue");
        sameEnumVal.setOrd(1);
        sameEnumVal.setDesc("A desc");
        enumTypeAdapter.getCurrentElem().getEnumVal().add(sameEnumVal);
        TEnumType anotherEnumType = new TEnumType();
        anotherEnumType.getEnumVal().add(sameEnumVal);
        // When Then
        assertThat(enumTypeAdapter.hasSameContentAs(anotherEnumType)).isTrue();
        // When Then
        assertThat(enumTypeAdapter.hasSameContentAs(new TEnumType())).isFalse(); // empty enumType
        TEnumVal notSameEnumVal = new TEnumVal();
        notSameEnumVal.setValue("notSameValue");
        notSameEnumVal.setOrd(1);
        notSameEnumVal.setDesc("Another desc");
        anotherEnumType = new TEnumType();
        anotherEnumType.getEnumVal().add(notSameEnumVal);
        // When Then
        assertThat(enumTypeAdapter.hasSameContentAs(anotherEnumType)).isFalse();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TEnumType enumType = new TEnumType();
        dataTemplate.getEnumType().add(enumType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        EnumTypeAdapter enumTypeAdapter = new EnumTypeAdapter(dataTemplateAdapter, enumType);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(enumTypeAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        enumTypeAdapter.addPrivate(tPrivate);
        // Then
        assertThat(enumTypeAdapter.getCurrentElem().getPrivate()).hasSize(1);
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TEnumType enumType = new TEnumType();
        enumType.setId("ID");
        dataTemplate.getEnumType().add(enumType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        EnumTypeAdapter enumTypeAdapter = new EnumTypeAdapter(dataTemplateAdapter, enumType);
        // When
        String result = enumTypeAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("EnumType[@id=\"ID\"]");
    }
}