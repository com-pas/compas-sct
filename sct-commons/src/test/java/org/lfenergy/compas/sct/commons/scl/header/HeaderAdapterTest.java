// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.header;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HeaderAdapterTest {

    @Test
    void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter =
                new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setHeader(new THeader());
        HeaderAdapter hAdapter = sclRootAdapter.getHeaderAdapter();
        assertTrue(hAdapter.amChildElementRef());

        assertThrows(IllegalArgumentException.class,
                () ->new HeaderAdapter(sclRootAdapter,new THeader()));
    }

    @Test
    void testAddHistoryItem() throws ScdException {
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

    @Test
    void addPrivate() {
        SclRootAdapter sclRootAdapter =  new SclRootAdapter(
                "hID","hVersion","hRevision"
        );
        HeaderAdapter hAdapter = sclRootAdapter.getHeaderAdapter();
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThrows(UnsupportedOperationException.class, () -> hAdapter.addPrivate(tPrivate));
    }

    @ParameterizedTest
    @CsvSource(value = {"hID;hVersion;hRevision;Header[@id=\"hID\" and @version=\"hVersion\" and @revision=\"hRevision\"]", ";;;Header[not(@id) and not(@version) and not(@revision)]"}
            , delimiter = ';')
    void elementXPath(String hID, String hVersion, String hRevision,String message) {
        // Given
        THeader tHeader = new THeader();
        tHeader.setId(hID);
        tHeader.setVersion(hVersion);
        tHeader.setRevision(hRevision);
        HeaderAdapter headerAdapter = new HeaderAdapter(null, tHeader);
        // When
        String elementXPathResult = headerAdapter.elementXPath();
        // Then
        assertThat(elementXPathResult).isEqualTo(message);
    }

}
