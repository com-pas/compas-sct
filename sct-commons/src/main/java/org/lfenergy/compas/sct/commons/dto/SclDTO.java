// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.UUID;


/**
 * A representation of the model object <em><b>SCL</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SclDTO#getId <em>Id</em>}</li>
 *   <li>{@link SclDTO#getVersion <em>Version</em>}</li>
 *   <li>{@link SclDTO#getRevision <em>Revision</em>}</li>
 *   <li>{@link SclDTO#getHeader <em>Refers To Header</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.SCL
 */
@Getter
@Setter
@NoArgsConstructor
public class SclDTO {
    protected UUID id;
    protected String version = SclRootAdapter.VERSION;
    protected String revision = SclRootAdapter.REVISION;
    protected short release = SclRootAdapter.RELEASE;
    protected HeaderDTO header;

    /**
     * Constructor
     * @param id input
     */
    public SclDTO(UUID id) {
        this.id = id;
    }

    /**
     * Initializes SclDTO
     * @param sclRootAdapter input
     * @return SclDTO object value
     */
    public static SclDTO from(SclRootAdapter sclRootAdapter) {
        SclDTO sclDTO = new SclDTO();
        sclDTO.version = sclRootAdapter.getSclVersion();
        sclDTO.release = sclRootAdapter.getSclRelease();
        sclDTO.revision = sclRootAdapter.getSclRevision();
        sclDTO.header = HeaderDTO.from(sclRootAdapter.getHeaderAdapter());
        return sclDTO;
    }

    /**
     * Gets History Who parameter value
     * @return string who value
     */
    @JsonIgnore
    public String getWho() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWho();
        }
        return "";
    }

    /**
     * Gets History What value
     * @return string what value
     */
    @JsonIgnore
    public String getWhat() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWhat();
        }
        return "";
    }

    /**
     * Gets History Why value
     * @return string why value
     */
    @JsonIgnore
    public String getWhy() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWhy();
        }
        return "";
    }

    /**
     * Gets History When value
     * @return string when value
     */
    @JsonIgnore
    public String getWhen() {
        if(header != null && !header.getHistoryItems().isEmpty()){
            return header.getHistoryItems().get(0).getWhen();
        }
        return "";
    }
}
