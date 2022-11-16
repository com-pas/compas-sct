// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LDeviceDTOTest {

    @Test
    void testConstruction(){
        LDeviceDTO lDeviceDTO = DTO.createLdDTO();

        assertAll("LD_DTO",
                () -> assertEquals(DTO.HOLDER_LD_INST, lDeviceDTO.getLdInst()),
                () -> assertEquals(DTO.LD_NAME, lDeviceDTO.getLdName()),
                () -> assertFalse(lDeviceDTO.getLNodes().isEmpty())
        );

        Set<LNodeDTO> nodeDTOs = Set.of(DTO.createLNodeDTO(),DTO.createLNodeDTO());
        lDeviceDTO.addAll(nodeDTOs);
        assertEquals(3,lDeviceDTO.getLNodes().size());
    }


    @Test
    void testAddLNOde(){
        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        assertTrue(lDeviceDTO.getLNodes().isEmpty());
        lDeviceDTO.addLNode(DTO.HOLDER_LN_CLASS,DTO.HOLDER_LN_INST,DTO.HOLDER_LN_PREFIX,DTO.LN_TYPE);
        assertFalse(lDeviceDTO.getLNodes().isEmpty());
    }

    @Test
    //@Disabled
    void testFromLDAdapter() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());

        LDeviceDTO lDeviceDTO = LDeviceDTO.from(lDeviceAdapter,null);
        assertNotNull(lDeviceDTO);
    }
}
