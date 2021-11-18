// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ConnectedApDTOTest {

    @InjectMocks    private ConnectedApDTO connectedApDTO;

    @Test
    public void testConstruction(){
        ConnectedAPAdapter connectedAPAdapter = Mockito.mock(ConnectedAPAdapter.class);

        Mockito.when(connectedAPAdapter.getApName()).thenReturn(DTO.AP_NAME);
        Mockito.when(connectedAPAdapter.getIedName()).thenReturn(DTO.IED_NAME);

        ConnectedApDTO connectedApDTO = new ConnectedApDTO(connectedAPAdapter);

        assertEquals(DTO.IED_NAME, connectedApDTO.getIedName());
        assertEquals(DTO.AP_NAME, connectedApDTO.getApName());

        connectedApDTO = ConnectedApDTO.from(connectedAPAdapter);

        assertEquals(DTO.IED_NAME, connectedApDTO.getIedName());
        assertEquals(DTO.AP_NAME, connectedApDTO.getApName());
    }
}