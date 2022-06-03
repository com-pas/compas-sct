// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LN0AdapterTest {

    private static final String SCD_IED_U_TEST = "/ied-test-schema-conf/ied_unit_test.xml";

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

        LN0 ln01 = new LN0();
        assertThrows(IllegalArgumentException.class,  () -> new LN0Adapter(lDeviceAdapter, ln01));
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

        assertFalse(ln0Adapter.lookUpControlBlocksByDataSetRef(dataSetRef,TGSEControl.class).isEmpty());

        // SMV LookUp
        TSampledValueControl sampledValueControl = new TSampledValueControl();
        sampledValueControl.setDatSet(dataSetRef);
        ln0.getSampledValueControl().add(sampledValueControl);

        assertFalse(ln0Adapter.lookUpControlBlocksByDataSetRef(dataSetRef,TSampledValueControl.class).isEmpty());

        // SMV LookUp
        TReportControl reportControl = new TReportControl();
        reportControl.setDatSet(dataSetRef);
        ln0.getReportControl().add(reportControl);

        assertFalse(ln0Adapter.lookUpControlBlocksByDataSetRef(dataSetRef,TReportControl.class).isEmpty());
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

        signalInfo.setPDO("Do.papa");
        extRefList = assertDoesNotThrow(()->ln0Adapter.getExtRefsBySignalInfo(signalInfo));
        assertTrue(extRefList.isEmpty());
    }

    @Test
    void testGetDataSetWith(){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));

        TDataSet tDataSet = new TDataSet();
        ln0.getDataSet().add(tDataSet);
        List<TDataSet> tDataSets = ln0Adapter.getDataSet(null);
        assertFalse(tDataSets.isEmpty());

        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo = Mockito.spy(extRefInfo);


        TFCDA tfcda = new TFCDA();
        tDataSet.getFCDA().add(tfcda);
        tDataSets = ln0Adapter.getDataSet(extRefInfo);
        assertTrue(tDataSets.isEmpty());

        Mockito.doReturn(true).when(extRefInfo).matchFCDA(ArgumentMatchers.any(TFCDA.class));
        tDataSets = ln0Adapter.getDataSet(extRefInfo);
        assertFalse(tDataSets.isEmpty());
    }

    @Test
    void testGetControlBlocks(){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        IEDAdapter iedAdapter = Mockito.mock(IEDAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        Mockito.when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);
        Mockito.when(iedAdapter.getName()).thenReturn("IED_NAME");
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));



        TDataSet tDataSet = new TDataSet();
        tDataSet.setName(DTO.CB_DATASET_REF);

        List<ControlBlock<?>> controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSet),null);
        assertTrue(controlBlocks.isEmpty());

        ln0Adapter = Mockito.spy(ln0Adapter);
        Mockito.doReturn(List.of(new TGSEControl()))
                .when(ln0Adapter)
                .lookUpControlBlocksByDataSetRef(
                        ArgumentMatchers.anyString(),ArgumentMatchers.eq(TGSEControl.class)
                );

        Mockito.doReturn(List.of(new TSampledValueControl()))
                .when(ln0Adapter)
                .lookUpControlBlocksByDataSetRef(
                        ArgumentMatchers.anyString(),ArgumentMatchers.eq(TSampledValueControl.class)
                );

        Mockito.doReturn(List.of(new TReportControl()))
                .when(ln0Adapter)
                .lookUpControlBlocksByDataSetRef(
                        ArgumentMatchers.anyString(),ArgumentMatchers.eq(TReportControl.class)
                );
        controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSet),TServiceType.REPORT);
        assertEquals(1,controlBlocks.size());
        controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSet),TServiceType.SMV);
        assertEquals(1,controlBlocks.size());
        controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSet),TServiceType.GOOSE);
        assertEquals(1,controlBlocks.size());
        controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSet),null);
        assertEquals(3,controlBlocks.size());
    }

    @Test
    void testGetControlSetByBindingInfo(){

        LN0 ln0 = new LN0();
        LN0Adapter ln0Adapter = Mockito.mock(LN0Adapter.class);
        ln0Adapter = Mockito.spy(ln0Adapter);

        Mockito.when(ln0Adapter.getCurrentElem()).thenReturn(ln0);

        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        ExtRefInfo extRefBindingInfo = DTO.createExtRefInfo();
        Mockito.doReturn(List.of(new TDataSet()))
                .when(ln0Adapter).getDataSet(ArgumentMatchers.any(ExtRefInfo.class));

        Mockito.doReturn(List.of(new ReportControlBlock()))
                .when(ln0Adapter).getControlBlocks(
                    ArgumentMatchers.any(List.class),ArgumentMatchers.any(TServiceType.class));

        List<ControlBlock<?>> controlBlocks =  ln0Adapter.getControlSetByExtRefInfo(extRefBindingInfo);
        assertFalse(controlBlocks.isEmpty());
        assertEquals(TServiceType.REPORT,controlBlocks.get(0).getServiceType());
    }

    @Test
    void testGetDOIAdapters(){
        LN0 ln0 = new LN0();
        LN0Adapter ln0Adapter = new LN0Adapter(null,ln0);

        TDOI tdoi = new TDOI();
        tdoi.setName("Do");
        ln0.getDOI().add(tdoi);
        assertFalse(ln0Adapter.getDOIAdapters().isEmpty());
        assertEquals("Do", ln0Adapter.getDOIAdapters().get(0).getCurrentElem().getName());
    }

    @Test
    void testGetDOIAdapterByName(){
        IEDAdapter iedAdapter = Mockito.mock(IEDAdapter.class);
        TIED tied = new TIED();
        Mockito.when(iedAdapter.getCurrentElem()).thenReturn(tied);
        Mockito.when(iedAdapter.getName()).thenReturn("IED_NAME");
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = new TLDevice();
        Mockito.when(lDeviceAdapter.amChildElementRef()).thenReturn(true);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        Mockito.when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);

        LN0 ln0 = new LN0();
        tlDevice.setLN0(ln0);
        LN0Adapter ln0Adapter = new LN0Adapter(lDeviceAdapter,ln0);

        TDOI tdoi = new TDOI();
        tdoi.setName("Do");
        ln0.getDOI().add(tdoi);
        assertDoesNotThrow(() -> ln0Adapter.getDOIAdapterByName("Do"));
        assertThrows(ScdException.class, () -> ln0Adapter.getDOIAdapterByName("Dod"));
    }

    @Test
    void testFindMatch() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile(SCD_IED_U_TEST);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS1").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        AbstractDAIAdapter<?> daiAdapter = (AbstractDAIAdapter<?>) assertDoesNotThrow(() -> ln0Adapter.findMatch(doTypeName,daTypeName).get());
        assertEquals("bda3",daiAdapter.getCurrentElem().getName());
        assertEquals("Completed-diff",daiAdapter.getCurrentElem().getVal().get(0).getValue());

        DoTypeName doTypeName2 = new DoTypeName("Do.sdo1");
        assertFalse(ln0Adapter.findMatch(doTypeName2,daTypeName).isPresent());
    }

    @Test
    void testHasControlBlockAndAddControlBlock() {
        LN0 tln = new LN0();
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        LN0Adapter lnAdapter = new LN0Adapter(null,tln);

        ReportControlBlock controlBlock = new ReportControlBlock();
        controlBlock.setName("rpt");
        controlBlock.setConfRev(2L);
        assertDoesNotThrow(()->lnAdapter.addControlBlock(controlBlock));
        assertTrue(lnAdapter.hasControlBlock(controlBlock));

        GooseControlBlock gooseControlBlock = new GooseControlBlock();
        gooseControlBlock.setName("gse");
        gooseControlBlock.setId("g1");
        assertDoesNotThrow(()->lnAdapter.addControlBlock(gooseControlBlock));
        assertTrue(lnAdapter.hasControlBlock(gooseControlBlock));

        SMVControlBlock smvControlBlock = new SMVControlBlock();
        smvControlBlock.setName("smv");
        smvControlBlock.setId("s1");
        assertDoesNotThrow(()->lnAdapter.addControlBlock(smvControlBlock));
        assertTrue(lnAdapter.hasControlBlock(smvControlBlock));

        ControlBlock<?> controlBlock1 = Mockito.mock(ReportControlBlock.class);
        Mockito.when(controlBlock1.getServiceType()).thenReturn(TServiceType.POLL);
        assertThrows(ScdException.class,()->lnAdapter.addControlBlock(controlBlock1));

    }

    @Test
    void addPrivate() {
        LN0 tln = new LN0();
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        LN0Adapter lnAdapter = new LN0Adapter(null,tln);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(lnAdapter.getCurrentElem().getPrivate().isEmpty());
        lnAdapter.addPrivate(tPrivate);
        assertEquals(1, lnAdapter.getCurrentElem().getPrivate().size());
    }
}
