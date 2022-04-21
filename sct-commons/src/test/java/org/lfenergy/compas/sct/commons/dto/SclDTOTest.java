// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SclDTOTest {


    @Test
    void testConstructor(){
        UUID id = UUID.randomUUID();
        SclRootAdapter sclRootAdapter = Mockito.mock(SclRootAdapter.class);
        HeaderAdapter headerAdapter = Mockito.mock(HeaderAdapter.class);
        Mockito.when(sclRootAdapter.getSclVersion()).thenReturn(SclRootAdapter.VERSION);
        Mockito.when(sclRootAdapter.getSclRevision()).thenReturn(SclRootAdapter.REVISION);
        Mockito.when(sclRootAdapter.getSclRelease()).thenReturn(SclRootAdapter.RELEASE);
        Mockito.when(sclRootAdapter.getHeaderAdapter()).thenReturn(headerAdapter);
        Mockito.when(headerAdapter.getHeaderId()).thenReturn(id.toString());
        Mockito.when(headerAdapter.getHeaderRevision()).thenReturn("hRevision");
        Mockito.when(headerAdapter.getHeaderVersion()).thenReturn("hVersion");


        SclDTO sclDTO  = SclDTO.from(sclRootAdapter);
        SclDTO finalSclDTO = sclDTO;
        assertAll("SCL_DTO",
                () -> assertNotNull(finalSclDTO.getHeader()),
                () -> assertEquals(id, finalSclDTO.getHeader().getId()),
                () -> assertEquals("hVersion",finalSclDTO.getHeader().getVersion()),
                () -> assertEquals("hRevision",finalSclDTO.getHeader().getRevision())
        );
        SclDTO sclDTO1 = new SclDTO();
        sclDTO1.setHeader(sclDTO.getHeader());
        sclDTO1.setRelease(sclDTO.getRelease());
        sclDTO1.setId(sclDTO.getId());
        sclDTO1.setRevision(sclDTO.getRevision());
        sclDTO1.setVersion(sclDTO.getVersion());
        assertAll("SCL_DTO",
                () -> assertNotNull(sclDTO1.getHeader()),
                () -> assertEquals(id, sclDTO1.getHeader().getId()),
                () -> assertEquals("hVersion",sclDTO1.getHeader().getVersion()),
                () -> assertEquals("hRevision",sclDTO1.getHeader().getRevision())
        );

        sclDTO = new SclDTO(id);
        assertEquals("",sclDTO.getWho());
        assertEquals("",sclDTO.getWhen());
        assertEquals("",sclDTO.getWhat());
        assertEquals("",sclDTO.getWhy());
        sclDTO.setHeader(DTO.createHeaderDTO(UUID.randomUUID()));

        assertEquals("who",sclDTO.getWho());
        assertEquals(DTO.NOW_STR,sclDTO.getWhen());
        assertEquals("what",sclDTO.getWhat());
        assertEquals("why",sclDTO.getWhy());
    }
}