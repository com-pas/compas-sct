// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    protected UUID id;
    protected String version = SclRootAdapter.VERSION;
    protected String revision = SclRootAdapter.REVISION;
    protected short release = SclRootAdapter.RELEASE;
    protected HeaderDTO header;

    public SclDTO(UUID id) {
        this.id = id;
    }

    public static SclDTO from(SclRootAdapter sclRootAdapter) {
        SclDTO sclDTO = new SclDTO();
        sclDTO.version = sclRootAdapter.getSclVersion();
        sclDTO.release = sclRootAdapter.getSclRelease();
        sclDTO.revision = sclRootAdapter.getSclRevision();
        sclDTO.header = HeaderDTO.from(sclRootAdapter.getHeaderAdapter());
        return sclDTO;
    }

    @JsonIgnore
    public String getWho() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWho();
        }
        return "";
    }

    @JsonIgnore
    public String getWhat() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWhat();
        }
        return "";
    }

    @JsonIgnore
    public String getWhy() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWhy();
        }
        return "";
    }

    @JsonIgnore
    public String getWhen() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWhen();
        }
        return "";
    }
}
