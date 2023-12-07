// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;

import java.io.StringReader;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FcdaCsvHelperTest {

    @Test
    void constructor_should_initialize_list_of_FcdaForDataSets() {
        //Given
        StringReader csvSourceForDataSetAndControlBlocks = new StringReader("GAPC;Ind1;stVal;ST");
        //When
        FcdaCsvHelper fcdaCsvHelper = new FcdaCsvHelper(csvSourceForDataSetAndControlBlocks);
        //Then
        Set<FcdaForDataSetsCreation> fcdaForDataSets = fcdaCsvHelper.getFcdaForDataSets();
        assertThat(fcdaForDataSets)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void get_return_list_of_FcdaForDataSets() {
        //Given
        StringReader csvSourceForDataSetAndControlBlocks = new StringReader("""
                GAPC;Ind1;stVal;ST
                LLN0;Health;ctVal;MX
                """);
        FcdaCsvHelper fcdaCsvHelper = new FcdaCsvHelper(csvSourceForDataSetAndControlBlocks);
        //When
        Set<FcdaForDataSetsCreation> fcdaForDataSets = fcdaCsvHelper.getFcdaForDataSets();
        //Then
        assertThat(fcdaForDataSets).hasSize(2)
                .extracting(FcdaForDataSetsCreation::getLnClass, FcdaForDataSetsCreation::getDoName, FcdaForDataSetsCreation::getDaName, FcdaForDataSetsCreation::getFc)
                .containsExactly(Tuple.tuple("LLN0", "Health", "ctVal", "MX"), Tuple.tuple("GAPC", "Ind1", "stVal", "ST"));
    }
}
