package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LDeviceDTOTest {

    @Test
    public void testConstruction(){
        LDeviceDTO lDeviceDTO = DTO.createLdDTO();

        assertAll("LD_DTO",
                () -> assertEquals(DTO.LD_INST, lDeviceDTO.getLdInst()),
                () -> assertEquals(DTO.LD_NAME, lDeviceDTO.getLdName()),
                () -> assertFalse(lDeviceDTO.getLNodes().isEmpty())
        );

        Set<LNodeDTO> nodeDTOs = Set.of(DTO.createLNodeDTO(),DTO.createLNodeDTO());
        lDeviceDTO.addAll(nodeDTOs);
        assertEquals(3,lDeviceDTO.getLNodes().size());
    }
}