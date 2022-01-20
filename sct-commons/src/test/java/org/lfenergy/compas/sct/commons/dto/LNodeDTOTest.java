// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.SclTestMarshaller;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LNodeDTOTest {

    @Test
    void testConstructor(){
        LNodeDTO lNodeDTO = new LNodeDTO();
        lNodeDTO.setNodeClass(DTO.HOLDER_LN_CLASS);
        lNodeDTO.setInst(DTO.HOLDER_LN_INST);
        lNodeDTO.setPrefix(DTO.HOLDER_LN_PREFIX);
        lNodeDTO.setNodeType(DTO.LN_TYPE);
        assertAll("LNODE",
                ()-> assertTrue(lNodeDTO.getGooseControlBlocks().isEmpty()),
                ()-> assertTrue(lNodeDTO.getResumedDataTemplates().isEmpty()),
                ()-> assertTrue(lNodeDTO.getDatSets().isEmpty()),
                ()-> assertTrue(lNodeDTO.getExtRefs().isEmpty()),
                ()-> assertEquals(DTO.HOLDER_LN_INST,lNodeDTO.getInst()),
                ()-> assertEquals(DTO.HOLDER_LN_CLASS,lNodeDTO.getNodeClass()),
                ()-> assertEquals(DTO.LN_TYPE,lNodeDTO.getNodeType()),
                ()-> assertEquals(DTO.HOLDER_LN_PREFIX,lNodeDTO.getPrefix())
        );
        lNodeDTO.addResumedDataTemplate(new ResumedDataTemplate());
        lNodeDTO.addExtRefInfo(new ExtRefInfo());
        lNodeDTO.addControlBlock(new ReportControlBlock());
        lNodeDTO.addDataSet(new DataSetInfo());

        lNodeDTO.addAllControlBlocks(List.of(new SMVControlBlock()));
        lNodeDTO.addAllDatSets(List.of(new DataSetInfo()));
        lNodeDTO.addAllExtRefInfo(List.of(new ExtRefInfo()));
        lNodeDTO.addAllResumedDataTemplate(List.of(new ResumedDataTemplate()));

        assertEquals(2, lNodeDTO.getExtRefs().size());
        assertEquals(2, lNodeDTO.getDatSets().size());
        assertEquals(1, lNodeDTO.getSmvControlBlocks().size());
        assertEquals(1, lNodeDTO.getReportControlBlocks().size());
        assertEquals(2, lNodeDTO.getResumedDataTemplates().size());
    }

    @Test
    public void testFrom(){
        IEDAdapter iedAdapter = Mockito.mock(IEDAdapter.class);
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        Mockito.when(iedAdapter.getName()).thenReturn(DTO.HOLDER_IED_NAME);
        Mockito.when(lDeviceAdapter.getInst()).thenReturn(DTO.HOLDER_LD_INST);
        Mockito.when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);

        LNAdapter lnAdapter = Mockito.mock(LNAdapter.class);
        Mockito.when(lnAdapter.getParentAdapter()).thenReturn(lDeviceAdapter);
        Mockito.when(lnAdapter.getLNClass()).thenReturn(DTO.HOLDER_LN_CLASS);
        Mockito.when(lnAdapter.getLNInst()).thenReturn(DTO.HOLDER_LN_INST);
        Mockito.when(lnAdapter.getLnType()).thenReturn(DTO.LN_TYPE);
        Mockito.when(lnAdapter.getPrefix()).thenReturn(DTO.HOLDER_LN_PREFIX);

        TExtRef extRef = DTO.createExtRef();
        Mockito.when(lnAdapter.getExtRefs(null)).thenReturn(List.of(extRef));

        LNodeDTO lNodeDTO = LNodeDTO.from(lnAdapter,
                new LogicalNodeOptions(true,false,false,false));
        assertNotNull(lNodeDTO);
        assertAll("LNODE",
                ()-> assertEquals(DTO.HOLDER_LN_INST,lNodeDTO.getInst()),
                ()-> assertEquals(DTO.HOLDER_LN_CLASS,lNodeDTO.getNodeClass()),
                ()-> assertEquals(DTO.LN_TYPE,lNodeDTO.getNodeType()),
                ()-> assertEquals(DTO.HOLDER_LN_PREFIX,lNodeDTO.getPrefix()),
                () -> assertEquals(1,lNodeDTO.getExtRefs().size())
        );
        ExtRefInfo extRefInfo = lNodeDTO.getExtRefs().iterator().next();

        assertEquals(DTO.HOLDER_IED_NAME,extRefInfo.getHolderIedName());
        assertEquals(DTO.HOLDER_LD_INST,extRefInfo.getHolderLdInst());
        assertEquals(DTO.HOLDER_LN_CLASS,extRefInfo.getHolderLnClass());
        assertEquals(DTO.HOLDER_LN_INST,extRefInfo.getHolderLnInst());
        assertEquals(DTO.HOLDER_LN_PREFIX,extRefInfo.getHolderPrefix());

    }

    @Test
    public void testExtractExtRefInfo(){
        LNAdapter lnAdapter = Mockito.mock(LNAdapter.class);
        Mockito.when(lnAdapter.getLNClass()).thenReturn(DTO.HOLDER_LN_CLASS);
        Mockito.when(lnAdapter.getLNInst()).thenReturn(DTO.HOLDER_LN_INST);
        Mockito.when(lnAdapter.getLnType()).thenReturn(DTO.LN_TYPE);
        Mockito.when(lnAdapter.getPrefix()).thenReturn(DTO.HOLDER_LN_PREFIX);
        Mockito.when(lnAdapter.hasInputs()).thenReturn(true);


        TExtRef extRef = DTO.createExtRef();
        Mockito.when(lnAdapter.getExtRefs(null)).thenReturn(List.of(extRef));


        LNodeDTO lNodeDTO = LNodeDTO.extractExtRefInfo(lnAdapter);
        assertNotNull(lNodeDTO);
        assertAll("LNODE",
                ()-> assertEquals(DTO.HOLDER_LN_INST,lNodeDTO.getInst()),
                ()-> assertEquals(DTO.HOLDER_LN_CLASS,lNodeDTO.getNodeClass()),
                ()-> assertEquals(DTO.LN_TYPE,lNodeDTO.getNodeType()),
                ()-> assertEquals(DTO.HOLDER_LN_PREFIX,lNodeDTO.getPrefix()),
                ()-> assertEquals(1,lNodeDTO.getExtRefs().size())
        );
    }
}