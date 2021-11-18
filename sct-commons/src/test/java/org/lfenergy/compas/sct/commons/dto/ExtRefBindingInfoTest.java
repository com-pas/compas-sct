// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefBindingInfoTest {

    @Test
    void testConstruction(){
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo();
        ExtRefBindingInfo bindingInfo_bis = DTO.createExtRefBindingInfo();
        ExtRefBindingInfo bindingInfo_ter = new ExtRefBindingInfo(DTO.createExtRef());
        ExtRefBindingInfo bindingInfo_qt = new ExtRefBindingInfo();

        assertEquals(bindingInfo,bindingInfo_ter);
        assertEquals(bindingInfo,bindingInfo_bis);
        assertNotEquals(bindingInfo, null);
        assertNotEquals(bindingInfo, bindingInfo_qt);
        bindingInfo_qt.setDaName(bindingInfo.getDaName());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setDoName(bindingInfo.getDoName());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setIedName(bindingInfo.getIedName());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setLdInst(bindingInfo.getLdInst());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setLnInst(bindingInfo.getLnInst());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setLnClass(bindingInfo.getLnClass());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setPrefix(bindingInfo.getPrefix());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setServiceType(bindingInfo.getServiceType());
        assertEquals(bindingInfo, bindingInfo_qt);
        assertEquals(bindingInfo.hashCode(), bindingInfo_qt.hashCode());
    }

    @Test
    void testIsValid(){
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo();
        assertTrue(bindingInfo.isValid());
        bindingInfo.setLnClass("PIOC");
        bindingInfo.setLnInst("");
        assertFalse(bindingInfo.isValid());
        bindingInfo.setDoName(new DoTypeName("do.sdo1.sdoError"));
        assertFalse(bindingInfo.isValid());
        bindingInfo.setDaName(new DaTypeName(""));
        assertFalse(bindingInfo.isValid());
        bindingInfo.setLnClass("");
        assertFalse(bindingInfo.isValid());
        bindingInfo.setLdInst("");
        assertFalse(bindingInfo.isValid());
        bindingInfo.setIedName("");
        assertFalse(bindingInfo.isValid());

    }
}