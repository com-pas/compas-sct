// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LDeviceAdapterTest {

    private IEDAdapter iAdapter;

    @BeforeEach
    public void init() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
    }

    @Test
    void testUpdateLDName() throws Exception {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS1").get());
        lDeviceAdapter.updateLDName();
        assertEquals("IED_NAMELD_INS1",lDeviceAdapter.getLdName());
        iAdapter.setIEDName("VERY_VERY_VERY_VERY_VERY_VERY_LONG_IED_NAME");
        assertThrows(ScdException.class, ()-> lDeviceAdapter.updateLDName());

        assertEquals("LD_INS1", lDeviceAdapter.getInst());
    }

    @Test
    void testGetLNAdapters()  {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());

        assertEquals(1,lDeviceAdapter.getLNAdapters().size());

        assertDoesNotThrow(() -> lDeviceAdapter.getLNAdapter("ANCR","1",null));
        assertThrows(ScdException.class, () -> lDeviceAdapter.getLNAdapter("ANCR","1","pr"));
    }

    @Test
    void testGetExtRefBinders() {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        signalInfo.setPDO("Do.sdo1");
        signalInfo.setPDA("da.bda1.bda2.bda3");

        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
    }

    @Test
    void testGetExtRefInfo() throws Exception {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        List<ExtRefInfo> extRefInfoList = assertDoesNotThrow(()-> lDeviceAdapter.getExtRefInfo());
        assertEquals(2,extRefInfoList.size());
    }

    @Test
    void TestGetDAI() throws Exception {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS1").get());
        var rDtts = lDeviceAdapter.getDAI(new ResumedDataTemplate(),true);
        assertEquals(4,rDtts.size());



        ResumedDataTemplate filter = new ResumedDataTemplate();
        filter.setLnClass(TLLN0Enum.LLN_0.value());
        rDtts = lDeviceAdapter.getDAI(filter,true);
        assertEquals(4,rDtts.size());

        lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        filter.setLnClass("ANCR");
        filter.setLnInst("1");
        rDtts = lDeviceAdapter.getDAI(filter,true);
        assertEquals(2,rDtts.size());
    }

    @Test
    void addPrivate() {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(lDeviceAdapter.getCurrentElem().getPrivate().isEmpty());
        lDeviceAdapter.addPrivate(tPrivate);
        assertEquals(1, lDeviceAdapter.getCurrentElem().getPrivate().size());
    }
}