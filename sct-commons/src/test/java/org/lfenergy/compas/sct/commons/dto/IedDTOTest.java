// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import static org.junit.jupiter.api.Assertions.*;

class IedDTOTest {

    @Test
    void testConstruction(){
        IedDTO iedDTO = DTO.createIedDTO();

        assertAll("IedDTO",
                () -> assertEquals(DTO.HOLDER_IED_NAME, iedDTO.getName()),
                () -> assertFalse(iedDTO.getLDevices().isEmpty())
        );
        assertEquals(DTO.HOLDER_IED_NAME, new IedDTO(DTO.HOLDER_IED_NAME).getName());
    }

    @Test
    void testFrom() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));

        IedDTO iedDTO = IedDTO.from(iAdapter,null);
        assertFalse(iedDTO.getLDevices().isEmpty());
    }

    @Test
    void testAddLDevice(){
        IedDTO iedDTO = new IedDTO();
        assertTrue(iedDTO.getLDevices().isEmpty());
        iedDTO.addLDevice(DTO.HOLDER_LD_INST, "LDName");
        assertFalse(iedDTO.getLDevices().isEmpty());

        assertTrue(iedDTO.getLDeviceDTO(DTO.HOLDER_LD_INST).isPresent());
    }

}
