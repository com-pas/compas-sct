// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDOType;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}