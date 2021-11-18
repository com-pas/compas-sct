// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimpleScdTest {

    private static final String HEADER_REVISION = "1.0";
    private static final String HEADER_VERSION = "1.0";
    private static final String FILE_NAME = "filename";
    private static final String DUMMY_PAYLOAD = "blablabla";
    
    @Test
    void testScd(){
        UUID id = UUID.randomUUID();
        SimpleScd scd = createScd(id);
        SimpleScd scd2 = scd;
        assertEquals(scd,scd2);

        assertAll("SCD",
                () -> assertEquals(id, scd.getId()),
                () -> assertArrayEquals(DUMMY_PAYLOAD.getBytes(), scd.getRawXml()),
                () -> assertEquals(HEADER_REVISION, scd.getHeaderRevision()),
                () -> assertEquals(HEADER_VERSION, scd.getHeaderVersion()),
                () -> assertEquals(id, scd.getHeaderId())
        );

        SimpleScd scd1 = createScd(id);
        assertEquals(scd1.hashCode(),scd.hashCode());
        assertEquals(scd1,scd);
        //change
        scd1.setFileName(scd1.getFileName() + "1");
        assertNotEquals(scd1.hashCode(),scd.hashCode());
        assertNotEquals(scd1,scd);
    }
    
    private SimpleScd createScd(UUID id){
        SimpleScd scd = new SimpleScd();
        scd.setId(id);
        scd.setRawXml(DUMMY_PAYLOAD.getBytes());
        scd.setFileName(FILE_NAME);
        scd.setHeaderId(id);
        scd.setHeaderRevision(HEADER_REVISION);
        scd.setHeaderVersion(HEADER_VERSION);

        return scd;
    }

}