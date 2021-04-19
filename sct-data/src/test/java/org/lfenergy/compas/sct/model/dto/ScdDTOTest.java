package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ScdDTOTest {


    @Test
    void testScdDTO(){
        UUID id = UUID.randomUUID();
        ScdDTO scdDTO = DTO.createScdDTO(id);
        ScdDTO scdDTO2 = new ScdDTO(id,DTO.FILE_NAME,id,DTO.HEADER_REVISION,DTO.HEADER_VERSION);

        assertAll("scdDTO",
                () -> assertEquals(id,scdDTO.getId()),
                () -> assertEquals(DTO.HEADER_REVISION,scdDTO.getHeaderRevision()),
                () -> assertEquals(DTO.HEADER_VERSION,scdDTO.getHeaderVersion()),
                () -> assertEquals(DTO.FILE_NAME,scdDTO.getFileName()),
                () -> assertEquals("WHO",scdDTO.getWho()),
                () -> assertEquals("WHY",scdDTO.getWhy()),
                () -> assertEquals("WHAT",scdDTO.getWhat()),
                () -> assertEquals(DTO.FILE_NAME,scdDTO2.getFileName()),
                () -> assertEquals(id,scdDTO.getHeaderId())
        );

        ScdDTO scdDTO1 = DTO.createScdDTO(id);

        assertEquals(scdDTO1,scdDTO);

        scdDTO1.setFileName(scdDTO1.getFileName() + "_1");
        assertNotEquals(scdDTO1,scdDTO);
        assertNotEquals(scdDTO1.hashCode(),scdDTO.hashCode());
    }
}