// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.LdeviceStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LdeviceTest {

    @Test
    void getLdeviceStatus_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        Ldevice ldevice = new Ldevice(std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0));
        //When
        Optional<LdeviceStatus> ldeviceStatus = ldevice.getLdeviceStatus();
        //Then
        assertThat(ldeviceStatus).contains(LdeviceStatus.OFF);
    }
}