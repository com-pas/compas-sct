// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.app.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.junit.jupiter.api.Assertions.*;

class SclAutomatisationServiceTest {

    private SclAutomatisationService sclAutomatisationService;
    private HeaderDTO headerDTO;

    @BeforeEach
    void init(){
        sclAutomatisationService = new SclAutomatisationService();
        headerDTO = new HeaderDTO();
        headerDTO.setRevision("hRevision");
        headerDTO.setVersion("hVersion");
    }

    @Test
    void createSCD() throws Exception {
        SCL ssd = SclTestMarshaller.getSCLFromFile("/ssd-create-scd/ssd.xml");
        SclRootAdapter expectedSCD = sclAutomatisationService.createSCD(ssd, headerDTO);
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertNull(expectedSCD.getCurrentElem().getHeader().getHistory());
        assertEquals(1, expectedSCD.getCurrentElem().getSubstation().size());
    }

    @Test
    @Disabled
    void createSCD_With_HItem() throws Exception {
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        headerDTO.getHistoryItems().add(historyItem);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/ssd-create-scd/ssd.xml");
        SclRootAdapter expectedSCD = sclAutomatisationService.createSCD(ssd, headerDTO);
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertFalse(expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().isEmpty());
        assertEquals(1, expectedSCD.getCurrentElem().getSubstation().size());
    }

    @Test
    @Disabled
    void createSCD1() throws Exception {
        SCL ssd = SclTestMarshaller.getSCLFromFile("/ssd-create-scd/ssd.xml");
        assertThrows(ScdException.class,
                () ->  sclAutomatisationService.createSCD(ssd, headerDTO) );
    }
}