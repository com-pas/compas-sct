// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DataObject;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDaFilter;
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
    void isDoModAndDaStValExist_when_LNodeType_not_exist_should_return_false() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        //When
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_Do_not_exist_should_return_false() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        dtt.getLNodeType().add(tlNodeType);
        //When
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void isDoModAndDaStValExist_when_DoType_not_exist_should_return_false() {
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
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_Da_Mod_not_exist_should_return_false() {
        //Given
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
        //When
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_Da_stVal_not_found_should_return_false() {
        //Given
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
        TDA tda = new TDA();
        tda.setName("daName");
        tdoType.getSDOOrDA().add(tda);
        dtt.getDOType().add(tdoType);
        //When
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_DO_Mod_And_DA_stVal_exist_return_true() {
        //Given
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
        TDA tda = new TDA();
        tda.setName("stVal");
        tdoType.getSDOOrDA().add(tda);
        dtt.getDOType().add(tdoType);
        //When
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void getFilteredDoLinkedToDa_should_return_expected_items() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        String lNodeTypeId = "LNodeType0";

        DoLinkedToDaFilter doLinkedToDaFilter = new DoLinkedToDaFilter(null, null, null, null);

        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, lNodeTypeId, doLinkedToDaFilter).toList();
        //Then
        assertThat(result).hasSize(9)
                .extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType())
                .containsExactlyInAnyOrder(
                        tuple("FirstDoName", List.of(),
                                "sampleDaName1", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName1"),
                                "sampleDaName21", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName1", "sdoName21"),
                                "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName1", "sdoName21", "sdoName31"),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName2"),
                                "sampleDaName11", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName2"),
                                "structDaName1", List.of("sampleBdaName1"), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName2"),
                                "structDaName1", List.of("structBdaName1", "sampleBdaName21"), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName2"),
                                "structDaName1", List.of("structBdaName1", "enumBdaName22"), TPredefinedBasicTypeEnum.ENUM, "EnumType1"),
                        tuple("SecondDoName", List.of(),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null)
                );
    }

    @Test
    void getFilteredDOAndDA_when_given_DoName_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        String lNodeTypeId = "LNodeType0";

        DoLinkedToDaFilter doLinkedToDaFilter = DoLinkedToDaFilter.from("SecondDoName", "");

        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, lNodeTypeId, doLinkedToDaFilter).toList();
        //Then
        assertThat(result).hasSize(1).extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType())
                .containsExactly(tuple("SecondDoName", List.of(), "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null));
    }


    @Test
    void getFilteredDOAndDA_when_given_DO_with_one_structName_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        String lNodeTypeId = "LNodeType0";

        DoLinkedToDaFilter doLinkedToDaFilter = DoLinkedToDaFilter.from("FirstDoName.sdoName1", "");
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, lNodeTypeId, doLinkedToDaFilter).toList();
        //Then
        assertThat(result).hasSize(3).extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType())
                .containsExactlyInAnyOrder(tuple("FirstDoName", List.of("sdoName1"), "sampleDaName21", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName1", "sdoName21"), "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName1", "sdoName21", "sdoName31"), "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null));
    }

    @Test
    void getFilteredDOAndDA_when_given_DO_with_many_structName_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        String lNodeTypeId = "LNodeType0";

        DoLinkedToDaFilter doLinkedToDaFilter = DoLinkedToDaFilter.from("FirstDoName.sdoName1.sdoName21", "");
        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, lNodeTypeId, doLinkedToDaFilter).toList();
        //Then
        assertThat(result).hasSize(2).extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType())
                .containsExactlyInAnyOrder(tuple("FirstDoName", List.of("sdoName1", "sdoName21"), "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName", List.of("sdoName1", "sdoName21", "sdoName31"), "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null));
    }

    @Test
    void getFilteredDOAndDA_when_given_DO_and_DA_with_structNames_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        String lNodeTypeId = "LNodeType0";

        DoLinkedToDaFilter doLinkedToDaFilter = DoLinkedToDaFilter.from("FirstDoName.sdoName2", "structDaName1.structBdaName1.enumBdaName22");

        //When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getFilteredDoLinkedToDa(dtt, lNodeTypeId, doLinkedToDaFilter).toList();
        //Then
        assertThat(result).hasSize(1).extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType())
                .containsExactlyInAnyOrder(tuple("FirstDoName", List.of("sdoName2"), "structDaName1", List.of("structBdaName1", "enumBdaName22"), TPredefinedBasicTypeEnum.ENUM, "EnumType1"));
    }

    @Test
    void findDoLinkedToDa_should_find_DO_SDO_DA_and_all_BDA() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
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
        dataAttribute.addDaVal(List.of(newVal("myValue")));
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        DoLinkedToDaFilter doLinkedToDaFilter = new DoLinkedToDaFilter("Do", List.of("sdo1", "d"), "antRef", List.of("bda1", "bda2", "bda3"));
        // When
        Optional<DoLinkedToDa> result = dataTypeTemplatesService.findDoLinkedToDa(dtt, "LNO1", doLinkedToDaFilter);
        // Then
        assertThat(result).get().usingRecursiveComparison().isEqualTo(doLinkedToDa);
    }

    @Test
    void findDoLinkedToDa_should_find_DO_SDO_DA_and_partial_BDA_list() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
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
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When
        Optional<DoLinkedToDa> result = dataTypeTemplatesService.findDoLinkedToDa(dtt, "LN1", new DoLinkedToDaFilter("Do1", List.of("sdo1", "sdo2"), "da2", List.of("bda1", "bda2")));
        // Then
        assertThat(result).get().usingRecursiveComparison().isEqualTo(doLinkedToDa);
    }

    @Test
    void findDoLinkedToDa_should_find_DO_DA() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do1");
        dataObject.setCdc(TPredefinedCDCEnum.WYE);
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("da1");
        dataAttribute.setBType(TPredefinedBasicTypeEnum.BOOLEAN);
        dataAttribute.setFc(TFCEnum.ST);
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When
        Optional<DoLinkedToDa> result = dataTypeTemplatesService.findDoLinkedToDa(dtt, "LN1", new DoLinkedToDaFilter("Do1", List.of(), "da1", List.of()));
        // Then
        assertThat(result).get().usingRecursiveComparison().isEqualTo(doLinkedToDa);
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
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        // When
        List<DoLinkedToDa> result = dataTypeTemplatesService.getAllDoLinkedToDa(dtt).toList();
        // Then
        assertThat(result).hasSize(34)
                .extracting(doLinkedToDa1 -> doLinkedToDa1.dataObject().getDoName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataObject().getSdoNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getDaName(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBdaNames(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getBType(),
                        doLinkedToDa1 -> doLinkedToDa1.dataAttribute().getType())
                .containsExactlyInAnyOrder(
                        // -> Do11
                        tuple("Do11", List.of(), "sampleDa11", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do11", List.of(), "objRefDa12", List.of(), TPredefinedBasicTypeEnum.OBJ_REF, null),

                        // Do11.sdo12
                        // -> Do11.sdo11.sdo21
                        tuple("Do11", List.of("sdo11", "sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do11", List.of("sdo11", "sdo21"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo11", "sdo21"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo11", "sdo21"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do11.sdo11.sdo22
                        tuple("Do11", List.of("sdo11", "sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do11", List.of("sdo11", "sdo22"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo11", "sdo22"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo11", "sdo22"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // Do11.sdo12
                        // -> Do11.sdo12.sdo21
                        tuple("Do11", List.of("sdo12", "sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do11", List.of("sdo12", "sdo21"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo12", "sdo21"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo12", "sdo21"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do11.sdo12.sdo22
                        tuple("Do11", List.of("sdo12", "sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do11", List.of("sdo12", "sdo22"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo12", "sdo22"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do11", List.of("sdo12", "sdo22"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // Do21
                        // -> Do21.sdo21
                        tuple("Do21", List.of("sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do21", List.of("sdo21"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do21", List.of("sdo21"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do21", List.of("sdo21"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do21.sdo22
                        tuple("Do21", List.of("sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do21", List.of("sdo22"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do21", List.of("sdo22"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do21", List.of("sdo22"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // Do22
                        // -> Do22.sdo21
                        tuple("Do22", List.of("sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do22", List.of("sdo21"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do22", List.of("sdo21"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do22", List.of("sdo21"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do22.sdo22
                        tuple("Do22", List.of("sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        tuple("Do22", List.of("sdo22"),
                                "structDa1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do22", List.of("sdo22"),
                                "structDa1", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        tuple("Do22", List.of("sdo22"),
                                "structDa1", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind")
                );
    }

    @Test
    void getEnumValues_should_succeed() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        // When
        List<String> enumValues = dataTypeTemplatesService.getEnumValues(dtt, "LN1", DoLinkedToDaFilter.from("Do11", "sampleDa11")).toList();
        // Then
        assertThat(enumValues).containsExactly("REB", "RVB", "RVL", "RVB+L");
    }

}
