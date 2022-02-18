// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefSourceInfoTest {

    @Test
    void testConstruction(){
        ExtRefSourceInfo sourceInfo = DTO.createExtRefSourceInfo();
        ExtRefSourceInfo bindingInfo_bis = DTO.createExtRefSourceInfo();
        ExtRefSourceInfo bindingInfo_ter = new ExtRefSourceInfo(DTO.createExtRef());
        ExtRefSourceInfo bindingInfo_qt = new ExtRefSourceInfo();

        assertEquals(sourceInfo,bindingInfo_ter);
        assertEquals(sourceInfo.hashCode(),bindingInfo_ter.hashCode());
        assertEquals(sourceInfo,bindingInfo_bis);
        assertNotEquals(null, sourceInfo);
        assertNotEquals(sourceInfo, bindingInfo_qt);
        assertNotEquals(sourceInfo, new ExtRefSourceInfo());
    }
}