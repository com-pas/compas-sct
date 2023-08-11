// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SclDTOTest {

    @Test
    void from_whenCalledWithSclRootAdapter_shouldFillValues(){
        // Given
        UUID id = UUID.randomUUID();
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        HeaderAdapter headerAdapter = mock(HeaderAdapter.class);
        when(sclRootAdapter.getSclVersion()).thenReturn(SclRootAdapter.VERSION);
        when(sclRootAdapter.getSclRevision()).thenReturn(SclRootAdapter.REVISION);
        when(sclRootAdapter.getSclRelease()).thenReturn(SclRootAdapter.RELEASE);
        when(sclRootAdapter.getHeaderAdapter()).thenReturn(headerAdapter);
        when(headerAdapter.getHeaderId()).thenReturn(id.toString());
        when(headerAdapter.getHeaderRevision()).thenReturn("hRevision");
        when(headerAdapter.getHeaderVersion()).thenReturn("hVersion");
        // When
        SclDTO sclDTO  = SclDTO.from(sclRootAdapter);
        // Then
        assertThat(sclDTO.getHeader()).isNotNull();
        assertThat(sclDTO.getHeader().getId()).isEqualTo(id);
        assertThat(sclDTO.getHeader().getVersion()).isEqualTo("hVersion");
        assertThat(sclDTO.getHeader().getRevision()).isEqualTo("hRevision");
    }

    @Test
    void constructor_whenCalledWithoutParameter_shouldFillValues(){
        // When
        SclDTO sclDTO1 = new SclDTO();
        HeaderDTO headerDTO = new HeaderDTO();
        UUID id = UUID.randomUUID();
        headerDTO.setId(id);
        headerDTO.setRevision("hRevision");
        headerDTO.setVersion("hVersion");
        sclDTO1.setHeader(headerDTO);
        sclDTO1.setId(id);
        sclDTO1.setRelease(SclRootAdapter.RELEASE);
        sclDTO1.setRevision(SclRootAdapter.REVISION);
        sclDTO1.setVersion(SclRootAdapter.VERSION);
        // Then
        assertThat(sclDTO1.getHeader()).isNotNull();
        assertThat(sclDTO1.getHeader().getId()).isEqualTo(id);
        assertThat(sclDTO1.getHeader().getVersion()).isEqualTo("hVersion");
        assertThat(sclDTO1.getHeader().getRevision()).isEqualTo("hRevision");
    }

    @Test
    void constructor_whenCalledWithID_shouldFillValues(){
        // When
        SclDTO sclDTO = new SclDTO(UUID.randomUUID());
        sclDTO.setHeader(DTO.createHeaderDTO(UUID.randomUUID()));
        // Then
        assertThat(sclDTO.getWho()).isEqualTo("who");
        assertThat(sclDTO.getWhen()).isEqualTo(DTO.NOW_STR);
        assertThat(sclDTO.getWhat()).isEqualTo("what");
        assertThat(sclDTO.getWhy()).isEqualTo("why");
    }
}