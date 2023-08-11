// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DataSetInfoTest {

    @Test
    @Tag("issue-321")
    void testConstructor(){
        //Given When
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertThat(dataSetInfo.getName()).isNull();
        //When
        dataSetInfo = new DataSetInfo("DATA_INFO");
        //Then
        assertThat(dataSetInfo.getName()).isEqualTo("DATA_INFO");
        //Given
        dataSetInfo.setName("DATA_INFO1");
        dataSetInfo.addFCDAInfo(new FCDAInfo());
        //When
        assertThat(dataSetInfo.getName()).isEqualTo("DATA_INFO1");
        assertThat(dataSetInfo.getFCDAInfos()).isNotEmpty();
    }

    @Test
    void from_WhenCalledWithDataSet_ThenValuesAreFilled(){
        //Given
        TDataSet dataSet = new TDataSet();
        dataSet.setName("dataset");
        dataSet.getFCDA().add(DTO.createFCDA());
        //When
        DataSetInfo dataSetInfo = DataSetInfo.from(dataSet);
        //Then
        assertThat(dataSetInfo.getName()).isEqualTo("dataset");
        assertThat(dataSetInfo.getFCDAInfos()).hasSize(1);
    }

    @Test
    void getDataSets_whenCalledWithNoDataSetInLN_shouldReturnEmptyList(){
        //Given
        LNAdapter lnAdapter = new LNAdapter(null, new TLN());
        //When
        Set<DataSetInfo> dataSetInfos = DataSetInfo.getDataSets(lnAdapter);
        //Then
        assertThat(dataSetInfos).isEmpty();
    }

    @Test
    void getDataSets_whenCalledWithLNContainsMatchingFCDA_shouldReturnDataSet(){
        //Given
        TDataSet dataSet = new TDataSet();
        dataSet.setName("datasetName");
        dataSet.getFCDA().add(DTO.createFCDA());
        TLN tln = new TLN();
        tln.getDataSet().add(dataSet);
        LNAdapter lnAdapter = new LNAdapter(null, tln);
        //When
        Set<DataSetInfo> dataSetInfos = DataSetInfo.getDataSets(lnAdapter);
        //Then
        assertThat(dataSetInfos).hasSize(1)
                .extracting(DataSetInfo::getName).contains("datasetName");
    }


    @Test
    void isValid_whenNameSizeMore32_shouldReturnFalse() {
        //Given
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertThat(dataSetInfo.getName()).isNull();
        dataSetInfo = new DataSetInfo("DATA_INFO_TEST_CHARACTERE_NAME_MORE_THAN_32_CHARACTERES");
        //When
        boolean isValid = dataSetInfo.isValid();
        //Then
        assertThat(isValid).isFalse();
    }
    @Test
    void isValid_whenFCDAInfoEmpty_shouldReturnFalse() {
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertThat(dataSetInfo.getName()).isNull();
        dataSetInfo = new DataSetInfo("DATA_INFO");
        //When
        boolean isValid = dataSetInfo.isValid();
        //Then
        assertThat(dataSetInfo.getFCDAInfos()).isEmpty();
        assertThat(isValid).isFalse();
    }

    @Test
    void isValid_whenFCDAInfosValidshouldReturnTrue() {
        //Given
        TDataSet dataSet = new TDataSet();
        dataSet.setName("dataset");
        dataSet.getFCDA().add(DTO.createFCDA());
        DataSetInfo dataSetInfo = DataSetInfo.from(dataSet);
        //When
        boolean isValid = dataSetInfo.isValid();
        //Then
        assertThat(isValid).isTrue();
    }

}