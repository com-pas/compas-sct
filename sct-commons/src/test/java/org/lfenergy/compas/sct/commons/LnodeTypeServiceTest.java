// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

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
                .containsExactly(tuple(List.of("LPAI"), "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0"));
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
    void getFilteredDOAndDA_when_given_DoName_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("LNodeType0");
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("SecondDoName");
        dataAttributeRef.setDoName(doTypeName);
        //When
        LnodeTypeService lnodeTypeService = new LnodeTypeService();
        List<DataAttributeRef> result = lnodeTypeService.getFilteredDOAndDA(dtt, dataAttributeRef).toList();
        //Then
        assertThat(result).hasSize(1).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames, DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(tuple("SecondDoName", List.of(), "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null));
    }

    @Test
    void getFilteredDOAndDA_when_given_DO_with_structName_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("LNodeType0");
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("FirstDoName");
        doTypeName.setStructNames(List.of("sdoName1"));
        dataAttributeRef.setDoName(doTypeName);
        DaTypeName daTypeName = new DaTypeName();
        dataAttributeRef.setDaName(daTypeName);
        //When
        LnodeTypeService lnodeTypeService = new LnodeTypeService();
        List<DataAttributeRef> result = lnodeTypeService.getFilteredDOAndDA(dtt, dataAttributeRef).toList();
        //Then
        assertThat(result).hasSize(3).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames, DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(
                        tuple("FirstDoName.sdoName1", List.of("sdoName1"),
                                "sampleDaName21", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName1.sdoName21", List.of("sdoName1", "sdoName21"),
                                "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName1.sdoName21.sdoName31", List.of("sdoName1", "sdoName21", "sdoName31"),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null));
    }

    @Test
    void getFilteredDOAndDA_when_given_DO_with_structNames_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("LNodeType0");
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("FirstDoName");
        doTypeName.addStructName("sdoName1.sdoName21");
        dataAttributeRef.setDoName(doTypeName);
        DaTypeName daTypeName = new DaTypeName();
        dataAttributeRef.setDaName(daTypeName);
        //When
        LnodeTypeService lnodeTypeService = new LnodeTypeService();
        List<DataAttributeRef> result = lnodeTypeService.getFilteredDOAndDA(dtt, dataAttributeRef).toList();
        //Then
        assertThat(result).hasSize(2).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames, DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(
                        tuple("FirstDoName.sdoName1.sdoName21", List.of("sdoName1", "sdoName21"),
                                "sampleDaName31", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null),
                        tuple("FirstDoName.sdoName1.sdoName21.sdoName31", List.of("sdoName1", "sdoName21", "sdoName31"),
                                "sampleDaName41", List.of(), TPredefinedBasicTypeEnum.BOOLEAN, null));
    }

    @Test
    void getFilteredDOAndDA_when_given_DO_and_DA_with_structNames_should_return_expected_dataReference() {
        //Given
        TDataTypeTemplates dtt = initDttFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("LNodeType0");
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("FirstDoName");
        doTypeName.addStructName("sdoName2");
        dataAttributeRef.setDoName(doTypeName);
        DaTypeName daTypeName = new DaTypeName();
        daTypeName.setName("structDaName1");
        daTypeName.addStructName("structBdaName1.enumBdaName22");
        dataAttributeRef.setDaName(daTypeName);
        //When
        LnodeTypeService lnodeTypeService = new LnodeTypeService();
        List<DataAttributeRef> result = lnodeTypeService.getFilteredDOAndDA(dtt, dataAttributeRef).toList();
        //Then
        assertThat(result).hasSize(1).extracting(
                        DataAttributeRef::getDoRef, DataAttributeRef::getSdoNames,
                        DataAttributeRef::getDaRef, DataAttributeRef::getBdaNames, DataAttributeRef::getBType, DataAttributeRef::getType)
                .containsExactlyInAnyOrder(
                        tuple("FirstDoName.sdoName2", List.of("sdoName2"),
                                "structDaName1.structBdaName1.enumBdaName22", List.of("structBdaName1", "enumBdaName22"), TPredefinedBasicTypeEnum.ENUM, "EnumType1"));
    }

}
