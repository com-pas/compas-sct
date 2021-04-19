package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class IedDTOTest {

    @Test
    public void testConstruction(){
        IedDTO iedDTO = DTO.createIedDTO();

        assertAll("IedDTO",
                () -> assertEquals(DTO.IED_NAME, iedDTO.getName()),
                () -> assertFalse(iedDTO.getLDevices().isEmpty())
        );
    }
}