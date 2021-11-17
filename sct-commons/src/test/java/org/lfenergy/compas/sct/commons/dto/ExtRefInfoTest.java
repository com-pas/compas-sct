// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefInfoTest {

    @Test
    void testConstruction(){
        ExtRefInfo extRefInfo = createExtRef();

        assertEquals(DTO.DESC,extRefInfo.getSignalInfo().getDesc());
        assertEquals(DTO.P_DA,extRefInfo.getSignalInfo().getPDA());
        assertEquals(DTO.P_DO,extRefInfo.getSignalInfo().getPDO());
        assertTrue(extRefInfo.getSignalInfo().getPLN().contains(DTO.P_LN));
        assertEquals(DTO.INT_ADDR, extRefInfo.getSignalInfo().getIntAddr());
        assertEquals(TServiceType.fromValue(DTO.P_SERV_T),extRefInfo.getSignalInfo().getPServT());
        assertNotNull(extRefInfo.getBindingInfo());
        assertNotNull(extRefInfo.getSourceInfo());

        TExtRef extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());
        ExtRefInfo extRefInfo1 = new ExtRefInfo(extRef);
        assertNotNull(extRefInfo1.getBindingInfo());
        assertNotNull(extRefInfo1.getSourceInfo());
    }



    private ExtRefInfo createExtRef(){
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setSignalInfo(DTO.createExtRefSignalInfo());
        extRefInfo.setBindingInfo(DTO.createExtRefBindingInfo());
        extRefInfo.setSourceInfo(DTO.createExtRefSourceInfo());

        return extRefInfo;
    }

}