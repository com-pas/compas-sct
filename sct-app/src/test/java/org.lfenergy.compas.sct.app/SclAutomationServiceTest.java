// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SclAutomationServiceTest {

    private HeaderDTO headerDTO;

    @BeforeEach
    void init(){
        headerDTO = new HeaderDTO();
        headerDTO.setRevision("hRevision");
        headerDTO.setVersion("hVersion");
    }

    @Test
    void createSCD() throws Exception {
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO);
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertNull(expectedSCD.getCurrentElem().getHeader().getHistory());
        assertEquals(1, expectedSCD.getCurrentElem().getSubstation().size());
    }

    @Test
    void createSCD_With_HItem() throws Exception {
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        headerDTO.getHistoryItems().add(historyItem);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO);
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertEquals(1 ,expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().size());
        assertEquals(1, expectedSCD.getCurrentElem().getSubstation().size());
    }

    @Test
    void createSCD_With_HItems() throws Exception {
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        HeaderDTO.HistoryItem historyItemBis = new HeaderDTO.HistoryItem();
        historyItemBis.setWhat("what Bis");
        historyItemBis.setWho("me bis");
        historyItemBis.setWhy("because bis");
        headerDTO.getHistoryItems().addAll(Arrays.asList(historyItem, historyItemBis));
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO);
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertEquals(1, expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().size());
        assertEquals("what", expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().get(0).getWhat());
    }

        @Test
    void createSCD_SSD_Without_Substation() throws Exception {
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_without_substations.xml");
        assertThrows(ScdException.class,
                () ->  SclAutomationService.createSCD(ssd, headerDTO) );
    }
}