// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SubNetworkDTOTest {

    @Test
    void testConstructor(){
        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        assertTrue(subNetworkDTO.getConnectedAPs().isEmpty());

        subNetworkDTO = new SubNetworkDTO("sName","IP");
        assertEquals("sName",subNetworkDTO.getName());
        assertEquals("IP",subNetworkDTO.getType());
    }

    @Test
    void testFrom(){
        SubNetworkAdapter subNetworkAdapter = Mockito.mock(SubNetworkAdapter.class);
        ConnectedAPAdapter connectedAPAdapter = Mockito.mock(ConnectedAPAdapter.class);
        when(subNetworkAdapter.getConnectedAPAdapters()).thenReturn(List.of(connectedAPAdapter));
        when(subNetworkAdapter.getName()).thenReturn("sName");
        when(subNetworkAdapter.getType()).thenReturn(SubNetworkDTO.SubnetworkType.IP.toString());
        when(connectedAPAdapter.getApName()).thenReturn(DTO.AP_NAME);
        when(connectedAPAdapter.getIedName()).thenReturn(DTO.HOLDER_IED_NAME);

        SubNetworkDTO subNetworkDTO = SubNetworkDTO.from(subNetworkAdapter);
        assertEquals("sName",subNetworkDTO.getName());
        assertEquals("IP",subNetworkDTO.getType());
        assertFalse(subNetworkDTO.getConnectedAPs().isEmpty());

    }

    @Test
    void createDefaultSubnetwork_should_return_filtered_subnetwork_list() {
        //When
        TCommunication communication = Mockito.mock(TCommunication.class);
        TSubNetwork subNetwork1 = new TSubNetwork();
        subNetwork1.setName("sName");
        subNetwork1.setName(SubNetworkDTO.SubnetworkType.IP.toString());
        TConnectedAP connectedAP1 = new TConnectedAP();
        connectedAP1.setApName("PROCESS_AP");
        connectedAP1.setIedName("IEDName");
        subNetwork1.getConnectedAP().add(connectedAP1);
        when(communication.getSubNetwork()).thenReturn(List.of(subNetwork1));
        List<SubNetworkTypeDTO> subNetworkTypes  =List.of(
                new SubNetworkTypeDTO("RSPACE_PROCESS_NETWORK", SubNetworkDTO.SubnetworkType.MMS.toString(), List.of("PROCESS_AP", "TOTO_AP_GE")),
                new SubNetworkTypeDTO("RSPACE_ADMIN_NETWORK", SubNetworkDTO.SubnetworkType.IP.toString(), List.of("ADMIN_AP", "TATA_AP_EFFACEC")));
        //When
        List<SubNetworkDTO> subNetworkDTOS = SubNetworkDTO.createDefaultSubnetwork("IEDName", communication, subNetworkTypes);
        //Then
        assertThat(subNetworkDTOS).hasSize(2);
        SubNetworkDTO expectedSubNetwork = subNetworkDTOS.stream().filter(subNetworkDTO -> !subNetworkDTO.getConnectedAPs().isEmpty()).findFirst().orElse(new SubNetworkDTO());
        assertThat(expectedSubNetwork.getConnectedAPs()).hasSize(1);

    }
}