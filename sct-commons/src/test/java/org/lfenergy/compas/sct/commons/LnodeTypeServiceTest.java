// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;

class LnodeTypeServiceTest {

    @Test
    void getLnodeTypes() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        LnodeTypeService lnodeTypeService = new LnodeTypeService();

        //When
        List<TLNodeType> tlnodeTypes = lnodeTypeService.getLnodeTypes(dataTypeTemplates).toList();

        //Then
        assertThat(tlnodeTypes)
                .hasSize(15)
                .flatExtracting(TLNodeType::getLnClass)
                .containsExactly("LPAI",
                        "LLN0",
                        "LLN0",
                        "TCTR",
                        "LSET",
                        "LCCH",
                        "LPCP",
                        "LGOS",
                        "LTMS",
                        "XSWI",
                        "GAPC",
                        "TVTR",
                        "LSYN",
                        "XSWI",
                        "LSET");
    }

    @Test
    void getFilteredLnodeTypes() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        LnodeTypeService lnodeTypeService = new LnodeTypeService();

        //When
        List<TLNodeType> tlnodeTypes =
                lnodeTypeService.getFilteredLnodeTypes(dataTypeTemplates, tlNodeType -> tlNodeType.getLnClass().contains("LPAI")).toList();

        //Then
        assertThat(tlnodeTypes)
                .hasSize(1)
                .extracting(TLNodeType::getLnClass, TLNodeType::getId)
                .containsExactly(Tuple.tuple(List.of("LPAI"), "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0"));
    }

    @Test
    void findLnodeType() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDataTypeTemplates dataTypeTemplates = std.getDataTypeTemplates();
        LnodeTypeService lnodeTypeService = new LnodeTypeService();

        //When
        TLNodeType tlnodeType =
                lnodeTypeService.findLnodeType(dataTypeTemplates, tlNodeType -> tlNodeType.getLnClass().contains("LPAI")).orElseThrow();

        //Then
        assertThat(tlnodeType)
                .extracting(TLNodeType::getLnClass, TLNodeType::getId)
                .containsExactly(List.of("LPAI"), "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0");
    }

    @Test
    void getFilteredDataAttributes_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("LNodeType0");
        //When
        LnodeTypeService lnodeTypeService = new LnodeTypeService();
        List<DataAttributeRef> result = lnodeTypeService.getFilteredDataAttributes(dtt, dataAttributeRef).toList();
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
    void getFilteredDataAttributes_should_return_all_dai() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("LN2");
        //When
        LnodeTypeService lnodeTypeService = new LnodeTypeService();
        List<DataAttributeRef> result = lnodeTypeService.getFilteredDataAttributes(dtt, dataAttributeRef).toList();
        //Then
        assertThat(result).hasSize(1622);
    }
}