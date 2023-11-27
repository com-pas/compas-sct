// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclHelper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn0;

class ControlBlockAdapterTest {

    @Test
    void getName_should_return_name(){
        // Given
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setName("cbName");
        ControlBlockAdapter controlBlockAdapter = new ControlBlockAdapter(null, tgseControl);
        // When
        String result = controlBlockAdapter.getName();
        // Then
        assertThat(result).isEqualTo("cbName");
    }

    @Test
    @Tag("issue-321")
    void addTargetIfNotExists_should_add_target(){
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        // When
        ln0.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        // When
        ControlBlockAdapter controlBlockAdapter = ln0.createControlBlockIfNotExists("cbName", "cbId", "datSet", ControlBlockEnum.GSE);
        LNAdapter targetLn = findLn(scd, "IED_NAME2", "LD_INST21", "ANCR", "1", "prefix");
        // When
        controlBlockAdapter.addTargetIfNotExists(targetLn);

        // Then
        TControl tControl = controlBlockAdapter.getCurrentElem();
        assertThat(tControl).isInstanceOf(TGSEControl.class);
        assertThat(((TGSEControl) tControl).getIEDName())
            .hasSize(1)
            .first()
            .extracting(TControlWithIEDName.IEDName::getApRef, TControlWithIEDName.IEDName::getValue,
                TControlWithIEDName.IEDName::getLdInst, TControlWithIEDName.IEDName::getLnInst, TControlWithIEDName.IEDName::getLnClass,
                TControlWithIEDName.IEDName::getPrefix)
            .containsExactly("AP_NAME", "IED_NAME2", "LD_INST21", "1", List.of("ANCR"), "prefix");
    }

    @Test
    @Tag("issue-321")
    void configureNetwork_should_add_GSE_element() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        TConnectedAP connectedAP = SclHelper.addConnectedAp(scd, "SUB_NETWORK_NAME", "AP_NAME", "IED_NAME1");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        // When
        ln0.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        // When
        ControlBlockAdapter controlBlockAdapter = ln0.createControlBlockIfNotExists("cbName", "cbId", "datSet", ControlBlockEnum.GSE);
        // When
        Optional<SclReportItem> sclReportItem = controlBlockAdapter.configureNetwork(10L, "00-01-02-04-05", 11, (byte) 12, SclConstructorHelper.newDurationInMilliSec(3),
            SclConstructorHelper.newDurationInMilliSec(20));
        // Then
        assertThat(sclReportItem).isEmpty();
        assertThat(connectedAP.getGSE()).hasSize(1);
        TGSE gse = connectedAP.getGSE().get(0);
        assertThat(gse.getLdInst()).isEqualTo("LD_INST11");
        assertThat(gse.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("3"));
        assertThat(gse.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("20"));
        assertThat(gse.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactlyInAnyOrder(
                Tuple.tuple("APPID", "000A"),
                Tuple.tuple("MAC-Address", "00-01-02-04-05"),
                Tuple.tuple("VLAN-ID", "00B"),
                Tuple.tuple("VLAN-PRIORITY", "12")
            );
    }

    @Test
    @Tag("issue-321")
    void configureNetwork_should_add_SMV_element() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        TConnectedAP connectedAP = SclHelper.addConnectedAp(scd, "SUB_NETWORK_NAME", "AP_NAME", "IED_NAME1");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        // When
        ln0.createDataSetIfNotExists("datSet", ControlBlockEnum.SAMPLED_VALUE);
        // When
        ControlBlockAdapter controlBlockAdapter = ln0.createControlBlockIfNotExists("cbName", "cbId", "datSet", ControlBlockEnum.SAMPLED_VALUE);
        // When
        Optional<SclReportItem> sclReportItem = controlBlockAdapter.configureNetwork(10L, "00-01-02-04-05", 11, (byte) 12, null, null);
        // Then
        assertThat(sclReportItem).isEmpty();
        assertThat(connectedAP.getSMV()).hasSize(1);
        TSMV smv = connectedAP.getSMV().get(0);
        assertThat(smv.getLdInst()).isEqualTo("LD_INST11");
        assertThat(smv.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactlyInAnyOrder(
                Tuple.tuple("APPID", "000A"),
                Tuple.tuple("MAC-Address", "00-01-02-04-05"),
                Tuple.tuple("VLAN-ID", "00B"),
                Tuple.tuple("VLAN-PRIORITY", "12")
            );
    }

    @Test
    @Tag("issue-321")
    void configureNetwork_when_connectApNotFound_should_return_sclReportItem() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        SclHelper.addConnectedAp(scd, "SUB_NETWORK_NAME", "AP_NAME", "IED_NAME2"); // ConnectedAp for IED_NAME2 instead of IED_NAME1
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        // When
        ln0.createDataSetIfNotExists("datSet", ControlBlockEnum.SAMPLED_VALUE);
        // When
        ControlBlockAdapter controlBlockAdapter = ln0.createControlBlockIfNotExists("cbName", "cbId", "datSet", ControlBlockEnum.SAMPLED_VALUE);
        // When
        Optional<SclReportItem> sclReportItem = controlBlockAdapter.configureNetwork(10L, "00-01-02-04-05", 11, (byte) 12, null, null);
        // Then
        assertThat(sclReportItem).isPresent();
    }

}
