// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class IedServiceTest {

    @Test
    void getFilteredIeds_should_return_ldevices() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        IedService iedService = new IedService();

        //When
        List<TIED> tieds = iedService.getFilteredIeds(std, tied -> "IED4d4fe1a8cda64cf88a5ee4176a1a0eef".equals(tied.getName())).toList();

        //Then
        assertThat(tieds)
                .hasSize(1)
                .extracting(TIED::getName, TIED::getType)
                .containsExactly(Tuple.tuple("IED4d4fe1a8cda64cf88a5ee4176a1a0eef", "ADU"));
    }

    @Test
    void findByName_should_return_ied_infos() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");
        IedService iedService = new IedService();

        //When
        TIED tied = iedService.findByName(std, "IED_NAME1").orElseThrow();

        //Then
        assertThat(tied)
                .extracting(TIED::getName, TIED::getType)
                .containsExactly("IED_NAME1", null);
    }

    @Test
    void findByName_should_fail() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");
        IedService iedService = new IedService();

        //When
        //Then
        assertThatCode(() -> iedService.findByName(std, null))
                .isInstanceOf(ScdException.class)
                .hasMessage("The given iedName is null");
    }

    @Test
    void findIed_should_return_ldevice() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        IedService iedService = new IedService();

        //When
        TIED tied = iedService.findIed(std, ied -> "IED4d4fe1a8cda64cf88a5ee4176a1a0eef".equals(ied.getName())).orElseThrow();

        //Then
        assertThat(tied)
                .extracting(TIED::getName, TIED::getType)
                .containsExactly("IED4d4fe1a8cda64cf88a5ee4176a1a0eef", "ADU");
    }

}