// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;

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
    void getLdeviceStatus_should_return_status_ON() {
        //Given
        TLDevice tlDevice = new TLDevice();
        LN0 ln0 = new LN0();
        PrivateUtils.setStringPrivate(ln0, "COMPAS-LNodeStatus", "on");
        tlDevice.setLN0(ln0);
        //When
        Optional<ActiveStatus> ldeviceStatus = ldeviceService.getLdeviceStatus(tlDevice);
        //Then
        assertThat(ldeviceStatus).contains(ActiveStatus.ON);
    }

    @Test
    void getLdeviceStatus_when_ln0_off_should_return_status_OFF() {
        //Given
        TLDevice tlDevice = new TLDevice();
        LN0 ln0 = new LN0();
        PrivateUtils.setStringPrivate(ln0, "COMPAS-LNodeStatus", "off");
        tlDevice.setLN0(ln0);
        //When
        Optional<ActiveStatus> ldeviceStatus = ldeviceService.getLdeviceStatus(tlDevice);
        //Then
        assertThat(ldeviceStatus).contains(ActiveStatus.OFF);
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
    void findLdevice_with_predicate_should_return_ldevice() {
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
    void findLdevice_with_ldInst_should_return_ldevice() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TIED tied = std.getIED().getFirst();
        //When
        TLDevice ldevice = ldeviceService.findLdevice(tied, "LDTM").orElseThrow();
        //Then
        assertThat(ldevice)
                .extracting(TLDevice::getInst, TLDevice::getLdName)
                .containsExactly("LDTM", "VirtualSAMULDTM");
    }

}
