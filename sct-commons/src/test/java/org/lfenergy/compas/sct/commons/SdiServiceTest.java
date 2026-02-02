// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SdiServiceTest {

    private final SdiService sdiService = new SdiService();

    @Test
    void getSdis() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst();

        //When
        List<TSDI> tsdis = sdiService.getSdis(tdoi).toList();

        //Then
        assertThat(tsdis)
                .hasSize(5)
                .extracting(TSDI::getName)
                .containsExactly("units", "minVal", "maxVal", "setMag", "stepSize");
    }

    @Test
    void getFilteredSdis() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst();

        //When
        List<TSDI> tsdis = sdiService.getFilteredSdis(tdoi, tsdi -> tsdi.getName().equals("units")).toList();

        //Then
        assertThat(tsdis)
                .hasSize(1)
                .extracting(TSDI::getName)
                .containsExactly("units");
    }

    @Test
    void findSdi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TDOI tdoi = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().getFirst();

        //When
        Optional<TSDI> sdi = sdiService.findSdi(tdoi, tdai -> tdai.getName().equals("units"));

        //Then
        assertThat(sdi.orElseThrow())
                .extracting(TSDI::getName, tsdi -> tsdi.getSDIOrDAI().size())
                .containsExactly("units", 2);
    }

    @Test
    void getSdis_in_sdi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TSDI tsdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().get(2).getSDIOrDAI().getFirst();

        //When
        List<TSDI> tsdis = sdiService.getSdis(tsdi).toList();

        //Then
        assertThat(tsdis)
                .hasSize(6)
                .extracting(org.lfenergy.compas.scl2007b4.model.TSDI::getName)
                .containsExactly("llLim", "hLim", "min", "lLim", "max", "hhLim");
    }

    @Test
    void getFilteredSdis_in_sdi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TSDI sdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().get(2).getSDIOrDAI().getFirst();

        //When
        List<TSDI> tsdis = sdiService.getFilteredSdis(sdi, tsdi -> tsdi.getName().equals("llLim")).toList();

        //Then
        assertThat(tsdis)
                .hasSize(1)
                .extracting(TSDI::getName)
                .containsExactly("llLim");
    }

    @Test
    void findSdi_in_sdi() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TSDI sdi = (TSDI) std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst().getLN().getFirst().getDOI().get(2).getSDIOrDAI().getFirst();

        //When
        Optional<TSDI> optionalTSDI = sdiService.findSdi(sdi, tsdi -> tsdi.getName().equals("llLim"));

        //Then
        assertThat(optionalTSDI.orElseThrow())
                .extracting(TSDI::getName, tsdi -> tsdi.getSDIOrDAI().size())
                .containsExactly("llLim", 1);
    }
}
