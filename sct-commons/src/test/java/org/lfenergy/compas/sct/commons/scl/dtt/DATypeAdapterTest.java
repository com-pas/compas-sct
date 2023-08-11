// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.SCD_DTT;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.initDttAdapterFromFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DATypeAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetween_between_DataTypeTemplates_and_DAType() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(new TDataTypeTemplates());
        TDAType tdaType = new TDAType();
        //When Then
        assertThatCode(() -> new DATypeAdapter(dataTemplateAdapter, tdaType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetween_DataTypeTemplates_and_DAType_exist() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        dataTemplate.getDAType().add(tdaType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        //When Then
        assertThatCode(() -> new DATypeAdapter(dataTemplateAdapter, tdaType)).doesNotThrowAnyException();
    }

    @Test
    void getBdaAdapters_should_return_list_of_BDAAdapter() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        tdaType.getBDA().add(tbda);
        dataTemplate.getDAType().add(tdaType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        DATypeAdapter daTypeAdapter = new DATypeAdapter(dataTemplateAdapter, tdaType);
        // When Then
        assertThat(daTypeAdapter.getBdaAdapters()).isNotEmpty();
        assertThat(daTypeAdapter.getBdaAdapters().get(0).getClass()).isEqualTo(DATypeAdapter.BDAAdapter.class);
    }

    @Test
    void containsBDAWithEnumTypeID_should_check_if_DAType_contains_BDA_with_specific_EnumType() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        tbda.setBType(TPredefinedBasicTypeEnum.ENUM);
        tbda.setType("enumTypeId");
        tdaType.getBDA().add(tbda);
        dataTemplate.getDAType().add(tdaType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);

        DATypeAdapter daTypeAdapter = new DATypeAdapter(dataTemplateAdapter, tdaType);
        // When Then
        assertThat(daTypeAdapter.containsBDAWithEnumTypeID("enumTypeId")).isTrue();
        assertThat(daTypeAdapter.containsBDAWithEnumTypeID("no_enumTypeId")).isFalse();
    }

    @Test
    void containsStructBdaWithDATypeId_should_check_if_DAType_contains_StructType() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        tbda.setBType(TPredefinedBasicTypeEnum.STRUCT);
        tbda.setType("daTypeId");
        tdaType.getBDA().add(tbda);
        dataTemplate.getDAType().add(tdaType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        DATypeAdapter daTypeAdapter = new DATypeAdapter(dataTemplateAdapter, tdaType);
        // When Then
        assertThat(daTypeAdapter.containsStructBdaWithDATypeId("daTypeId")).isTrue();
        assertThat(daTypeAdapter.containsStructBdaWithDATypeId("no_daTypeId")).isFalse();
    }

    @Test
    @Tag("issue-321")
    void hasSameContentAs() {
        // Given
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        assertThat(rcvDttAdapter.getDATypeAdapters()).hasSizeGreaterThan(1);
        DATypeAdapter rcvDATypeAdapter =  rcvDttAdapter.getDATypeAdapters().get(0);
        // When Then
        assertThat(rcvDATypeAdapter.hasSameContentAs(rcvDATypeAdapter.getCurrentElem())).isTrue();
        // When Then
        assertThat(rcvDATypeAdapter.hasSameContentAs(rcvDttAdapter.getDATypeAdapters().get(1).getCurrentElem())).isFalse();
    }

    @Test
    @Tag("issue-321")
    void testCheckStructuredData() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDATypeAdapterById("DA1").get());
        DaTypeName daTypeName = new DaTypeName("origin","origin.ctlVal");
        // When
        daTypeAdapter.check(daTypeName);
        // Then
        assertThat(daTypeName.getBType()).isEqualTo(TPredefinedBasicTypeEnum.ENUM);
        assertThat(daTypeName.getType()).isEqualTo("RecCycModKind");
        DaTypeName daTypeName1 = new DaTypeName("origin","origin");
        // When Then
        assertThatCode(() -> daTypeAdapter.check(daTypeName1))
                .isInstanceOf(ScdException.class);
        // Given
        DaTypeName daTypeName2 = new DaTypeName("d","check.ctlVal");
        // When Then
        assertThatCode(() -> daTypeAdapter.check(daTypeName2))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void getDataAttributeRefs_should_return_list_of_dataAttribute() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        DataAttributeRef rootDataAttributeRef = new DataAttributeRef();
        rootDataAttributeRef.getDaName().setName("origin");
        rootDataAttributeRef.getDoName().setName("StrVal");
        // When
        List<DataAttributeRef> dataAttributeRefs = daTypeAdapter.getDataAttributeRefs(rootDataAttributeRef, new DataAttributeRef());
        // When
        assertThat(dataAttributeRefs).hasSize(2);
    }

    @Test
    void getDataAttributeRefByDaName_should_not_throw_exception() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        DaTypeName daTypeName = new DaTypeName("antRef","origin.ctlVal");
        DoTypeName doTypeName = new DoTypeName("Op.origin");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDoName(doTypeName);
        dataAttributeRef.getDaName().setName("antRef");
        assertThat(dataAttributeRef.getBdaNames()).isEmpty();
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        // When Then
        assertThatCode(() -> daTypeAdapter.getDataAttributeRefByDaName(daTypeName,0,dataAttributeRef).get())
                .doesNotThrowAnyException();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        dataTemplate.getDAType().add(tdaType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);

        DATypeAdapter daTypeAdapter = new DATypeAdapter(dataTemplateAdapter, tdaType);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(daTypeAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        daTypeAdapter.addPrivate(tPrivate);
        // Then
        assertThat(daTypeAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void elementXPath_should_return_expected_xpath_value()  {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        // When
        String result = daTypeAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DAType[@id=\"DA1\"]");
    }


}
