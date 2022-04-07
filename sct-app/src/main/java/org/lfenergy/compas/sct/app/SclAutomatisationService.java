// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.SclService;

import java.util.Optional;

@Slf4j
public class SclAutomatisationService {

    public SclAutomatisationService(){throw new IllegalStateException("SclAutomatisationService class"); }

    public static SclRootAdapter createSCD(@NonNull SCL ssd, @NonNull HeaderDTO headerDTO) throws ScdException {
        SclRootAdapter scdAdapter = SclService.initScl(Optional.ofNullable(headerDTO.getId()),headerDTO.getVersion(),headerDTO.getRevision());
        if(!headerDTO.getHistoryItems().isEmpty()) {
            HeaderDTO.HistoryItem hItem = headerDTO.getHistoryItems().get(0);
            SclService.addHistoryItem(scdAdapter.getCurrentElem(), hItem.getWho(), hItem.getWhat(), hItem.getWhy());
        }
        SclService.addSubstation(scdAdapter.getCurrentElem(), ssd);
        return scdAdapter;
    }
}
