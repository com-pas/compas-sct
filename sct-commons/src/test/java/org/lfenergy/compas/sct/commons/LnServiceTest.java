// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LnServiceTest {

    @Test
    void getAnylns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        List<TAnyLN> tAnyLNS = lnService.getAnylns(tlDevice).toList();

        //Then
        assertThat(tAnyLNS)
                .hasSize(2)
                .extracting(TAnyLN::getLnType)
                .containsExactly("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0");
    }

    @Test
    void getDaiModStval_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        Optional<ActiveStatus> daiModStval = lnService.getDaiModStval(tlDevice.getLN0());

        //Then
        assertThat(daiModStval).contains(ActiveStatus.ON);
    }

    @Test
    void getLnStatus_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        ActiveStatus lnStatus = lnService.getLnStatus(tlDevice.getLN().get(0), tlDevice.getLN0());

        //Then
        assertThat(lnStatus).isEqualTo(ActiveStatus.ON);
    }

    @Test
    void getActiveLns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        List<TAnyLN> tAnyLNS = lnService.getActiveLns(tlDevice).toList();

        //Then
        assertThat(tAnyLNS)
                .hasSize(2)
                .extracting(TAnyLN::getLnType, TAnyLN::getDesc)
                .containsExactly(Tuple.tuple("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", ""),
                        Tuple.tuple("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0", ""));
    }
}