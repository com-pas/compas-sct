// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefSourceInfoTest {
    @Test
    void testConstruction(){
        ExtRefSourceInfo sourceInfo = DTO.createExtRefSourceInfo();
        ExtRefSourceInfo bindingInfo_bis = DTO.createExtRefSourceInfo();
        ExtRefSourceInfo bindingInfo_ter = sourceInfo;
        ExtRefSourceInfo bindingInfo_qt = new ExtRefSourceInfo();

        assertEquals(sourceInfo,bindingInfo_ter);
        assertEquals(sourceInfo,bindingInfo_bis);
        assertNotEquals(sourceInfo, null);
        assertNotEquals(sourceInfo, bindingInfo_qt);
        assertFalse(sourceInfo.equals(new ExtRefSignalInfo()));
    }

}