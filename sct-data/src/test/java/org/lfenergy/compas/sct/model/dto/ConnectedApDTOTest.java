package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class ConnectedApDTOTest {

    @Test
    public void testConstruction(){
        ConnectedApDTO connectedApDTO = DTO.createCapDTO(false);
        ConnectedApDTO connectedApDTO1 = DTO.createCapDTO(true);
        ConnectedApDTO connectedApDTO2 = new ConnectedApDTO(DTO.createCap());
        assertAll("CAP",
                () -> assertEquals(DTO.IED_NAME, connectedApDTO.getIedName()),
                () -> assertEquals(DTO.AP_NAME, connectedApDTO.getApName()),
                () -> assertEquals(DTO.AP_NAME, connectedApDTO2.getApName()),
                () -> assertEquals(connectedApDTO1, connectedApDTO),
                () -> assertFalse(connectedApDTO.equals(null)),
                () -> assertFalse(connectedApDTO.equals("TOTO")),
                () -> assertEquals(connectedApDTO.hashCode(), connectedApDTO1.hashCode())
        );


    }
}