// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.*;

@Disabled
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
                .containsExactly(Tuple.tuple(TPredefinedCDCEnum.DPL, "RTE_X_X_X_48BA5C40D0913654FA5291A28C0D9716_DPL_V1.0.0"),
                        Tuple.tuple(TPredefinedCDCEnum.DPL, "RTE_X_X_X_D93000A2D6F9B026504B48576A914DA3_DPL_V1.0.0"));

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
    void getDataAttributeRefs_should_return_expected_dataReference() {
        //Given
        String SCD_DTT_DO_SDO_DA_BDA = "/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda_test.xml";
        TDataTypeTemplates dtt = initDttFromFile(SCD_DTT_DO_SDO_DA_BDA);

        DoTypeService doTypeService = new DoTypeService();
        TDOType tdoType = doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId()
                .equals("SDO1")).get();

        DataAttributeRef dataRef = new DataAttributeRef();
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("firstDONAME");
        DaTypeName daTypeName = new DaTypeName();
        dataRef.setDoName(doTypeName);
        dataRef.setDaName(daTypeName);

        //When
        List<DataAttributeRef> list = doTypeService.getDataAttributes(dtt, tdoType, dataRef);
        //Then
        assertThat(list).hasSize(8);
        assertThat(list.stream().map(DataAttributeRef::getDoRef))
                .containsExactly(
                        "firstDONAME.unused",
                        "firstDONAME.unused.otherSdo",
                        "firstDONAME.unused.otherSdo.otherSdo2",
                        "firstDONAME.sdo2",
                        "firstDONAME.sdo2",
                        "firstDONAME.sdo2",
                        "firstDONAME.sdo2",
                        "firstDONAME");
        assertThat(list.stream().map(DataAttributeRef::getDaRef))
                .containsExactly(
                        "unused",
                        "unused",
                        "danameForotherSdo2",
                        "da1",
                        "da2.bda1sample",
                        "da2.bda1Struct.bda2sample",
                        "da2.bda1Struct.bda2Enum",
                        "daname");
    }

    @Test
    void getDataAttributeRefs_should_return_all_dai() {
        // GIVEN
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        TDataTypeTemplates dtt = scd.getDataTypeTemplates();

        DoTypeService doTypeService = new DoTypeService();
        TDOType tdoType = doTypeService.findDoType(dtt, tdoType1 -> tdoType1.getId()
                .equals("DO11")).get();
        DataAttributeRef dataRef = new DataAttributeRef();
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("firstDONAME");
        DaTypeName daTypeName = new DaTypeName();
        dataRef.setDoName(doTypeName);
        dataRef.setDaName(daTypeName);
        // When
        List<DataAttributeRef> list = doTypeService.getDataAttributes(dtt, tdoType, dataRef);
        // Then
        assertThat(list).hasSize(811);
    }
}
