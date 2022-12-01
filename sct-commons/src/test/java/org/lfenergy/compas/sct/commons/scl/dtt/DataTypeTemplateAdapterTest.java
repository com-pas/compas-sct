// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class DataTypeTemplateAdapterTest {



    DataTypeTemplateAdapter dataTypeTemplateAdapter;

    protected void init(){
        SclRootAdapter sclRootAdapter = null;
        try {
            sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        } catch (ScdException e) {
            e.printStackTrace();
        }
        assert sclRootAdapter != null;
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        SclRootAdapter finalSclRootAdapter = sclRootAdapter;
        dataTypeTemplateAdapter = assertDoesNotThrow(
                finalSclRootAdapter::getDataTypeTemplateAdapter
        );
    }

    @BeforeEach
    public void setUp(){
        init();
    }
    @Test
    void testAmChildElementRef() {
        SclRootAdapter sclRootAdapter = dataTypeTemplateAdapter.getParentAdapter();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        assertThatThrownBy(() -> new DataTypeTemplateAdapter(sclRootAdapter, dtt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetLNodeTypeAdapterById() {
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        Optional<LNodeTypeAdapter> lNodeTypeAdapter =
                dataTypeTemplateAdapter.getLNodeTypeAdapterById("ID");
        assertTrue(lNodeTypeAdapter.isPresent());

        lNodeTypeAdapter =  dataTypeTemplateAdapter.getLNodeTypeAdapterById("UNKNOWN_ID");
        assertTrue(lNodeTypeAdapter.isEmpty());
    }

    @Test
    void testGetLNodeTypeAdapters() {
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        assertFalse(dataTypeTemplateAdapter.getLNodeTypeAdapters().isEmpty());
    }

    @Test
    void testGetDOTypeAdapterById() {
        TDOType tdoType = new TDOType();
        tdoType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getDOType().add(tdoType);
        Optional<DOTypeAdapter> doTypeAdapter =
                dataTypeTemplateAdapter.getDOTypeAdapterById("ID");
        assertTrue(doTypeAdapter.isPresent());

        doTypeAdapter =  dataTypeTemplateAdapter.getDOTypeAdapterById("UNKNOWN_ID");
        assertTrue(doTypeAdapter.isEmpty());
    }

    @Test
    void testGetDOTypeAdapters() {
        TDOType tdoType = new TDOType();
        tdoType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getDOType().add(tdoType);
        assertFalse(dataTypeTemplateAdapter.getDOTypeAdapters().isEmpty());
    }

    @Test
    void testGetDATypeAdapterById() {
        TDAType tdaType = new TDAType();
        tdaType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getDAType().add(tdaType);
        Optional<DATypeAdapter> daTypeAdapter =
                dataTypeTemplateAdapter.getDATypeAdapterById("ID");
        assertTrue(daTypeAdapter.isPresent());

        daTypeAdapter =  dataTypeTemplateAdapter.getDATypeAdapterById("UNKNOWN_ID");
        assertTrue(daTypeAdapter.isEmpty());
    }

    @Test
    void testGetDATypeAdapters() {
        TDAType tdaType = new TDAType();
        tdaType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getDAType().add(tdaType);
        assertFalse(dataTypeTemplateAdapter.getDATypeAdapters().isEmpty());
    }

    @Test
    void testGetEnumTypeAdapterById() {
        TEnumType tEnumType = new TEnumType();
        tEnumType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getEnumType().add(tEnumType);
        Optional<EnumTypeAdapter> enumTypeAdapter =
                dataTypeTemplateAdapter.getEnumTypeAdapterById("ID");
        assertTrue(enumTypeAdapter.isPresent());

        enumTypeAdapter =  dataTypeTemplateAdapter.getEnumTypeAdapterById("UNKNOWN_ID");
        assertTrue(enumTypeAdapter.isEmpty());
    }

    @Test
    void testGetEnumTypeAdapters() {
        TEnumType tEnumType = new TEnumType();
        tEnumType.setId("ID");
        dataTypeTemplateAdapter.getCurrentElem().getEnumType().add(tEnumType);
        assertFalse(dataTypeTemplateAdapter.getEnumTypeAdapters().isEmpty());
    }

    @Test
    void testHasSameID() {
        TDAType tdaType1 = new TDAType();
        tdaType1.setId("SAME_ID");

        TDAType tdaType2 = new TDAType();
        tdaType2.setId("SAME_ID");

        assertTrue(DataTypeTemplateAdapter.hasSameID(tdaType1,tdaType2));
        tdaType2.setId("ANOTHER_ID");
        assertFalse(DataTypeTemplateAdapter.hasSameID(tdaType1,tdaType2));
    }

    @Test
    void testHasSamePrivates() {

        TDAType tdaType1 = new TDAType();
        TPrivate aPrivate1 = new TPrivate();
        aPrivate1.setType("A_PRIVATE1");
        aPrivate1.setSource("A_URI_1");
        tdaType1.getPrivate().add(aPrivate1);

        TDAType tdaType2 = new TDAType();
        assertFalse(DataTypeTemplateAdapter.hasSamePrivates(tdaType1,tdaType2));

        TPrivate aPrivate2 = new TPrivate();
        aPrivate2.setType("A_PRIVATE1");
        aPrivate2.setSource("A_URI_2");
        tdaType2.getPrivate().add(aPrivate2);
        assertFalse(DataTypeTemplateAdapter.hasSamePrivates(tdaType1,tdaType2));

        aPrivate2.setSource("A_URI_1");
        assertTrue(DataTypeTemplateAdapter.hasSamePrivates(tdaType1,tdaType2));

    }

    @Test
    void testFindDATypesWhichBdaContainsEnumTypeId() throws Exception {
        String enumTypeId = "RecCycModKind";
        DataTypeTemplateAdapter dataTypeTemplateAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        List<DATypeAdapter> daTypeAdapters = dataTypeTemplateAdapter.findDATypesWhichBdaContainsEnumTypeId(enumTypeId);
        assertEquals(1,daTypeAdapters.size());
    }

    @Test
    void testFindDOTypesWhichDAContainsEnumTypeId() throws Exception {
        String enumTypeId = "PhaseAngleReferenceKind";
        DataTypeTemplateAdapter dataTypeTemplateAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        List<DOTypeAdapter> doTypeAdapters = dataTypeTemplateAdapter.findDOTypesWhichDAContainsEnumTypeId(enumTypeId);
        assertEquals(1,doTypeAdapters.size());

        enumTypeId = "RecCycModKind";
        doTypeAdapters = dataTypeTemplateAdapter.findDOTypesWhichDAContainsEnumTypeId(enumTypeId);
        assertTrue(doTypeAdapters.isEmpty());
    }

    @Test
    void testFindDATypesFromStructBdaWithDATypeId() throws Exception {
        DataTypeTemplateAdapter dataTypeTemplateAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        String daTypeId = "DA2";
        List<DATypeAdapter> daTypeAdapters = dataTypeTemplateAdapter.findDATypesFromStructBdaWithDATypeId(daTypeId);

        assertEquals(1,daTypeAdapters.size());
        daTypeId = "DA1";

        daTypeAdapters = dataTypeTemplateAdapter.findDATypesFromStructBdaWithDATypeId(daTypeId);
        assertTrue(daTypeAdapters.isEmpty());
    }

    @Test
    void testFindDOTypesWhichDAContainsStructWithDATypeId() throws Exception {
        DataTypeTemplateAdapter dataTypeTemplateAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        String daTypeId = "DA1";
        List<DOTypeAdapter> doTypeAdapters = dataTypeTemplateAdapter.findDOTypesWhichDAContainsStructWithDATypeId(daTypeId);
        assertEquals(1,doTypeAdapters.size());
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
    void testFindDOTypesFromSDOWithDOTypeId() throws Exception {
        DataTypeTemplateAdapter dataTypeTemplateAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        String doTypeId = "DO4";
        List<DOTypeAdapter> doTypeList = dataTypeTemplateAdapter.findDOTypesFromSDOWithDOTypeId(doTypeId);
        assertEquals(1,doTypeList.size());

        doTypeId = "UnknownDOID";
        doTypeList = dataTypeTemplateAdapter.findDOTypesFromSDOWithDOTypeId(doTypeId);
        assertTrue(doTypeList.isEmpty());
    }

    @Test
    void testFindLNodeTypesFromDoWithDoTypeId() throws Exception {
        DataTypeTemplateAdapter dataTypeTemplateAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        String doTypeId = "DO1";
        List<LNodeTypeAdapter> lNodeTypeAdapters = dataTypeTemplateAdapter.findLNodeTypesFromDoWithDoTypeId(doTypeId);
        assertEquals(1,lNodeTypeAdapters.size());

        doTypeId = "UnknownDOID";
        lNodeTypeAdapters = dataTypeTemplateAdapter.findLNodeTypesFromDoWithDoTypeId(doTypeId);
        assertTrue(lNodeTypeAdapters.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"IED_NAME, DTT_ID, IED_NAME_DTT_ID",
            "IED_NAME, Z6A2chUEHc7a15MvIUbQTVvioCgzOcWlNMfOzNbfjLJaueNf9T2GmQP7ShgYFr3SfYex5HdwvC5tRr9oAp0lmSwtqxx1cHEKL" +
                    "MgKX7hZuUWCpKYPJ3I1fmE7NVIvVOtB1JsIOSGclfQfLGDEFjFG7vIozpkijZ0ugtZSOZuCavC5v5JL58yHO1RWCpYVdMDp4Jh" +
                    "ChU4YjhAhVGbOykJi0b4pc0saXoqf0q5imWmXiiuMuq0sc25IVA2v0TmCSxJ, " +
                    "IED_NAME_Z6A2chUEHc7a15MvIUbQTVvioCgzOcWlNMfOzNbfjLJaueNf9T2GmQP7ShgYFr3SfYex5HdwvC5tRr9oAp0lmSwtqx" +
                    "x1cHEKLMgKX7hZuUWCpKYPJ3I1fmE7NVIvVOtB1JsIOSGclfQfLGDEFjFG7vIozpkijZ0ugtZSOZuCavC5v5JL58yHO1RWCpYVdM" +
                    "Dp4JhChU4YjhAhVGbOykJi0b4pc0saXoqf0q5imWmXiiuMuq0sc25IVA"})
    void generateDttId_shouldReturnIEdNameWithDTTId_whenBothLessThan255(String iedName, String dttId, String newDTTId) {
        assertThat(dataTypeTemplateAdapter.generateDttId(iedName, dttId)).hasSizeLessThan(256)
                .isEqualTo(newDTTId);

    }

    @Test
    void importEnumTypes_shouldAddNewEnum_whenDifferentContent() throws Exception {
        //Given
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        Optional<EnumTypeAdapter> enumTypeAdapter = rcvDttAdapter.getEnumTypeAdapterById("PhaseAngleReferenceKind");
        int rcvDTTEnumValsSize = enumTypeAdapter.get().getCurrentElem().getEnumVal().size();
        //When
        rcvDttAdapter.importEnumType("IEDName",prvDttAdapter);
        Optional<EnumTypeAdapter> rcvEnumTypeAdapter = rcvDttAdapter.getEnumTypeAdapterById("PhaseAngleReferenceKind");
        //Then
        assertThat(rcvDttAdapter.getEnumTypeAdapters())
                .hasSize(rcvDTTEnumValsSize + prvDttAdapter.getEnumTypeAdapters().size());
        assertThat(rcvEnumTypeAdapter.get().getCurrentElem().getEnumVal()).hasSize(rcvDTTEnumValsSize);
    }

    @Test
    void importEnumTypes_shouldUpdateExistingEnum_whenSameContent() throws Exception {
        //Given
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        //When
        rcvDttAdapter.importEnumType("IEDName",prvDttAdapter);
        //Then
        assertThat(rcvDttAdapter.getEnumTypeAdapters()).hasSize(prvDttAdapter.getEnumTypeAdapters().size());
    }


    @Test
    void testImportDTT() throws Exception {
        //
        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        int nbLNodeType = rcvDttAdapter.getLNodeTypeAdapters().size();
        rcvDttAdapter.importDTT("IEDName",prvDttAdapter);
        assertEquals(nbLNodeType,rcvDttAdapter.getLNodeTypeAdapters().size());

        prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        var mapOldNewId =rcvDttAdapter.importDTT("IEDName",prvDttAdapter);
        assertTrue(nbLNodeType < rcvDttAdapter.getLNodeTypeAdapters().size());
        assertFalse(mapOldNewId.isEmpty());
    }

    @Test
    void testImportLNodeType() throws Exception {
        //
        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);

        int nbLNodeType = rcvDttAdapter.getLNodeTypeAdapters().size();
        rcvDttAdapter.importLNodeType("IEDName",prvDttAdapter);
        assertEquals(nbLNodeType,rcvDttAdapter.getLNodeTypeAdapters().size());

        prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        var mapOldNewId = rcvDttAdapter.importLNodeType("IEDName",prvDttAdapter);
        assertTrue(nbLNodeType < rcvDttAdapter.getLNodeTypeAdapters().size());
        assertFalse(mapOldNewId.isEmpty());

        System.out.println(MarshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(MarshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
    }

    @Test
    void testImportDOType() throws Exception {
        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);

        int nbDOType = rcvDttAdapter.getDOTypeAdapters().size();
        rcvDttAdapter.importDOType("IEDName",prvDttAdapter);
        assertEquals(nbDOType,rcvDttAdapter.getDOTypeAdapters().size());

        prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        rcvDttAdapter.importDOType("IEDName",prvDttAdapter);
        assertTrue(nbDOType < rcvDttAdapter.getDOTypeAdapters().size());

        System.out.println(MarshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(MarshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
    }

    @Test
    void testImportDAType() throws Exception {
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        int nbDAType = rcvDttAdapter.getDATypeAdapters().size();

        rcvDttAdapter.importDAType("IEDName",prvDttAdapter);
        assertEquals(nbDAType,rcvDttAdapter.getDATypeAdapters().size());
        prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        rcvDttAdapter.importDAType("IEDName",prvDttAdapter);
        assertTrue(nbDAType < rcvDttAdapter.getDATypeAdapters().size());
        System.out.println(MarshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(MarshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
    }

    @ParameterizedTest
    @CsvSource({"A,LN1,No coherence or path between DOType(DO2) and DA(A)",
            "antRef,LN1,Invalid ExtRef signal: no coherence between pDO(Op.origin) and pDA(antRef)",
            "antRef.origin.ctlVal,LN_Type1,Unknown LNodeType:LN_Type1"})
    void getBinderResumedDTT_shouldThrowScdException_whenDONotContainDA(String pDA, String lnType, String message) throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA(pDA);
        //When Then
        assertThatThrownBy(() -> dttAdapter.getBinderResumedDTT(lnType,signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage(message);
    }

    @Test
    void getBinderResumedDTT_shouldThrowScdException_whenLnClassNotMatches() throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        LNodeTypeAdapter lNodeTypeAdapter = dttAdapter.getLNodeTypeAdapterById("LN1").get();
        lNodeTypeAdapter.getCurrentElem().unsetLnClass();

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("CSWI");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA("antRef.origin.ctlVal");
        //When Then
        assertThatThrownBy(() -> dttAdapter.getBinderResumedDTT("LN1",signalInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lnClass is mandatory for LNodeType in DataTemplate : LN1");
    }

    @Test
    void getBinderResumedDTT_shouldThrowScdException_whenDOIdNotFound() throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Do");
        //When Then
        assertThatThrownBy(() -> dttAdapter.getBinderResumedDTT("LN1",signalInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown doName :Do");
    }

    @Test
    void getBinderResumedDTT_shouldReturnBindingInfoWithoutDO_whenSignalPDOEmpty() throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO(null);
        //When Then
        ExtRefBindingInfo bindingInfo = assertDoesNotThrow(() -> dttAdapter.getBinderResumedDTT("LN1",signalInfo));
        assertThat(bindingInfo)
                .extracting("lnType", "lnClass")
                .containsExactlyInAnyOrder("LN1", "PIOC");
        assertThat(bindingInfo.getDoName()).isNull();
    }

    @Test
    void getBinderResumedDTT_shouldReturnBindingInfoWithoutDA_whenSignalPDAEmpty() throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA(null);
        //When Then
        ExtRefBindingInfo bindingInfo = assertDoesNotThrow(() -> dttAdapter.getBinderResumedDTT("LN1",signalInfo));
        assertThat(bindingInfo)
                .extracting("lnType", "lnClass")
                .containsExactlyInAnyOrder("LN1", "PIOC");
        assertThat(bindingInfo.getDoName()).isNotNull();
        assertThat(bindingInfo.getDaName()).isNull();
    }

    @Test
    void getBinderResumedDTT_shouldReturnBindingInfo_whenExist() throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("PIOC");
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA("antRef.origin.ctlVal");
        //When Then
        ExtRefBindingInfo bindingInfo = assertDoesNotThrow(() -> dttAdapter.getBinderResumedDTT("LN1",signalInfo));
        assertThat(bindingInfo)
                .extracting("lnType", "lnClass")
                .containsExactlyInAnyOrder("LN1", "PIOC");
    }

    @Test
    void addPrivate() throws Exception {
        //Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        //When Then
        assertThrows(UnsupportedOperationException.class, () -> dttAdapter.addPrivate(tPrivate));
    }

    @Test
    void elementXPath() throws Exception {
        // Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        // When
        String result = dttAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DataTypeTemplates");
    }

}
