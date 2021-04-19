package org.lfenergy.compas.sct.model.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.sct.testhelper.DTO;

class ExtRefDTOTest {

    @Test
    void testIsIdentical(){
        ExtRefDTO extRefDTO = DTO.createExtRefDTO();
        ExtRefDTO extRefDTO_bis = new ExtRefDTO(DTO.createExtRef());
        assertTrue(extRefDTO.isIdentical(DTO.createExtRef()));
        assertTrue(extRefDTO.isIdentical(extRefDTO_bis));
        assertFalse(extRefDTO.isIdentical(new TExtRef()));
        assertFalse(extRefDTO.isIdentical(new ExtRefDTO()));

        extRefDTO_bis.setIntAddr(DTO.INT_ADDR + "_1");
        assertFalse(extRefDTO.isIdentical(extRefDTO_bis));
        extRefDTO_bis.setPLN(DTO.P_LN + "_1");
        assertFalse(extRefDTO.isIdentical(extRefDTO_bis));
    }

}