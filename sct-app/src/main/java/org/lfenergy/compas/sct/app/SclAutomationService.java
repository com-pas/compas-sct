// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.SclService;
import org.lfenergy.compas.sct.commons.scl.SubstationService;

import java.util.*;

/**
 * A representation of the <em><b>{@link SclAutomationService SclAutomationService}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SclAutomationService#createSCD(SCL, HeaderDTO, Set) Adds all elements under the <b>SCL </b> object from given <b>SSD </b> and <b>STD </b> files}
 *  </ul>
 */
public class SclAutomationService {

    /**
     * Possible Subnetwork and ConnectAP names which should be used in generated SCD in order a have global coherence
     * Configuration based on used framework can be used to externalize this datas
     */
    private static final Map<Pair<String, String>, List<String>> comMap = Map.of(
            Pair.of("RSPACE_PROCESS_NETWORK", SubNetworkDTO.SubnetworkType.MMS.toString()), Arrays.asList("PROCESS_AP", "TOTO_AP_GE"),
            Pair.of("RSPACE_ADMIN_NETWORK", SubNetworkDTO.SubnetworkType.IP.toString()), Arrays.asList("ADMIN_AP", "TATA_AP_EFFACEC"));

    private SclAutomationService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Create a SCD file from specified parameters, it calls all functions defined in the process one by one, every step
     * return a SCD file which will be used by the next step.
     * @param ssd : (mandatory) file contains substation datas
     * @param headerDTO : (mandatory) object which hold header datas and historys' one
     * @param stds : (optional) list of STD files containing IED datas (IED, Communication and DataTypeTemplate)
     * @return a SCD file encapsuled in object SclRootAdapter
     * @throws ScdException
     */
    public static SclRootAdapter createSCD(@NonNull SCL ssd, @NonNull HeaderDTO headerDTO, Set<SCL> stds) throws ScdException {
        SclRootAdapter scdAdapter = SclService.initScl(Optional.ofNullable(headerDTO.getId()),
                headerDTO.getVersion(), headerDTO.getRevision());
        if (!headerDTO.getHistoryItems().isEmpty()) {
            HeaderDTO.HistoryItem hItem = headerDTO.getHistoryItems().get(0);
            SclService.addHistoryItem(scdAdapter.getCurrentElem(), hItem.getWho(), hItem.getWhat(), hItem.getWhy());
        }
        SubstationService.addSubstation(scdAdapter.getCurrentElem(), ssd);
        SclService.importSTDElementsInSCD(scdAdapter, stds, comMap);
        SclService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scdAdapter.getCurrentElem());
        return scdAdapter;
    }
}
