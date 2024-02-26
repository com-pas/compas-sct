// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;

class DataTypeTemplatesServiceTest {


    @Test
    void isDoModAndDaStValExist_when_LNodeType_not_exist_should_return_false() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isTrue();
    }


    @Test
    void getFilteredDataObjectsAndDataAttributes_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        TAnyLN ln0 = new LN0();
        ln0.setLnType("LNodeType0");
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result = dataTypeTemplatesService.getFilteredDataObjectsAndDataAttributes(dtt, ln0, new DataAttributeRef()).toList();
        //Then
        assertThat(result).hasSize(9).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames, DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("FirstDoName", List.of(),
                                "sampleDaName1", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName1", List.of("sdoName1"),
                                "sampleDaName21", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName1.sdoName21", List.of("sdoName1", "sdoName21"),
                                "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName1.sdoName21.sdoName31", List.of("sdoName1", "sdoName21", "sdoName31"),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "sampleDaName11", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.sampleBdaName1", List.of("sampleBdaName1"), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.structBdaName1.sampleBdaName21", List.of("structBdaName1", "sampleBdaName21"), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        Tuple.tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.structBdaName1.enumBdaName22", List.of("structBdaName1", "enumBdaName22"), TPredefinedBasicTypeEnum.ENUM, "EnumType1"),
                        Tuple.tuple("SecondDoName", List.of(),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null)
                );
    }


    @Test
    void findDataObjectsAndDataAttributesByDataReference_should_find_DO_SDO_DA_and_all_BDA() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        String dataRef = "Do.sdo1.d.antRef.bda1.bda2.bda3";
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        Optional<DataAttributeRef> dataAttributeRefs = dataTypeTemplatesService.findDataObjectsAndDataAttributesByDataReference(
                dtt, "LNO1", dataRef);
        // Then
        assertThat(dataAttributeRefs).isPresent();

        assertThat(dataAttributeRefs.get()).extracting(DataAttributeRef::getDoRef, DataAttributeRef::getDaRef)
                .containsExactly("Do.sdo1.d", "antRef.bda1.bda2.bda3");
        assertThat(dataAttributeRefs.get().getDoName().getCdc()).isEqualTo(TPredefinedCDCEnum.WYE);
        assertThat(dataAttributeRefs.get().getDaName()).extracting(DaTypeName::getBType, DaTypeName::getFc)
                .containsExactly(TPredefinedBasicTypeEnum.ENUM, TFCEnum.ST);
        assertThat(dataAttributeRefs.get().getDaName().isValImport()).isTrue();
        assertThat(dataAttributeRefs.get().getDaName().isUpdatable()).isTrue();
    }

    @Test
    void findDataObjectsAndDataAttributesByDataReference_should_find_DO_SDO_DA_and_partial_BDA_list() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        Optional<DataAttributeRef> dataAttributeRefs = dataTypeTemplatesService.findDataObjectsAndDataAttributesByDataReference(
                dtt, "LN1", "Do1.sdo1.sdo2.da2.bda1.bda2");
        // Then

        assertThat(dataAttributeRefs).isPresent();
        assertThat(dataAttributeRefs.get()).extracting(DataAttributeRef::getDoRef, DataAttributeRef::getDaRef)
                .containsExactly("Do1.sdo1.sdo2", "da2.bda1.bda2");
        assertThat(dataAttributeRefs.get().getDoName().getCdc()).isEqualTo(TPredefinedCDCEnum.WYE);
        assertThat(dataAttributeRefs.get().getDaName()).extracting(DaTypeName::getBType, DaTypeName::getFc)
                .containsExactly(TPredefinedBasicTypeEnum.ENUM, TFCEnum.ST);
    }

    @Test
    void findDataObjectsAndDataAttributesByDataReference_should_find_DO_DA() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        Optional<DataAttributeRef> dataAttributeRefs = dataTypeTemplatesService.findDataObjectsAndDataAttributesByDataReference(
                dtt, "LN1", "Do1.da1");
        // Then

        assertThat(dataAttributeRefs).isPresent();
        assertThat(dataAttributeRefs.get()).extracting(DataAttributeRef::getDoRef, DataAttributeRef::getDaRef)
                .containsExactly("Do1", "da1");
        assertThat(dataAttributeRefs.get().getDoName().getCdc()).isEqualTo(TPredefinedCDCEnum.WYE);
        assertThat(dataAttributeRefs.get().getDaName()).extracting(DaTypeName::getBType, DaTypeName::getFc)
                .containsExactly(TPredefinedBasicTypeEnum.BOOLEAN, TFCEnum.ST);
    }

    @Test
    void getAllDataObjectsAndDataAttributes_when_LNodeType_not_exist_should_return_empty_list() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result = dataTypeTemplatesService.getAllDataObjectsAndDataAttributes(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDataObjectsAndDataAttributes_when_DO_not_exist_should_return_empty_list() {
        //Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        dtt.getLNodeType().add(tlNodeType);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result =  dataTypeTemplatesService.getAllDataObjectsAndDataAttributes(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDataObjectsAndDataAttributes_when_DoType_not_exist_should_return_empty_list() {
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result =  dataTypeTemplatesService.getAllDataObjectsAndDataAttributes(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDataObjectsAndDataAttributes_when_DA_not_exist_should_return_empty_list() {
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result = dataTypeTemplatesService.getAllDataObjectsAndDataAttributes(dtt).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAllDataObjectsAndDataAttributes_when_1DO_linked_to_1DA_should_return_expectedItems() {
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
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result = dataTypeTemplatesService.getAllDataObjectsAndDataAttributes(dtt).toList();
        //Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).extracting(DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames)
                .containsExactly("doName", List.of(), "daName", List.of());
    }

    @Test
    void getAllDataObjectsAndDataAttributes_should_return_all_dataReference() {
        // Given
        // File contain all combinations that can be made
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_tests.xml");
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<DataAttributeRef> result = dataTypeTemplatesService.getAllDataObjectsAndDataAttributes(dtt).toList();
        // Then
        assertThat(result).hasSize(34).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames,
                        DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(
                        // -> Do11
                        Tuple.tuple("Do11", List.of(), "sampleDa11", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do11", List.of(), "objRefDa12", List.of(), TPredefinedBasicTypeEnum.OBJ_REF, null),

                        // Do11.sdo12
                        // -> Do11.sdo11.sdo21
                        Tuple.tuple("Do11.sdo11.sdo21", List.of("sdo11", "sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do11.sdo11.sdo21", List.of("sdo11", "sdo21"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo11.sdo21", List.of("sdo11", "sdo21"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo11.sdo21", List.of("sdo11", "sdo21"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do11.sdo11.sdo22
                        Tuple.tuple("Do11.sdo11.sdo22", List.of("sdo11", "sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do11.sdo11.sdo22", List.of("sdo11", "sdo22"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo11.sdo22", List.of("sdo11", "sdo22"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo11.sdo22", List.of("sdo11", "sdo22"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // Do11.sdo12
                        // -> Do11.sdo12.sdo21
                        Tuple.tuple("Do11.sdo12.sdo21", List.of("sdo12", "sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do11.sdo12.sdo21", List.of("sdo12", "sdo21"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo12.sdo21", List.of("sdo12", "sdo21"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo12.sdo21", List.of("sdo12", "sdo21"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do11.sdo12.sdo22
                        Tuple.tuple("Do11.sdo12.sdo22", List.of("sdo12", "sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do11.sdo12.sdo22", List.of("sdo12", "sdo22"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo12.sdo22", List.of("sdo12", "sdo22"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do11.sdo12.sdo22", List.of("sdo12", "sdo22"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // Do21
                        // -> Do21.sdo21
                        Tuple.tuple("Do21.sdo21", List.of("sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do21.sdo21", List.of("sdo21"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do21.sdo21", List.of("sdo21"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do21.sdo21", List.of("sdo21"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do21.sdo22
                        Tuple.tuple("Do21.sdo22", List.of("sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do21.sdo22", List.of("sdo22"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do21.sdo22", List.of("sdo22"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do21.sdo22", List.of("sdo22"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // Do22
                        // -> Do22.sdo21
                        Tuple.tuple("Do22.sdo21", List.of("sdo21"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do22.sdo21", List.of("sdo21"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do22.sdo21", List.of("sdo21"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do22.sdo21", List.of("sdo21"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),

                        // -> Do22.sdo22
                        Tuple.tuple("Do22.sdo22", List.of("sdo22"),
                                "sampleDa2", List.of(), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind"),
                        Tuple.tuple("Do22.sdo22", List.of("sdo22"),
                                "structDa1.sampleBda1", List.of("sampleBda1"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do22.sdo22", List.of("sdo22"),
                                "structDa1.structBda1.sampleBda2", List.of("structBda1", "sampleBda2"), TPredefinedBasicTypeEnum.VIS_STRING_255, null),
                        Tuple.tuple("Do22.sdo22", List.of("sdo22"),
                                "structDa1.structBda1.structBda2.sampleBda3", List.of("structBda1", "structBda2", "sampleBda3"), TPredefinedBasicTypeEnum.ENUM, "RecCycModKind")
                );
    }

    @Test
    void isDoObjectsAndDataAttributesExists_when_LNodeType_not_exist_should_return_error_report_item() {
        // Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        dtt.getLNodeType().add(tlNodeType);
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<SclReportItem> sclReportItems = dataTypeTemplatesService.isDataObjectsAndDataAttributesExists(dtt,
                "lnodeTypeId2", "Do1.sdo1.sdo2.da2.bda1.bda2");
        // Then
        assertThat(sclReportItems)
                .hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactly("No Data Attribute found with this reference Do1.sdo1.sdo2.da2.bda1.bda2 for LNodeType.id (lnodeTypeId2)");
    }

    @Test
    void isDoObjectsAndDataAttributesExists_when_DO_not_exist_should_return_error_report_item() {
        // Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mods");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<SclReportItem> sclReportItems = dataTypeTemplatesService.isDataObjectsAndDataAttributesExists(dtt,
                "lnodeTypeId", "Mod.stVal");
        // Then
        assertThat(sclReportItems)
                .hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactly("Unknown DO.name (Mod) in DOType.id (lnodeTypeId)");
    }

    @Test
    void isDoObjectsAndDataAttributesExists_when_DOType_not_exist_should_return_error_report_item() {
        // Given
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<SclReportItem> sclReportItems = dataTypeTemplatesService.isDataObjectsAndDataAttributesExists(dtt,
                "lnodeTypeId", "Mod.stVal");
        // Then
        assertThat(sclReportItems)
                .hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactly("DOType.id (doTypeId) for DO.name (Mod) not found in DataTypeTemplates");
    }

    @Test
    void isDoObjectsAndDataAttributesExists_when_DA_not_exist_should_return_error_report_item() {
        // Given
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
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<SclReportItem> sclReportItems = dataTypeTemplatesService.isDataObjectsAndDataAttributesExists(dtt,
                "lnodeTypeId", "Mod.stVal");
        // Then
        assertThat(sclReportItems)
                .hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactly("Unknown Sub Data Object SDO or Data Attribute DA (stVal) in DOType.id (doTypeId)");
    }

    @Test
    void isDoObjectsAndDataAttributesExists_when_DO_DA_exist_should_return_empty_report() {
        // Given
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
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<SclReportItem> sclReportItems = dataTypeTemplatesService.isDataObjectsAndDataAttributesExists(dtt,
                "lnodeTypeId", "Mod.stVal");
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void isDoObjectsAndDataAttributesExists_should_return_empty_report() {
        // Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml");
        // When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        List<SclReportItem> sclReportItems = dataTypeTemplatesService.isDataObjectsAndDataAttributesExists(
                dtt, "LN1", "Do1.sdo1.sdo2.da2.bda1.bda2");
        // Then
        assertThat(sclReportItems).isEmpty();
    }

}