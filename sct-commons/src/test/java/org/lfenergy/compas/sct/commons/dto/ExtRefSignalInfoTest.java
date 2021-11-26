// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ExtRefSignalInfoTest {
    @Test
    void testConstruction(){
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        log.info("Signal : {}",signalInfo);
        assertEquals(DTO.DESC,signalInfo.getDesc());
        assertEquals(DTO.P_DA,signalInfo.getPDA());
        assertEquals(DTO.P_DO,signalInfo.getPDO());
        assertEquals(DTO.P_LN,signalInfo.getPLN());
        assertEquals(DTO.INT_ADDR, signalInfo.getIntAddr());
        assertEquals(TServiceType.fromValue(DTO.P_SERV_T),signalInfo.getPServT());

    }

    @Test
    void testInitExtRef(){
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        TExtRef extRef = ExtRefSignalInfo.initExtRef(signalInfo);

        assertEquals(DTO.DESC,extRef.getDesc());
        assertEquals(DTO.P_DA,extRef.getPDA());
        assertEquals(DTO.P_DO,extRef.getPDO());
        assertTrue(extRef.getPLN().contains(DTO.P_LN));
        assertEquals(DTO.INT_ADDR, extRef.getIntAddr());
        assertEquals(TServiceType.fromValue(DTO.P_SERV_T),extRef.getPServT());
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

    @Test
    void testIsValid(){
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        assertTrue(signalInfo.isValid());

        signalInfo.setIntAddr("");
        assertFalse(signalInfo.isValid());
        signalInfo.setPDO("");
        assertFalse(signalInfo.isValid());

    }

}