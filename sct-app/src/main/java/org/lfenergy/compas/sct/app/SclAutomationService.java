// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.dto.SubNetworkTypeDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclService;
import org.lfenergy.compas.sct.commons.scl.SubstationService;

import java.util.*;

/**
 * A representation of the <em><b>{@link SclAutomationService SclAutomationService}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SclAutomationService#createSCD(SCL, HeaderDTO, List) Adds all elements under the <b>SCL </b> object from given <b>SSD </b> and <b>STD </b> files}
 *  </ul>
 */
public class SclAutomationService {

    /**
     * Possible Subnetwork and ConnectedAP names which should be used in generated SCD in order a have global coherence
     * Configuration based on used framework can be used to externalize this datas
     */
    public static final List<SubNetworkTypeDTO> SUB_NETWORK_TYPES = List.of(
            new SubNetworkTypeDTO("RSPACE_PROCESS_NETWORK", SubNetworkDTO.SubnetworkType.MMS.toString(), List.of("PROCESS_AP", "TOTO_AP_GE")),
            new SubNetworkTypeDTO("RSPACE_ADMIN_NETWORK", SubNetworkDTO.SubnetworkType.IP.toString(), List.of("ADMIN_AP", "TATA_AP_EFFACEC")));

    private SclAutomationService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Create an SCD file from specified parameters, it calls all functions defined in the process one by one, every step
     * return an SCD file which will be used by the next step.
     * @param ssd : (mandatory) file contains substation datas
     * @param headerDTO : (mandatory) object which hold header datas and historys' one
     * @param stds : list of STD files containing IED datas (IED, Communication and DataTypeTemplate)
     * @return an SCD object
     * @throws ScdException
     */
    public static SCL createSCD(@NonNull SCL ssd, @NonNull HeaderDTO headerDTO, List<SCL> stds) throws ScdException {
        SCL scd = SclService.initScl(headerDTO.getId(), headerDTO.getVersion(), headerDTO.getRevision());
        if (!headerDTO.getHistoryItems().isEmpty()) {
            HeaderDTO.HistoryItem hItem = headerDTO.getHistoryItems().get(0);
            SclService.addHistoryItem(scd, hItem.getWho(), hItem.getWhat(), hItem.getWhy());
        }
        SubstationService.addSubstation(scd, ssd);
        SclService.importSTDElementsInSCD(scd, stds, SUB_NETWORK_TYPES);
        SclService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scd);
        return scd;
    }
}
