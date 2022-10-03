// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThrows(
                IllegalArgumentException.class,
                () ->new DataTypeTemplateAdapter(sclRootAdapter, new TDataTypeTemplates())
        );
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
    void testImportEnumTypes() {

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
    void testRetrieveSdoOrDO() {
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

    @Test
    void testGenerateDttId() {
    }

    @Test
    void testImportEnumType() throws Exception {
        //
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);

        DataTypeTemplateAdapter prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        int nbEnumType = prvDttAdapter.getEnumTypeAdapters().size();
        rcvDttAdapter.importEnumType("IEDName",prvDttAdapter);
        assertEquals(nbEnumType,rcvDttAdapter.getEnumTypeAdapters().size());

        prvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        rcvDttAdapter.importEnumType("IEDName",prvDttAdapter);
        assertTrue(nbEnumType < rcvDttAdapter.getEnumTypeAdapters().size());
        MarshallerWrapper marshallerWrapper = AbstractDTTLevel.createWrapper();
        System.out.println(marshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(marshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
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

        MarshallerWrapper marshallerWrapper = AbstractDTTLevel.createWrapper();
        System.out.println(marshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(marshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
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

        MarshallerWrapper marshallerWrapper = AbstractDTTLevel.createWrapper();
        System.out.println(marshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(marshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
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
        MarshallerWrapper marshallerWrapper = AbstractDTTLevel.createWrapper();
        System.out.println(marshallerWrapper.marshall(prvDttAdapter.getParentAdapter().getCurrentElem()));
        System.out.println(marshallerWrapper.marshall(rcvDttAdapter.getParentAdapter().getCurrentElem()));
    }

    @Test
    void testGetBinderResumedDTT() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        signalInfo.setPDO("Op.origin");
        signalInfo.setPDA("antRef");

        assertDoesNotThrow(() -> dttAdapter.getBinderResumedDTT("LN1",signalInfo));

    }

    /*@Test
    void testCheckSdoAndDaLink() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);

        assertDoesNotThrow(() -> dttAdapter.checkDoAndDaLink("origin","origin"));
        assertThrows(ScdException.class, () -> dttAdapter.checkDoAndDaLink("f","origin"));
        assertTrue( dttAdapter.checkDoAndDaLink("origin","d").isEmpty());
    }*/

    @Test
    void addPrivate() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
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
