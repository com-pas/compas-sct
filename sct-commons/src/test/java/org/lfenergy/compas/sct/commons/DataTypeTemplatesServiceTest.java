// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.*;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.initDttFromFile;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newVal;

class DataTypeTemplatesServiceTest {


    private DataTypeTemplatesService dataTypeTemplatesService;

    @BeforeEach
    void setUp() {
        dataTypeTemplatesService = new DataTypeTemplatesService();
    }

    @Test
    void findDoLinkedToDa_should_find_DO_SDO_DA_and_all_BDA() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        dataObject.setSdoNames(List.of("sdo1", "d"));
        dataObject.setCdc(TPredefinedCDCEnum.WYE);
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("antRef");
        dataAttribute.setBdaNames(List.of("bda1", "bda2", "bda3"));
        dataAttribute.setFc(TFCEnum.ST);
        dataAttribute.setBType(TPredefinedBasicTypeEnum.ENUM);
        dataAttribute.setType("RecCycModKind");
        dataAttribute.setValImport(true);
        dataAttribute.setValKind(TValKindEnum.SET);
        dataAttribute.addDaVal(List.of(newVal("myValue")));
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        DataRef dataRef = new DataRef("Do", List.of("sdo1", "d"), "antRef", List.of("bda1", "bda2", "bda3"));
        // When
        Optional<DoLinkedToDa> result = dataTypeTemplatesService.findDoLinkedToDa(dtt, "LNO1", dataRef);
        // Then
        assertThat(result).get().usingRecursiveComparison().isEqualTo(doLinkedToDa);
    }

    @Test
    void findDoLinkedToDa_should_find_DO_SDO_DA_and_partial_BDA_list() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do1");
        dataObject.setCdc(TPredefinedCDCEnum.WYE);
        dataObject.setSdoNames(List.of("sdo1", "sdo2"));
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("da2");
        dataAttribute.setFc(TFCEnum.ST);
        dataAttribute.setBdaNames(List.of("bda1", "bda2"));
        dataAttribute.setBType(TPredefinedBasicTypeEnum.ENUM);
        dataAttribute.setType("EnumType1");
        dataAttribute.setValKind(TValKindEnum.SET);
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When
        Optional<DoLinkedToDa> result = dataTypeTemplatesService.findDoLinkedToDa(dtt, "LN1", new DataRef("Do1", List.of("sdo1", "sdo2"), "da2", List.of("bda1", "bda2")));
        // Then
        assertThat(result).get().usingRecursiveComparison().isEqualTo(doLinkedToDa);
    }

    @Test
    void findDoLinkedToDa_should_find_DO_DA() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do1");
        dataObject.setCdc(TPredefinedCDCEnum.WYE);
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("da1");
        dataAttribute.setBType(TPredefinedBasicTypeEnum.BOOLEAN);
        dataAttribute.setFc(TFCEnum.ST);
        dataAttribute.setValKind(TValKindEnum.SET);
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When
        Optional<DoLinkedToDa> result = dataTypeTemplatesService.findDoLinkedToDa(dtt, "LN1", new DataRef("Do1", List.of(), "da1", List.of()));
        // Then
        assertThat(result).get().usingRecursiveComparison().isEqualTo(doLinkedToDa);
    }

    @Test
    void getAllDOAndDA_when_1DO_linked_to_1DA_should_return_expectedItems() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("doName");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        TDOType tdoType = new TDOType();
        tdoType.setId("doTypeId");
        TDA tda = new TDA();
        tda.setName("daName");
        tdoType.getSDOOrDA().add(tda);
        dtt.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dtt);
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst())
                .extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames())
                .containsExactly("doName", List.of(), "daName", List.of());
    }

    @Test
    void getAllDOAndDA_should_return_all_dataReference() {
        // Given
        // File contain all combinations that can be made
        TDataTypeTemplates dtt = initDttFromFile("dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        // When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        // Then
        assertThat(result).hasSize(34)
                .extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getCdc(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getFc(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getValKind(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().isValImport(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaiValues())
                .containsExactlyInAnyOrder(
                        // -> Do11
                        tuple("Do11", List.of(), TPredefinedCDCEnum.WYE, "sampleDa11", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),
                        tuple("Do11", List.of(), TPredefinedCDCEnum.WYE, "objRefDa12", List.of(), TPredefinedBasicTypeEnum.OBJ_REF, null, TFCEnum.SP, null, true, List.of()),

                        // Do11.sdo12
                        // -> Do11.sdo11.sdo21
                        tuple("Do11", List.of("sdo11", "sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do11", List.of("sdo11", "sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo11", "sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo11", "sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do11.sdo11.sdo22
                        tuple("Do11", List.of("sdo11", "sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do11", List.of("sdo11", "sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo11", "sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo11", "sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // Do11.sdo12
                        // -> Do11.sdo12.sdo21
                        tuple("Do11", List.of("sdo12", "sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do11", List.of("sdo12", "sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo12", "sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo12", "sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do11.sdo12.sdo22
                        tuple("Do11", List.of("sdo12", "sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do11", List.of("sdo12", "sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo12", "sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do11", List.of("sdo12", "sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // Do21
                        // -> Do21.sdo21
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do21.sdo22
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // Do22
                        // -> Do22.sdo21
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do22.sdo22
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of())
                );
    }

    @Test
    void getAllDOAndDA_when_LNodeType_not_exist_should_return_empty_list() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDOAndDA_when_DO_not_exist_should_return_empty_list() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        dtt.getLNodeType().add(tlNodeType);
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDOAndDA_when_DoType_not_exist_should_return_empty_list() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDOAndDA_when_DA_not_exist_should_return_empty_list() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        TDOType tdoType = new TDOType();
        tdoType.setId("doTypeId");
        dtt.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dtt);
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDOAndDA_with_lnodeType_should_return_all_dataReference_for_this_lnodetype() {
        // Given
        // File contain all combinations that can be made
        TDataTypeTemplates dtt = initDttFromFile("dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        String lNodeTypeId = "LN2";
        // When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt, lNodeTypeId).toList();
        // Then
        assertThat(result)
                .extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getCdc(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getFc(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getValKind(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().isValImport(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaiValues())
                .containsExactlyInAnyOrder(
                        // Do21
                        // -> Do21.sdo21
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do21.sdo22
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // Do22
                        // -> Do22.sdo21
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do22.sdo22
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do22", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of())
                );
    }

    @Test
    void getAllDOAndDA_with_lnodeType_and_doName_should_return_all_dataReference_for_this_lnodetype_and_doName() {
        // Given
        // File contain all combinations that can be made
        TDataTypeTemplates dtt = initDttFromFile("dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        String lNodeTypeId = "LN2";
        String doName = "Do21";
        // When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt, lNodeTypeId, doName).toList();
        // Then
        assertThat(result)
                .extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getCdc(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getFc(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getValKind(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().isValImport(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaiValues())
                .containsExactlyInAnyOrder(
                        // Do21
                        // -> Do21.sdo21
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo21"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of()),

                        // -> Do21.sdo22
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, TValKindEnum.RO, true, List.of(new DaVal("value"))),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null, TFCEnum.ST, null, false, List.of()),
                        tuple("Do21", List.of("sdo22"), TPredefinedCDCEnum.ACT,
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind", TFCEnum.ST, null, true, List.of())
                );
    }

    @Test
    void getEnumValues_should_succeed() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        // When
        List<String> enumValues = dataTypeTemplatesService.getEnumValues(dtt, "RecCycModKind");
        // Then
        assertThat(enumValues).containsExactly("REB", "RVB", "RVL", "RVB+L");
    }

}
