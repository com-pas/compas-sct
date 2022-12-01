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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    // AbstractLNAdapter class test
    @Test
    void containsFCDA() {
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));
    }

    @Test
    void isExtRefExist_shouldThrowScdException_whenNoInputsInLN0() {
        //Given
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        //When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("No Inputs for LN or no ExtRef signal to check");
    }

    @Test
    void isExtRefExist_shouldThrowScdException_whenSignalNotValid() {
        //Given
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TExtRef tExtRef = new TExtRef();
        TInputs tInputs = new TInputs();
        tInputs.getExtRef().add(tExtRef);
        ln0.setInputs(tInputs);
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        //When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("Invalid or missing attributes in ExtRef signal info");
    }


    @Test
    void isExtRefExist_shouldThrowScdException_whenSignalNull(){
        //Given
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));
        //When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(null))
                .isInstanceOf(ScdException.class)
                .hasMessage("No Inputs for LN or no ExtRef signal to check");
    }

    @Test
    void isExtRefExist_shouldThrowScdException_whenNotExistInTargetLN(){
        //Given
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        extRef.setPDO("pdo");
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        //When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("ExtRef signal does not exist in target LN");
    }

    @Test
    void isExtRefExist_shouldNotThrowException_whenExtRefExist() {
        //Given
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));

        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        //When Then
        assertDoesNotThrow(() -> ln0Adapter.isExtRefExist(signalInfo));
    }

    @Test
    void isExtRefExist_shouldNotThrowException_whenExtRefExistWithPDA() {
        //Given
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        Mockito.when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));

        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        signalInfo.setPDA("Da.papa");
        //When Then
        assertDoesNotThrow(() -> ln0Adapter.isExtRefExist(signalInfo));
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
        List<TDataSet> tDataSets = ln0Adapter.getDataSetMatchingExtRefInfo(null);
        assertFalse(tDataSets.isEmpty());

        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo = Mockito.spy(extRefInfo);


        TFCDA tfcda = new TFCDA();
        tDataSet.getFCDA().add(tfcda);
        tDataSets = ln0Adapter.getDataSetMatchingExtRefInfo(extRefInfo);
        assertTrue(tDataSets.isEmpty());

        Mockito.doReturn(true).when(extRefInfo).checkMatchingFCDA(ArgumentMatchers.any(TFCDA.class));
        tDataSets = ln0Adapter.getDataSetMatchingExtRefInfo(extRefInfo);
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
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setDatSet("GSE_REF");
        TSampledValueControl tSampledValueControl = new TSampledValueControl();
        tSampledValueControl.setDatSet("SMV_REF");
        TReportControl tReportControl = new TReportControl();
        tReportControl.setDatSet("RPT_REF");
        ln0Adapter.getCurrentElem().getGSEControl().add(tgseControl);
        ln0Adapter.getCurrentElem().getSampledValueControl().add(tSampledValueControl);
        ln0Adapter.getCurrentElem().getReportControl().add(tReportControl);

        TDataSet tDataSetGSE = new TDataSet();
        tDataSetGSE.setName(DTO.CB_DATASET_REF);

        List<ControlBlock<?>> controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSetGSE),null);
        assertTrue(controlBlocks.isEmpty());

        tDataSetGSE.setName("GSE_REF");
        TDataSet tDataSetSMV = new TDataSet();
        tDataSetSMV.setName("SMV_REF");
        TDataSet tDataSetRPT = new TDataSet();
        tDataSetRPT.setName("RPT_REF");

        List<TDataSet> tDataSets = List.of(tDataSetGSE, tDataSetSMV, tDataSetRPT);

        controlBlocks = ln0Adapter.getControlBlocks(tDataSets,TServiceType.REPORT);
        assertThat(controlBlocks).hasSize(1);
        controlBlocks = ln0Adapter.getControlBlocks(tDataSets,TServiceType.SMV);
        assertThat(controlBlocks).hasSize(1);
        controlBlocks = ln0Adapter.getControlBlocks(tDataSets,TServiceType.GOOSE);
        assertThat(controlBlocks).hasSize(1);
        controlBlocks = ln0Adapter.getControlBlocks(tDataSets,null);
        assertThat(controlBlocks).hasSize(3);
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
                .when(ln0Adapter).getDataSetMatchingExtRefInfo(ArgumentMatchers.any(ExtRefInfo.class));

        Mockito.doReturn(List.of(new ReportControlBlock()))
                .when(ln0Adapter).getControlBlocks(
                    ArgumentMatchers.any(List.class),ArgumentMatchers.any(TServiceType.class));

        List<ControlBlock<?>> controlBlocks =  ln0Adapter.getControlBlocksForMatchingFCDA(extRefBindingInfo);
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
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
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


    @Test
    void testGetDAI() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ResumedDataTemplate filter = new ResumedDataTemplate();
        filter.setLnClass(ln0Adapter.getLNClass());
        filter.setLnInst(ln0Adapter.getLNInst());
        filter.setPrefix(ln0Adapter.getPrefix());
        filter.setLnType(ln0Adapter.getLnType());
        filter.setDoName(new DoTypeName("Beh"));
        DaTypeName daTypeName = new DaTypeName();
        daTypeName.setName("stVal");
        daTypeName.setBType(TPredefinedBasicTypeEnum.ENUM);
        daTypeName.setFc(TFCEnum.ST);
        filter.setDaName(daTypeName);
        //When
        var rDtts = ln0Adapter.getDAI(filter,false);
        //Then
        assertFalse(rDtts.isEmpty());
        assertEquals(1,rDtts.size());
        assertNotNull(rDtts.get(0).getDaName().getType());
        assertEquals("BehaviourModeKind", rDtts.get(0).getDaName().getType());
    }

    @Test
    void getEnumValue_shouldReturnNothing_whenEnumUnknow() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        //When
        Set<String> enumValues = ln0Adapter.getEnumValues("Behaviour");
        //Then
        assertThat(enumValues).isEmpty();
    }

    @Test
    void getEnumValue_shouldReturnEnumValues_whenEnumKnown() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        //When
        Set<String> enumValues = ln0Adapter.getEnumValues("BehaviourModeKind");
        //Then
        assertEquals(5, enumValues.size());
        assertThat(enumValues).containsExactlyInAnyOrder("blocked", "test", "test/blocked", "off", "on");
    }

    @Test
    void addControlBlock_shouldAddControlBlock_whenReport() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ReportControlBlock reportControlBlock = new ReportControlBlock();
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        //When
        ln0Adapter.addControlBlock(reportControlBlock);
        //Then
        assertThat(ln0Adapter.getCurrentElem().getReportControl()).hasSize(reportCBInitSize+1);
    }

    @Test
    void addControlBlock_shouldAddControlBlock_whenGoose() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        GooseControlBlock gooseControlBlock = new GooseControlBlock();
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        //When
        ln0Adapter.addControlBlock(gooseControlBlock);
        //Then
        assertThat(ln0Adapter.getCurrentElem().getGSEControl()).hasSize(reportCBInitSize+1);
    }

    @Test
    void addControlBlock_shouldAddControlBlock_whenSMV() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        SMVControlBlock smvControlBlock = new SMVControlBlock();
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        //When
        ln0Adapter.addControlBlock(smvControlBlock);
        //Then
        assertThat(ln0Adapter.getCurrentElem().getSampledValueControl()).hasSize(reportCBInitSize+1);
    }

    @Test
    void elementXPath() {
        // Given
        LN0 tln = new LN0();
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        LN0Adapter lnAdapter = new LN0Adapter(null,tln);
        // When
        String result = lnAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("LN0");
    }

    @Test
    void getLDeviceStatus_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Optional<LN0Adapter> optionalLN0Adapter = sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(lDeviceAdapter -> "IED_NAME1LD_INST13".equals(lDeviceAdapter.getLdName()))
            .map(LDeviceAdapter::getLN0Adapter)
            .findFirst();
        assertThat(optionalLN0Adapter).isPresent();
        LN0Adapter ln0Adapter = optionalLN0Adapter.get();
        // When
        Optional<String> result = ln0Adapter.getLDeviceStatus();
        // Then
        assertThat(result)
            .isPresent()
            .hasValue("test");
    }
}
