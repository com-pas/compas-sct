// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SclDTO {
    private UUID id;
    private String version;
    private String revision;
    private short release;
    private Header header;

    public static SclDTO from(SclRootAdapter sclRootAdapter) {
        SclDTO sclDTO = new SclDTO();
        sclDTO.version = sclRootAdapter.getSclVersion();
        sclDTO.release = sclRootAdapter.getSclRelease();
        sclDTO.revision = sclRootAdapter.getSclRevision();
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        sclDTO.header = new Header();
        sclDTO.header.id = UUID.fromString(headerAdapter.getHeaderId());
        sclDTO.header.revision = headerAdapter.getHeaderRevision();
        sclDTO.header.version = headerAdapter.getHeaderVersion();

        return sclDTO;
    }

    @Getter
    @Setter
    public static class Header {
        private UUID id;
        private String version;
        private String revision;
    }
}
