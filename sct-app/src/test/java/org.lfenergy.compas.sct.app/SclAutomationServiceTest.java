// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.sct.commons.api.ControlBlockEditor;
import org.lfenergy.compas.sct.commons.api.SclEditor;
import org.lfenergy.compas.sct.commons.api.SubstationEditor;
import org.lfenergy.compas.sct.commons.dto.HeaderDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SclAutomationServiceTest {

    @InjectMocks
    private SclAutomationService sclAutomationService ;
    @Mock
    private SclEditor sclEditor;
    @Mock
    private SubstationEditor substationEditor;
    @Mock
    private ControlBlockEditor controlBlockEditor;

    public static final short RELEASE = 4;
    public static final String REVISION = "B";
    public static final String VERSION = "2007";
    private SCL scl;
    private HeaderDTO headerDTO;

    @BeforeEach
    void setUp() {
        scl = new SCL();
        scl.setRelease(RELEASE);
        scl.setVersion(VERSION);
        scl.setRevision(REVISION);
        THeader header = new THeader();
        header.setId(UUID.randomUUID().toString());
        scl.setHeader(header);
        headerDTO = new HeaderDTO();
        headerDTO.setId(UUID.randomUUID());
        headerDTO.setRevision("Revision");
        headerDTO.setVersion("Version");
    }

    @Test
    void createSCD_without_headerHistory_should_return_generatedSCD() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        Mockito.when(sclEditor.initScl(any(UUID.class), anyString(), anyString())).thenReturn((SCL) BeanUtils.cloneBean(scl));
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std));
        // Then
        assertThat(scd.getHeader()).isNotNull();
        assertThat(scd.getHeader().getHistory()).isNull();
        assertThat(scd.getDataTypeTemplates()).isNull();
        assertThat(scd.getCommunication()).isNull();
        assertThat(scd.getSubstation()).isEmpty();
        assertThat(scd.getIED()).isEmpty();
        assertIsMarshallable(scd);
        verify(sclEditor, times(1)).initScl(headerDTO.getId(), headerDTO.getVersion(), headerDTO.getRevision());
        verify(sclEditor, times(0)).addHistoryItem(any(SCL.class), anyString(), anyString(), anyString());
        verify(substationEditor, times(1)).addSubstation(any(SCL.class), any(SCL.class));
        verify(sclEditor, times(1)).importSTDElementsInSCD(any(SCL.class), anyList(), anyList());
        verify(controlBlockEditor, times(1)).removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(any(SCL.class));
    }

    @Test
    void createSCD_with_headerHistory_should_return_generatedSCD() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        HeaderDTO.HistoryItem historyItem = new HeaderDTO.HistoryItem();
        historyItem.setWhat("test");
        historyItem.setWho("name");
        historyItem.setWhy("test");
        headerDTO.getHistoryItems().add(historyItem);
        when(sclEditor.initScl(any(UUID.class), anyString(), anyString())).thenReturn((SCL) BeanUtils.cloneBean(scl));
        doNothing().when(sclEditor).addHistoryItem(any(SCL.class), anyString(), anyString(), anyString());
        // When
        SCL scd = sclAutomationService.createSCD(ssd, headerDTO, List.of(std));
        // Then
        assertThat(scd.getHeader()).isNotNull();
        assertThat(scd.getHeader().getHistory()).isNull();
        assertThat(scd.getDataTypeTemplates()).isNull();
        assertThat(scd.getCommunication()).isNull();
        assertThat(scd.getSubstation()).isEmpty();
        assertThat(scd.getIED()).isEmpty();
        assertIsMarshallable(scd);
        verify(sclEditor, times(1)).initScl(headerDTO.getId(), headerDTO.getVersion(), headerDTO.getRevision());
        verify(sclEditor, times(1)).addHistoryItem(any(SCL.class), eq(historyItem.getWho()), eq(historyItem.getWhat()), eq(historyItem.getWhy()));
        verify(substationEditor, times(1)).addSubstation(any(SCL.class), any(SCL.class));
        verify(sclEditor, times(1)).importSTDElementsInSCD(any(SCL.class), anyList(), anyList());
        verify(controlBlockEditor, times(1)).removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(any(SCL.class));
    }

    @Test
    void createSCD_when_sclEditor_initScl_Fail_should_throw_exception() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        doThrow(new ScdException("initScl fail")).when(sclEditor).initScl(any(UUID.class), anyString(), anyString());
        // When Then
        assertThatThrownBy(() -> sclAutomationService.createSCD(ssd, headerDTO, List.of(std)))
                .isInstanceOf(ScdException.class)
                .hasMessage("initScl fail");
    }

    @Test
    void createSCD_when_sclEditor_addHistoryItem_Fail_should_throw_exception() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        headerDTO.getHistoryItems().add(new HeaderDTO.HistoryItem());
        when(sclEditor.initScl(any(UUID.class), anyString(), anyString())).thenReturn((SCL) BeanUtils.cloneBean(scl));
        doThrow(new ScdException("addHistoryItem fail")).when(sclEditor).addHistoryItem(any(SCL.class), any(), any(), any());
        // When Then
        assertThatThrownBy(() -> sclAutomationService.createSCD(ssd, headerDTO, List.of(std)))
                .isInstanceOf(ScdException.class)
                .hasMessage("addHistoryItem fail");
    }

    @Test
    void createSCD_when_substationEditor_addSubstation_Fail_should_throw_exception() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        headerDTO.getHistoryItems().add(new HeaderDTO.HistoryItem());
        when(sclEditor.initScl(any(UUID.class), anyString(), anyString())).thenReturn((SCL) BeanUtils.cloneBean(scl));
        doNothing().when(sclEditor).addHistoryItem(any(SCL.class), any(), any(), any());
        doThrow(new ScdException("addSubstation fail")).when(substationEditor).addSubstation(any(SCL.class), any(SCL.class));
        // When Then
        assertThatThrownBy(() -> sclAutomationService.createSCD(ssd, headerDTO, List.of(std)))
                .isInstanceOf(ScdException.class)
                .hasMessage("addSubstation fail");
    }

    @Test
    void createSCD_when_sclEditor_importSTDElementsInSCD_Fail_should_throw_exception() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        headerDTO.getHistoryItems().add(new HeaderDTO.HistoryItem());
        when(sclEditor.initScl(any(UUID.class), anyString(), anyString())).thenReturn((SCL) BeanUtils.cloneBean(scl));
        doNothing().when(sclEditor).addHistoryItem(any(SCL.class), any(), any(), any());
        doNothing().when(substationEditor).addSubstation(any(SCL.class), any(SCL.class));
        doThrow(new ScdException("importSTDElementsInSCD fail"))
                .when(sclEditor).importSTDElementsInSCD(any(SCL.class), anyList(), anyList());
        // When Then
        assertThatThrownBy(() -> sclAutomationService.createSCD(ssd, headerDTO, List.of(std)))
                .isInstanceOf(ScdException.class)
                .hasMessage("importSTDElementsInSCD fail");
    }

    @Test
    void createSCD_when_controlBlockEditor_removeAllControlBlocksAndDatasetsAndExtRefSrcBindings_Fail_should_throw_exception() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Given
        SCL ssd = (SCL) BeanUtils.cloneBean(scl);
        SCL std = (SCL) BeanUtils.cloneBean(scl);
        headerDTO.getHistoryItems().add(new HeaderDTO.HistoryItem());
        when(sclEditor.initScl(any(UUID.class), anyString(), anyString())).thenReturn((SCL) BeanUtils.cloneBean(scl));
        doNothing().when(sclEditor).addHistoryItem(any(SCL.class), any(), any(), any());
        doNothing().when(substationEditor).addSubstation(any(SCL.class), any(SCL.class));
        doNothing().when(sclEditor).importSTDElementsInSCD(any(SCL.class), anyList(), anyList());
        doThrow(new ScdException("removeAllControlBlocksAndDatasetsAndExtRefSrcBindings fail"))
                .when(controlBlockEditor).removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(any(SCL.class));
        // When Then
        assertThatThrownBy(() -> sclAutomationService.createSCD(ssd, headerDTO, List.of(std)))
                .isInstanceOf(ScdException.class)
                .hasMessage("removeAllControlBlocksAndDatasetsAndExtRefSrcBindings fail");
    }

}
