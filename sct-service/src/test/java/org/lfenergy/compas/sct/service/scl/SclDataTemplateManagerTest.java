// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service.scl;


import org.junit.jupiter.api.Test;
import org.lfenergy.compas.commons.MarshallerWrapper;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TBDA;
import org.lfenergy.compas.scl.TDA;
import org.lfenergy.compas.scl.TDAType;
import org.lfenergy.compas.scl.TDOType;
import org.lfenergy.compas.scl.TDataTypeTemplates;
import org.lfenergy.compas.scl.TEnumType;
import org.lfenergy.compas.scl.TFCEnum;
import org.lfenergy.compas.scl.TLLN0Enum;
import org.lfenergy.compas.scl.TLNodeType;
import org.lfenergy.compas.scl.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl.TPredefinedCDCEnum;


import org.lfenergy.compas.sct.model.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.model.dto.ExtRefDTO;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SclDataTemplateManagerTest {

    private static final String SCD_FILE = "scl/SCD/scd_with_dtt_test.xml";
    private static final String ICD_WITH_IDENTICAL_DTT = "scl/IEDImportHelper/Icd_With_Identical_DTT.xml";
    private static final String ICD_WITH_DTT_DIFF_IDS = "scl/IEDImportHelper/Icd_With_DTT_DIFF_IDS.xml";
    private static final String ICD_WITH_DTT_DIFF_CONTENT = "scl/IEDImportHelper/Icd_With_DTT_Same_IDs_Diff_Contents.xml";


    //private MarshallerWrapper marshallerWrapper = createWrapper();

    @Test
    void ShouldReturnTrueWhenIsIdenticalForDTTEnumType() throws Exception  {
        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TEnumType> rcvEnumType = scd.getDataTypeTemplates().getEnumType();
        List<TEnumType> prdEnumType = icd.getDataTypeTemplates().getEnumType();

        assertFalse(rcvEnumType.isEmpty());
        assertFalse(prdEnumType.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvEnumType.get(0),prdEnumType.get(0),true);
        assertTrue(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTEnumTypeCauseIdsAreDifferent()  throws Exception {

        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_IDS);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TEnumType> rcvEnumType = scd.getDataTypeTemplates().getEnumType();
        List<TEnumType> prdEnumType = icd.getDataTypeTemplates().getEnumType();
        assertFalse(rcvEnumType.isEmpty());
        assertFalse(prdEnumType.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvEnumType.get(0),prdEnumType.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTEnumTypeCauseContentsAreDifferent() throws Exception  {

        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TEnumType> rcvEnumType = scd.getDataTypeTemplates().getEnumType();
        List<TEnumType> prdEnumType = icd.getDataTypeTemplates().getEnumType();
        assertFalse(rcvEnumType.isEmpty());
        assertFalse(prdEnumType.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvEnumType.get(0),prdEnumType.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnTrueWhenIsIdenticalForDTTDaType()  throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TDAType> rcvDaTypes = scd.getDataTypeTemplates().getDAType();
        List<TDAType> prdDaTypes = icd.getDataTypeTemplates().getDAType();

        assertFalse(rcvDaTypes.isEmpty());
        assertFalse(prdDaTypes.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvDaTypes.get(0),prdDaTypes.get(0),true);
        assertTrue(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTDaTypeCauseIdsAreDifferent() throws Exception  {

        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_IDS);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TDAType> rcvDaTypes = scd.getDataTypeTemplates().getDAType();
        List<TDAType> prdDaTypes = icd.getDataTypeTemplates().getDAType();

        assertFalse(rcvDaTypes.isEmpty());
        assertFalse(prdDaTypes.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvDaTypes.get(0),prdDaTypes.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTDaTypeCauseContentsAreDifferent() throws Exception  {
        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TDAType> rcvDaTypes = scd.getDataTypeTemplates().getDAType();
        List<TDAType> prdDaTypes = icd.getDataTypeTemplates().getDAType();

        assertFalse(rcvDaTypes.isEmpty());
        assertFalse(prdDaTypes.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvDaTypes.get(0),prdDaTypes.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnTrueWhenIsIdenticalForDTTDoType()  throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TDOType> rcvDoTypes = scd.getDataTypeTemplates().getDOType();
        List<TDOType> prdDoTypes = icd.getDataTypeTemplates().getDOType();

        assertFalse(rcvDoTypes.isEmpty());
        assertFalse(prdDoTypes.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvDoTypes.get(0),prdDoTypes.get(0),true);
        assertTrue(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTDoTypeCauseIdsAreDifferent()  throws Exception {

        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_IDS);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TDOType> rcvDoTypes = scd.getDataTypeTemplates().getDOType();
        List<TDOType> prdDoTypes = icd.getDataTypeTemplates().getDOType();

        assertFalse(rcvDoTypes.isEmpty());
        assertFalse(prdDoTypes.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvDoTypes.get(0),prdDoTypes.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTDoTypeCauseContentsAreDifferent()  throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TDOType> rcvDoTypes = scd.getDataTypeTemplates().getDOType();
        List<TDOType> prdDoTypes = icd.getDataTypeTemplates().getDOType();

        assertFalse(rcvDoTypes.isEmpty());
        assertFalse(prdDoTypes.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvDoTypes.get(0),prdDoTypes.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnTrueWhenIsIdenticalForDTTLNodeType()  throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TLNodeType> rcvLNodeType = scd.getDataTypeTemplates().getLNodeType();
        List<TLNodeType> prdLNodeType = icd.getDataTypeTemplates().getLNodeType();

        assertFalse(rcvLNodeType.isEmpty());
        assertFalse(prdLNodeType.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvLNodeType.get(0),prdLNodeType.get(0),true);
        assertTrue(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTLNodeTypeCauseIdsAreDifferent()  throws Exception {

        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_IDS);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TLNodeType> rcvLNodeType = scd.getDataTypeTemplates().getLNodeType();
        List<TLNodeType> prdLNodeType = icd.getDataTypeTemplates().getLNodeType();

        assertFalse(rcvLNodeType.isEmpty());
        assertFalse(prdLNodeType.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvLNodeType.get(0),prdLNodeType.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnFalseWhenIsIdenticalForDTTLNodeTypeCauseContentsAreDifferent() throws Exception  {

        SCL scd = getSCLFromFile(SCD_FILE);
        SCL icd = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        List<TLNodeType> rcvLNodeType = scd.getDataTypeTemplates().getLNodeType();
        List<TLNodeType> prdLNodeType = icd.getDataTypeTemplates().getLNodeType();

        assertFalse(rcvLNodeType.isEmpty());
        assertFalse(prdLNodeType.isEmpty());
        boolean isIdentical = sclDataTemplateManager.isIdentical(rcvLNodeType.get(0),prdLNodeType.get(0),true);
        assertFalse(isIdentical);
    }

    @Test
    void ShouldReturnOKWhenFindDATypesWhichBdaContainsEnumTypeId() throws Exception {
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        String enumTypeId = "RecCycModKind";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        List<TDAType> daTypeList = sclDataTemplateManager.findDATypesWhichBdaContainsEnumTypeId(icd,enumTypeId);

        assertEquals(1,daTypeList.size());
    }

    @Test
    void ShouldReturnOKWhenFindDOTypesWhichDAContainsEnumTypeId() throws Exception {
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        String enumTypeId = "PhaseAngleReferenceKind";
        List<TDOType> doTypeList = sclDataTemplateManager.findDOTypesWhichDAContainsEnumTypeId(icd,enumTypeId);
        assertEquals(1,doTypeList.size());

        enumTypeId = "RecCycModKind";
        doTypeList = sclDataTemplateManager.findDOTypesWhichDAContainsEnumTypeId(icd,enumTypeId);
        assertTrue(doTypeList.isEmpty());
    }

    @Test
    void ShouldReturnOKWhenFindDATypesFromStructBdaWithDATypeId()  throws Exception {
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        String daTypeId = "DA3";
        List<TDAType> daTypeList = sclDataTemplateManager.findDATypesFromStructBdaWithDATypeId(icd,daTypeId);

        assertEquals(1,daTypeList.size());
        daTypeId = "DA1";

        daTypeList = sclDataTemplateManager.findDATypesFromStructBdaWithDATypeId(icd,daTypeId);
        assertTrue(daTypeList.isEmpty());
    }

    @Test
    void ShouldReturnOKWhenFindDOTypesWhichDAContainsStructWithDATypeId() throws Exception {
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        String daTypeId = "DA3";
        List<TDOType> doTypeList = sclDataTemplateManager.findDOTypesWhichDAContainsStructWithDATypeId(icd,daTypeId);
        assertEquals(1,doTypeList.size());

    }

    @Test
    void ShouldReturnOKWhenFindDOTypesFromSDOWithDOTypeId() throws Exception {
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        String doTypeId = "DO4";
        List<TDOType> doTypeList = sclDataTemplateManager.findDOTypesFromSDOWithDOTypeId(icd,doTypeId);
        assertEquals(2,doTypeList.size());

        doTypeId = "UnknownDOID";
        doTypeList = sclDataTemplateManager.findDOTypesFromSDOWithDOTypeId(icd,doTypeId);
        assertTrue(doTypeList.isEmpty());
    }

    @Test
    void ShouldReturnOKWhenFindLNodeTypesFromDoWithDoTypeId() throws Exception {
        SCL icd = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        String doTypeId = "DO1";
        List<TLNodeType> lNodeTypes = sclDataTemplateManager.findLNodeTypesFromDoWithDoTypeId(icd,doTypeId);
        assertEquals(2,lNodeTypes.size());

        doTypeId = "UnknownDOID";
        lNodeTypes = sclDataTemplateManager.findLNodeTypesFromDoWithDoTypeId(icd,doTypeId);
        assertTrue(lNodeTypes.isEmpty());
    }

    @Test
    void testComputeImportableDTTFromEnumTypeWithIdenticalIdsAndSameContent() throws Exception {
        // identical id, same content
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        String iedName = "IED_NAME";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        sclDataTemplateManager.computeImportableDTTFromEnumType(provider,receiver,iedName);

        assertTrue(sclDataTemplateManager.getEnumTypeToAdd().isEmpty());
        assertTrue(sclDataTemplateManager.getDoTypeToAdd().isEmpty());
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().isEmpty());

    }

    @Test
    void testComputeImportableDTTFromEnumTypeWithIdenticalIdsAndDiffContent() throws Exception {

        // identical id, different content
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);
        String iedName = "IED_NAME";
        String newExpectedEnumTypeID1 = iedName + "_PhaseAngleReferenceKind";
        String newExpectedEnumTypeID2 = iedName + "_RecCycModKind";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        sclDataTemplateManager.computeImportableDTTFromEnumType(provider,receiver,iedName);

        assertEquals(2, sclDataTemplateManager.getEnumTypeToAdd().size());
        assertTrue(sclDataTemplateManager.getEnumTypeToAdd().containsKey(newExpectedEnumTypeID1));
        assertTrue(sclDataTemplateManager.getEnumTypeToAdd().containsKey(newExpectedEnumTypeID2));

        List<TDAType> daTypeListRefEnumID = sclDataTemplateManager.findDATypesWhichBdaContainsEnumTypeId(provider,newExpectedEnumTypeID1);
        List<TDOType> doTypeListRefEnumID = sclDataTemplateManager.findDOTypesWhichDAContainsEnumTypeId(provider,newExpectedEnumTypeID1);
        assertTrue(daTypeListRefEnumID.isEmpty());
        assertEquals(1, doTypeListRefEnumID.size());
        List<TDA> tdaList = sclDataTemplateManager.retrieveSdoOrDO(doTypeListRefEnumID.get(0).getSDOOrDA(), TDA.class);
        assertFalse(tdaList.isEmpty());
        assertEquals(TPredefinedBasicTypeEnum.ENUM,tdaList.get(0).getBType());
        assertEquals(newExpectedEnumTypeID1,tdaList.get(0).getType());

        daTypeListRefEnumID = sclDataTemplateManager.findDATypesWhichBdaContainsEnumTypeId(provider,newExpectedEnumTypeID2);
        doTypeListRefEnumID = sclDataTemplateManager.findDOTypesWhichDAContainsEnumTypeId(provider,newExpectedEnumTypeID2);

        assertEquals(1, daTypeListRefEnumID.size());
        assertTrue(doTypeListRefEnumID.isEmpty());
        List<TBDA> bdaList = daTypeListRefEnumID.get(0).getBDA();
        assertTrue(!bdaList.isEmpty());
        Optional<TBDA> opBda = bdaList.stream()
                .filter(tbda -> TPredefinedBasicTypeEnum.ENUM.equals(tbda.getBType())
                        && newExpectedEnumTypeID2.equals(tbda.getType()))
                .findFirst();
        assertTrue(opBda.isPresent());
    }

    @Test
    void testComputeImportableDTTFromEnumTypeWithDiffIds() throws Exception {
        // different id
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_DTT_DIFF_IDS);
        String iedName = "IED_NAME";
        String newExpectedEnumTypeID1 = "MultiplierKind";
        String newExpectedEnumTypeID2 = "CtlModelKind";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        sclDataTemplateManager.computeImportableDTTFromEnumType(provider,receiver,iedName);

        assertEquals(2, sclDataTemplateManager.getEnumTypeToAdd().size());
        assertTrue(sclDataTemplateManager.getEnumTypeToAdd().containsKey(newExpectedEnumTypeID1));
        assertTrue(sclDataTemplateManager.getEnumTypeToAdd().containsKey(newExpectedEnumTypeID2));

        List<TDAType> daTypeListRefEnumID = sclDataTemplateManager.findDATypesWhichBdaContainsEnumTypeId(provider,newExpectedEnumTypeID1);
        List<TDOType> doTypeListRefEnumID = sclDataTemplateManager.findDOTypesWhichDAContainsEnumTypeId(provider,newExpectedEnumTypeID1);
        assertTrue( daTypeListRefEnumID.isEmpty());
        assertEquals(1, doTypeListRefEnumID.size());
        List<TDA> tdaList = sclDataTemplateManager.retrieveSdoOrDO(doTypeListRefEnumID.get(0).getSDOOrDA(), TDA.class);
        assertFalse(tdaList.isEmpty());
        assertEquals(TPredefinedBasicTypeEnum.ENUM,tdaList.get(0).getBType());
        assertEquals(newExpectedEnumTypeID1,tdaList.get(0).getType());

        daTypeListRefEnumID = sclDataTemplateManager.findDATypesWhichBdaContainsEnumTypeId(provider,newExpectedEnumTypeID2);
        doTypeListRefEnumID = sclDataTemplateManager.findDOTypesWhichDAContainsEnumTypeId(provider,newExpectedEnumTypeID2);

        assertEquals(1, daTypeListRefEnumID.size());
        assertFalse(!doTypeListRefEnumID.isEmpty());
        List<TBDA> bdaList = daTypeListRefEnumID.get(0).getBDA();
        assertTrue(!bdaList.isEmpty());
        Optional<TBDA> opBda = bdaList.stream()
                .filter(tbda -> TPredefinedBasicTypeEnum.ENUM.equals(tbda.getBType())
                        && newExpectedEnumTypeID2.equals(tbda.getType()))
                .findFirst();
        assertTrue(opBda.isPresent());
    }

    @Test
    void testComputeImportableDTTFromDATypeWithIdenticalIdsAndSameContent() throws Exception {

        // identical id, same content
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_IDENTICAL_DTT);
        String iedName = "IED_NAME";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        sclDataTemplateManager.computeImportableDTTFromDAType(provider,receiver,iedName);

        assertTrue(sclDataTemplateManager.getEnumTypeToAdd().isEmpty());
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().isEmpty());
        assertTrue(sclDataTemplateManager.getDoTypeToAdd().isEmpty());
        assertTrue(sclDataTemplateManager.getLNodeTypeToAdd().isEmpty());

    }

    @Test
    void testComputeImportableDTTFromDATypeWithIdenticalIdsAndDiffContent() throws Exception {
        // identical id, different content
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);
        String iedName = "IED_NAME";
        String expectedDATypeID1 = iedName + "_DA1";
        String expectedDATypeID3 = iedName + "_DA3";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        sclDataTemplateManager.computeImportableDTTFromDAType(provider,receiver,iedName);

        assertEquals(2, sclDataTemplateManager.getDaTypeToAdd().size());
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().containsKey(expectedDATypeID1));
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().containsKey(expectedDATypeID3));
    }

    @Test
    void testComputeImportableDTTFromDATypeWithDiffIds() throws Exception {
        // identical id, different content
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_DTT_DIFF_IDS);
        String iedName = "IED_NAME";
        String expectedDATypeID1 = "DA11";
        String expectedDATypeID2 = "DA21";
        String expectedDATypeID3 = "DA31";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();

        sclDataTemplateManager.computeImportableDTTFromDAType(provider,receiver,iedName);

        assertEquals(3, sclDataTemplateManager.getDaTypeToAdd().size());
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().containsKey(expectedDATypeID1));
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().containsKey(expectedDATypeID2));
        assertTrue(sclDataTemplateManager.getDaTypeToAdd().containsKey(expectedDATypeID3));
    }

    @Test
    void testComputeImportableDTTFromDOType(){

    }
    @Test
    void testComputeImportableDTTFromLNodeType(){

    }

    @Test
    void testImportDTT() throws Exception {
        SCL receiver = getSCLFromFile(SCD_FILE);
        SCL provider = getSCLFromFile(ICD_WITH_DTT_DIFF_CONTENT);
        String iedName = "IED_NAME";

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        receiver = sclDataTemplateManager.importDTT(provider,receiver,iedName);

        System.out.print(createWrapper().marshall(receiver));
    }

    @Test
    void testShouldReturnOKWhenGetResumedDTT() throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        String lnType = "LN2";
        ExtRefDTO extRef = new ExtRefDTO();
        extRef.setIntAddr("IntAddr");
        extRef.setPLN(TLLN0Enum.LLN_0.value());
        extRef.setPDO("FACntRs.res");
        extRef.setPDA("d");

        ResumedDataTemplate res = SclDataTemplateManager.getResumedDTT(lnType,extRef,dtt);
        System.out.println(res);
        assertEquals(lnType,res.getLnType());
        assertEquals(TLLN0Enum.LLN_0.value(),res.getLnClass());
        assertEquals(extRef.getPDO(),res.getDoName());
        assertEquals(extRef.getPDA(),res.getDaName());
        assertEquals(TPredefinedCDCEnum.WYE,res.getCdc());
        assertEquals(TFCEnum.CF,res.getFc());
    }

    @Test
    void testShouldReturnNOKWhenGetResumedDTTCauseUnreferencedDoName() throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        String lnType = "LN2";
        ExtRefDTO extRef = new ExtRefDTO();
        extRef.setIntAddr("IntAddr");
        extRef.setPLN(TLLN0Enum.LLN_0.value());
        extRef.setPDO("FACntRs1.res");
        extRef.setPDA("d");

        assertThrows(ScdException.class, () -> SclDataTemplateManager.getResumedDTT(lnType,extRef,dtt));
    }

    @Test
    void testShouldReturnNOKWhenGetResumedDTTCauseUnknownLnType() throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        String lnType = "LN210";
        ExtRefDTO extRef = new ExtRefDTO();
        extRef.setIntAddr("IntAddr");
        extRef.setPLN(TLLN0Enum.LLN_0.value());
        extRef.setPDO("FACntRs.res");
        extRef.setPDA("d");

        assertThrows(ScdException.class, () -> SclDataTemplateManager.getResumedDTT(lnType,extRef,dtt));
    }

    @Test
    void testShouldReturnNOKWhenGetResumedDTTCauseUnreferencedSDoName() throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        String lnType = "LN210";
        ExtRefDTO extRef = new ExtRefDTO();
        extRef.setIntAddr("IntAddr");
        extRef.setPLN(TLLN0Enum.LLN_0.value());
        extRef.setPDO("FACntRs.res1");
        extRef.setPDA("d");

        assertThrows(ScdException.class, () -> SclDataTemplateManager.getResumedDTT(lnType,extRef,dtt));
    }

    @Test
    void testShouldReturnNOKWhenGetResumedDTTCauseUnreferencedDaName() throws Exception {
        SCL scd = getSCLFromFile(SCD_FILE);
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        String lnType = "LN210";
        ExtRefDTO extRef = new ExtRefDTO();
        extRef.setIntAddr("IntAddr");
        extRef.setPLN(TLLN0Enum.LLN_0.value());
        extRef.setPDO("FACntRs.res1");
        extRef.setPDA("d");

        assertThrows(ScdException.class, () -> SclDataTemplateManager.getResumedDTT(lnType,extRef,dtt));
    }

    private SCL getSCLFromFile(String filename) throws Exception {
        MarshallerWrapper marshallerWrapper = createWrapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        return marshallerWrapper.unmarshall(is);
    }

    private MarshallerWrapper createWrapper() throws Exception {
        return (new MarshallerWrapper.Builder()).build();
    }

}