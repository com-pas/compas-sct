// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TFCDA;

import static org.junit.jupiter.api.Assertions.*;

class DataSetInfoTest {

    @Test
    void testConstructor(){
        DataSetInfo dataSetInfo = new DataSetInfo();
        assertNull(dataSetInfo.getName());
        dataSetInfo = new DataSetInfo("DATA_INFO");
        assertEquals("DATA_INFO",dataSetInfo.getName());
        dataSetInfo.setName("DATA_INFO1");
        dataSetInfo.addFCDAInfo(new FCDAInfo());

        assertEquals("DATA_INFO1",dataSetInfo.getName());
        assertFalse(dataSetInfo.getFCDAInfos().isEmpty());
    }

    @Test
    void testFrom(){
        TDataSet dataSet = new TDataSet();
        dataSet.setName("dataset");
        dataSet.getFCDA().add(DTO.createFCDA());

        DataSetInfo dataSetInfo = DataSetInfo.from(dataSet);

        assertEquals("dataset", dataSetInfo.getName());
        assertEquals(1,dataSetInfo.getFCDAInfos().size());
    }

}