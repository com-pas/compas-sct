// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void addSubstation_when_SSD_with_Multiple_Substations_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_with_2_substations.xml");

        // When & Then
        assertThrows(ScdException.class, () -> SubstationService.addSubstation(scd, ssd));
    }

    @Test
    void addSubstation_when_SSD_with_No_Substation_should_throw_exception() throws Exception {
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

    @Test
    void updateLNodeIEDNames_should_set_IedName_when_LNode_has_single_private() throws Exception {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/scd-with-substation-lnode.xml");
        // When
        SubstationService.updateLNodeIEDNames(scl);
        // Then
        assertThat(scl.getSubstation()).hasSize(1);
        assertThat(scl.getSubstation().get(0).getVoltageLevel()).hasSize(1);
        assertThat(scl.getSubstation().get(0).getVoltageLevel().get(0).getBay()).hasSize(1);
        assertThat(scl.getSubstation().get(0).getVoltageLevel().get(0).getBay().get(0).getFunction()).hasSize(1);
        List<TLNode> lNodes = scl.getSubstation().get(0).getVoltageLevel().get(0).getBay().get(0).getFunction().get(0).getLNode();
        assertThat(lNodes).extracting(TLNode::getIedName).containsOnlyOnce("iedName1");
        assertIsMarshallable(scl);
    }

    @Test
    void updateLNodeIEDNames_should_set_IedName_when_LNode_has_multiples_private() throws Exception {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/scd-with-substation-lnode.xml");
        // When
        SubstationService.updateLNodeIEDNames(scl);
        // Then
        assertThat(scl.getSubstation()).hasSize(1);
        assertThat(scl.getSubstation().get(0).getVoltageLevel()).hasSize(1);
        assertThat(scl.getSubstation().get(0).getVoltageLevel().get(0).getBay()).hasSize(1);
        assertThat(scl.getSubstation().get(0).getVoltageLevel().get(0).getBay().get(0).getFunction()).hasSize(1);
        List<TLNode> lNodes = scl.getSubstation().get(0).getVoltageLevel().get(0).getBay().get(0).getFunction().get(0).getLNode();
        assertThat(lNodes)
            .hasSize(4)
            .extracting(TLNode::getIedName)
            .containsExactly("iedName1", "iedName2", "iedName3", "iedName4");
        assertThat(lNodes.subList(1, 4)).allSatisfy(
            tlNode -> assertThat(tlNode)
                .hasFieldOrPropertyWithValue("lnClass", List.of("LPHD"))
                .hasFieldOrPropertyWithValue("ldInst", "ldInst1")
                .hasFieldOrPropertyWithValue("lnInst", "1")
                .hasFieldOrPropertyWithValue("lnType", "lnType1"));
        assertThat(lNodes.subList(1, 4)).allSatisfy(tlNode ->
            assertThat(tlNode.getPrivate())
                .isNotEmpty()
                .allSatisfy(tPrivate -> assertThat(PrivateService.getCompasICDHeader(tPrivate)).isPresent()));
        assertIsMarshallable(scl);
    }

    @Test
    void updateLNodeIEDNames_should_throw_exception_when_private_is_missing() throws Exception {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/scd-with-substation-lnode.xml");
        List<TLNode> lNodes = scl.getSubstation().get(0).getVoltageLevel().get(0).getBay().get(0).getFunction().get(0).getLNode();
        lNodes.get(0).unsetPrivate();
        // When & Then
        assertThatThrownBy(() -> SubstationService.updateLNodeIEDNames(scl)).isInstanceOf(ScdException.class);
    }

    @Test
    void updateLNodeIEDNames_should_throw_exception_when_private_is_empty() throws Exception {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/scd-with-substation-lnode.xml");
        List<TLNode> lNodes = scl.getSubstation().get(0).getVoltageLevel().get(0).getBay().get(0).getFunction().get(0).getLNode();
        lNodes.get(0).getPrivate().clear();
        // When & Then
        assertThatThrownBy(() -> SubstationService.updateLNodeIEDNames(scl)).isInstanceOf(ScdException.class);
    }

}
