// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.header;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.junit.jupiter.api.Assertions.*;

public class HeaderAdapterTest {

    @Test
    public void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter =
                new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setHeader(new THeader());
        HeaderAdapter hAdapter = sclRootAdapter.getHeaderAdapter();
        assertTrue(hAdapter.amChildElementRef());

        assertThrows(IllegalArgumentException.class,
                () ->new HeaderAdapter(sclRootAdapter,new THeader()));
    }

    @Test
    public void testAddHistoryItem() throws ScdException {
        SclRootAdapter sclRootAdapter =  new SclRootAdapter(
                "hID","hVersion","hRevision"
        );

        HeaderAdapter hAdapter = sclRootAdapter.getHeaderAdapter();
        assertEquals("hID", hAdapter.getHeaderId());
        assertEquals("hVersion", hAdapter.getHeaderVersion());
        assertEquals("hRevision", hAdapter.getHeaderRevision());

        hAdapter.addHistoryItem("who","what","why");

        assertNotNull(hAdapter.getCurrentElem().getHistory());
        Assertions.assertFalse(hAdapter.getHistoryItems().isEmpty());
        THitem tHitem = hAdapter.getHistoryItems().get(0);
        assertAll("HISTORY",
                () -> assertEquals("who", tHitem.getWho()),
                () -> assertEquals("what", tHitem.getWhat()),
                () -> assertEquals("why", tHitem.getWhy()),
                () -> Assertions.assertEquals(hAdapter.getCurrentElem().getRevision(), tHitem.getRevision()),
                () -> Assertions.assertEquals(hAdapter.getCurrentElem().getVersion(), tHitem.getVersion())
        );

        hAdapter.updateRevision("newRevision");
        hAdapter.updateVersion("newVersion");
        assertEquals("newVersion", hAdapter.getHeaderVersion());
        assertEquals("newRevision", hAdapter.getHeaderRevision());
    }
}