// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;

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
    void getAllSDOAndDA_should_return_expected_dataReference() {
        //Given
        String SCD_DTT_DO_SDO_DA_BDA = "/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml";
        TDataTypeTemplates dtt = initDttFromFile(SCD_DTT_DO_SDO_DA_BDA);

        DoTypeService doTypeService = new DoTypeService();
        TDOType tdoType = doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId()
                .equals("DOType0")).get();

        DataAttributeRef dataRef = new DataAttributeRef();
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("FirstDoName");
        DaTypeName daTypeName = new DaTypeName();
        dataRef.setDoName(doTypeName);
        dataRef.setDaName(daTypeName);

        //When
        List<DataAttributeRef> result = doTypeService.getAllSDOAndDA(dtt, tdoType, dataRef);
        //Then
        assertThat(result).hasSize(8).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames, DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(
                        tuple("FirstDoName", List.of(),
                                "sampleDaName1", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName1", List.of("sdoName1"),
                                "sampleDaName21", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName1.sdoName21", List.of("sdoName1", "sdoName21"),
                                "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName1.sdoName21.sdoName31", List.of("sdoName1", "sdoName21", "sdoName31"),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "sampleDaName11", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.sampleBdaName1", List.of("sampleBdaName1"), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.structBdaName1.sampleBdaName21", List.of("structBdaName1", "sampleBdaName21"), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.structBdaName1.enumBdaName22", List.of("structBdaName1", "enumBdaName22"), TPredefinedBasicTypeEnum.ENUM, "EnumType1")
                );
    }

    @Test
    void getAllSDOAndDA_should_return_all_dai() {
        // GIVEN
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();

        DoTypeService doTypeService = new DoTypeService();
        TDOType tdoType = doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId()
                .equals("DO11")).orElseThrow();
        DataAttributeRef dataRef = new DataAttributeRef();
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("firstDONAME");
        DaTypeName daTypeName = new DaTypeName();
        dataRef.setDoName(doTypeName);
        dataRef.setDaName(daTypeName);
        // When
        List<DataAttributeRef> list = doTypeService.getAllSDOAndDA(dtt, tdoType, dataRef);
        // Then
        assertThat(list)
                .hasSize(811)
                .allMatch(dataAttributeRef -> dataAttributeRef.getPrefix() == null)
                .allMatch(dataAttributeRef -> dataAttributeRef.getLnType() == null)
                .allMatch(dataAttributeRef -> dataAttributeRef.getLnClass() == null)
                .allMatch(dataAttributeRef -> dataAttributeRef.getLnInst() == null)
                .allMatch(dataAttributeRef -> StringUtils.startsWith(dataAttributeRef.getDoName().getName(), "firstDONAME"))
                .areExactly(1, new Condition<>(dataAttributeRef -> dataAttributeRef.getDaName().getName().equals("da1"), "Il n'y a que certaines réponses contenant da1"))
                .areExactly(270, new Condition<>(dataAttributeRef -> dataAttributeRef.getDaName().getName().equals("da11"), "Il n'y a que certaines réponses contenant da11"))
                .areExactly(270, new Condition<>(dataAttributeRef -> dataAttributeRef.getDaName().getName().equals("da22"), "Il n'y a que certaines réponses contenant da22"))
                .areExactly(270, new Condition<>(dataAttributeRef -> dataAttributeRef.getDaName().getName().equals("da32"), "Il n'y a que certaines réponses contenant da32"));
    }
}
