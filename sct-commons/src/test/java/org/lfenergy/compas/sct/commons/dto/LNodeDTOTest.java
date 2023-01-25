// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        lNodeDTO.addResumedDataTemplate(ResumedDataTemplate.builder().daName(new DaTypeName("da1")).build());
        lNodeDTO.addExtRefInfo(new ExtRefInfo());
        lNodeDTO.addControlBlock(new ReportControlBlock("rpt", "rptID", "rptDatSet"));
        lNodeDTO.addDataSet(new DataSetInfo());

        lNodeDTO.addAllControlBlocks(List.of(new SMVControlBlock("smv", "smvID", "smvDatSet")));
        lNodeDTO.addAllDatSets(List.of(new DataSetInfo()));
        lNodeDTO.addAllExtRefInfo(List.of(new ExtRefInfo()));
        lNodeDTO.addAllResumedDataTemplate(List.of(ResumedDataTemplate.builder().daName(new DaTypeName("da2")).build()));

        assertEquals(2, lNodeDTO.getExtRefs().size());
        assertEquals(2, lNodeDTO.getDatSets().size());
        assertEquals(1, lNodeDTO.getSmvControlBlocks().size());
        assertEquals(1, lNodeDTO.getReportControlBlocks().size());
        assertEquals(2, lNodeDTO.getResumedDataTemplates().size());
    }

    @Test
    void testFrom(){
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        when(iedAdapter.getName()).thenReturn(DTO.HOLDER_IED_NAME);
        when(lDeviceAdapter.getInst()).thenReturn(DTO.HOLDER_LD_INST);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);

        LNAdapter lnAdapter = mock(LNAdapter.class);
        when(lnAdapter.getParentAdapter()).thenReturn(lDeviceAdapter);
        when(lnAdapter.getLNClass()).thenReturn(DTO.HOLDER_LN_CLASS);
        when(lnAdapter.getLNInst()).thenReturn(DTO.HOLDER_LN_INST);
        when(lnAdapter.getLnType()).thenReturn(DTO.LN_TYPE);
        when(lnAdapter.getPrefix()).thenReturn(DTO.HOLDER_LN_PREFIX);

        DataTypeTemplateAdapter dataTypeTemplateAdapter = mock(DataTypeTemplateAdapter.class);
        when(lnAdapter.getDataTypeTemplateAdapter()).thenReturn(dataTypeTemplateAdapter);
        LNodeTypeAdapter lNodeTypeAdapter = mock(LNodeTypeAdapter.class);
        when(dataTypeTemplateAdapter.getLNodeTypeAdapterById(any())).thenReturn(Optional.of(lNodeTypeAdapter));
        when(lNodeTypeAdapter.getResumedDTTs(any())).thenReturn(List.of(ResumedDataTemplate.builder().build()));

        TExtRef extRef = DTO.createExtRef();
        when(lnAdapter.getExtRefs(null)).thenReturn(List.of(extRef));

        LNodeDTO lNodeDTO = LNodeDTO.from(lnAdapter,
                new LogicalNodeOptions(true,true,false,false));
        assertNotNull(lNodeDTO);
        assertAll("LNODE",
                ()-> assertEquals(DTO.HOLDER_LN_INST,lNodeDTO.getInst()),
                ()-> assertEquals(DTO.HOLDER_LN_CLASS,lNodeDTO.getNodeClass()),
                ()-> assertEquals(DTO.LN_TYPE,lNodeDTO.getNodeType()),
                ()-> assertEquals(DTO.HOLDER_LN_PREFIX,lNodeDTO.getPrefix()),
                () -> assertEquals(1,lNodeDTO.getExtRefs().size())
        );
        ExtRefInfo extRefInfo = lNodeDTO.getExtRefs().iterator().next();

        assertEquals(DTO.HOLDER_IED_NAME,extRefInfo.getHolderIEDName());
        assertEquals(DTO.HOLDER_LD_INST,extRefInfo.getHolderLDInst());
        assertEquals(DTO.HOLDER_LN_CLASS,extRefInfo.getHolderLnClass());
        assertEquals(DTO.HOLDER_LN_INST,extRefInfo.getHolderLnInst());
        assertEquals(DTO.HOLDER_LN_PREFIX,extRefInfo.getHolderLnPrefix());
    }

    @Test
    void testExtractExtRefInfo(){
        LNAdapter lnAdapter = mock(LNAdapter.class);
        when(lnAdapter.getLNClass()).thenReturn(DTO.HOLDER_LN_CLASS);
        when(lnAdapter.getLNInst()).thenReturn(DTO.HOLDER_LN_INST);
        when(lnAdapter.getLnType()).thenReturn(DTO.LN_TYPE);
        when(lnAdapter.getPrefix()).thenReturn(DTO.HOLDER_LN_PREFIX);
        when(lnAdapter.hasInputs()).thenReturn(true);


        TExtRef extRef = DTO.createExtRef();
        when(lnAdapter.getExtRefs(null)).thenReturn(List.of(extRef));


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
