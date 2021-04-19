// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.THitem;
import org.lfenergy.compas.exception.ScdException;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class SclHeaderManagerTest {

    private final String H_VERSION = "1.0";
    private final String TOOL_ID = "COMPAS";
    private final String H_REVISION = "1.0";
    private final UUID ID = UUID.randomUUID();

    @Test
    void testShouldReturnOKWhenAddHeader() throws ScdException {
        SCL receiver = new SCL();
        SclHeaderManager sclHeaderManager = new SclHeaderManager(receiver);
        assertNull(receiver.getHeader());
        sclHeaderManager.addHeader(ID.toString(),H_VERSION,H_REVISION);
        assertAll("header",
                () ->  assertNotNull(receiver.getHeader()),
                () ->  assertEquals(ID.toString(),receiver.getHeader().getId()),
                () ->  assertNotNull(H_VERSION,receiver.getHeader().getVersion()),
                () ->  assertNotNull(H_REVISION,receiver.getHeader().getRevision()),
                () ->  assertNotNull(TOOL_ID,receiver.getHeader().getToolID())
        );
    }

    @Test
    void testShouldReturnNOKWhenAddHeaderCauseHeaderExistAlready() throws ScdException {

        SCL receiver = new SCL();
        SclHeaderManager sclHeaderManager = new SclHeaderManager(receiver);
        assertNull(receiver.getHeader());
        sclHeaderManager.addHeader(ID.toString(),H_VERSION,H_REVISION);

        assertThrows(ScdException.class, () -> sclHeaderManager.addHeader(ID.toString(),H_VERSION,H_REVISION));
    }

    @Test
    void testShouldReturnOKWhenAddHistoryItem() throws ScdException {
        SCL receiver = new SCL();
        SclHeaderManager sclHeaderManager = new SclHeaderManager(receiver);
        assertNull(receiver.getHeader());
        sclHeaderManager.addHeader(ID.toString(),H_VERSION,H_REVISION);
        assertNotNull(receiver.getHeader());
        sclHeaderManager.addHistoryItem("who","what","why");

        assertNotNull(receiver.getHeader().getHistory());
        assertEquals(1,receiver.getHeader().getHistory().getHitem().size());
        THitem tHitem = receiver.getHeader().getHistory().getHitem().get(0);
        assertEquals("who",tHitem.getWho());
        assertEquals("what",tHitem.getWhat());
        assertEquals("why",tHitem.getWhy());
        assertNotNull(tHitem.getWhen());
        assertNotNull(receiver.getHeader().getRevision(),tHitem.getRevision());
        assertNotNull(receiver.getHeader().getVersion(),tHitem.getVersion());
        assertNotNull(tHitem.getWhen());
    }
}