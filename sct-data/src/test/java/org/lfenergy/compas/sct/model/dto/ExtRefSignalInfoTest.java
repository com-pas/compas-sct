// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefSignalInfoTest {

    @Test
    void testConstruction(){
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        ExtRefSignalInfo signalInfoBis = DTO.createExtRefSignalInfo();
        ExtRefSignalInfo signalInfoTer = signalInfo;
        ExtRefSignalInfo signalInfoQt = new ExtRefSignalInfo();

        assertEquals(signalInfo,signalInfoTer);
        assertEquals(signalInfo,signalInfoBis);
        assertNotEquals(signalInfo, null);
        assertNotEquals(signalInfo, signalInfoQt);

    }

    @Test
    void testIsWrappedIn(){

        TExtRef tExtRef = DTO.createExtRef();
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo(tExtRef);
        ExtRefSignalInfo signalInfo1 = new ExtRefSignalInfo();
        assertTrue(signalInfo.isWrappedIn(tExtRef));

        assertFalse(signalInfo1.isWrappedIn(tExtRef));

        signalInfo1.setDesc(signalInfo.getDesc());
        assertFalse(signalInfo1.isWrappedIn(tExtRef));

        signalInfo1.setPDA(signalInfo.getPDA());
        assertFalse(signalInfo1.isWrappedIn(tExtRef));

        signalInfo1.setPDO(signalInfo.getPDO());
        assertFalse(signalInfo1.isWrappedIn(tExtRef));

        signalInfo1.setIntAddr(signalInfo.getIntAddr());
        assertFalse(signalInfo1.isWrappedIn(tExtRef));

        signalInfo1.setPLN(signalInfo.getPLN());
        assertFalse(signalInfo1.isWrappedIn(tExtRef));

        signalInfo1.setPServT(signalInfo.getPServT());
        assertTrue(signalInfo1.isWrappedIn(tExtRef));
    }
}