// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefBindingInfoTest {

    @Test
    void testConstruction(){
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo();
        ExtRefBindingInfo bindingInfo_bis = DTO.createExtRefBindingInfo();
        ExtRefBindingInfo bindingInfo_ter = bindingInfo;
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
    }
}