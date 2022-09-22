// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SubstationServiceTest {

    @Test
    void addSubstation_when_SCD_has_no_substation_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        // When
        SclRootAdapter resultScdAdapter = SubstationService.addSubstation(scd, ssd);
        // Then
        assertNotEquals(scdRootAdapter, resultScdAdapter);
        assertEquals(resultScdAdapter.getCurrentElem().getSubstation(), ssdRootAdapter.getCurrentElem().getSubstation());
        assertIsMarshallable(scd);
    }

    @Test
    void addSubstation_when_SCD_has_a_substation_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        TSubstation ssdSubstation = ssdRootAdapter.getCurrentElem().getSubstation().get(0);
        // When
        SclRootAdapter resultScdAdapter = SubstationService.addSubstation(scd, ssd);
        // Then
        TSubstation resultSubstation = resultScdAdapter.getCurrentElem().getSubstation().get(0);
        assertNotEquals(scdRootAdapter, resultScdAdapter);
        assertEquals(ssdSubstation.getName(), resultSubstation.getName());
        assertEquals(ssdSubstation.getVoltageLevel().size(), resultSubstation.getVoltageLevel().size());
        assertIsMarshallable(scd);
    }

    @Test
    void addSubstation_when_SSD_with_multiple_Substations_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_with_2_substations.xml");

        // When & Then
        assertThrows(ScdException.class, () -> SubstationService.addSubstation(scd, ssd));
    }

    @Test
    void addSubstation_when_SSD_with_no_substation_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_without_substations.xml");

        // When & Then
        assertThrows(ScdException.class, () -> SubstationService.addSubstation(scd, ssd));
    }

    @Test
    void addSubstation_when_substations_names_differ_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation_name_different.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        // When & Then
        assertThrows(ScdException.class, () -> SubstationService.addSubstation(scd, ssd));
    }

}
