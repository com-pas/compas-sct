// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ConnectedApDTOTest {

    @Test
    void testConstruction(){
        ConnectedAPAdapter connectedAPAdapter = Mockito.mock(ConnectedAPAdapter.class);

        Mockito.when(connectedAPAdapter.getApName()).thenReturn(DTO.AP_NAME);
        Mockito.when(connectedAPAdapter.getIedName()).thenReturn(DTO.HOLDER_IED_NAME);

        ConnectedApDTO connectedApDTO = ConnectedApDTO.from(connectedAPAdapter);

        assertEquals(DTO.HOLDER_IED_NAME, connectedApDTO.iedName());
        assertEquals(DTO.AP_NAME, connectedApDTO.apName());

        connectedApDTO = ConnectedApDTO.from(connectedAPAdapter);

        assertEquals(DTO.HOLDER_IED_NAME, connectedApDTO.iedName());
        assertEquals(DTO.AP_NAME, connectedApDTO.apName());
    }
}