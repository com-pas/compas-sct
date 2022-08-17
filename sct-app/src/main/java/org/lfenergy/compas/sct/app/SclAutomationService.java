// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.SclService;
import org.lfenergy.compas.sct.commons.scl.SubstationService;

import java.util.*;

@Slf4j
public class SclAutomationService {

    private static final Map<Pair<String, String>, List<String>> comMap = Map.of(
            Pair.of("RSPACE_PROCESS_NETWORK", SubNetworkDTO.SubnetworkType.MMS.toString()), Arrays.asList("PROCESS_AP", "TOTO_AP_GE"),
            Pair.of("RSPACE_ADMIN_NETWORK", SubNetworkDTO.SubnetworkType.IP.toString()), Arrays.asList("ADMIN_AP","TATA_AP_EFFACEC"));

    private SclAutomationService(){throw new IllegalStateException("SclAutomationService class"); }

    public static SclRootAdapter createSCD(@NonNull SCL ssd, @NonNull HeaderDTO headerDTO, Set<SCL> stds) throws ScdException {
        SclRootAdapter scdAdapter = SclService.initScl(Optional.ofNullable(headerDTO.getId()),
                headerDTO.getVersion(),headerDTO.getRevision());
        if(!headerDTO.getHistoryItems().isEmpty()) {
            HeaderDTO.HistoryItem hItem = headerDTO.getHistoryItems().get(0);
            SclService.addHistoryItem(scdAdapter.getCurrentElem(), hItem.getWho(), hItem.getWhat(), hItem.getWhy());
        }
        SubstationService.addSubstation(scdAdapter.getCurrentElem(), ssd);
        SclService.importSTDElementsInSCD(scdAdapter, stds, comMap);
        return scdAdapter;
    }
}
