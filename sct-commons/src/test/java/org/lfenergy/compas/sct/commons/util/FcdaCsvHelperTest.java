// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;

import java.io.StringReader;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FcdaCsvHelperTest {

    @Test
    void constructor_should_initialize_list_of_FcdaForDataSets_and_FcdaForHmiReportControls() {
        //Given
        StringReader csvSourceForHmiReportControl = new StringReader("LDGRP1;;GAPC;12;Ind1;ST");
        StringReader csvSourceForDataSetAndControlBlocks = new StringReader("GAPC;Ind1;stVal;ST");
        //When
        FcdaCsvHelper fcdaCsvHelper = new FcdaCsvHelper(csvSourceForHmiReportControl, csvSourceForDataSetAndControlBlocks);
        //Then
        List<TFCDA> fcdaForHmiReportControls = fcdaCsvHelper.getFcdaForHmiReportControls();
        assertThat(fcdaForHmiReportControls)
                .isNotNull()
                .hasSize(1);

        Set<FcdaForDataSetsCreation> fcdaForDataSets = fcdaCsvHelper.getFcdaForDataSets();
        assertThat(fcdaForDataSets)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void get_return_list_of_FcdaForDataSets_and_FcdaForHmiReportControls() {
        //Given
        StringReader csvSourceForHmiReportControl = new StringReader("""
                LDGRP1;;GAPC;12;Ind1;ST
                LDCMDSS2;;LLN0;;Health;MX
                """);
        StringReader csvSourceForDataSetAndControlBlocks = new StringReader("""
                GAPC;Ind1;stVal;ST
                LLN0;Health;ctVal;MX
                """);
        FcdaCsvHelper fcdaCsvHelper = new FcdaCsvHelper(csvSourceForHmiReportControl, csvSourceForDataSetAndControlBlocks);
        //When
        List<TFCDA> fcdaForHmiReportControls = fcdaCsvHelper.getFcdaForHmiReportControls();
        Set<FcdaForDataSetsCreation> fcdaForDataSets = fcdaCsvHelper.getFcdaForDataSets();
        //Then
        assertThat(fcdaForHmiReportControls).hasSize(2);
        assertThat(fcdaForHmiReportControls.get(0)).usingRecursiveComparison().isEqualTo(
                SclConstructorHelper.newFcda("LDGRP1", "GAPC", "12", null, "Ind1", null, TFCEnum.ST));
        assertThat(fcdaForHmiReportControls.get(1)).usingRecursiveComparison().isEqualTo(
                SclConstructorHelper.newFcda("LDCMDSS2", "LLN0", null, null, "Health", null, TFCEnum.MX));

        assertThat(fcdaForDataSets).hasSize(2)
                .extracting(FcdaForDataSetsCreation::getLnClass, FcdaForDataSetsCreation::getDoName, FcdaForDataSetsCreation::getDaName, FcdaForDataSetsCreation::getFc)
                .containsExactly(Tuple.tuple("LLN0", "Health", "ctVal", "MX"), Tuple.tuple("GAPC", "Ind1", "stVal", "ST"));
    }
}
