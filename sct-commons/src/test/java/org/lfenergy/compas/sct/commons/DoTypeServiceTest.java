// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DataObject;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.initDttFromFile;

class DoTypeServiceTest {

    @Test
    void getDoTypes() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        DoTypeService doTypeService = new DoTypeService();

        //When
        List<TDOType> tdoTypes = doTypeService.getDoTypes(dataTypeTemplates).toList();

        //Then
        assertThat(tdoTypes)
                .hasSize(47);
    }

    @Test
    void getFilteredDoTypes() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        DoTypeService doTypeService = new DoTypeService();

        //When
        List<TDOType> tdoTypes =
                doTypeService.getFilteredDoTypes(dataTypeTemplates, tdoType -> TPredefinedCDCEnum.DPL.equals(tdoType.getCdc())).toList();

        //Then
        assertThat(tdoTypes)
                .hasSize(2)
                .extracting(TDOType::getCdc, TDOType::getId)
                .containsExactly(tuple(TPredefinedCDCEnum.DPL, "RTE_X_X_X_48BA5C40D0913654FA5291A28C0D9716_DPL_V1.0.0"),
                        tuple(TPredefinedCDCEnum.DPL, "RTE_X_X_X_D93000A2D6F9B026504B48576A914DA3_DPL_V1.0.0"));

    }

    @Test
    void findDoType() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        DoTypeService doTypeService = new DoTypeService();

        //When
        TDOType tdoType = doTypeService.findDoType(dataTypeTemplates, tdoType1 -> TPredefinedCDCEnum.DPL.equals(tdoType1.getCdc())).orElseThrow();

        //Then
        assertThat(tdoType)
                .extracting(TDOType::getCdc, TDOType::getId)
                .containsExactly(TPredefinedCDCEnum.DPL, "RTE_X_X_X_48BA5C40D0913654FA5291A28C0D9716_DPL_V1.0.0");
    }

    @Test
    void getAllSDOLinkedToDa_should_return_expected_dataReference() {
        //Given
        String SCD_DTT_DO_SDO_DA_BDA = "/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml";
        TDataTypeTemplates dtt = initDttFromFile(SCD_DTT_DO_SDO_DA_BDA);

        DoTypeService doTypeService = new DoTypeService();
        TDOType tdoType = doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId()
                .equals("DOType0")).orElseThrow();

        DataObject dataObject = new DataObject();
        dataObject.setDoName("FirstDoName");
        DataAttribute dataAttribute = new DataAttribute();
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        //When
        List<DoLinkedToDa> result = doTypeService.getAllSDOLinkedToDa(dtt, tdoType, doLinkedToDa);
        //Then
        assertThat(result).hasSize(8)
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
                                "structDaName1", List.of("structBdaName1", "enumBdaName22"), TPredefinedBasicTypeEnum.ENUM, "EnumType1")
                );
    }

    @Test
    void getAllSDOLinkedToDa_should_return_all_dai() {
        // GIVEN
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();

        DoTypeService doTypeService = new DoTypeService();
        TDOType tdoType = doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId()
                .equals("DO11")).orElseThrow();
        DataObject dataObject = new DataObject();
        dataObject.setDoName("firstDONAME");
        DataAttribute dataAttribute = new DataAttribute();
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When
        List<DoLinkedToDa> list = doTypeService.getAllSDOLinkedToDa(dtt, tdoType, doLinkedToDa);
        // Then
        assertThat(list)
                .hasSize(811)
                .allMatch(dataAttributeRef -> StringUtils.startsWith(dataAttributeRef.dataObject().getDoName(), "firstDONAME"))
                .areExactly(1, new Condition<>(dataAttributeRef -> dataAttributeRef.dataAttribute().getDaName().equals("da1"), "Il n'y a que certaines réponses contenant da1"))
                .areExactly(270, new Condition<>(dataAttributeRef -> dataAttributeRef.dataAttribute().getDaName().equals("da11"), "Il n'y a que certaines réponses contenant da11"))
                .areExactly(270, new Condition<>(dataAttributeRef -> dataAttributeRef.dataAttribute().getDaName().equals("da22"), "Il n'y a que certaines réponses contenant da22"))
                .areExactly(270, new Condition<>(dataAttributeRef -> dataAttributeRef.dataAttribute().getDaName().equals("da32"), "Il n'y a que certaines réponses contenant da32"));
    }

}
