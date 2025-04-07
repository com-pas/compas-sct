// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.scl2007b4.model.TDOType;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaServiceTest {

    private final DaService daService = new DaService();

    @Test
    void getDAs() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/std/std_sample.std").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().getFirst();

        //When
        List<TDA> dAs = daService.getDAs(tdoType).toList();

        //Then
        assertThat(dAs).hasSize(8)
                .extracting(TDA::getName)
                .containsExactly("vendor", "hwRev", "swRev", "serNum", "model", "location", "name", "d");
    }

    @Test
    void getFilteredDAs() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/std/std_sample.std").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().getFirst();

        //When
        List<TDA> dAs = daService.getFilteredDAs(tdoType, tda -> tda.getName().equals("vendor")).toList();

        //Then
        assertThat(dAs)
                .hasSize(1)
                .extracting(TDA::getName)
                .containsExactly("vendor");
    }

    @Test
    void findDA() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/std/std_sample.std").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().getFirst();

        //When
        TDA da = daService.findDA(tdoType, tda -> tda.getName().equals("vendor")).orElseThrow();

        //Then
        assertThat(da)
                .extracting(TDA::getName)
                .isEqualTo("vendor");
    }

    @Test
    void findDA_should_find_by_name() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/std/std_sample.std").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().getFirst();

        //When
        TDA da = daService.findDA(tdoType, "vendor").orElseThrow();

        //Then
        assertThat(da)
                .extracting(TDA::getName)
                .isEqualTo("vendor");
    }
}