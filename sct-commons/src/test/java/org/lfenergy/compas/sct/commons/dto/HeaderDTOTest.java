// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.THitem;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HeaderDTOTest {

    @Test
    void testConstructor(){
        UUID id = UUID.randomUUID();
        HeaderDTO headerDTO = DTO.createHeaderDTO(id);


        assertEquals(id,headerDTO.getId());
        assertEquals("1.0",headerDTO.getVersion());
        assertEquals("1.0",headerDTO.getRevision());

        assertFalse(headerDTO.getHistoryItems().isEmpty());
        HeaderDTO.HistoryItem historyItem = headerDTO.getHistoryItems().get(0);
        assertEquals("1.0",historyItem.getRevision());
        assertEquals("1.0",historyItem.getVersion());
        assertEquals("what",historyItem.getWhat());
        assertEquals("why",historyItem.getWhy());
        assertEquals("who",historyItem.getWho());
        assertEquals(DTO.NOW_STR,historyItem.getWhen());

        headerDTO = new HeaderDTO(id,"1.0","1.0");
        assertEquals("1.0",headerDTO.getVersion());
        assertEquals("1.0",headerDTO.getRevision());
    }

    @Test
    void testHItemFrom(){
        THitem tHitem = new THitem();
        tHitem.setRevision("1.0");
        tHitem.setVersion("1.0");
        tHitem.setWhat("what");
        tHitem.setWho("who");
        tHitem.setWhy("why");
        tHitem.setWhen(DTO.NOW_STR);

        HeaderDTO.HistoryItem historyItem = HeaderDTO.HistoryItem.from(tHitem);
        assertEquals("1.0",historyItem.getRevision());
        assertEquals("1.0",historyItem.getVersion());
        assertEquals("what",historyItem.getWhat());
        assertEquals("why",historyItem.getWhy());
        assertEquals("who",historyItem.getWho());
    }
}