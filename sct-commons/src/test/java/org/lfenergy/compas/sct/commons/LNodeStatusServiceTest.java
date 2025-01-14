// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.testhelpers.SclHelper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;

class LNodeStatusServiceTest {

    private LNodeStatusService lNodeStatusService;

    @BeforeEach
    void setUp() {
        lNodeStatusService = new LNodeStatusService(new LdeviceService(), new LnService(), new DataTypeTemplatesService());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateModStVal")
    void updateLnStatusBasedOnPrivateLNodeStatus_should_update_Mod_stVal(String ldInst, String lnClass, String lnInst, String expected) {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(findDai(scl, "IED_NAME_1", ldInst, lnClass, lnInst, "", "Mod", "stVal"))
                .map(SclHelper::getValue)
                .hasValue(expected);
    }

    public static Stream<Arguments> provideUpdateModStVal() {
        return Stream.of(
                // Tests on LN
                Arguments.of(named("LN 'on;off' to set to 'on'", "LDEVICE_1"), "PDIS", "1", "on"),
                Arguments.of(named("LN 'off;on' to set to 'on'", "LDEVICE_1"), "PDIS", "2", "on"),
                Arguments.of(named("LN 'on' to set to 'on'", "LDEVICE_1"), "PDIS", "3", "on"),
                Arguments.of(named("LN 'on;off' to set to 'off'", "LDEVICE_1"), "PDIS", "4", "off"),
                Arguments.of(named("LN 'off;on' to set to 'off'", "LDEVICE_1"), "PDIS", "5", "off"),
                Arguments.of(named("LN 'off' to set to 'off'", "LDEVICE_1"), "PDIS", "6", "off"),
                // Tests on LN0
                Arguments.of(named("LN0 'on;off' to set to 'on'", "LDEVICE_1"), "LLN0", "", "on"),
                Arguments.of(named("LN0 'off;on' to set to 'on'", "LDEVICE_2"), "LLN0", "", "on"),
                Arguments.of(named("LN0 'on' to set to 'on'", "LDEVICE_3"), "LLN0", "", "on"),
                Arguments.of(named("LN0 'on;off' to set to 'off'", "LDEVICE_4"), "LLN0", "", "off"),
                Arguments.of(named("LN0 'off;on' to set to 'off'", "LDEVICE_5"), "LLN0", "", "off"),
                Arguments.of(named("LN0 'off' to set to 'off'", "LDEVICE_6"), "LLN0", "", "off")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_do_nothing_if_DAI_Mod_stVal_is_missing() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus_without_mod_stval.scd");
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(getLDevices(scl.getIED().getFirst())
                .flatMap(tlDevice -> Stream.concat(Stream.of(tlDevice.getLN0()), tlDevice.getLN().stream())))
                .flatMap(TAnyLN::getDOI)
                .isEmpty();
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_no_compasLNodeStatus_in_LNode_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        scl.getSubstation().getFirst().getVoltageLevel().getFirst().getBay().getFirst().getFunction().getFirst().getLNode().getFirst()
                .getPrivate().clear();
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).containsExactly(
                SclReportItem.error("LNode(iedName=IED_NAME_1, ldInst=LDEVICE_1, lnClass=PDIS, lnInst=1, prefix=)",
                        "The private COMPAS-LNodeStatus of the LNode has invalid value. Expecting one of [on, off] but got : null")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_invalid_compasLNodeStatus_value_in_LNode_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        scl.getSubstation().getFirst().getVoltageLevel().getFirst().getBay().getFirst().getFunction().getFirst().getLNode().getFirst()
                .getPrivate().getFirst().getContent().set(0, "helloworld");
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).containsExactly(
                SclReportItem.error("LNode(iedName=IED_NAME_1, ldInst=LDEVICE_1, lnClass=PDIS, lnInst=1, prefix=)",
                        "The private COMPAS-LNodeStatus of the LNode has invalid value. Expecting one of [on, off] but got : helloworld")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_LNode_does_not_match_any_LN_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        ((TLN) findAnyLn(scl, "IED_NAME_1", "LDEVICE_1", "PDIS", "1", ""))
                .setPrefix("helloworld");
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).containsExactly(
                SclReportItem.error("LNode(iedName=IED_NAME_1, ldInst=LDEVICE_1, lnClass=PDIS, lnInst=1, prefix=)",
                        "LNode in Substation section does not have a matching LN in IED section")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_no_compasLNodeStatus_in_LN_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        findAnyLn(scl, "IED_NAME_1", "LDEVICE_1", "PDIS", "1", "")
                .getPrivate().clear();
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).containsExactly(
                SclReportItem.error("IED(IED_NAME_1)/LD(LDEVICE_1)/LN[PDIS,1,]",
                        "The private COMPAS-LNodeStatus of the LN has invalid value. Expecting one of [off;on, on;off, on, off] but got : null")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_compasLNodeStatus_is_on_in_LNode_but_off_in_LN_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        findAnyLn(scl, "IED_NAME_1", "LDEVICE_1", "LLN0", "", "")
                .getPrivate().getFirst().getContent().set(0, "off");
        findAnyLn(scl, "IED_NAME_1", "LDEVICE_1", "PDIS", "1", "")
                .getPrivate().getFirst().getContent().set(0, "off");
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).containsExactlyInAnyOrder(
                SclReportItem.error("IED(IED_NAME_1)/LD(LDEVICE_1)/LN[LLN0,,]",
                        "Cannot set DAI Mod.stVal to on, because LN private COMPAS-LNodeStatus is set to off"),
                SclReportItem.error("IED(IED_NAME_1)/LD(LDEVICE_1)/LN[PDIS,1,]",
                        "Cannot set DAI Mod.stVal to on, because LN private COMPAS-LNodeStatus is set to off")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_compasLNodeStatus_is_off_in_LNode_but_on_in_LN_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        findAnyLn(scl, "IED_NAME_1", "LDEVICE_4", "LLN0", "", "")
                .getPrivate().getFirst().getContent().set(0, "on");
        findAnyLn(scl, "IED_NAME_1", "LDEVICE_1", "PDIS", "4", "")
                .getPrivate().getFirst().getContent().set(0, "on");
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).containsExactlyInAnyOrder(
                SclReportItem.error("IED(IED_NAME_1)/LD(LDEVICE_4)/LN[LLN0,,]",
                        "Cannot set DAI Mod.stVal to off, because LN private COMPAS-LNodeStatus is set to on"),
                SclReportItem.error("IED(IED_NAME_1)/LD(LDEVICE_1)/LN[PDIS,4,]",
                        "Cannot set DAI Mod.stVal to off, because LN private COMPAS-LNodeStatus is set to on")
        );
    }

    @Test
    void updateLnStatusBasedOnPrivateLNodeStatus_when_Mod_stVal_enumType_does_not_contains_value_should_return_error() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-lnodestatus/lnodestatus.scd");
        scl.getDataTypeTemplates().getEnumType().getFirst().getEnumVal().removeIf(tEnumVal -> tEnumVal.getValue().equals("on"));
        // When
        List<SclReportItem> sclReportItems = lNodeStatusService.updateLnModStValBasedOnLNodeStatus(scl);
        // Then
        assertThat(sclReportItems).contains(
                SclReportItem.error("IED(IED_NAME_1)/LD(LDEVICE_1)/LN[PDIS,1,]",
                        "Cannot set DAI Mod.stVal to 'on' because value is not in EnumType [off, test]")
        );
    }

}
