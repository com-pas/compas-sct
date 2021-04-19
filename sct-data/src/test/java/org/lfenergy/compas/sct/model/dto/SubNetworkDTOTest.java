package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class SubNetworkDTOTest {

    @Test
    public void testConstruction(){
        SubNetworkDTO subNetworkDTO = DTO.createSnDTO(false);
        SubNetworkDTO subNetworkDTO1 = DTO.createSnDTO(true);
        SubNetworkDTO subNetworkDTO2 = new SubNetworkDTO(DTO.createSn());

        assertAll("SN",
                () -> assertEquals(DTO.SN_NAME,subNetworkDTO.getName()),
                () -> assertEquals(SubNetworkDTO.SubnetworkType.MMS.toString(),subNetworkDTO.getType()),
                () -> assertFalse(subNetworkDTO.getConnectedAPs().isEmpty()),
                () -> assertFalse(subNetworkDTO2.getConnectedAPs().isEmpty()),
                () -> assertTrue(subNetworkDTO1.getConnectedAPs().isEmpty())
        );

        assertNotNull(SubNetworkDTO.SubnetworkType.fromValue("IP"));
        assertNull(SubNetworkDTO.SubnetworkType.fromValue("IPSEC"));
    }

}