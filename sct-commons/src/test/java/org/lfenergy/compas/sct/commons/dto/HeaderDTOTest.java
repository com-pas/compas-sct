// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.THitem;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderDTOTest {

    @Test
    void constructor_whenCalled_shouldFillValues(){
        // Given
        UUID id = UUID.randomUUID();
        // When
        HeaderDTO headerDTO = DTO.createHeaderDTO(id);
        // Then
        assertThat(headerDTO.getId()).isEqualTo(id);
        assertThat(headerDTO.getVersion()).isEqualTo("1.0");
        assertThat(headerDTO.getRevision()).isEqualTo("1.0");
        assertThat(headerDTO.getHistoryItems()).asInstanceOf(InstanceOfAssertFactories.LIST).isNotEmpty();
        HeaderDTO.HistoryItem historyItem = headerDTO.getHistoryItems().getFirst();
        assertThat(historyItem.getRevision()).isEqualTo("1.0");
        assertThat(historyItem.getVersion()).isEqualTo("1.0");
        assertThat(historyItem.getWhat()).isEqualTo("what");
        assertThat(historyItem.getWhy()).isEqualTo("why");
        assertThat(historyItem.getWho()).isEqualTo("who");
        assertThat(historyItem.getWhen()).isEqualTo(DTO.NOW_STR);
        headerDTO = new HeaderDTO(id,"1.0","1.0");
        assertThat(headerDTO.getVersion()).isEqualTo("1.0");
        assertThat(headerDTO.getRevision()).isEqualTo("1.0");
    }

    @Test
    void from_WhenCalledWithHitem_shouldFillValues(){
        // Given
        THitem tHitem = new THitem();
        tHitem.setRevision("1.0");
        tHitem.setVersion("1.0");
        tHitem.setWhat("what");
        tHitem.setWho("who");
        tHitem.setWhy("why");
        tHitem.setWhen(DTO.NOW_STR);
        // When
        HeaderDTO.HistoryItem historyItem = HeaderDTO.HistoryItem.from(tHitem);
        // Then
        assertThat(historyItem.getRevision()).isEqualTo("1.0");
        assertThat(historyItem.getVersion()).isEqualTo("1.0");
        assertThat(historyItem.getWhat()).isEqualTo("what");
        assertThat(historyItem.getWhy()).isEqualTo("why");
        assertThat(historyItem.getWho()).isEqualTo("who");
    }
}
