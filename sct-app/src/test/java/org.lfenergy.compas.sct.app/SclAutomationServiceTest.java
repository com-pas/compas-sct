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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SclAutomationServiceTest {

    private HeaderDTO headerDTO;

    @BeforeEach
    void init() {
        headerDTO = new HeaderDTO();
        headerDTO.setRevision("hRevision");
        headerDTO.setVersion("hVersion");
    }

    @Test
    void createSCD_should_return_generatedSCD() throws Exception {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        // When
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO, Set.of(std));
        // Then
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertNull(expectedSCD.getCurrentElem().getHeader().getHistory());
        assertEquals(1, expectedSCD.getCurrentElem().getSubstation().size());
        assertEquals(1, expectedSCD.getCurrentElem().getIED().size());
        assertNotNull(expectedSCD.getCurrentElem().getDataTypeTemplates());
        assertEquals(2, expectedSCD.getCurrentElem().getCommunication().getSubNetwork().size());
        assertIsMarshallable(expectedSCD.getCurrentElem());
    }

    @Test
    void createSCD_With_HItem() throws Exception {
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
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO, Set.of(std1, std2, std3));
        // Then
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertEquals(1, expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().size());
        assertEquals(1, expectedSCD.getCurrentElem().getSubstation().size());
        assertIsMarshallable(expectedSCD.getCurrentElem());
    }

    @Test
    void createSCD_With_HItems() throws Exception {
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
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO, Set.of(std1, std2, std3));
        // Then
        assertNotNull(expectedSCD.getCurrentElem().getHeader().getId());
        assertEquals(1, expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().size());
        assertEquals("what", expectedSCD.getCurrentElem().getHeader().getHistory().getHitem().get(0).getWhat());
        assertIsMarshallable(expectedSCD.getCurrentElem());
    }

    @Test
    void createSCD_SSD_Without_Substation() throws Exception {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd_without_substations.xml");
        // When & Then
        Set<SCL> stdListEmpty = new HashSet<>();
        assertThrows(ScdException.class,
                () -> SclAutomationService.createSCD(ssd, headerDTO, stdListEmpty));
    }

    @Test
    void createSCD_should_throw_exception_when_null_ssd() throws Exception {
        // Given
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("what");
        historyItem.setWho("me");
        historyItem.setWhy("because");
        headerDTO.getHistoryItems().add(historyItem);
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        Set<SCL> stdList = Set.of(std1);

        // When & Then
        assertThrows(NullPointerException.class, () -> SclAutomationService.createSCD(null, headerDTO, stdList));
    }

    @Test
    void createSCD_should_throw_exception_when_null_headerDTO() throws Exception {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/std_1.xml");
        Set<SCL> stdList = Set.of(std1);

        // When & Then
        assertThrows(NullPointerException.class, () -> SclAutomationService.createSCD(ssd, null, stdList));
    }

    @Test
    void createSCD_should_delete_ControlBlocks_DataSet_and_ExtRef_src_attributes() throws Exception {
        // Given
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        SclRootAdapter expectedSCD = SclAutomationService.createSCD(ssd, headerDTO, Set.of(std));
        // Then
        LN0 ln0 = expectedSCD.streamIEDAdapters()
                .findFirst()
                .map(iedAdapter -> iedAdapter.findLDeviceAdapterByLdInst("lDeviceInst1").orElseThrow())
                .map(LDeviceAdapter::getLN0Adapter)
                .map(SclElementAdapter::getCurrentElem)
                .orElseThrow(() -> new RuntimeException("Test shouldn't fail here, please check your XML input file"));

        assertThat(ln0.getDataSet()).isEmpty();
        assertThat(ln0.getInputs().getExtRef()).hasSize(2);
        assertFalse(ln0.getInputs().getExtRef().get(0).isSetSrcLDInst());
        assertIsMarshallable(expectedSCD.getCurrentElem());
    }

    @Test
    void class_should_not_be_instantiable() {
        // Given
        Constructor<?>[] constructors = SclAutomationService.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        // When & Then
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .getCause().isInstanceOf(UnsupportedOperationException.class);
    }
}
