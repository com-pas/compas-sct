// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TGSEControl;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TSampledValueControl;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.GooseControlBlock;
import org.lfenergy.compas.sct.commons.dto.ReportControlBlock;
import org.lfenergy.compas.sct.commons.dto.SMVControlBlock;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LN0AdapterTest {

    @Test
    void testAmChildElementRef() throws ScdException {
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        ln0.setLnType("LT1");
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));

        assertEquals(LN0.class,ln0Adapter.getElementClassType());
        assertEquals("LT1",ln0Adapter.getLnType());
        assertEquals(TLLN0Enum.LLN_0.value(),ln0Adapter.getLNClass());
        assertFalse(ln0Adapter.hasInputs());
        ln0.setInputs(new TInputs());
        assertTrue(ln0Adapter.hasInputs());
        assertTrue(ln0Adapter.isLN0());

        assertTrue(ln0Adapter.getLNInst().isEmpty());
        assertTrue(ln0Adapter.getPrefix().isEmpty());
        assertTrue(ln0Adapter.getCurrentElem().getGSEControl().isEmpty());
        assertTrue(ln0Adapter.getCurrentElem().getSampledValueControl().isEmpty());
        assertTrue(ln0Adapter.getCurrentElem().getReportControl().isEmpty());

        ln0Adapter.addControlBlock(new ReportControlBlock());
        ln0Adapter.addControlBlock(new SMVControlBlock());
        ln0Adapter.addControlBlock(new GooseControlBlock());

        assertFalse(ln0Adapter.getCurrentElem().getGSEControl().isEmpty());
        assertFalse(ln0Adapter.getCurrentElem().getSampledValueControl().isEmpty());
        assertFalse(ln0Adapter.getCurrentElem().getReportControl().isEmpty());

        assertThrows(IllegalArgumentException.class,  () -> new LN0Adapter(lDeviceAdapter,new LN0()));
    }

    @Test
    void testLookUpControlBlocksByDataSetRef(){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));
        // GSE LookUp
        TGSEControl tgseControl = new TGSEControl();
        String dataSetRef = "DATASET_REF";
        tgseControl.setDatSet(dataSetRef);
        ln0.getGSEControl().add(tgseControl);

        assertFalse(ln0Adapter.lookUpGseControlBlocks(dataSetRef).isEmpty());

        // SMV LookUp
        TSampledValueControl sampledValueControl = new TSampledValueControl();
        sampledValueControl.setDatSet(dataSetRef);
        ln0.getSampledValueControl().add(sampledValueControl);

        assertFalse(ln0Adapter.lookUpSMVControlBlocks(dataSetRef).isEmpty());
    }

    // AbstractLNAdapter class test
    @Test
    void containsFCDA() {
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));
    }

    @Test
    void testGetExtRefsBySignalInfo(){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));

        List<TExtRef> extRefList = assertDoesNotThrow(()->ln0Adapter.getExtRefsBySignalInfo(new ExtRefSignalInfo()));
        assertTrue(extRefList.isEmpty());

        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        extRefList = assertDoesNotThrow(()->ln0Adapter.getExtRefsBySignalInfo(null));
        assertFalse(extRefList.isEmpty());

        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        extRefList = assertDoesNotThrow(()->ln0Adapter.getExtRefsBySignalInfo(signalInfo));
        assertFalse(extRefList.isEmpty());

        signalInfo.setPDO("do.papa");
        extRefList = assertDoesNotThrow(()->ln0Adapter.getExtRefsBySignalInfo(signalInfo));
        assertTrue(extRefList.isEmpty());
    }
}