// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LdeviceServiceTest {

    private LdeviceService ldeviceService;

    @BeforeEach
    void setUp() {
        ldeviceService = new LdeviceService(new LnService());
    }

    @Test
    void getLdeviceStatus_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();
        //When
        Optional<ActiveStatus> ldeviceStatus = ldeviceService.getLdeviceStatus(tlDevice);
        //Then
        assertThat(ldeviceStatus).contains(ActiveStatus.ON);
    }

    @Test
    void getLdevices_should_return_ldevices() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TIED tied = std.getIED().getFirst();
        //When
        List<TLDevice> tlDevices = ldeviceService.getLdevices(tied).toList();
        //Then
        assertThat(tlDevices)
                .hasSize(2)
                .extracting(TLDevice::getInst, TLDevice::getLdName)
                .containsExactly(Tuple.tuple("LDSUIED", "VirtualSAMULDSUIED"),
                        Tuple.tuple("LDTM", "VirtualSAMULDTM"));
    }

    @Test
    void getFilteredLdevices_should_return_ldevices() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TIED tied = std.getIED().getFirst();
        //When
        List<TLDevice> tlDevices = ldeviceService.getFilteredLdevices(tied, tlDevice -> "LDTM".equals(tlDevice.getInst())).toList();
        //Then
        assertThat(tlDevices)
                .hasSize(1)
                .extracting(TLDevice::getInst, TLDevice::getLdName)
                .containsExactly(Tuple.tuple("LDTM", "VirtualSAMULDTM"));
    }

    @Test
    void findLdevice_should_return_ldevice() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TIED tied = std.getIED().getFirst();
        //When
        TLDevice ldevice = ldeviceService.findLdevice(tied, tlDevice -> "LDTM".equals(tlDevice.getInst())).orElseThrow();
        //Then
        assertThat(ldevice)
                .extracting(TLDevice::getInst, TLDevice::getLdName)
                .containsExactly("LDTM", "VirtualSAMULDTM");
    }

    @Test
    void getActiveLdevices_should_return_ldevices() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TIED tied = std.getIED().getFirst();
        //When
        List<TLDevice> tlDevices = ldeviceService.getActiveLdevices(tied).toList();
        //Then
        assertThat(tlDevices)
                .hasSize(1)
                .extracting(TLDevice::getInst, TLDevice::getLdName)
                .containsExactly(Tuple.tuple("LDSUIED", "VirtualSAMULDSUIED"));
    }

}
