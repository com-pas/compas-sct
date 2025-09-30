// SPDX-FileCopyrightText: 2025 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BayServiceTest {

    private BayService bayService;

    @BeforeEach
    void setUp() {
        bayService = new BayService();

    }

    @Test
    void getBays_on_Scl_should_return_bays() {
        // Given
        SCL scl = new SCL();
        TSubstation tSubstation = createSubstation();
        scl.getSubstation().add(tSubstation);

        // When
        Stream<TBay> bays = bayService.getBays(scl);

        // Then
        assertThat(bays)
                .extracting(TBay::getName)
                .containsExactlyInAnyOrder("BayA", "BayB", "BayC");
    }

    @Test
    void getBays_on_Substation_should_return_bays() {
        // Given
        TSubstation tSubstation = createSubstation();

        // When
        Stream<TBay> bays = bayService.getBays(tSubstation);

        // Then
        assertThat(bays)
                .extracting(TBay::getName)
                .containsExactlyInAnyOrder("BayA", "BayB", "BayC");
    }

    @Test
    void getFilteredBays_should_succeed() {
        // Given
        TSubstation tSubstation = createSubstation();
        // When
        Stream<TBay> result = bayService.getFilteredBays(tSubstation, tBay -> tBay.getName().equals("BayA") || tBay.getName().equals("BayC"));
        // Then
        assertThat(result)
                .extracting(TBay::getName)
                .containsExactlyInAnyOrder("BayA", "BayC");
    }

    @Test
    void findBay_by_predicate_should_succeed() {
        // Given
        TSubstation tSubstation = createSubstation();
        // When
        Optional<TBay> result = bayService.findBay(tSubstation, tBay -> tBay.getName().equals("BayA"));
        // Then
        assertThat(result)
                .map(TBay::getName)
                .hasValue("BayA");
    }

    @Test
    void findBay_by_name_should_succeed() {
        // Given
        TSubstation tSubstation = createSubstation();
        // When
        Optional<TBay> result = bayService.findBay(tSubstation, "BayA");
        // Then
        assertThat(result)
                .map(TBay::getName)
                .hasValue("BayA");
    }

    @Test
    void findBay_should_succeed() {
        // Given
        TSubstation tSubstation = createSubstation();
        // When
        Optional<TBay> result = bayService.findBay(tSubstation, "BayA");
        // Then
        assertThat(result)
                .map(TBay::getName)
                .hasValue("BayA");
    }

    private static TSubstation createSubstation() {
        TSubstation tSubstation = new TSubstation();
        TVoltageLevel tVoltageLevel1 = new TVoltageLevel();
        tSubstation.getVoltageLevel().add(tVoltageLevel1);
        tVoltageLevel1.getBay().add(createBay("BayA"));
        tVoltageLevel1.getBay().add(createBay("BayB"));
        TVoltageLevel tVoltagelevel2 = new TVoltageLevel();
        tSubstation.getVoltageLevel().add(tVoltagelevel2);
        tVoltagelevel2.getBay().add(createBay("BayC"));
        return tSubstation;
    }

    private static TBay createBay(String name) {
        TBay bay = new TBay();
        bay.setName(name);
        return bay;
    }

}
