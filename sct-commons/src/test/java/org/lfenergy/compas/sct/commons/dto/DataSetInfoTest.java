// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DataSetInfoTest {

    @Test
    void testConstructor(){
        //Given When
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertNull(dataSetInfo.getName());
        dataSetInfo = new DataSetInfo("DATA_INFO");
        assertEquals("DATA_INFO",dataSetInfo.getName());
        dataSetInfo.setName("DATA_INFO1");
        dataSetInfo.addFCDAInfo(new FCDAInfo());
        //Then
        assertEquals("DATA_INFO1",dataSetInfo.getName());
        assertFalse(dataSetInfo.getFCDAInfos().isEmpty());
    }

    @Test
    void testFrom(){
        //Given
        TDataSet dataSet = new TDataSet();
        dataSet.setName("dataset");
        dataSet.getFCDA().add(DTO.createFCDA());
        //When
        DataSetInfo dataSetInfo = DataSetInfo.from(dataSet);
        //Then
        assertEquals("dataset", dataSetInfo.getName());
        assertEquals(1,dataSetInfo.getFCDAInfos().size());
    }

    @Test
    void getDataSets_shouldReturnEmptyList_whenNoDataSetInLN(){
        //Given
        LNAdapter lnAdapter = new LNAdapter(null, new TLN());
        //When
        Set<DataSetInfo> dataSetInfos = DataSetInfo.getDataSets(lnAdapter);
        //Then
        assertThat(dataSetInfos).isEmpty();
    }

    @Test
    void getDataSets_shouldReturnDataSet_whenLNContainsMatchingFCDA(){
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
    void isValid_shouldReturnFalse_whenNameSizeMore32() {
        //Given
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertNull(dataSetInfo.getName());
        dataSetInfo = new DataSetInfo("DATA_INFO_TEST_CHARACTERE_NAME_MORE_THAN_32_CHARACTERES");
        //When
        boolean isValid = dataSetInfo.isValid();
        //Then
        assertThat(isValid).isFalse();
    }
    @Test
    void isValid_shouldReturnFalse_whenFCDAInfoEmpty() {
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertNull(dataSetInfo.getName());
        dataSetInfo = new DataSetInfo("DATA_INFO");
        //When
        boolean isValid = dataSetInfo.isValid();
        //Then
        assertThat(dataSetInfo.getFCDAInfos()).isEmpty();
        assertThat(isValid).isFalse();
    }

    @Test
    void isValid_shouldReturnTrue_whenFCDAInfosValid() {
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