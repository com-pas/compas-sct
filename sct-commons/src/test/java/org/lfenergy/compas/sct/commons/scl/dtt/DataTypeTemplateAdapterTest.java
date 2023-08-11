// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataTypeTemplateAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenSCLAndDataTypeTemplates_shouldThrowException() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        // When Then
        assertThatCode(() -> new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenSCLAndDataTypeTemplates_shouldNotThrowException() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        // When Then
        assertThatCode(() -> new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate)).doesNotThrowAnyException();
    }


    @Test
    @Tag("issue-321")
    void testGetLNodeTypeAdapterById() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        dataTemplate.getLNodeType().add(tlNodeType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);

        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        // When
        Optional<LNodeTypeAdapter> lNodeTypeAdapter1 = dataTypeTemplateAdapter.getLNodeTypeAdapterById("ID");
        // Then
        assertThat(lNodeTypeAdapter1).isPresent();
        // When
        Optional<LNodeTypeAdapter> lNodeTypeAdapter2 =  dataTypeTemplateAdapter.getLNodeTypeAdapterById("UNKNOWN_ID");
        // Then
        assertThat(lNodeTypeAdapter2).isEmpty();
    }

    @Test
    void getLNodeTypeAdapters_should_return_list_of_LNodeTypeAdapter() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        dataTemplate.getLNodeType().add(tlNodeType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        // When Then
        assertThat(dataTypeTemplateAdapter.getLNodeTypeAdapters()).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void testGetDOTypeAdapterById() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        tdoType.setId("ID");
        dataTemplate.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);

        // When
        Optional<DOTypeAdapter> doTypeAdapter1 = dataTypeTemplateAdapter.getDOTypeAdapterById("ID");
        // Then
        assertThat(doTypeAdapter1).isPresent();
        // When
        Optional<DOTypeAdapter> doTypeAdapter2 = dataTypeTemplateAdapter.getDOTypeAdapterById("UNKNOWN_ID");
        // Then
        assertThat(doTypeAdapter2).isEmpty();
    }

    @Test
    void getDOTypeAdapters_should_return_list_of_DOTypeAdapter() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDOType tdoType = new TDOType();
        tdoType.setId("ID");
        dataTemplate.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        // When Then
        assertThat(dataTypeTemplateAdapter.getDOTypeAdapters()).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void testGetDATypeAdapterById() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        tdaType.setId("ID");
        dataTemplate.getDAType().add(tdaType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);

        // When
        Optional<DATypeAdapter> daTypeAdapter1 = dataTypeTemplateAdapter.getDATypeAdapterById("ID");
        // Then
        assertThat(daTypeAdapter1).isPresent();
        // When
        Optional<DATypeAdapter> daTypeAdapter2 = dataTypeTemplateAdapter.getDATypeAdapterById("UNKNOWN_ID");
        // Then
        assertThat(daTypeAdapter2).isEmpty();
    }

    @Test
    void getDATypeAdapters_should_return_list_of_DATypeAdapter() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TDAType tdaType = new TDAType();
        tdaType.setId("ID");
        dataTemplate.getDAType().add(tdaType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        // When Then
        assertThat(dataTypeTemplateAdapter.getDATypeAdapters()).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void testGetEnumTypeAdapterById() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TEnumType tEnumType = new TEnumType();
        tEnumType.setId("ID");
        dataTemplate.getEnumType().add(tEnumType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);

        // When
        Optional<EnumTypeAdapter> enumTypeAdapter1 = dataTypeTemplateAdapter.getEnumTypeAdapterById("ID");
        // Then
        assertThat(enumTypeAdapter1).isPresent();
        // When
        Optional<EnumTypeAdapter> enumTypeAdapter2 = dataTypeTemplateAdapter.getEnumTypeAdapterById("UNKNOWN_ID");
        // Then
        assertThat(enumTypeAdapter2).isEmpty();
    }

    @Test
    void getEnumTypeAdapters_should_return_list_of_EnumTypeAdapter() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        TEnumType tEnumType = new TEnumType();
        tEnumType.setId("ID");
        dataTemplate.getEnumType().add(tEnumType);
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        // When Then
        assertThat(dataTypeTemplateAdapter.getEnumTypeAdapters()).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void testHasSameID() {
        // Given
        TDAType tdaType1 = new TDAType();
        tdaType1.setId("SAME_ID");
        TDAType tdaType2 = new TDAType();
        tdaType2.setId("SAME_ID");
        // When Then
        assertThat(DataTypeTemplateAdapter.hasSameID(tdaType1,tdaType2)).isTrue();
        // Given
        tdaType2.setId("ANOTHER_ID");
        // When Then
        assertThat(DataTypeTemplateAdapter.hasSameID(tdaType1,tdaType2)).isFalse();
    }

    @Test
    @Tag("issue-321")
    void testHasSamePrivates() {
        // Given
        TDAType tdaType1 = new TDAType();
        TPrivate aPrivate1 = new TPrivate();
        aPrivate1.setType("A_PRIVATE1");
        aPrivate1.setSource("A_URI_1");
        tdaType1.getPrivate().add(aPrivate1);
        TDAType tdaType2 = new TDAType();
        // When Then
        assertThat(DataTypeTemplateAdapter.hasSamePrivates(tdaType1,tdaType2)).isFalse();
        // Given
        TPrivate aPrivate2 = new TPrivate();
        aPrivate2.setType("A_PRIVATE1");
        aPrivate2.setSource("A_URI_2");
        tdaType2.getPrivate().add(aPrivate2);
        // When Then
        assertThat(DataTypeTemplateAdapter.hasSamePrivates(tdaType1,tdaType2)).isFalse();
        // Given
        aPrivate2.setSource("A_URI_1");
        // When Then
        assertThat(DataTypeTemplateAdapter.hasSamePrivates(tdaType1,tdaType2)).isTrue();

    }

    @Test
    void findDATypesWhichBdaContainsEnumTypeId_when_BDA_containing_specified_EnumTypeID_should_return_DATypeAdapters() {
        // Given
        String enumTypeId = "RecCycModKind";
        DataTypeTemplateAdapter dataTypeTemplateAdapter = initDttAdapterFromFile(SCD_DTT);
        // When
        List<DATypeAdapter> daTypeAdapters = dataTypeTemplateAdapter.findDATypesWhichBdaContainsEnumTypeId(enumTypeId);
        // Then
        assertThat(daTypeAdapters).hasSize(1);
    }

    @Test
    @Tag("issue-321")
    void testFindDOTypesWhichDAContainsEnumTypeId() {
        // Given
        String enumTypeId = "PhaseAngleReferenceKind";
        DataTypeTemplateAdapter dataTypeTemplateAdapter = initDttAdapterFromFile(SCD_DTT);
        // When
        List<DOTypeAdapter> doTypeAdapters = dataTypeTemplateAdapter.findDOTypesWhichDAContainsEnumTypeId(enumTypeId);
        // Then
        assertThat(doTypeAdapters).hasSize(1);
        // Given
        enumTypeId = "RecCycModKind";
        // When
        doTypeAdapters = dataTypeTemplateAdapter.findDOTypesWhichDAContainsEnumTypeId(enumTypeId);
        // Then
        assertThat(doTypeAdapters).isEmpty();
    }

    @Test
    @Tag("issue-321")
    void testFindDATypesFromStructBdaWithDATypeId() {
        // Given
        DataTypeTemplateAdapter dataTypeTemplateAdapter = initDttAdapterFromFile(SCD_DTT);
        String daTypeId = "DA2";
        // When
        List<DATypeAdapter> daTypeAdapters = dataTypeTemplateAdapter.findDATypesFromStructBdaWithDATypeId(daTypeId);
        // Then
        assertThat(daTypeAdapters).hasSize(1);
        // Given
        daTypeId = "DA1";
        // When
        daTypeAdapters = dataTypeTemplateAdapter.findDATypesFromStructBdaWithDATypeId(daTypeId);
        // Then
        assertThat(daTypeAdapters).isEmpty();
    }

    @Test
    void findDOTypesWhichDAContainsStructWithDATypeId_when_DA_containing_specific_StructDaTypeID_should_return_DOTypeAdapters() {
        // Given
        DataTypeTemplateAdapter dataTypeTemplateAdapter = initDttAdapterFromFile(SCD_DTT);
        String daTypeId = "DA1";
        // When
        List<DOTypeAdapter> doTypeAdapters = dataTypeTemplateAdapter.findDOTypesWhichDAContainsStructWithDATypeId(daTypeId);
        // Then
        assertThat(doTypeAdapters).hasSize(1);
    }

    @Test
    void retrieveSdoOrDO_shouldReturnEmptyList() {
        //Given
        TSDO tsdo = new TSDO();
        TBDA tbda = new TBDA();
        TDA tda = new TDA();
        List<TUnNaming> list = Arrays.asList(tsdo,tbda,tda);
        //When Then
        assertThat(DataTypeTemplateAdapter.retrieveSdoOrDA(list, TDO.class)).isEmpty();
    }

    @Test
    void retrieveSdoOrDO_shouldReturnListWithTwoElements() {
        //Given
        TSDO tsdo = new TSDO();
        TDO tdo = new TDO();
        TBDA tbda1 = new TBDA();
        TBDA tbda2 = new TBDA();
        TDA tda = new TDA();
        List<TUnNaming> list = Arrays.asList(tsdo,tdo,tbda1,tbda2,tda);
        //When Then
        assertThat(DataTypeTemplateAdapter.retrieveSdoOrDA(list, TBDA.class))
                .hasSize(2)
                .hasOnlyElementsOfType(TBDA.class);
    }

    @Test
    @Tag("issue-321")
    void testFindDOTypesFromSDOWithDOTypeId() {
        // Given
        DataTypeTemplateAdapter dataTypeTemplateAdapter = initDttAdapterFromFile(SCD_DTT);
        String doTypeId = "DO4";
        // When
        List<DOTypeAdapter> doTypeList = dataTypeTemplateAdapter.findDOTypesFromSDOWithDOTypeId(doTypeId);
        // Then
        assertThat(doTypeList).hasSize(1);
        // Given
        doTypeId = "UnknownDOID";
        // When
        doTypeList = dataTypeTemplateAdapter.findDOTypesFromSDOWithDOTypeId(doTypeId);
        // Then
        assertThat(doTypeList).isEmpty();
    }

    @Test
    @Tag("issue-321")
    void testFindLNodeTypesFromDoWithDoTypeId() {
        // Given
        DataTypeTemplateAdapter dataTypeTemplateAdapter = initDttAdapterFromFile(SCD_DTT);
        String doTypeId = "DO1";
        // When
        List<LNodeTypeAdapter> lNodeTypeAdapters = dataTypeTemplateAdapter.findLNodeTypesFromDoWithDoTypeId(doTypeId);
        // Then
        assertThat(lNodeTypeAdapters).hasSize(1);
        // Given
        doTypeId = "UnknownDOID";
        // When
        lNodeTypeAdapters = dataTypeTemplateAdapter.findLNodeTypesFromDoWithDoTypeId(doTypeId);
        // Then
        assertThat(lNodeTypeAdapters).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"IED_NAME, DTT_ID, IED_NAME_DTT_ID",
            "IED_NAME, Z6A2chUEHc7a15MvIUbQTVvioCgzOcWlNMfOzNbfjLJaueNf9T2GmQP7ShgYFr3SfYex5HdwvC5tRr9oAp0lmSwtqxx1cHEKL" +
                    "MgKX7hZuUWCpKYPJ3I1fmE7NVIvVOtB1JsIOSGclfQfLGDEFjFG7vIozpkijZ0ugtZSOZuCavC5v5JL58yHO1RWCpYVdMDp4Jh" +
                    "ChU4YjhAhVGbOykJi0b4pc0saXoqf0q5imWmXiiuMuq0sc25IVA2v0TmCSxJ, " +
                    "IED_NAME_Z6A2chUEHc7a15MvIUbQTVvioCgzOcWlNMfOzNbfjLJaueNf9T2GmQP7ShgYFr3SfYex5HdwvC5tRr9oAp0lmSwtqx" +
                    "x1cHEKLMgKX7hZuUWCpKYPJ3I1fmE7NVIvVOtB1JsIOSGclfQfLGDEFjFG7vIozpkijZ0ugtZSOZuCavC5v5JL58yHO1RWCpYVdM" +
                    "Dp4JhChU4YjhAhVGbOykJi0b4pc0saXoqf0q5imWmXiiuMuq0sc25IVA"})
    void generateDttId_whenBothLessThan255_shouldReturnIEdNameWithDTTId(String iedName, String dttId, String newDTTId) {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        // When Then
        assertThat(dataTypeTemplateAdapter.generateDttId(iedName, dttId)).hasSizeLessThan(256)
                .isEqualTo(newDTTId);

    }

    @Test
    @Tag("issue-321")
    void importEnumTypes_whenDifferentContent_shouldAddNewEnum() {
        //Given
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        DataTypeTemplateAdapter prvDttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        Optional<EnumTypeAdapter> enumTypeAdapter = rcvDttAdapter.getEnumTypeAdapterById("PhaseAngleReferenceKind");
        int rcvDTTEnumValsSize = enumTypeAdapter.get().getCurrentElem().getEnumVal().size();
        //When
        rcvDttAdapter.importEnumType("IEDName",prvDttAdapter);
        //When
        Optional<EnumTypeAdapter> rcvEnumTypeAdapter = rcvDttAdapter.getEnumTypeAdapterById("PhaseAngleReferenceKind");
        //Then
        assertThat(rcvDttAdapter.getEnumTypeAdapters())
                .hasSize(rcvDTTEnumValsSize + prvDttAdapter.getEnumTypeAdapters().size());
        assertThat(rcvEnumTypeAdapter.get().getCurrentElem().getEnumVal()).hasSize(rcvDTTEnumValsSize);
    }

    @Test
    void importEnumTypes_whenSameContent_shouldUpdateExistingEnum() {
        //Given
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        DataTypeTemplateAdapter prvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        //When
        rcvDttAdapter.importEnumType("IEDName",prvDttAdapter);
        //Then
        assertThat(rcvDttAdapter.getEnumTypeAdapters()).hasSize(prvDttAdapter.getEnumTypeAdapters().size());
    }

    @Test
    @Tag("issue-321")
    void testImportDTT() {
        // Given
        DataTypeTemplateAdapter prvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        int nbLNodeType = rcvDttAdapter.getLNodeTypeAdapters().size();
        // When
        rcvDttAdapter.importDTT("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getLNodeTypeAdapters()).hasSize(nbLNodeType);
        // Given
        prvDttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        // When
        var mapOldNewId =rcvDttAdapter.importDTT("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getLNodeTypeAdapters()).hasSizeGreaterThan(nbLNodeType);
        assertThat(mapOldNewId).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void testImportLNodeType() {
        // Given
        DataTypeTemplateAdapter prvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        int nbLNodeType = rcvDttAdapter.getLNodeTypeAdapters().size();
        // When
        rcvDttAdapter.importLNodeType("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getLNodeTypeAdapters()).hasSize(nbLNodeType);
        // Given
        prvDttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        // When
        var mapOldNewId = rcvDttAdapter.importLNodeType("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getLNodeTypeAdapters()).hasSizeGreaterThan(nbLNodeType);
        assertThat(mapOldNewId).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void testImportDOType() {
        // Given
        DataTypeTemplateAdapter prvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        int nbDOType = rcvDttAdapter.getDOTypeAdapters().size();
        // When
        rcvDttAdapter.importDOType("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getDOTypeAdapters()).hasSize(nbDOType);
        // Given
        prvDttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        // When
        rcvDttAdapter.importDOType("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getDOTypeAdapters()).hasSizeGreaterThan(nbDOType);
    }

    @Test
    @Tag("issue-321")
    void testImportDAType() {
        // Given
        DataTypeTemplateAdapter rcvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        DataTypeTemplateAdapter prvDttAdapter = initDttAdapterFromFile(SCD_DTT);
        int nbDAType = rcvDttAdapter.getDATypeAdapters().size();
        // When
        rcvDttAdapter.importDAType("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getDATypeAdapters()).hasSize(nbDAType);
        // Given
        prvDttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        // When
        rcvDttAdapter.importDAType("IEDName",prvDttAdapter);
        // Then
        assertThat(rcvDttAdapter.getDATypeAdapters()).hasSizeGreaterThan(nbDAType);
    }

    @ParameterizedTest
    @CsvSource({"A,LN1,No coherence or path between DOType(DO2) and DA(A)",
            "antRef,LN1,Invalid ExtRef signal: no coherence between pDO(Op.origin) and pDA(antRef)",
            "antRef.origin.ctlVal,LN_Type1,Unknown LNodeType:LN_Type1"})
    void getBinderDataAttributeRef_whenDONotContainDA_shouldThrowScdException(String pDA, String lnType, String message) throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA(pDA);
        //When Then
        assertThatThrownBy(() -> dttAdapter.getBinderDataAttribute(lnType,signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage(message);
    }

    @Test
    void getBinderDataAttributeRef_whenLnClassNotMatches_shouldThrowScdException() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById("LN1").get();
        lNodeTypeAdapter.getCurrentElem().unsetLnClass();

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("CSWI");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA("antRef.origin.ctlVal");
        //When Then
        assertThatThrownBy(() -> dttAdapter.getBinderDataAttribute("LN1",signalInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lnClass is mandatory for LNodeType in DataTemplate : LN1");
    }

    @Test
    void getBinderDataAttributeRef_whenDOIdNotFound_shouldThrowScdException() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Do");
        //When Then
        assertThatThrownBy(() -> dttAdapter.getBinderDataAttribute("LN1",signalInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown doName :Do");
    }

    @Test
    void getBinderDataAttributeRef_whenSignalPDOEmpty_shouldReturnBindingInfoWithoutDO() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO(null);
        //When Then
        ExtRefBindingInfo bindingInfo = assertDoesNotThrow(() -> dttAdapter.getBinderDataAttribute("LN1",signalInfo));
        assertThat(bindingInfo)
                .extracting("lnType", "lnClass")
                .containsExactlyInAnyOrder("LN1", "PIOC");
        assertThat(bindingInfo.getDoName()).isNull();
    }

    @Test
    void getBinderDataAttributeRef_whenSignalPDAEmpty_shouldReturnBindingInfoWithoutDA() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA(null);
        //When Then
        ExtRefBindingInfo bindingInfo = assertDoesNotThrow(() -> dttAdapter.getBinderDataAttribute("LN1",signalInfo));
        assertThat(bindingInfo)
                .extracting("lnType", "lnClass")
                .containsExactlyInAnyOrder("LN1", "PIOC");
        assertThat(bindingInfo.getDoName()).isNotNull();
        assertThat(bindingInfo.getDaName()).isNull();
    }

    @Test
    void getBinderDataAttributeRef_whenExist_shouldReturnBindingInfo() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA("antRef.origin.ctlVal");
        //When Then
        ExtRefBindingInfo bindingInfo = assertDoesNotThrow(() -> dttAdapter.getBinderDataAttribute("LN1",signalInfo));
        assertThat(bindingInfo)
                .extracting("lnType", "lnClass")
                .containsExactlyInAnyOrder("LN1", "PIOC");
    }

    @Test
    void addPrivate_should_throw_exception() {
        // Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        SCL scl = new SCL();
        TDataTypeTemplates dataTemplate = new TDataTypeTemplates();
        scl.setDataTypeTemplates(dataTemplate);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);

        DataTypeTemplateAdapter dttAdapter = new DataTypeTemplateAdapter(sclRootAdapter, dataTemplate);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        //When Then
        assertThatCode(() -> dttAdapter.addPrivate(tPrivate))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void elementXPath_should_return_expected_xpath_value()  {
        // Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT_DIFF_CONTENT_SAME_ID);
        // When
        String result = dttAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DataTypeTemplates");
    }

}
