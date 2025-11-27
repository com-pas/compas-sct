// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
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
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN0().getDOI().get(3);

        //When
        List<TDAI> tdais = daiService.getDais(tdoi).toList();

        //Then
        assertThat(tdais)
                .hasSize(5)
                .extracting(TDAI::getName)
                .containsExactly("paramRev", "valRev", "d", "configRev", "swRev");
    }

    @Test
    void getDais_in_sdi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TSDI tsdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst().getSDIOrDAI().getFirst();

        //When
        List<TDAI> tdais = daiService.getDais(tsdi).toList();

        //Then
        assertThat(tdais)
                .hasSize(2)
                .extracting(TDAI::getName)
                .containsExactly("multiplier", "SIUnit");
    }

    @Test
    void getFilteredDais() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN0().getDOI().get(3);

        //When
        List<TDAI> tdais = daiService.getFilteredDais(tdoi, tdai -> tdai.getName().equals("configRev")).toList();

        //Then
        assertThat(tdais)
                .hasSize(1)
                .extracting(TDAI::getName)
                .containsExactly("configRev");
    }

    @Test
    void getFilteredDais_in_sdi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TSDI tsdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst().getSDIOrDAI().getFirst();

        //When
        List<TDAI> tdais = daiService.getFilteredDais(tsdi, tdai -> tdai.getName().equals("multiplier")).toList();

        //Then
        assertThat(tdais)
                .hasSize(1)
                .extracting(TDAI::getName)
                .containsExactly("multiplier");
    }

    @Test
    void findDai_by_predicate_should_return_dai() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN0().getDOI().get(3);

        //When
        Optional<TDAI> dai = daiService.findDai(tdoi, tdai -> tdai.getName().equals("configRev"));

        //Then
        assertThat(dai.orElseThrow())
                .extracting(TDAI::getName, tdai -> tdai.getVal().size())
                .containsExactly("configRev", 1);
    }

    @Test
    void findDai_in_sdi_by_predicate_should_return_dai() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TSDI tsdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst().getSDIOrDAI().getFirst();

        //When
        Optional<TDAI> dai = daiService.findDai(tsdi, tdai -> tdai.getName().equals("multiplier"));

        //Then
        assertThat(dai.orElseThrow())
                .extracting(TDAI::getName, tdai -> tdai.getVal().size())
                .containsExactly("multiplier", 1);
    }

    @Test
    void findDai_by_name_should_return_dai() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN0().getDOI().get(3);

        //When
        Optional<TDAI> dai = daiService.findDai(tdoi, "configRev");

        //Then
        assertThat(dai.orElseThrow())
                .extracting(TDAI::getName, tdai -> tdai.getVal().size())
                .containsExactly("configRev", 1);
    }

    @Test
    void findDai_in_sdi_by_name_should_return_dai() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TSDI tsdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst().getSDIOrDAI().getFirst();

        //When
        Optional<TDAI> dai = daiService.findDai(tsdi, "multiplier");

        //Then
        assertThat(dai.orElseThrow())
                .extracting(TDAI::getName, tdai -> tdai.getVal().size())
                .containsExactly("multiplier", 1);
    }
}
