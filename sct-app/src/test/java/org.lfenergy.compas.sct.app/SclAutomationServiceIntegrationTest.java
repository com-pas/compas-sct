// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.*;
import org.lfenergy.compas.sct.commons.api.ControlBlockEditor;
import org.lfenergy.compas.sct.commons.api.SclEditor;
import org.lfenergy.compas.sct.commons.api.SubstationEditor;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ControlService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SclAutomationServiceIntegrationTest {

    private SclAutomationService sclAutomationService ;
    private static final SclEditor sclEditor = new SclService() ;
    private static final SubstationEditor substationEditor = new SubstationService(new VoltageLevelService()) ;
    private static final ControlBlockEditor controlBlockEditor = new ControlBlockEditorService(new ControlService(), new LdeviceService(new LnService()), new ConnectedAPService(), new SubNetworkService());

    private HeaderDTO headerDTO;

    @BeforeEach
    void setUp() {
        headerDTO = new HeaderDTO();
        headerDTO.setId(UUID.randomUUID());
        headerDTO.setRevision("hRevision");
        headerDTO.setVersion("hVersion");
        sclAutomationService = new SclAutomationService(sclEditor, substationEditor, controlBlockEditor);
    }

    @Test
    void createSCD_should_return_generatedSCD() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std));
        // Then
        assertThat(scd.getHeader().getId()).isNotNull();
        assertThat(scd.getHeader().getHistory()).isNull();
        assertThat(scd.getSubstation()).hasSize(1);
        assertThat(scd.getIED()).hasSize(1);
        assertThat(scd.getDataTypeTemplates()).isNotNull();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertIsMarshallable(scd);
    }

    @Test
    void createSCD_WithHItem_should_return_generatedSCD() {
        // Given
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        headerDTO.getHistoryItems().add(historyItem);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/std_2.xml");
        SCL std3 = SclTestMarshaller.getSCLFromFile("/std_3.xml");
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std1, std2, std3));
        // Then
        assertThat(scd.getHeader().getId()).isNotNull();
        assertThat(scd.getHeader().getHistory().getHitem()).hasSize(1);
        assertThat(scd.getSubstation()).hasSize(1);
        assertIsMarshallable(scd);
    }

    @Test
    void createSCD_WithManyHItem_should_return_generatedSCD() {
        // Given
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        HeaderDTO.HistoryItem historyItemBis = new HeaderDTO.HistoryItem();
        historyItemBis.setWhat("what Bis");
        historyItemBis.setWho("me bis");
        historyItemBis.setWhy("because bis");
        headerDTO.getHistoryItems().addAll(Arrays.asList(historyItem, historyItemBis));
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/std_2.xml");
        SCL std3 = SclTestMarshaller.getSCLFromFile("/std_3.xml");
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std1, std2, std3));
        // Then
        assertThat(scd.getHeader().getId()).isNotNull();
        assertThat(scd.getHeader().getHistory().getHitem()).hasSize(1);
        assertThat(scd.getHeader().getHistory().getHitem().getFirst().getWhat()).isEqualTo("what");
        assertIsMarshallable(scd);
    }

    @Test
    void createSCD_whenSSDWithoutSubstation_shouldThrowException() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_without_substations.xml");
        // When & Then
        List<SCL> stdListEmpty = List.of();
        assertThatThrownBy(() -> sclAutomationService.createSCD(ssd, headerDTO, stdListEmpty))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void createSCD_whenSSDIsNull_shouldThrowException() {
        // Given
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        headerDTO.getHistoryItems().add(historyItem);
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        List<SCL> stdList = List.of(std1);

        // When & Then
        assertThatCode(() -> sclAutomationService.createSCD(null, headerDTO, stdList))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createSCD_whenheaderDTOIsNull_shouldThrowException() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        List<SCL> stdList = List.of(std1);

        // When & Then
        assertThatCode(() -> sclAutomationService.createSCD(ssd, null, stdList))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createSCD_should_delete_ControlBlocks_DataSet_and_ExtRef_src_attributes() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std));
        // Then
        LN0 ln0 = new SclRootAdapter(scd).streamIEDAdapters()
                .findFirst()
                .map(iedAdapter -> iedAdapter.findLDeviceAdapterByLdInst("lDeviceInst1").orElseThrow())
                .map(LDeviceAdapter::getLN0Adapter)
                .map(SclElementAdapter::getCurrentElem)
                .orElseThrow(() -> new RuntimeException("Test shouldn't fail here, please check your XML input file"));

        assertThat(ln0.getDataSet()).isEmpty();
        assertThat(ln0.getInputs().getExtRef()).hasSize(2);
        assertThat(ln0.getInputs().getExtRef().getFirst().isSetSrcLDInst()).isFalse();
        assertIsMarshallable(scd);
    }

}
