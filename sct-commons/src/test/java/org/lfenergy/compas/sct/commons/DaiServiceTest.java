// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DaiServiceTest {

    private final DaiService daiService = new DaiService();

    @Test
    void getDais() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDOI tdoi = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0).getLN0().getDOI().get(3);

        //When
        List<TDAI> tdais = daiService.getDais(tdoi).toList();

        //Then
        assertThat(tdais)
                .hasSize(5)
                .extracting(TDAI::getName)
                .containsExactly("paramRev", "valRev", "d", "configRev", "swRev");
    }

    @Test
    void getFilteredDais() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDOI tdoi = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0).getLN0().getDOI().get(3);

        //When
        List<TDAI> tdais = daiService.getFilteredDais(tdoi, tdai -> tdai.getName().equals("configRev")).toList();

        //Then
        assertThat(tdais)
                .hasSize(1)
                .extracting(TDAI::getName)
                .containsExactly("configRev");
    }

    @Test
    void findDai() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDOI tdoi = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0).getLN0().getDOI().get(3);

        //When
        Optional<TDAI> dai = daiService.findDai(tdoi, tdai -> tdai.getName().equals("configRev"));

        //Then
        assertThat(dai.orElseThrow())
                .extracting(TDAI::getName, tdai -> tdai.getVal().size())
                .containsExactly("configRev", 1);
    }
}