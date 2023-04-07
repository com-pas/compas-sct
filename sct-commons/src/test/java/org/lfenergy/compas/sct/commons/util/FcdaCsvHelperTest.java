// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FcdaCsvHelperTest {

    @Test
    void getFcdaRecords_should_parse_csv_file() {
        //Given
        StringReader csvReader = new StringReader("""
                LDGRP1;;GAPC;12;Ind1;ST
                LDCMDSS2;;LLN0;;Health;MX
                """);
        FcdaCsvHelper fcdaCsvHelper = new FcdaCsvHelper(csvReader);
        //When
        List<TFCDA> fcdaRecords = fcdaCsvHelper.getFcdas();
        //Then
        assertThat(fcdaRecords).hasSize(2);
        assertThat(fcdaRecords.get(0)).usingRecursiveComparison().isEqualTo(
                SclConstructorHelper.newFcda("LDGRP1", "GAPC", "12", null, "Ind1", null, TFCEnum.ST));
        assertThat(fcdaRecords.get(1)).usingRecursiveComparison().isEqualTo(
                SclConstructorHelper.newFcda("LDCMDSS2", "LLN0", null, null, "Health", null, TFCEnum.MX));
    }
}
