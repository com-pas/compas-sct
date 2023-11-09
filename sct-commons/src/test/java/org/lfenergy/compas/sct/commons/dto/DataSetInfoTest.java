// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDataSet;

import static org.assertj.core.api.Assertions.assertThat;

class DataSetInfoTest {

    @Test
    void from_WhenCalledWithDataSet_ThenValuesAreFilled(){
        //Given
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName("dataset");
        tDataSet.getFCDA().add(DTO.createFCDA());

        //When
        DataSetInfo dataSetInfo = new DataSetInfo(tDataSet);

        //Then
        assertThat(dataSetInfo.getName()).isEqualTo("dataset");
        assertThat(dataSetInfo.getFcdaInfos()).hasSize(1);
    }

    @Test
    void isValid_whenNameSizeMore32_shouldReturnFalse() {
        //Given
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName("DATA_INFO_TEST_CHARACTERE_NAME_MORE_THAN_32_CHARACTERES");
        tDataSet.getFCDA().add(DTO.createFCDA());
        DataSetInfo dataSetInfo = new DataSetInfo(tDataSet);

        //When
        boolean isValid = dataSetInfo.isValid();

        //Then
        assertThat(isValid).isFalse();
    }
    @Test
    void isValid_whenFCDAInfoEmpty_shouldReturnFalse() {
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName("DATA_INFO");
        DataSetInfo dataSetInfo = new DataSetInfo(tDataSet);

        //When
        boolean isValid = dataSetInfo.isValid();

        //Then
        assertThat(dataSetInfo.getFcdaInfos()).isEmpty();
        assertThat(isValid).isFalse();
    }

    @Test
    void isValid_whenFCDAInfosValidshouldReturnTrue() {
        //Given
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName("dataset");
        tDataSet.getFCDA().add(DTO.createFCDA());
        DataSetInfo dataSetInfo = new DataSetInfo(tDataSet);
        //When
        boolean isValid = dataSetInfo.isValid();
        //Then
        assertThat(isValid).isTrue();
    }

}