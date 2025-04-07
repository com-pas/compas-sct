// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDOType;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.scl2007b4.model.TSDO;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoServiceTest {

    private final SdoService sdoService = new SdoService();

    @Test
    void getSDOs() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().getFirst();

        //When
        List<TSDO> tsdos = sdoService.getSDOs(tdoType).toList();

        //Then
        assertThat(tsdos)
                .hasSize(1)
                .extracting(TSDO::getName)
                .containsExactly("sdo1");
    }

    @Test
    void getFilteredSDOs() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().get(1);

        //When
        List<TSDO> tsdos = sdoService.getFilteredSDOs(tdoType, tsdo -> tsdo.getName().equals("sdo2")).toList();

        //Then
        assertThat(tsdos)
                .hasSize(1)
                .extracting(TSDO::getName)
                .containsExactly("sdo2");
    }

    @Test
    void findSDO() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().get(1);

        //When
        TSDO sdo = sdoService.findSDO(tdoType, tsdo -> tsdo.getName().equals("sdo2")).orElseThrow();

        //Then
        assertThat(sdo)
                .extracting(TSDO::getName)
                .isEqualTo("sdo2");
    }

    @Test
    void findSDO_should_find_by_id() {
        //Given
        TDataTypeTemplates dataTypeTemplates = SclTestMarshaller.getSCLFromFile("/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml").getDataTypeTemplates();
        TDOType tdoType = dataTypeTemplates.getDOType().get(1);

        //When
        TSDO sdo = sdoService.findSDO(tdoType, "sdo2").orElseThrow();

        //Then
        assertThat(sdo)
                .extracting(TSDO::getName)
                .isEqualTo("sdo2");
    }
}