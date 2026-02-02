// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IedDTOTest {

    @Test
    void constructor_whenCalled_shouldFillValues(){
        // When
        IedDTO iedDTO = DTO.createIedDTO();
        // Then
        assertAll("IedDTO",
                () -> assertThat(iedDTO.getName()).isEqualTo(DTO.HOLDER_IED_NAME),
                () -> assertThat(iedDTO.getLDevices()).isNotEmpty()
        );
        assertThat(new IedDTO(DTO.HOLDER_IED_NAME).getName()).isEqualTo(DTO.HOLDER_IED_NAME);
    }

    @Test
    void from_whenCalledWithIEDAdapter_shouldFillValues() {
        // When
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        // When
        IedDTO iedDTO = IedDTO.from(iAdapter,null);
        // Then
        assertThat(iedDTO.getLDevices()).isNotEmpty();
    }

    @Test
    void addLDevice_whenCalled_shouldUpdateLDevicesList(){
        // Given
        IedDTO iedDTO = new IedDTO();
        assertThat(iedDTO.getLDevices()).isEmpty();
        // When
        iedDTO.addLDevice(DTO.HOLDER_LD_INST, "LDName");
        // Then
        assertThat(iedDTO.getLDevices()).isNotEmpty();
        assertThat(iedDTO.getLDeviceDTO(DTO.HOLDER_LD_INST)).isPresent();
    }

}
