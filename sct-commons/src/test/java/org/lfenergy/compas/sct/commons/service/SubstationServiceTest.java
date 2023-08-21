// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.service.impl.SubstationService;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

@ExtendWith(MockitoExtension.class)
class SubstationServiceTest {

    @InjectMocks
    SubstationService substationService;

    @Test
    void addSubstation_when_SCD_has_no_substation_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        assertThat(scd.getSubstation().size()).isZero();
        // When
        substationService.addSubstation(scd, ssd);
        // Then
        assertIsMarshallable(scd);
        assertThat(scd.getSubstation().size()).isNotZero();
        assertThat(scd.getSubstation()).isEqualTo(ssd.getSubstation());
    }

    @Test
    void addSubstation_when_SCD_has_a_substation_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        TSubstation scdSubstation = scd.getSubstation().get(0);
        TSubstation ssdSubstation = ssd.getSubstation().get(0);
        assertThat(scdSubstation.getVoltageLevel().stream().map(TVoltageLevel::getBay).count()).isEqualTo(1);
        // When
        substationService.addSubstation(scd, ssd);
        // Then
        assertIsMarshallable(scd);
        assertThat(scdSubstation.getName()).isEqualTo(ssdSubstation.getName());
        assertThat(scd.getSubstation().size()).isEqualTo(ssd.getSubstation().size());
        assertThat(scdSubstation.getVoltageLevel().stream().map(TVoltageLevel::getBay).count()).isEqualTo(2);
    }

    @Test
    void addSubstation_when_SSD_with_multiple_Substations_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_with_2_substations.xml");

        // When & Then
        assertThrows(ScdException.class, () -> substationService.addSubstation(scd, ssd));
    }

    @Test
    void addSubstation_when_SSD_with_no_substation_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_without_substations.xml");

        // When & Then
        assertThrows(ScdException.class, () -> substationService.addSubstation(scd, ssd));
    }

    @Test
    void addSubstation_when_substations_names_differ_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation_name_different.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        // When & Then
        assertThrows(ScdException.class, () -> substationService.addSubstation(scd, ssd));
    }

}
