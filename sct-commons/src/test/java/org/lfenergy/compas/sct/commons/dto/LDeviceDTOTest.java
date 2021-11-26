// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LDeviceDTOTest {

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testConstruction(){
        LDeviceDTO lDeviceDTO = DTO.createLdDTO();

        assertAll("LD_DTO",
                () -> assertEquals(DTO.HOLDER_LD_INST, lDeviceDTO.getLdInst()),
                () -> assertEquals(DTO.LD_NAME, lDeviceDTO.getLdName()),
                () -> assertFalse(lDeviceDTO.getLNodes().isEmpty())
        );

        Set<LNodeDTO> nodeDTOs = Set.of(DTO.createLNodeDTO(),DTO.createLNodeDTO());
        lDeviceDTO.addAll(nodeDTOs);
        assertEquals(3,lDeviceDTO.getLNodes().size());
    }

    @Test
    @Disabled
    void testFrom(){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        LN0Adapter ln0Adapter = Mockito.mock(LN0Adapter.class);
        LNAdapter lnAdapter = Mockito.mock(LNAdapter.class);

        MockedStatic<LNodeDTO> lNodeDTOMockedStatic = Mockito.mockStatic(LNodeDTO.class);
        Mockito.when(lDeviceAdapter.getLNAdapters()).thenReturn(List.of(lnAdapter));
        Mockito.when(lDeviceAdapter.getLN0Adapter()).thenReturn(ln0Adapter);
        lNodeDTOMockedStatic.when(()-> LNodeDTO.from(ln0Adapter,null)).thenReturn(new LNodeDTO());
        lNodeDTOMockedStatic.when(()-> LNodeDTO.from(lnAdapter,null)).thenReturn(new LNodeDTO());
        Mockito.when(lDeviceAdapter.getInst()).thenReturn(DTO.HOLDER_LD_INST);
        Mockito.when(lDeviceAdapter.getLdName()).thenReturn(DTO.LD_NAME);

        LDeviceDTO lDeviceDTO = LDeviceDTO.from(lDeviceAdapter,null);
        assertNotNull(lDeviceDTO);

        Mockito.reset(lnAdapter,ln0Adapter);
    }

    @Test
    void testAddLNOde(){
        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        assertTrue(lDeviceDTO.getLNodes().isEmpty());
        lDeviceDTO.addLNode(DTO.HOLDER_LN_CLASS,DTO.HOLDER_LN_INST,DTO.HOLDER_LN_PREFIX,DTO.LN_TYPE);
        assertFalse(lDeviceDTO.getLNodes().isEmpty());
    }
}