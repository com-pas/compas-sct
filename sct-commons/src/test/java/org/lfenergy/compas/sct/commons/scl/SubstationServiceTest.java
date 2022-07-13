// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class SubstationServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"/scd-substation-import-ssd/ssd_with_2_substations.xml", "/scd-substation-import-ssd/ssd_without_substations.xml"})
    void testAddSubstation_Check_SSD_Validity(String ssdFileName) throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile(ssdFileName);

        assertThrows(ScdException.class,
            () -> SubstationService.addSubstation(scd, ssd));
    }

    @Test
    void testAddSubstation_SCD_Without_Substation() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        SclRootAdapter expectedScdAdapter = SubstationService.addSubstation(scd, ssd);

        assertNotEquals(scdRootAdapter, expectedScdAdapter);
        assertEquals(expectedScdAdapter.getCurrentElem().getSubstation(), ssdRootAdapter.getCurrentElem().getSubstation());
    }

    @Test
    void testAddSubstation_SCD_With_Different_Substation_Name() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation_name_different.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");

        assertThrows(ScdException.class,
            () -> SubstationService.addSubstation(scd, ssd));
    }

    @Test
    void testAddSubstation_SCD_With_Substation() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        SclRootAdapter expectedScdAdapter = SubstationService.addSubstation(scd, ssd);
        TSubstation expectedTSubstation = expectedScdAdapter.getCurrentElem().getSubstation().get(0);
        TSubstation tSubstation = ssdRootAdapter.getCurrentElem().getSubstation().get(0);

        assertNotEquals(scdRootAdapter, expectedScdAdapter);
        assertEquals(expectedTSubstation.getName(), tSubstation.getName());
        assertEquals(expectedTSubstation.getVoltageLevel().size(), tSubstation.getVoltageLevel().size());
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
        assertThatNoException().isThrownBy(() -> SclTestMarshaller.createWrapper().marshall(scl));
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
