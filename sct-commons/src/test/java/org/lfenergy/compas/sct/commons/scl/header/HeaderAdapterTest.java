// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.header;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenSCLAndHeader_shouldThrowException() {
        //Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        THeader header = new THeader();
        //When Then
        assertThatCode(() -> new HeaderAdapter(sclRootAdapter, header))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenSCLAndHeader_shouldNotThrowException() {
        //Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        SCL scl = new SCL();
        THeader header = new THeader();
        scl.setHeader(header);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        //When Then
        assertThatCode(() -> new HeaderAdapter(sclRootAdapter, header)).doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    // test here should be only for addHistoryItem method
    void testAddHistoryItem() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        assertThat(headerAdapter.getHeaderId()).isEqualTo("hID");
        assertThat(headerAdapter.getHeaderVersion()).isEqualTo("hVersion");
        assertThat(headerAdapter.getHeaderRevision()).isEqualTo("hRevision");
        //When
        headerAdapter.addHistoryItem("who","what","why");
        //Then
        assertThat(headerAdapter.getCurrentElem().getHistory()).isNotNull();
        assertThat(headerAdapter.getHistoryItems()).isNotEmpty();
        THitem tHitem = headerAdapter.getHistoryItems().get(0);
        assertAll("HISTORY",
                () -> assertThat(tHitem.getWho()).isEqualTo("who"),
                () -> assertThat(tHitem.getWhat()).isEqualTo("what"),
                () -> assertThat(tHitem.getWhy()).isEqualTo("why"),
                () -> assertThat(tHitem.getRevision()).isEqualTo(headerAdapter.getCurrentElem().getRevision()),
                () -> assertThat(tHitem.getVersion()).isEqualTo(headerAdapter.getCurrentElem().getVersion()));
        //When
        headerAdapter.updateRevision("newRevision");
        //When
        headerAdapter.updateVersion("newVersion");
        //Then
        assertThat(headerAdapter.getHeaderVersion()).isEqualTo("newVersion");
        assertThat(headerAdapter.getHeaderRevision()).isEqualTo("newRevision");
    }

    @Test
    void addPrivate_should_throw_UnsupportedOperationException() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        //When THen
        assertThatCode(() -> headerAdapter.addPrivate(tPrivate))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"hID;hVersion;hRevision;Header[@id=\"hID\" and @version=\"hVersion\" and @revision=\"hRevision\"]", ";;;Header[not(@id) and not(@version) and not(@revision)]"}
            , delimiter = ';')
    void elementXPath_should_return_expected_xpath_value(String hID, String hVersion, String hRevision,String message) {
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
