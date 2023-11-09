// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TLN;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DataSetServiceTest {

    @Test
    void getDataSets_whenCalledWithLNContainsMatchingFCDA_shouldReturnDataSet() {
        //Given
        TDataSet dataSet = new TDataSet();
        dataSet.setName("datasetName");
        TLN tln = new TLN();
        tln.getDataSet().add(dataSet);
        DataSetService dataSetService = new DataSetService();

        //When
        Set<TDataSet> dataSetInfos = dataSetService.getDataSets(tln).collect(Collectors.toSet());

        //Then
        assertThat(dataSetInfos)
                .hasSize(1)
                .extracting(TDataSet::getName)
                .containsExactly("datasetName");
    }

    @Test
    void getDataSets_whenCalledWithNoDataSetInLN_shouldReturnEmptyList(){
        //Given
        TLN tln = new TLN();
        DataSetService dataSetService = new DataSetService();

        //When
        Set<TDataSet> dataSetInfos = dataSetService.getDataSets(tln).collect(Collectors.toSet());

        //Then
        assertThat(dataSetInfos).isEmpty();
    }
}