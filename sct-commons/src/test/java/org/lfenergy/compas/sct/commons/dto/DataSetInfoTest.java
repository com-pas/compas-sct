// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;

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
        assertFalse(dataSetInfo.getFcdaInfos().isEmpty());
    }

}