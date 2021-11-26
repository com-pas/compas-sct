// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
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

    @Test
    void testMatchTFCDA(){
        ExtRefInfo extRefInfo = new ExtRefInfo();

        TFCDA tfcda = new TFCDA();
        assertFalse(extRefInfo.matchFCDA(tfcda));

        tfcda.setLdInst("LD");
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.getBindingInfo().setLdInst("LD1");
        assertFalse(extRefInfo.matchFCDA(tfcda));

        extRefInfo.getBindingInfo().setLdInst("LD");
        tfcda.getLnClass().add("LNCLASS");
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.getBindingInfo().setLnClass("LNCLASS1");
        assertFalse(extRefInfo.matchFCDA(tfcda));

        extRefInfo.getBindingInfo().setLnClass("LNCLASS");
        tfcda.setLnInst("1");
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.getBindingInfo().setLnInst("2");
        assertFalse(extRefInfo.matchFCDA(tfcda));

        extRefInfo.getBindingInfo().setLnInst("1");
        tfcda.setPrefix("PR");
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.getBindingInfo().setPrefix("RP");
        assertFalse(extRefInfo.matchFCDA(tfcda));

        extRefInfo.getBindingInfo().setPrefix("PR");
        tfcda.setDoName("Do");
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.getSignalInfo().setPDO("Do1");
        assertFalse(extRefInfo.matchFCDA(tfcda));

        extRefInfo.getSignalInfo().setPDO("Do");
        tfcda.setDaName("Da");
        assertFalse(extRefInfo.matchFCDA(tfcda));
        extRefInfo.getSignalInfo().setPDA("Da1");
        assertFalse(extRefInfo.matchFCDA(tfcda));

        extRefInfo.getSignalInfo().setPDA("Da");
        assertTrue(extRefInfo.matchFCDA(tfcda));
    }


    private ExtRefInfo createExtRef(){
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setSignalInfo(DTO.createExtRefSignalInfo());
        extRefInfo.setBindingInfo(DTO.createExtRefBindingInfo());
        extRefInfo.setSourceInfo(DTO.createExtRefSourceInfo());

        return extRefInfo;
    }

}