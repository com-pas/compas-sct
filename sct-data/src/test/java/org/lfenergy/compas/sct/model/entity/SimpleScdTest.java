// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.entity;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.testhelper.DTO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimpleScdTest {
    @Test
    void testScd(){
        UUID id = UUID.randomUUID();
        SimpleScd scd = DTO.createScd(id);
        SimpleScd scd2 = scd;
        assertEquals(scd,scd2);

        assertAll("SCD",
                () -> assertEquals(id, scd.getId()),
                () -> assertArrayEquals(DTO.DUMMY_PAYLOAD.getBytes(), scd.getRawXml()),
                () -> assertEquals(DTO.HEADER_REVISION, scd.getHeaderRevision()),
                () -> assertEquals(DTO.HEADER_VERSION, scd.getHeaderVersion()),
                () -> assertEquals(id, scd.getHeaderId())
        );

        SimpleScd scd1 = DTO.createScd(id);
        assertEquals(scd1.hashCode(),scd.hashCode());
        assertEquals(scd1,scd);
        //change
        scd1.setFileName(scd1.getFileName() + "1");
        assertNotEquals(scd1.hashCode(),scd.hashCode());
        assertNotEquals(scd1,scd);
    }

}