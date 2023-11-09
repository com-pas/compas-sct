// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.dto.DTO.HOLDER_LD_INST;
import static org.lfenergy.compas.sct.commons.dto.DTO.LD_NAME;

class LDeviceDTOTest {

    @Test
    void constructor_should_fill_values() {
        // Given
        // When
        LDeviceDTO lDeviceDTO = new LDeviceDTO(HOLDER_LD_INST, LD_NAME);

        // Then
        assertThat(lDeviceDTO.getLdInst()).isEqualTo(HOLDER_LD_INST);
        assertThat(lDeviceDTO.getLdName()).isEqualTo(LD_NAME);
        assertThat(lDeviceDTO.getLNodes()).isEmpty();
    }

    @Test
    void addLNode_whenCalled_shouldNotUpdateLNodeList(){
        // Given
        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        assertThat(lDeviceDTO.getLNodes()).isEmpty();
        // When
        lDeviceDTO.addLNode(DTO.HOLDER_LN_CLASS, DTO.HOLDER_LN_INST, DTO.HOLDER_LN_PREFIX, DTO.LN_TYPE);
        // Then
        assertThat(lDeviceDTO.getLNodes()).isNotEmpty();
    }

    @Test
    @Tag("issue-321")
    void from_whenCalledWithLDeviceAdapter_shouldFillValues() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());

        // When
        LDeviceDTO lDeviceDTO = LDeviceDTO.from(lDeviceAdapter,null);
        // Then
        assertThat(lDeviceDTO).isNotNull();
    }
}
