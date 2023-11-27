// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.dto.DTO.P_DO;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LNodeTypeAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenDataTypeTemplatesAndLNodeType_shouldThrowException() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(new TDataTypeTemplates());
        TLNodeType nodeType = new TLNodeType();
        // When Then
        assertThatCode(() -> new LNodeTypeAdapter(dataTemplateAdapter, nodeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenDataTypeTemplatesAndLNodeType_shouldNotThrowException() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TLNodeType nodeType = new TLNodeType();
        dataTemplate.getLNodeType().add(nodeType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        // When Then
        assertThatCode(() -> new LNodeTypeAdapter(dataTemplateAdapter, nodeType)).doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    void testHasSameContentAs() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TLNodeType tlNodeType = createLNOdeType();
        TLNodeType tlNodeType1 = createLNOdeType();
        dataTemplate.getLNodeType().add(tlNodeType);
        dataTemplate.getLNodeType().add(tlNodeType1);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        LNodeTypeAdapter lNodeTypeAdapter =  new LNodeTypeAdapter(dataTemplateAdapter, tlNodeType);
        assertThat(lNodeTypeAdapter.getLNClass()).isEqualTo(DTO.HOLDER_LN_CLASS);
        assertThat(lNodeTypeAdapter.getDOTypeId("Op")).isPresent();

        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isTrue();
        assertThat(tlNodeType1.getDO()).hasSize(3);
        // Given
        tlNodeType1.getDO().get(2).setAccessControl("AC");
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
        // Given
        tlNodeType1.getDO().get(2).setTransient(true);
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
        // Given
        tlNodeType1.getDO().get(2).setName("NAME");
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
        // Given
        tlNodeType1.getDO().get(2).setType("DO11");
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
        // Given
        TDO tdo = new TDO();
        tdo.setType("DO12");
        tdo.setName("OpPPP");
        tlNodeType1.getDO().add(tdo);
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
        // Given
        tlNodeType1.setIedType("ANOTHER_TYPE");
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
        // Given
        TPrivate aPrivate = new TPrivate();
        aPrivate.setType("TYPE");
        aPrivate.setSource("https://a.com");
        tlNodeType1.getPrivate().add(aPrivate);
        // When Then
        assertThat(lNodeTypeAdapter.hasSameContentAs(tlNodeType1)).isFalse();
    }

    @Test
    void containsDOWithDOTypeId_should_check_if_LNodeType_contains_DO_with_specific_DOType_ID() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDO tdo = new TDO();
        tdo.setType("DO1");
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.getDO().add(tdo);
        dataTemplate.getLNodeType().add(tlNodeType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        LNodeTypeAdapter lNodeTypeAdapter = new LNodeTypeAdapter(dataTemplateAdapter, tlNodeType);
        // When Then
        assertThat(lNodeTypeAdapter.containsDOWithDOTypeId("DO1")).isTrue();
        // When Then
        assertThat(lNodeTypeAdapter.containsDOWithDOTypeId("DO11")).isFalse();
    }


    @Test
    @Tag("issue-321")
    void testGetDataAttributeRefs() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").get());
        DataAttributeRef rootDataAttributeRef = new DataAttributeRef();
        rootDataAttributeRef.setDoName(new DoTypeName("Op"));
        DataAttributeRef filter = new DataAttributeRef();
        filter.setDoName(new DoTypeName("Op.res"));
        // When
        var dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs(filter);
        // Then
        assertThat(dataAttributeRefs).hasSize(2);
        // Given
        filter.setDoName(new DoTypeName("Op.res"));
        filter.setDaName(new DaTypeName("d"));
        // When
        dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs(filter);
        // Then
        assertThat(dataAttributeRefs).hasSize(1);
        // Given
        filter.setDoName(new DoTypeName("Op.res"));
        filter.setDaName(new DaTypeName("antRef"));
        // When Then
        assertThat(lNodeTypeAdapter.getDataAttributeRefs(filter)).isEmpty();
    }

    @Test
    @Tag("issue-321")
    void testGetDataAttributeRefsString() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").get());
        // When
        var dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs("StrVal.origin.origin.ctlVal");
        // Then
        assertThat(dataAttributeRefs).isNotNull();
    }

    @Test
    void getDataAttributeRefs_should_find_DO_SDO_DA_and_BDA() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DO_SDO_DA_BDA);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").orElseThrow());
        // When
        DataAttributeRef dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs("Do1.sdo1.sdo2.da2.bda1.bda2");
        // Then
        assertThat(dataAttributeRefs).extracting(DataAttributeRef::getDoRef, DataAttributeRef::getDaRef)
                .containsExactly("Do1.sdo1.sdo2", "da2.bda1.bda2");
        assertThat(dataAttributeRefs.getDoName().getCdc()).isEqualTo(TPredefinedCDCEnum.WYE);
        assertThat(dataAttributeRefs.getDaName()).extracting(DaTypeName::getBType, DaTypeName::getFc)
                .containsExactly(TPredefinedBasicTypeEnum.ENUM, TFCEnum.ST);
    }

    @Test
    void getDataAttributeRefs_should_find_DO_and_DA() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DO_SDO_DA_BDA);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").orElseThrow());
        // When
        DataAttributeRef dataAttributeRefs = lNodeTypeAdapter.getDataAttributeRefs("Do1.da1");
        // Then
        assertThat(dataAttributeRefs).extracting(DataAttributeRef::getDoRef, DataAttributeRef::getDaRef)
                .containsExactly("Do1", "da1");
        assertThat(dataAttributeRefs.getDoName().getCdc()).isEqualTo(TPredefinedCDCEnum.WYE);
        assertThat(dataAttributeRefs.getDaName()).extracting(DaTypeName::getBType, DaTypeName::getFc)
                .containsExactly(TPredefinedBasicTypeEnum.BOOLEAN, TFCEnum.ST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "malformed", "Do1", "InexistantDo.da1", "Do1.inexistantDa", "Do1.da1.inexistantBda", "Do1.sdo1.inexistantSdo.da2", "Do1.sdo1.sdo2.da2.bda1.inexistantBda"})
    void getDataAttributeRefs_when_dataRef_not_found_should_throw_exception(String dataRef) {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DO_SDO_DA_BDA);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").orElseThrow());
        // When & Then
        assertThatThrownBy(() -> lNodeTypeAdapter.getDataAttributeRefs(dataRef))
                .isInstanceOf(ScdException.class);
    }

    @Test
    @Tag("issue-321")
    void testCheck() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").get());
        DoTypeName doTypeName1 = new DoTypeName("");
        DaTypeName daTypeName1 = new DaTypeName("");
        // When Then
        assertThatThrownBy(() -> lNodeTypeAdapter.checkDoAndDaTypeName(doTypeName1,daTypeName1)).isInstanceOf(ScdException.class);
        DoTypeName doTypeName2 = new DoTypeName("do");
        DaTypeName daTypeName2 = new DaTypeName("");
        // When Then
        assertThatThrownBy(() -> lNodeTypeAdapter.checkDoAndDaTypeName(doTypeName2,daTypeName2)).isInstanceOf(ScdException.class);
        DoTypeName doTypeName3 = new DoTypeName("do");
        DaTypeName daTypeName3 = new DaTypeName("da");
        // When Then
        assertThatThrownBy(() -> lNodeTypeAdapter.checkDoAndDaTypeName(doTypeName3,daTypeName3)).isInstanceOf(ScdException.class);
        DoTypeName doTypeName = new DoTypeName("Op.res");
        DaTypeName daTypeName = new DaTypeName("d");
        // When Then
        assertThatCode(() -> lNodeTypeAdapter.checkDoAndDaTypeName(doTypeName,daTypeName)).doesNotThrowAnyException();
        doTypeName.setName("StrVal");
        doTypeName.getStructNames().clear();
        daTypeName.setName("origin");
        daTypeName.getStructNames().clear();
        daTypeName.setStructNames(List.of("origin","ctlVal"));
        // When Then
        assertThatCode(() -> lNodeTypeAdapter.checkDoAndDaTypeName(doTypeName,daTypeName)).doesNotThrowAnyException();

    }

    @Test
    void getDataAttributeRefByDaName_should_return_list_of_DataAttributeRef() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        DaTypeName daTypeName = new DaTypeName("antRef","origin.ctlVal");
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").get());
        // When
        List<DataAttributeRef> dataAttributeRefs = assertDoesNotThrow(()-> lNodeTypeAdapter.getDataAttributeRefByDaName(daTypeName));
        // Then
        assertThat(dataAttributeRefs).hasSize(2);
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        DataTypeTemplateAdapter dataTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TLNodeType nodeType = new TLNodeType();
        dataTemplate.getLNodeType().add(nodeType);
        when(dataTemplateAdapter.getCurrentElem()).thenReturn(dataTemplate);
        LNodeTypeAdapter lNodeTypeAdapter = new LNodeTypeAdapter(dataTemplateAdapter, nodeType);
        assertThat(lNodeTypeAdapter.getCurrentElem().getPrivate()).isEmpty();

        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        // When
        lNodeTypeAdapter.addPrivate(tPrivate);
        // Then
        assertThat(lNodeTypeAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }


    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getLNodeTypeAdapterById("LN1").get());
        // When
        String result = lNodeTypeAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("LNodeType[@id=\"LN1\" and @lnClass=\"PIOC\"]");
    }

    @Test
    @Tag("issue-321")
    void findMatchingDOType_shouldFindOneDO() {
        // Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        DataTypeTemplateAdapter dttAdapter = assertDoesNotThrow(sclRootAdapter::getDataTypeTemplateAdapter);

        TDO tdo= new TDO();
        tdo.setName("P_DO");
        tdo.setType("DO1");

        TDOType tdoType = new TDOType();
        tdoType.setId("DO1");

        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        tlNodeType.getDO().add(tdo);
        dttAdapter.getCurrentElem().getDOType().add(tdoType);

        dttAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("ID").get());

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPDO("P_DO12");
        //When
        DataTypeTemplateAdapter.DOTypeInfo expectedDoTypeInfo = assertDoesNotThrow(() -> lNodeTypeAdapter.findMatchingDOType(signalInfo));
        //Then
        assertThat(expectedDoTypeInfo.getDoTypeId()).isEqualTo("DO1");
        assertThat(expectedDoTypeInfo.getDoTypeName().getName()).isEqualTo("P_DO12");
        assertThat(expectedDoTypeInfo.getDoTypeAdapter()).isNotNull();
    }

    @Test
    @Tag("issue-321")
    void checkMatchingDOType_whenDOUnknown_shouldThrowException() {
        //Given
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        //When Then
        DataTypeTemplateAdapter dttAdapter = assertDoesNotThrow(sclRootAdapter::getDataTypeTemplateAdapter);

        //Given
        TDO tdo= new TDO();
        tdo.setName(P_DO);
        tdo.setType("DO1");

        TDOType tdoType = new TDOType();
        tdoType.setId("DO1");

        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        tlNodeType.getDO().add(tdo);
        dttAdapter.getCurrentElem().getDOType().add(tdoType);

        dttAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("ID").get());
        //When Then
        assertThatThrownBy(() -> lNodeTypeAdapter.findMatchingDOType(signalInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown doName :"+P_DO);
    }

    @Test
    @Tag("issue-321")
    void checkMatchingDOType_whenDONotReferenced_shouldThrowException() {
        //Given
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPDO("P_DO");

        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        //When Then
        DataTypeTemplateAdapter dttAdapter = assertDoesNotThrow(sclRootAdapter::getDataTypeTemplateAdapter);

        //Given
        TDO tdo= new TDO();
        tdo.setName("P_DO");
        tdo.setType("DO2");

        TDOType tdoType = new TDOType();
        tdoType.setId("DO1");

        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        tlNodeType.getDO().add(tdo);
        dttAdapter.getCurrentElem().getDOType().add(tdoType);

        dttAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("ID").get());
        //When Then
        assertThatThrownBy(() -> lNodeTypeAdapter.findMatchingDOType(signalInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("P_DO: No referenced to DO id : DO2, scl file not valid");
    }


    private TLNodeType createLNOdeType(){
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("BDA1");
        tlNodeType.setIedType("IEDTYPE");
        tlNodeType.getLnClass().add(DTO.HOLDER_LN_CLASS);
        TDO tdo = new TDO();
        tdo.setType("DO1");
        tdo.setName("Op");
        TDO tdo1 = new TDO();
        tdo1.setType("DO2");
        tdo1.setName("Beh");
        TDO tdo2 = new TDO();
        tdo2.setType("DO3");
        tdo2.setName("StrVal");
        tlNodeType.getDO().addAll(List.of(tdo,tdo1,tdo2));
        return tlNodeType;
    }


}
