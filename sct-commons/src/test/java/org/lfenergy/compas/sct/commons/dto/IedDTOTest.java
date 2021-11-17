// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IedDTOTest {

    @Test
    public void testConstruction(){
        IedDTO iedDTO = DTO.createIedDTO();

        assertAll("IedDTO",
                () -> assertEquals(DTO.IED_NAME, iedDTO.getName()),
                () -> assertFalse(iedDTO.getLDevices().isEmpty())
        );
        assertEquals(DTO.IED_NAME, new IedDTO(DTO.IED_NAME).getName());
    }

    @Test
    void testFrom(){
        IEDAdapter iedAdapter = Mockito.mock(IEDAdapter.class);
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        MockedStatic<LDeviceDTO>  lDeviceDTOMockedStatic= Mockito.mockStatic(LDeviceDTO.class);
        Mockito.when(iedAdapter.getLDeviceAdapters()).thenReturn(List.of(lDeviceAdapter));
        lDeviceDTOMockedStatic.when(()-> LDeviceDTO.from(lDeviceAdapter,null)).thenReturn(new LDeviceDTO());
        Mockito.when(iedAdapter.getName()).thenReturn(DTO.IED_NAME);


        IedDTO iedDTO = IedDTO.from(iedAdapter,null);
        assertFalse(iedDTO.getLDevices().isEmpty());
        Mockito.reset(lDeviceAdapter);
    }

    @Test
    void testAddLDevice(){
        IedDTO iedDTO = new IedDTO();
        assertTrue(iedDTO.getLDevices().isEmpty());
        iedDTO.addLDevice(DTO.LD_INST, "LDName");
        assertFalse(iedDTO.getLDevices().isEmpty());

        assertTrue(iedDTO.getLDeviceDTO(DTO.LD_INST).isPresent());
    }

}