// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ConnectedApDTOTest {

    @Test
    void from_whenCalledWithConnectedAPAdapter_shouldFillValues() {
        // Given
        ConnectedAPAdapter connectedAPAdapter = Mockito.mock(ConnectedAPAdapter.class);
        when(connectedAPAdapter.getApName()).thenReturn(DTO.AP_NAME);
        when(connectedAPAdapter.getIedName()).thenReturn(DTO.HOLDER_IED_NAME);

        // When
        ConnectedApDTO connectedApDTO = ConnectedApDTO.from(connectedAPAdapter);

        //Then
        assertThat(connectedApDTO.iedName()).isEqualTo(DTO.HOLDER_IED_NAME);
        assertThat(connectedApDTO.apName()).isEqualTo(DTO.AP_NAME);
    }
}