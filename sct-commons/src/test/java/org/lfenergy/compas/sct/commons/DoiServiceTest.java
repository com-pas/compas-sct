// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DoiServiceTest {

    private final DoiService doiService = new DoiService();

    @Test
    void getDois() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        LN0 ln0 = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0).getLN0();

        //When
        List<TDOI> tdois = doiService.getDois(ln0).toList();

        //Then
        assertThat(tdois)
                .hasSize(4)
                .extracting(TDOI::getName)
                .containsExactly("Beh", "Health", "Mod", "NamPlt");
    }

    @Test
    void getFilteredDois() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        LN0 ln0 = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0).getLN0();

        //When
        List<TDOI> tdois = doiService.getFilteredDois(ln0, tdoi -> tdoi.getName().equals("Beh")).toList();

        //Then
        assertThat(tdois)
                .hasSize(1)
                .extracting(TDOI::getName)
                .containsExactly("Beh");
    }

    @Test
    void findDoi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        LN0 ln0 = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0).getLN0();

        //When
        Optional<TDOI> doi = doiService.findDoi(ln0, tdoi -> tdoi.getName().equals("Beh"));

        //Then
        assertThat(doi.orElseThrow())
                .extracting(TDOI::getName, tdoi -> tdoi.getSDIOrDAI().size())
                .containsExactly("Beh", 1);
    }
}