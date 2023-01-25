// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.lfenergy.compas.scl2007b4.model.TSampledValueControl.SmvOpts;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn0;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LN0AdapterTest {

    private static final String SCD_IED_U_TEST = "/ied-test-schema-conf/ied_unit_test.xml";
    private static final String CB_NAME = "cbName";

    @Test
    void testAmChildElementRef() throws ScdException {
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        ln0.setLnType("LT1");
        when(tlDevice.getLN0()).thenReturn(ln0);
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

        LN0 ln01 = new LN0();
        assertThrows(IllegalArgumentException.class,  () -> new LN0Adapter(lDeviceAdapter, ln01));
    }

    // AbstractLNAdapter class test
    @Test
    void containsFCDA() {
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
        assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));
    }
    @Test
    void isExtRefExist_shouldThrowScdException_whenNoInputsInLN0() {
        //Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
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
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TExtRef tExtRef = new TExtRef();
        TInputs tInputs = new TInputs();
        tInputs.getExtRef().add(tExtRef);
        ln0.setInputs(tInputs);
        when(tlDevice.getLN0()).thenReturn(ln0);
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
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = assertDoesNotThrow( () -> new LN0Adapter(lDeviceAdapter,ln0));
        //When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(null))
                .isInstanceOf(ScdException.class)
                .hasMessage("No Inputs for LN or no ExtRef signal to check");
    }

    @Test
    void isExtRefExist_shouldThrowScdException_whenNotExistInTargetLN(){
        //Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        extRef.setPDO("pdo");
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        when(tlDevice.getLN0()).thenReturn(ln0);
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
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
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
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
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
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
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

        Mockito.doReturn(true).when(extRefInfo).checkMatchingFCDA(any(TFCDA.class));
        tDataSets = ln0Adapter.getDataSetMatchingExtRefInfo(extRefInfo);
        assertFalse(tDataSets.isEmpty());
    }

    @Test
    void testGetControlBlocks(){
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);
        when(iedAdapter.getName()).thenReturn("IED_NAME");
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
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

        List<ControlBlock> controlBlocks = ln0Adapter.getControlBlocks(List.of(tDataSetGSE),null);
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
        LN0Adapter ln0Adapter = mock(LN0Adapter.class);
        ln0Adapter = Mockito.spy(ln0Adapter);

        when(ln0Adapter.getCurrentElem()).thenReturn(ln0);

        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        ExtRefInfo extRefBindingInfo = DTO.createExtRefInfo();
        Mockito.doReturn(List.of(new TDataSet()))
                .when(ln0Adapter).getDataSetMatchingExtRefInfo(any(ExtRefInfo.class));

        Mockito.doReturn(List.of(new ReportControlBlock("rpt", "rptID", "rptDatSet")))
                .when(ln0Adapter).getControlBlocks(
                    any(List.class), any(TServiceType.class));

        List<ControlBlock> controlBlocks =  ln0Adapter.getControlBlocksForMatchingFCDA(extRefBindingInfo);
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
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        TIED tied = new TIED();
        when(iedAdapter.getCurrentElem()).thenReturn(tied);
        when(iedAdapter.getName()).thenReturn("IED_NAME");
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = new TLDevice();
        when(lDeviceAdapter.amChildElementRef()).thenReturn(true);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);

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
    void testFindMatch() {
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

    @ParameterizedTest
    @EnumSource(value = ControlBlockEnum.class, mode = EnumSource.Mode.EXCLUDE, names = "LOG")
    void hasControlBlock_should_return_true(ControlBlockEnum controlBlockEnum) {
        // Given
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        when(lDeviceAdapter.hasDataSetCreationCapability(any())).thenReturn(true);
        when(lDeviceAdapter.hasControlBlockCreationCapability(any())).thenReturn(true);

        LN0 ln0 = new LN0();
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = new LN0Adapter(lDeviceAdapter, ln0);
        ln0Adapter.createDataSetIfNotExists("datSet", controlBlockEnum);
        ln0Adapter.createControlBlockIfNotExists(CB_NAME, "id", "datSet", controlBlockEnum);

        // When
        boolean found = ln0Adapter.hasControlBlock(CB_NAME, controlBlockEnum);
        // Then
        assertThat(found).isTrue();
    }

    @Test
    void hasControlBlock_when_wrong_controlBlockEnum_should_return_false() {
        // Given
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        when(lDeviceAdapter.hasDataSetCreationCapability(any())).thenReturn(true);
        when(lDeviceAdapter.hasControlBlockCreationCapability(any())).thenReturn(true);

        LN0 ln0 = new LN0();
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        when(tlDevice.getLN0()).thenReturn(ln0);
        LN0Adapter ln0Adapter = new LN0Adapter(lDeviceAdapter, ln0);
        ln0Adapter.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        ln0Adapter.createControlBlockIfNotExists(CB_NAME, "id", "datSet", ControlBlockEnum.GSE);

        // When
        boolean found = ln0Adapter.hasControlBlock(CB_NAME, ControlBlockEnum.SAMPLED_VALUE);
        // Then
        assertThat(found).isFalse();
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
    void testGetDAI() {
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
    void getEnumValue_shouldReturnNothing_whenEnumUnknow() {
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
    void getEnumValue_shouldReturnEnumValues_whenEnumKnown() {
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

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    void addControlBlock_should_add_ControlBlock(ControlBlock controlBlock) {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists(controlBlock.getDataSetRef(), controlBlock.getControlBlockEnum());
        int initialControlBlockCount = ln0Adapter.getTControlsByType(controlBlock.getControlBlockEnum().getControlBlockClass()).size();
        //When
        ln0Adapter.addControlBlock(controlBlock);
        //Then
        assertThat(ln0Adapter.getTControlsByType(controlBlock.getControlBlockEnum().getControlBlockClass())).hasSize(initialControlBlockCount + 1);
    }

    @Test
    void addControlBlock_should_add_ReportControlBlock() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists("rptDatSet", ControlBlockEnum.REPORT);
        ReportControlBlock reportControlBlock = new ReportControlBlock("rpt", "rptID", "rptDatSet");
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        //When
        ln0Adapter.addControlBlock(reportControlBlock);
        //Then
        assertThat(ln0Adapter.getCurrentElem().getReportControl()).hasSize(reportCBInitSize+1);
    }

    @Test
    void addControlBlock_should_add_GooseControlBlock() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        GooseControlBlock gooseControlBlock = new GooseControlBlock("gse", "gseID", "datSet");
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();

        //When
        ln0Adapter.addControlBlock(gooseControlBlock);
        //Then
        assertThat(ln0Adapter.getCurrentElem().getGSEControl()).hasSize(reportCBInitSize + 1);
    }

    @Test
    void addControlBlock_should_add_SMVControlBlock() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists("smvDatSet", ControlBlockEnum.SAMPLED_VALUE);
        SMVControlBlock smvControlBlock = new SMVControlBlock("smv", "smvID", "smvDatSet");
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        //When
        ln0Adapter.addControlBlock(smvControlBlock);
        //Then
        assertThat(ln0Adapter.getCurrentElem().getSampledValueControl()).hasSize(reportCBInitSize+1);
    }

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    void addControlBlock_when_accessPoint_does_not_have_capability_should_throw_exception(ControlBlock controlBlock) {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists("dataSet", ControlBlockEnum.REPORT);
        ln0Adapter.getParentLDevice().getAccessPoint().setServices(new TServices());
        //When & Then
        assertThatThrownBy(() -> ln0Adapter.addControlBlock(controlBlock))
            .isInstanceOf(ScdException.class)
            .hasMessageContaining("because IED/AccessPoint does not have capability to create ControlBlock");
    }

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    void addControlBlock_when_controlBlock_already_exists_should_throw_exception(ControlBlock controlBlock) {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists("dataSet", ControlBlockEnum.REPORT);
        ln0Adapter.addControlBlock(controlBlock);
        //When & Then
        assertThatThrownBy(() -> ln0Adapter.addControlBlock(controlBlock))
            .isInstanceOf(ScdException.class)
            .hasMessageContaining("because it already exists");
    }

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    void addControlBlock_when_dataSet_does_not_exist_should_throw_exception(ControlBlock controlBlock) {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        //When & Then
        assertThatThrownBy(() -> ln0Adapter.addControlBlock(controlBlock))
            .isInstanceOf(ScdException.class)
            .hasMessageContaining("because target DataSet dataSet does not exists");
    }

    private static Stream<Arguments> provideControlBlocks(){
        return Stream.of(
            Arguments.of(named("ReportControlBlock", new ReportControlBlock("name", "id", "dataSet"))),
            Arguments.of(named("GooseControlBlock", new GooseControlBlock("name", "id", "dataSet"))),
            Arguments.of(named("SMVControlBlock", new SMVControlBlock("name", "id", "dataSet")))
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetTControlsByType")
    void getTControlsByType_should_return_LN0_list_of_controls_for_this_class(Class<? extends TControl> tControlClass, Function<LN0, List<?>> getter) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        LN0Adapter ln0Adapter = findLn0(new SclRootAdapter(scd), "IED4d4fe1a8cda64cf88a5ee4176a1a0eef", "LDSUIED");
        // When
        List<? extends TControl> controlList = ln0Adapter.getTControlsByType(tControlClass);
        // Then
        assertThat(controlList).isSameAs(getter.apply(ln0Adapter.getCurrentElem()));
    }

    private static Stream<Arguments> provideGetTControlsByType(){
        return Stream.of(
            Arguments.of(TGSEControl.class, (Function<LN0, List<TGSEControl>>) LN0::getGSEControl),
            Arguments.of(TSampledValueControl.class, (Function<LN0, List<TSampledValueControl>>) LN0::getSampledValueControl),
            Arguments.of(TReportControl.class, (Function<LN0, List<TReportControl>>) LN0::getReportControl)
        );
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
    void getLDeviceStatus_should_succeed() {
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

    @Test
    void createDataSetIfNotExists_should_create_dataSet() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(new SclRootAdapter(scd), "IED_NAME1", "LD_INST11");
        assertThat(ln0.getCurrentElem().getDataSet()).isEmpty();
        // When
        DataSetAdapter newDataSet = ln0.createDataSetIfNotExists("newDataSet", ControlBlockEnum.GSE);
        // Then
        assertThat(newDataSet.getCurrentElem().getName()).isEqualTo("newDataSet");
        assertThat(newDataSet.getParentAdapter().getParentAdapter().getInst()).isEqualTo("LD_INST11");
        assertThat(ln0.getCurrentElem().getDataSet())
            .map(TDataSet::getName)
            .containsExactly("newDataSet");
    }

    @Test
    void createDataSetIfNotExists_when_dataset_exists_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(new SclRootAdapter(scd), "IED_NAME1", "LD_INST12");
        assertThat(ln0.getCurrentElem().getDataSet()).hasSize(1);
        // When
        DataSetAdapter newDataSet = ln0.createDataSetIfNotExists("existingDataSet", ControlBlockEnum.GSE);
        // Then
        assertThat(ln0.getCurrentElem().getDataSet()).hasSize(1)
            .map(TDataSet::getName)
            .containsExactly("existingDataSet");
        assertThat(newDataSet.getCurrentElem().getName()).isEqualTo("existingDataSet");
        assertThat(newDataSet.getParentAdapter().getParentAdapter().getInst()).isEqualTo("LD_INST12");
    }

    @Test
    void createDataSetIfNotExists_when_ied_does_not_have_creation_capabilities_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(new SclRootAdapter(scd), "IED_NAME2", "LD_INST21");
        // When & Then
        assertThatThrownBy(() -> ln0.createDataSetIfNotExists("existingDataSet", ControlBlockEnum.GSE))
            .isInstanceOf(ScdException.class);
    }

    @Test
    void createControlBlockIfNotExists_should_create_GSEControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter sourceLn0 = findLn0(new SclRootAdapter(scd), "IED_NAME1", "LD_INST11");
        assertThat(sourceLn0.getCurrentElem().getDataSet()).isEmpty();
        final String NEW_DATASET_NAME = "newDataSet";
        final String NEW_CB_NAME = "newControlBlock";
        final String NEW_CB_ID = "newControlBlockId";
        sourceLn0.createDataSetIfNotExists(NEW_DATASET_NAME, ControlBlockEnum.GSE);
        // When
        sourceLn0.createControlBlockIfNotExists(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME, ControlBlockEnum.GSE);
        // Then
        assertThat(sourceLn0.getCurrentElem().getGSEControl())
            .hasSize(1)
        .first().extracting(TControl::getName, TGSEControl::getAppID, TControl::getDatSet,
                TGSEControl::getType, TGSEControl::isFixedOffs, TGSEControl::getSecurityEnable, TControlWithIEDName::getConfRev)
                .containsExactly(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME,
                    TGSEControlTypeEnum.GOOSE, false, TPredefinedTypeOfSecurityEnum.NONE, 10000L);
    }

    @Test
    void createControlBlockIfNotExists_should_create_SampledValueControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter sourceLn0 = findLn0(new SclRootAdapter(scd), "IED_NAME1", "LD_INST11");
        assertThat(sourceLn0.getCurrentElem().getDataSet()).isEmpty();
        final String NEW_DATASET_NAME = "newDataSet";
        final String NEW_CB_NAME = "newControlBlock";
        final String NEW_CB_ID = "newControlBlockId";
        sourceLn0.createDataSetIfNotExists(NEW_DATASET_NAME, ControlBlockEnum.SAMPLED_VALUE);
        // When
        sourceLn0.createControlBlockIfNotExists(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME, ControlBlockEnum.SAMPLED_VALUE);
        // Then
        assertThat(sourceLn0.getCurrentElem().getSampledValueControl())
            .hasSize(1);
        TSampledValueControl tSampledValueControl = sourceLn0.getCurrentElem().getSampledValueControl().get(0);
        assertThat(tSampledValueControl)
            .extracting(TControl::getName, TSampledValueControl::getSmvID, TControl::getDatSet,
                TSampledValueControl::isMulticast, TSampledValueControl::getSmpRate, TSampledValueControl::getNofASDU, TSampledValueControl::getSmpMod, TSampledValueControl::getSecurityEnable,
                TControlWithIEDName::getConfRev)
            .containsExactly(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME,
                true, 4800L, 2L, TSmpMod.SMP_PER_SEC, TPredefinedTypeOfSecurityEnum.NONE, 10000L);
        assertThat(tSampledValueControl.getSmvOpts())
            .extracting(SmvOpts::isRefreshTime, SmvOpts::isSampleSynchronized, SmvOpts::isSampleRate, SmvOpts::isDataSet,
                SmvOpts::isSecurity, SmvOpts::isTimestamp)
            .containsExactly(false, true, true, false, false, false);
    }

    @Test
    void createControlBlockIfNotExists_should_create_ReportControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter sourceLn0 = findLn0(new SclRootAdapter(scd), "IED_NAME1", "LD_INST11");

        assertThat(sourceLn0.getCurrentElem().getDataSet()).isEmpty();
        final String NEW_DATASET_NAME = "newDataSet";
        final String NEW_CB_NAME = "newControlBlock";
        final String NEW_CB_ID = "newControlBlockId";
        sourceLn0.createDataSetIfNotExists(NEW_DATASET_NAME, ControlBlockEnum.REPORT);
        // When
        sourceLn0.createControlBlockIfNotExists(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME, ControlBlockEnum.REPORT);
        // Then
        assertThat(sourceLn0.getCurrentElem().getReportControl())
            .hasSize(1);
        TReportControl tReportControl = sourceLn0.getCurrentElem().getReportControl().get(0);
        assertThat(tReportControl)
            .extracting(TControl::getName, TReportControl::getRptID, TControl::getDatSet,
                TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed, TControlWithTriggerOpt::getIntgPd, TReportControl::getConfRev)
            .containsExactly(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME,
                true, 0L, true, 60000L, 1L);

        assertThat(tReportControl.getTrgOps())
            .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isPeriod, TTrgOps::isGi)
            .containsOnly(true);
        assertThat(tReportControl.getTrgOps().isDupd()).isFalse();

        assertThat(tReportControl.getOptFields())
            .extracting(TReportControl.OptFields::isSeqNum, TReportControl.OptFields::isTimeStamp, TReportControl.OptFields::isDataSet,
                TReportControl.OptFields::isReasonCode, TReportControl.OptFields::isDataRef, TReportControl.OptFields::isEntryID,
                TReportControl.OptFields::isConfigRef)
            .containsOnly(false);
        assertThat(tReportControl.getOptFields().isBufOvfl()).isTrue();
    }

}
