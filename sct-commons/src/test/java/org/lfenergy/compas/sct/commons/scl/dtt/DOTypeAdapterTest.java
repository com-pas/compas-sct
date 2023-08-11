// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DOTypeAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenDataTypeTemplatesAndDOType_shouldThrowException() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(new TDataTypeTemplates());
        TDOType tdoType = new TDOType();
        //When Then
        assertThatCode(() -> new DOTypeAdapter(dataTemplateAdapter, tdoType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenDataTypeTemplatesAndDOType_shouldNotThrowException() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        dataTemplate.getDOType().add(tdoType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        //When Then
        assertThatCode(() -> new DOTypeAdapter(dataTemplateAdapter, tdoType)).doesNotThrowAnyException();
    }

    @Test
    void containsDAWithEnumTypeId_should_check_if_DOType_contains_DA_with_specific_EnumType() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        TDA tda = new TDA();
        tda.setType("ID_BDA");
        tda.setBType(TPredefinedBasicTypeEnum.ENUM);
        tda.setType("enumTypeId");
        tdoType.getSDOOrDA().add(tda);
        dataTemplate.getDOType().add(tdoType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);

        DOTypeAdapter doTypeAdapter = new DOTypeAdapter(dataTemplateAdapter, tdoType);
        //When Then
        assertThat(doTypeAdapter.containsDAWithEnumTypeId("enumTypeId")).isTrue();
        //When Then
        assertThat(doTypeAdapter.containsDAWithEnumTypeId("no_enumTypeId")).isFalse();
    }

    @Test
    @Tag("issue-321")
    void containsDAStructWithDATypeId_should_check_if_DOType_contains_StructType() {
        //Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        TDA tda = new TDA();
        tda.setType("ID_BDA");
        tda.setBType(TPredefinedBasicTypeEnum.STRUCT);
        tda.setType("daTypeId");
        tdoType.getSDOOrDA().add(tda);
        dataTemplate.getDOType().add(tdoType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);

        DOTypeAdapter doTypeAdapter = new DOTypeAdapter(dataTemplateAdapter, tdoType);
        //When Then
        assertThat(doTypeAdapter.containsDAStructWithDATypeId("daTypeId")).isTrue();
        //When Then
        assertThat(doTypeAdapter.containsDAStructWithDATypeId("no_daTypeId")).isFalse();
    }

    @Test
    @Tag("issue-321")
    void hasSameContentAs() {
        //Given
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        assertThat(rcvDttAdapter.getDATypeAdapters()).hasSizeGreaterThan(1);
        DOTypeAdapter rcvDOTypeAdapter = rcvDttAdapter.getDOTypeAdapters().get(0);
        //When Then
        assertThat(rcvDOTypeAdapter.hasSameContentAs(rcvDOTypeAdapter.getCurrentElem())).isTrue();
        //When Then
        assertThat(rcvDOTypeAdapter.hasSameContentAs(rcvDttAdapter.getDOTypeAdapters().get(1).getCurrentElem())).isFalse();
    }

    @Test
    @Tag("issue-321")
    void testCheckAndCompleteStructData() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        //When Then
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDOTypeAdapterById("DO2").get());
        DoTypeName doTypeName = new DoTypeName("Op","origin");
        //When Then
        assertThatCode(() -> doTypeAdapter.checkAndCompleteStructData(doTypeName)).doesNotThrowAnyException();
        assertThat(doTypeName.getCdc()).isEqualTo(TPredefinedCDCEnum.WYE);
        DoTypeName doTypeName1 = new DoTypeName("Op","toto");
        //When Then
        assertThatThrownBy(() -> doTypeAdapter.checkAndCompleteStructData(doTypeName1)).isInstanceOf(ScdException.class);
    }

    @Test
    @Tag("issue-321")
    void testGetDataAttributeRefs_filter_on_DO() {
        // given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        // when then
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDOTypeAdapterById("DO2").get());
        // then
        DataAttributeRef rootDataAttributeRef = new DataAttributeRef();
        rootDataAttributeRef.setDoName(new DoTypeName("Op"));
        DataAttributeRef filter = new DataAttributeRef();
        filter.setDoName(new DoTypeName("Op.res"));

        // when
        List<DataAttributeRef> dataAttributeRefs = doTypeAdapter.getDataAttributeRefs(rootDataAttributeRef, filter);

        // then
        assertThat(dataAttributeRefs).hasSize(2);
    }

    @Test
    void testGetDataAttributeRefs_filter_on_DO_and_DA() {
        // given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        // when then
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDOTypeAdapterById("DO2").get());
        // given
        DataAttributeRef rootDataAttributeRef = new DataAttributeRef();
        rootDataAttributeRef.setDoName(new DoTypeName("Op"));
        DataAttributeRef filter = new DataAttributeRef();
        filter.setDoName(new DoTypeName("Op.res"));
        filter.setDaName(new DaTypeName("d"));
        // when
        List<DataAttributeRef> dataAttributeRefs = doTypeAdapter.getDataAttributeRefs(rootDataAttributeRef, filter);

        // then
        assertThat(dataAttributeRefs).hasSize(1);
    }

    @Test
    @Tag("issue-321")
    void testFindPathSDO2DA() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDOTypeAdapterById("DO1").get());
        // When Then
        assertThatCode(() -> doTypeAdapter.findPathSDOToDA("origin","unknown"))
                .isInstanceOf(ScdException.class);
        // When Then
        assertThatCode(() ->  doTypeAdapter.findPathSDOToDA("unknown","antRef"))
                .isInstanceOf(ScdException.class);
        // When Then
        var pair = assertDoesNotThrow(
                () -> doTypeAdapter.findPathSDOToDA("origin","antRef")
        );
        assertThat(pair.getKey()).isEqualTo("d");
        DOTypeAdapter lastDoTypeAdapter = pair.getValue();
        assertThat(lastDoTypeAdapter.getCurrentElem().getId()).isEqualTo("DO3");
    }

    @Test
    void getDataAttributeRefByDaName_should_not_throw_exception() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        DaTypeName daTypeName = new DaTypeName("antRef","origin.ctlVal");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDoName(new DoTypeName("Op"));
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDOTypeAdapterById("DO2").get());
        // When Then
        assertThatCode(() -> doTypeAdapter.getDataAttributeRefByDaName(daTypeName, dataAttributeRef)).doesNotThrowAnyException();
        assertThat(dataAttributeRef.getDoName()).hasToString("Op.origin");
        assertThat(dataAttributeRef.getDaName()).hasToString("antRef.origin.ctlVal");
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        dataTemplate.getDOType().add(tdoType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        DOTypeAdapter doTypeAdapter = new DOTypeAdapter(dataTemplateAdapter, tdoType);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(doTypeAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        doTypeAdapter.addPrivate(tPrivate);
        // Then
        assertThat(doTypeAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        tdoType.setId("DO1");
        dataTemplate.getDOType().add(tdoType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        DOTypeAdapter doTypeAdapter = new DOTypeAdapter(dataTemplateAdapter, tdoType);
        // When
        String result = doTypeAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DOType[@id=\"DO1\"]");
    }

    @ParameterizedTest
    @CsvSource({"angRef,CF,PhaseAngleReferenceKind", "antRef,ST,DA1"})
    void getDAByName_should_not_throw_exception(String daName, String fc, String type) {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getDOTypeAdapterById("DO2").get());
        // When
        TDA result = assertDoesNotThrow(() -> doTypeAdapter.getDAByName(daName).get());
        // Then
        assertThat(result).isNotNull()
                .extracting(TDA::getName, TDA::getFc, TDA::getType)
                .containsExactlyInAnyOrder(daName,TFCEnum.fromValue(fc),type);
    }

}
