// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class HeaderDTO {

    private UUID id;
    private String version;
    private String revision;
    private List<HistoryItem> historyItems = new ArrayList<>();

    /**
     * Constructor
     * @param id input
     * @param version input
     * @param revision input
     */
    public HeaderDTO(UUID id, String version, String revision) {
        this.id = id;
        this.version = version;
        this.revision = revision;
    }

    /**
     * Creates HeaderDTO from HeaderAdapter object
     * @param headerAdapter input
     * @return HeaderDTO object
     */
    public static HeaderDTO from(HeaderAdapter headerAdapter) {
        HeaderDTO headerDTO = new HeaderDTO();
        headerDTO.id = UUID.fromString(headerAdapter.getHeaderId());
        headerDTO.version = headerAdapter.getHeaderVersion();
        headerDTO.revision = headerAdapter.getHeaderRevision();
        headerAdapter.getHistoryItems().forEach(tHItem -> headerDTO.historyItems.add(HistoryItem.from(tHItem)));

        return headerDTO;
    }

    /**
     * Anaonymous class for History management
     */
    @Getter
    @Setter
    public static class HistoryItem {
        private String version;
        private String revision;
        private String who;
        private String what;
        private String why;
        private String when;

        /**
         * Initializes History
         * @param tHitem input
         * @return HistoryItem object
         */
        public static HistoryItem from(THitem tHitem){
            HistoryItem historyItem = new HistoryItem();
            historyItem.version = tHitem.getVersion();
            historyItem.revision = tHitem.getRevision();
            historyItem.what = tHitem.getWhat();
            historyItem.who = tHitem.getWho();
            historyItem.why = tHitem.getWhy();
            historyItem.when = tHitem.getWhen();
            return historyItem;
        }
    }
}
