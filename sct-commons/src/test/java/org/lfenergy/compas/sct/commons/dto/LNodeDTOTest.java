// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.LNodeTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

class LNodeDTOTest {

    @Test
    @Tag("issue-321")
    void testConstructor(){
        // When
        LNodeDTO lNodeDTO = new LNodeDTO();
        lNodeDTO.setNodeClass(DTO.HOLDER_LN_CLASS);
        lNodeDTO.setInst(DTO.HOLDER_LN_INST);
        lNodeDTO.setPrefix(DTO.HOLDER_LN_PREFIX);
        lNodeDTO.setNodeType(DTO.LN_TYPE);
        // Then
        assertAll("LNODE",
                ()-> assertThat(lNodeDTO.getGooseControlBlocks()).isEmpty(),
                ()-> assertThat(lNodeDTO.getDataAttributeRefs()).isEmpty(),
                ()-> assertThat(lNodeDTO.getDatSets()).isEmpty(),
                ()-> assertThat(lNodeDTO.getExtRefs()).isEmpty(),
                ()-> assertThat(lNodeDTO.getInst()).isEqualTo(DTO.HOLDER_LN_INST),
                ()-> assertThat(lNodeDTO.getNodeClass()).isEqualTo(DTO.HOLDER_LN_CLASS),
                ()-> assertThat(lNodeDTO.getNodeType()).isEqualTo(DTO.LN_TYPE),
                ()-> assertThat(lNodeDTO.getPrefix()).isEqualTo(DTO.HOLDER_LN_PREFIX)
        );
        // When
        lNodeDTO.addDataAttributeRef(DataAttributeRef.builder().daName(new DaTypeName("da1")).build());
        // When
        lNodeDTO.addExtRefInfo(new ExtRefInfo());
        // When
        lNodeDTO.addControlBlock(new ReportControlBlock("rpt", "rptID", "rptDatSet"));
        // When
        lNodeDTO.addDataSet(new DataSetInfo());
        // When
        lNodeDTO.addAllControlBlocks(List.of(new SMVControlBlock("smv", "smvID", "smvDatSet")));
        // When
        lNodeDTO.addAllDatSets(List.of(new DataSetInfo()));
        // When
        lNodeDTO.addAllExtRefInfo(List.of(new ExtRefInfo()));
        // When
        lNodeDTO.addAllDataAttributeRef(List.of(DataAttributeRef.builder().daName(new DaTypeName("da2")).build()));
        // Then
        assertThat(lNodeDTO.getExtRefs()).hasSize(2);
        assertThat(lNodeDTO.getDatSets()).hasSize(2);
        assertThat(lNodeDTO.getSmvControlBlocks()).hasSize(1);
        assertThat(lNodeDTO.getReportControlBlocks()).hasSize(1);
        assertThat(lNodeDTO.getDataAttributeRefs()).hasSize(2);
    }

    @Test
    void from_whenCalledWithLNAdapter_shouldFillValues(){
        // When
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
        when(lNodeTypeAdapter.getDataAttributeRefs(any(DataAttributeRef.class))).thenReturn(List.of(DataAttributeRef.builder().build()));

        TExtRef extRef = DTO.createExtRef();
        when(lnAdapter.getExtRefs(null)).thenReturn(List.of(extRef));

        // When
        LNodeDTO lNodeDTO = LNodeDTO.from(lnAdapter,
                new LogicalNodeOptions(true,true,false,false));
        // Then
        assertThat(lNodeDTO).isNotNull();
        assertAll("LNODE",
                ()-> assertThat(lNodeDTO.getInst()).isEqualTo(DTO.HOLDER_LN_INST),
                ()-> assertThat(lNodeDTO.getNodeClass()).isEqualTo(DTO.HOLDER_LN_CLASS),
                ()-> assertThat(lNodeDTO.getNodeType()).isEqualTo(DTO.LN_TYPE),
                ()-> assertThat(lNodeDTO.getPrefix()).isEqualTo(DTO.HOLDER_LN_PREFIX),
                () -> assertThat(lNodeDTO.getExtRefs()).hasSize(1)
        );
        ExtRefInfo extRefInfo = lNodeDTO.getExtRefs().iterator().next();
        assertThat(extRefInfo.getHolderIEDName()).isEqualTo(DTO.HOLDER_IED_NAME);
        assertThat(extRefInfo.getHolderLDInst()).isEqualTo(DTO.HOLDER_LD_INST);
        assertThat(extRefInfo.getHolderLnClass()).isEqualTo(DTO.HOLDER_LN_CLASS);
        assertThat(extRefInfo.getHolderLnInst()).isEqualTo(DTO.HOLDER_LN_INST);
        assertThat(extRefInfo.getHolderLnPrefix()).isEqualTo(DTO.HOLDER_LN_PREFIX);
    }

    @Test
    void extractExtRefInfo_whenCalledWithLNAdapter_shouldFillValues(){
        // Given
        LNAdapter lnAdapter = mock(LNAdapter.class);
        when(lnAdapter.getLNClass()).thenReturn(DTO.HOLDER_LN_CLASS);
        when(lnAdapter.getLNInst()).thenReturn(DTO.HOLDER_LN_INST);
        when(lnAdapter.getLnType()).thenReturn(DTO.LN_TYPE);
        when(lnAdapter.getPrefix()).thenReturn(DTO.HOLDER_LN_PREFIX);
        when(lnAdapter.hasInputs()).thenReturn(true);

        TExtRef extRef = DTO.createExtRef();
        when(lnAdapter.getExtRefs(null)).thenReturn(List.of(extRef));
        // When
        LNodeDTO lNodeDTO = LNodeDTO.extractExtRefInfo(lnAdapter);
        // Then
        assertThat(lNodeDTO).isNotNull();
        assertAll("LNODE",
                ()-> assertThat(lNodeDTO.getInst()).isEqualTo(DTO.HOLDER_LN_INST),
                ()-> assertThat(lNodeDTO.getNodeClass()).isEqualTo(DTO.HOLDER_LN_CLASS),
                ()-> assertThat(lNodeDTO.getNodeType()).isEqualTo(DTO.LN_TYPE),
                ()-> assertThat(lNodeDTO.getPrefix()).isEqualTo(DTO.HOLDER_LN_PREFIX),
                () -> assertThat(lNodeDTO.getExtRefs()).hasSize(1)
        );
    }
}
