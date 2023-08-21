// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SclAutomationServiceTest {

    SclAutomationService sclAutomationService = new SclAutomationService();

    private HeaderDTO headerDTO;

    @BeforeEach
    void init() {
        headerDTO = new HeaderDTO();
        headerDTO.setId(UUID.randomUUID());
        headerDTO.setRevision("hRevision");
        headerDTO.setVersion("hVersion");
    }

    @Test
    void createSCD_should_return_generatedSCD() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std));
        // Then
        assertNotNull(scd.getHeader().getId());
        assertNull(scd.getHeader().getHistory());
        assertEquals(1, scd.getSubstation().size());
        assertEquals(1, scd.getIED().size());
        assertNotNull(scd.getDataTypeTemplates());
        assertEquals(2, scd.getCommunication().getSubNetwork().size());
        assertIsMarshallable(scd);
    }

    @Test
    void createSCD_With_HItem() {
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
        assertNotNull(scd.getHeader().getId());
        assertEquals(1, scd.getHeader().getHistory().getHitem().size());
        assertEquals(1, scd.getSubstation().size());
        assertIsMarshallable(scd);
    }

    @Test
    void createSCD_With_HItems() {
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
        assertNotNull(scd.getHeader().getId());
        assertEquals(1, scd.getHeader().getHistory().getHitem().size());
        assertEquals("what", scd.getHeader().getHistory().getHitem().get(0).getWhat());
        assertIsMarshallable(scd);
    }

    @Test
    void createSCD_SSD_Without_Substation() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_without_substations.xml");
        // When & Then
        List<SCL> stdListEmpty = List.of();
        assertThrows(ScdException.class,
                () -> sclAutomationService.createSCD(ssd, headerDTO, stdListEmpty));
    }

    @Test
    void createSCD_should_throw_exception_when_null_ssd() {
        // Given
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        headerDTO.getHistoryItems().add(historyItem);
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        List<SCL> stdList = List.of(std1);

        // When & Then
        assertThrows(NullPointerException.class, () -> sclAutomationService.createSCD(null, headerDTO, stdList));
    }

    @Test
    void createSCD_should_throw_exception_when_null_headerDTO() {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        List<SCL> stdList = List.of(std1);

        // When & Then
        assertThrows(NullPointerException.class, () -> sclAutomationService.createSCD(ssd, null, stdList));
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
        assertFalse(ln0.getInputs().getExtRef().get(0).isSetSrcLDInst());
        assertIsMarshallable(scd);
    }

}
