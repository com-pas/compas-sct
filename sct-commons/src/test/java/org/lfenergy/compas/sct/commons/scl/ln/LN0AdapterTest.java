// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ln;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Named.named;
import static org.lfenergy.compas.scl2007b4.model.TSampledValueControl.SmvOpts;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LN0AdapterTest {

    private static final String SCD_IED_U_TEST = "/ied-test-schema-conf/ied_unit_test.xml";
    private static final String CB_NAME = "cbName";

    @Test
    void constructor_whenCalledWithNoRelationBetweenLDeviceAndLN0_shouldThrowException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(new TLDevice());
        LN0 ln0 = new LN0();
        // When Then
        assertThatCode(() -> new LN0Adapter(lDeviceAdapter, ln0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenLDeviceAndLN0_shouldNotThrowException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = new TLDevice();
        LN0 ln0 = new LN0();
        tlDevice.setLN0(ln0);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        // When Then
        assertThatCode(() -> new LN0Adapter(lDeviceAdapter, ln0)).doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    void isExtRefExist_whenNoInputsInLN0_shouldThrowScdException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        // Given
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        // When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("No Inputs for LN or no ExtRef signal to check");
    }

    @Test
    @Tag("issue-321")
    void isExtRefExist_whenSignalNotValid_shouldThrowScdException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TExtRef tExtRef = new TExtRef();
        TInputs tInputs = new TInputs();
        tInputs.getExtRef().add(tExtRef);
        ln0.setInputs(tInputs);
        when(tlDevice.getLN0()).thenReturn(ln0);
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        // Given
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        // When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("Invalid or missing attributes in ExtRef signal info");
    }

    @Test
    @Tag("issue-321")
    void isExtRefExist_whenSignalNull_shouldThrowScdException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        when(tlDevice.getLN0()).thenReturn(ln0);
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        // When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(null))
                .isInstanceOf(ScdException.class)
                .hasMessage("No Inputs for LN or no ExtRef signal to check");
    }

    @Test
    @Tag("issue-321")
    void isExtRefExist_whenNotExistInTargetLN_shouldThrowScdException() {
        // Given
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
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));
        // Given
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        // When Then
        assertThatThrownBy(() -> ln0Adapter.isExtRefExist(signalInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("ExtRef signal does not exist in target LN");
    }

    @Test
    @Tag("issue-321")
    void isExtRefExist_whenExtRefExist_shouldNotThrowException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));

        // Given
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        // When Then
        assertDoesNotThrow(() -> ln0Adapter.isExtRefExist(signalInfo));
    }

    @Test
    @Tag("issue-321")
    void isExtRefExist_whenExtRefExistWithPDA_shouldNotThrowException() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));

        // Given
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);

        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        signalInfo.setPDA("Da.papa");
        // When Then
        assertDoesNotThrow(() -> ln0Adapter.isExtRefExist(signalInfo));
    }

    @Test
    @Tag("issue-321")
    void testGetDataSetWith() {
        // Given
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = mock(TLDevice.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0 ln0 = new LN0();
        when(tlDevice.getLN0()).thenReturn(ln0);
        // When Then
        LN0Adapter ln0Adapter = assertDoesNotThrow(() -> new LN0Adapter(lDeviceAdapter, ln0));

        // Given
        TDataSet tDataSet = new TDataSet();
        ln0.getDataSet().add(tDataSet);
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo = Mockito.spy(extRefInfo);
        TFCDA tfcda = new TFCDA();
        tDataSet.getFCDA().add(tfcda);
        // When
        List<TDataSet> tDataSets = ln0Adapter.getDataSetMatchingExtRefInfo(extRefInfo).toList();
        // Then
        assertThat(tDataSets).isEmpty();

        doReturn(true).when(extRefInfo).checkMatchingFCDA(any(TFCDA.class));
        // When
        tDataSets = ln0Adapter.getDataSetMatchingExtRefInfo(extRefInfo).toList();
        // Then
        assertThat(tDataSets).isNotEmpty();
    }

    @Test
    void testGetControlBlocks() {
        // Given
        LN0 ln0 = new LN0();
        TReportControl tReportControl = new TReportControl();
        tReportControl.setDatSet("dataSetName");
        ln0.getReportControl().add(tReportControl);
        TSampledValueControl tSampledValueControl = new TSampledValueControl();
        tSampledValueControl.setDatSet("dataSetName");
        ln0.getSampledValueControl().add(tSampledValueControl);
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setDatSet("dataSetName");
        ln0.getGSEControl().add(tgseControl);

        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName("dataSetName");
        TFCDA tfcda = new TFCDA();
        tfcda.setDaName("d");
        tfcda.setDoName("FACntRs1.res");
        tfcda.setPrefix("LN_PREFIX_R");
        tfcda.setLdInst("LD_INST_R");
        tfcda.setLnInst("1");
        tfcda.getLnClass().add("LN_CLASS_R");
        tDataSet.getFCDA().add(tfcda);
        ln0.getDataSet().add(tDataSet);
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        ExtRefBindingInfo extRefBindingInfo = DTO.createExtRefBindingInfo_Remote();
        extRefBindingInfo.setServiceType(null);
        extRefInfo.setBindingInfo(extRefBindingInfo);

        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);

        // When
        List<ControlBlock> controlBlocks = ln0Adapter.getControlBlocksForMatchingFCDA(extRefInfo).toList();
        // Then
        assertThat(controlBlocks).hasSize(3);
    }

    @ParameterizedTest
    @MethodSource("provideServiceType")
    void getControlBlocksForMatchingFCDA_should_return_expected_Items(LN0 ln0, TServiceType serviceType) {
        // Given
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        tInputs.getExtRef().add(extRef);
        ln0.setInputs(tInputs);
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName("dataSetName");
        TFCDA tfcda = new TFCDA();
        tfcda.setDaName("d");
        tfcda.setDoName("FACntRs1.res");
        tfcda.setPrefix("LN_PREFIX_R");
        tfcda.setLdInst("LD_INST_R");
        tfcda.setLnInst("1");
        tfcda.getLnClass().add("LN_CLASS_R");
        tDataSet.getFCDA().add(tfcda);
        ln0.getDataSet().add(tDataSet);
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        ExtRefBindingInfo extRefBindingInfo = DTO.createExtRefBindingInfo_Remote();
        extRefBindingInfo.setServiceType(serviceType);
        extRefInfo.setBindingInfo(extRefBindingInfo);

        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);

        // When
        List<ControlBlock> controlBlocks = ln0Adapter.getControlBlocksForMatchingFCDA(extRefInfo).toList();
        // Then
        assertThat(controlBlocks).hasSize(1);
        assertThat(controlBlocks.getFirst().getServiceType()).isEqualTo(serviceType);
    }

    private static Stream<Arguments> provideServiceType() {
        LN0 ln0Report = new LN0();
        TReportControl tReportControl = new TReportControl();
        tReportControl.setDatSet("dataSetName");
        ln0Report.getReportControl().add(tReportControl);
        LN0 ln0Smv = new LN0();
        TSampledValueControl tSampledValueControl = new TSampledValueControl();
        tSampledValueControl.setDatSet("dataSetName");
        ln0Smv.getSampledValueControl().add(tSampledValueControl);
        LN0 ln0Goose = new LN0();
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setDatSet("dataSetName");
        ln0Goose.getGSEControl().add(tgseControl);
        return Stream.of(
                Arguments.of(ln0Report, TServiceType.REPORT),
                Arguments.of(ln0Smv, TServiceType.SMV),
                Arguments.of(ln0Goose, TServiceType.GOOSE)
        );
    }

    @Test
    void getDOIAdapters_should_return_expected_list_of_DOIAdapter() {
        // Given
        LN0 ln0 = new LN0();
        TDOI tdoi = new TDOI();
        tdoi.setName("Do");
        ln0.getDOI().add(tdoi);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);
        // When Then
        assertThat(ln0Adapter.getDOIAdapters()).isNotEmpty();
        assertThat(ln0Adapter.getDOIAdapters().getFirst().getCurrentElem().getName()).isEqualTo("Do");
    }

    @Test
    void findDoiAdapterByName_should_find_DOI(){
        // Given
        LN0Adapter ln0Adapter = createLn0AdapterWithDoi("doi2");
        // When
        Optional<DOIAdapter> result = ln0Adapter.findDoiAdapterByName("doi2");
        // Then
        assertThat(result).map(DOIAdapter::getName).hasValue("doi2");
    }

    @Test
    void findDoiAdapterByName_should_return_empty(){
        // Given
        LN0Adapter ln0Adapter = createLn0AdapterWithDoi("doi3");
        // When
        Optional<DOIAdapter> result = ln0Adapter.findDoiAdapterByName("doi2");
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getDOIAdapterByName_should_return_DOI(){
        // Given
        LN0Adapter ln0Adapter = createLn0AdapterWithDoi("doi4");
        // When
        DOIAdapter result = ln0Adapter.getDOIAdapterByName("doi4");
        // Then
        assertThat(result.getName()).isEqualTo("doi4");
    }

    @Test
    void getDOIAdapterByName_should_throw_exception(){
        // Given
        LN0Adapter ln0Adapter = createLn0AdapterWithDoi("doi5");
        // When & Then
        assertThatThrownBy(() -> ln0Adapter.getDOIAdapterByName("doi2"))
                .isInstanceOf(ScdException.class);
    }

    private static LN0Adapter createLn0AdapterWithDoi(String doiName) {
        LN0 ln0 = new LN0();
        TLDevice tlDevice = new TLDevice();
        tlDevice.setLN0(ln0);
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        TDOI tdoi = new TDOI();
        tdoi.setName(doiName);
        ln0.getDOI().add(tdoi);
        return new LN0Adapter(lDeviceAdapter, ln0);
    }

    @Test
    // Test should be modified to reflect each test case.
    @Tag("issue-321")
    void getDOIAdapterByName_should_return_expected_DOIAdapter() {
        // Given
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        TIED tied = new TIED();
        when(iedAdapter.getCurrentElem()).thenReturn(tied);
        when(iedAdapter.getName()).thenReturn("IED_NAME");
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        TLDevice tlDevice = new TLDevice();
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);
        LN0 ln0 = new LN0();
        tlDevice.setLN0(ln0);
        LN0Adapter ln0Adapter = new LN0Adapter(lDeviceAdapter, ln0);
        TDOI tdoi = new TDOI();
        tdoi.setName("Do");
        ln0.getDOI().add(tdoi);
        // When Then
        assertThatCode(() -> ln0Adapter.getDOIAdapterByName("Do"))
                .doesNotThrowAnyException();
        // When Then
        assertThatCode(() -> ln0Adapter.getDOIAdapterByName("Dod"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown DOI(Dod)");
    }

    @Test
    // Test should be modified to reflect each test case.
    @Tag("issue-321")
    void testFindMatch() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile(SCD_IED_U_TEST);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        // When Then
        AbstractDAIAdapter<?> daiAdapter = (AbstractDAIAdapter<?>) assertDoesNotThrow(() -> ln0Adapter.findMatch(doTypeName, daTypeName).get());
        assertThat(daiAdapter.getCurrentElem().getName()).isEqualTo("bda3");
        assertThat(daiAdapter.getCurrentElem().getVal().getFirst().getValue()).isEqualTo("Completed-diff");
        DoTypeName doTypeName2 = new DoTypeName("Do.sdo1");
        // When Then
        assertThat(ln0Adapter.findMatch(doTypeName2, daTypeName)).isEmpty();
    }

    @Tag("issue-321")
    // Test should be modified to reflect each test case.
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
        // When
        ln0Adapter.createDataSetIfNotExists("datSet", controlBlockEnum);
        // When
        ln0Adapter.createControlBlockIfNotExists(CB_NAME, "id", "datSet", controlBlockEnum);

        // When
        boolean found = ln0Adapter.hasControlBlock(CB_NAME, controlBlockEnum);
        // Then
        assertThat(found).isTrue();
    }

    @Tag("issue-321")
    // Test should be modified to reflect each test case.
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
        // When
        ln0Adapter.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        // When
        ln0Adapter.createControlBlockIfNotExists(CB_NAME, "id", "datSet", ControlBlockEnum.GSE);

        // When
        boolean found = ln0Adapter.hasControlBlock(CB_NAME, ControlBlockEnum.SAMPLED_VALUE);
        // Then
        assertThat(found).isFalse();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        LN0 tln = new LN0();
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        LN0Adapter lnAdapter = new LN0Adapter(null, tln);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(lnAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        lnAdapter.addPrivate(tPrivate);
        // Then
        assertThat(lnAdapter.getCurrentElem().getPrivate()).hasSize(1);
    }


    @Test
    @Tag("issue-321")
    void testGetDAI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        DataAttributeRef filter = new DataAttributeRef();
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
        // When
        var dataAttributeRefs = ln0Adapter.getDAI(filter, false);
        // Then
        assertThat(dataAttributeRefs).hasSize(1);
        assertThat(dataAttributeRefs.getFirst().getDaName().getType()).isEqualTo("BehaviourModeKind");
    }

    @Test
    @Tag("issue-321")
    void getEnumValue_whenEnumUnknown_shouldReturnNothing() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        Set<String> enumValues = ln0Adapter.getEnumValues("Behaviour");
        // Then
        assertThat(enumValues).isEmpty();
    }

    @Test
    @Tag("issue-321")
    void getEnumValue_whenEnumKnown_shouldReturnEnumValues() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        Set<String> enumValues = ln0Adapter.getEnumValues("BehaviourModeKind");
        // Then
        assertThat(enumValues).hasSize(5)
                .containsExactlyInAnyOrder("blocked", "test", "test/blocked", "off", "on");
    }

    @ParameterizedTest
    @Tag("issue-321")
    @MethodSource("provideControlBlocks")
    void addControlBlock_should_add_ControlBlock(ControlBlock controlBlock) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        ln0Adapter.createDataSetIfNotExists(controlBlock.getDataSetRef(), controlBlock.getControlBlockEnum());
        int initialControlBlockCount = ln0Adapter.getTControlsByType(controlBlock.getControlBlockEnum().getControlBlockClass()).size();
        // When
        ln0Adapter.addControlBlock(controlBlock);
        // Then
        assertThat(ln0Adapter.getTControlsByType(controlBlock.getControlBlockEnum().getControlBlockClass())).hasSize(initialControlBlockCount + 1);
    }

    @Test
    @Tag("issue-321")
    void addControlBlock_should_add_ReportControlBlock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        ln0Adapter.createDataSetIfNotExists("rptDatSet", ControlBlockEnum.REPORT);
        ReportControlBlock reportControlBlock = new ReportControlBlock("rpt", "rptID", "rptDatSet");
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        // When
        ln0Adapter.addControlBlock(reportControlBlock);
        // Then
        assertThat(ln0Adapter.getCurrentElem().getReportControl()).hasSize(reportCBInitSize + 1);
    }

    @Test
    @Tag("issue-321")
    void addControlBlock_should_add_GooseControlBlock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        ln0Adapter.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        GooseControlBlock gooseControlBlock = new GooseControlBlock("gse", "gseID", "datSet");
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();

        // When
        ln0Adapter.addControlBlock(gooseControlBlock);
        // Then
        assertThat(ln0Adapter.getCurrentElem().getGSEControl()).hasSize(reportCBInitSize + 1);
    }

    @Test
    @Tag("issue-321")
    void addControlBlock_should_add_SMVControlBlock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        ln0Adapter.createDataSetIfNotExists("smvDatSet", ControlBlockEnum.SAMPLED_VALUE);
        SMVControlBlock smvControlBlock = new SMVControlBlock("smv", "smvID", "smvDatSet");
        int reportCBInitSize = ln0Adapter.getCurrentElem().getReportControl().size();
        // When
        ln0Adapter.addControlBlock(smvControlBlock);
        // Then
        assertThat(ln0Adapter.getCurrentElem().getSampledValueControl()).hasSize(reportCBInitSize + 1);
    }

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    @Tag("issue-321")
    void addControlBlock_when_accessPoint_does_not_have_capability_should_throw_exception(ControlBlock controlBlock) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        ln0Adapter.createDataSetIfNotExists("dataSet", ControlBlockEnum.REPORT);
        ln0Adapter.getParentLDevice().getAccessPoint().setServices(new TServices());
        // When & Then
        assertThatThrownBy(() -> ln0Adapter.addControlBlock(controlBlock))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("because IED/AccessPoint does not have capability to create ControlBlock");
    }

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    @Tag("issue-321")
    void addControlBlock_when_controlBlock_already_exists_should_throw_exception(ControlBlock controlBlock) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When
        ln0Adapter.createDataSetIfNotExists("dataSet", ControlBlockEnum.REPORT);
        ln0Adapter.addControlBlock(controlBlock);
        // When & Then
        assertThatThrownBy(() -> ln0Adapter.addControlBlock(controlBlock))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("because it already exists");
    }

    @ParameterizedTest
    @MethodSource("provideControlBlocks")
    @Tag("issue-321")
    void addControlBlock_when_dataSet_does_not_exist_should_throw_exception(ControlBlock controlBlock) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED4d4fe1a8cda64cf88a5ee4176a1a0eef"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED"));
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        // When & Then
        assertThatThrownBy(() -> ln0Adapter.addControlBlock(controlBlock))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("because target DataSet dataSet does not exists");
    }

    private static Stream<Arguments> provideControlBlocks() {
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
        LN0Adapter ln0Adapter = findLn0(scd, "IED4d4fe1a8cda64cf88a5ee4176a1a0eef", "LDSUIED");
        // When
        List<? extends TControl> controlList = ln0Adapter.getTControlsByType(tControlClass);
        // Then
        assertThat(controlList).isSameAs(getter.apply(ln0Adapter.getCurrentElem()));
    }

    private static Stream<Arguments> provideGetTControlsByType() {
        return Stream.of(
                Arguments.of(TGSEControl.class, (Function<LN0, List<TGSEControl>>) LN0::getGSEControl),
                Arguments.of(TSampledValueControl.class, (Function<LN0, List<TSampledValueControl>>) LN0::getSampledValueControl),
                Arguments.of(TReportControl.class, (Function<LN0, List<TReportControl>>) LN0::getReportControl)
        );
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        LN0 tln = new LN0();
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        LN0Adapter lnAdapter = new LN0Adapter(null, tln);
        // When
        String result = lnAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("LN0");
    }

    @Test
    void getLnStatus_should_return_ModStValValue() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST13");
        // When
        Optional<String> lnStatus = ln0.getDaiModStValValue();
        // Then
        assertThat(lnStatus).hasValue("test");
    }

    @Test
    void getLnStatus_should_return_empty_Optional() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST14");
        // When
        Optional<String> lnStatus = ln0.getDaiModStValValue();
        // Then
        assertThat(lnStatus).isEmpty();
    }

    @Test
    void createDataSetIfNotExists_should_create_dataSet() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
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
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST12");
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
        LN0Adapter ln0 = findLn0(scd, "IED_NAME2", "LD_INST21");
        // When & Then
        assertThatThrownBy(() -> ln0.createDataSetIfNotExists("existingDataSet", ControlBlockEnum.GSE))
                .isInstanceOf(ScdException.class);
    }

    @Test
    @Tag("issue-321")
    void createControlBlockIfNotExists_should_create_GSEControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        assertThat(sourceLn0.getCurrentElem().getDataSet()).isEmpty();
        final String NEW_DATASET_NAME = "newDataSet";
        final String NEW_CB_NAME = "newControlBlock";
        final String NEW_CB_ID = "newControlBlockId";
        // When
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
    @Tag("issue-321")
    void createControlBlockIfNotExists_should_create_SampledValueControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        assertThat(sourceLn0.getCurrentElem().getDataSet()).isEmpty();
        final String NEW_DATASET_NAME = "newDataSet";
        final String NEW_CB_NAME = "newControlBlock";
        final String NEW_CB_ID = "newControlBlockId";
        // When
        sourceLn0.createDataSetIfNotExists(NEW_DATASET_NAME, ControlBlockEnum.SAMPLED_VALUE);
        // When
        sourceLn0.createControlBlockIfNotExists(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME, ControlBlockEnum.SAMPLED_VALUE);
        // Then
        assertThat(sourceLn0.getCurrentElem().getSampledValueControl())
                .hasSize(1);
        TSampledValueControl tSampledValueControl = sourceLn0.getCurrentElem().getSampledValueControl().getFirst();
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
    @Tag("issue-321")
    void createControlBlockIfNotExists_should_create_ReportControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_INST11");

        assertThat(sourceLn0.getCurrentElem().getDataSet()).isEmpty();
        final String NEW_DATASET_NAME = "newDataSet";
        final String NEW_CB_NAME = "newControlBlock";
        final String NEW_CB_ID = "newControlBlockId";
        // When
        sourceLn0.createDataSetIfNotExists(NEW_DATASET_NAME, ControlBlockEnum.REPORT);
        // When
        sourceLn0.createControlBlockIfNotExists(NEW_CB_NAME, NEW_CB_ID, NEW_DATASET_NAME, ControlBlockEnum.REPORT);
        // Then
        assertThat(sourceLn0.getCurrentElem().getReportControl())
                .hasSize(1);
        TReportControl tReportControl = sourceLn0.getCurrentElem().getReportControl().getFirst();
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

    @Test
    void getFCDAs_should_return_list_of_FCDAs() {
        // Given
        TFCDA tfcda = new TFCDA();
        tfcda.setFc(TFCEnum.CF);
        TFCDA tfcda1 = new TFCDA();
        tfcda1.setFc(TFCEnum.CF);
        TFCDA tfcda2 = new TFCDA();
        tfcda2.setFc(TFCEnum.CF);

        TDataSet tDataSet1 = new TDataSet();
        tDataSet1.setName("gse_dat_set");
        tDataSet1.getFCDA().addAll(List.of(tfcda,tfcda1));

        TDataSet tDataSet2 = new TDataSet();
        tDataSet2.setName("smv_dat_set");
        tDataSet2.getFCDA().add(tfcda2);

        TGSEControl gseCB = new TGSEControl();
        gseCB.setName("gse1");
        gseCB.setDatSet("gse_dat_set");
        TSampledValueControl smvCB = new TSampledValueControl();
        smvCB.setName("smv1");
        smvCB.setDatSet("smv_dat_set");

        LN0 ln0 = new LN0();
        ln0.getDataSet().addAll(List.of(tDataSet1, tDataSet2));
        ln0.getGSEControl().add(gseCB);
        ln0.getSampledValueControl().add(smvCB);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);

        TExtRef tExtRef = new TExtRef();
        tExtRef.setSrcCBName("gse1");
        tExtRef.setServiceType(TServiceType.GOOSE);

        // When
        List<TFCDA> result = ln0Adapter.getFCDAs(tExtRef);

        // Then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(tfcda, tfcda1);

    }

    @Test
    void getFCDAs_should_return_empty_list_of_FCDAs() {
        // Givens
        TDataSet tDataSet2 = new TDataSet();
        tDataSet2.setName("smv_dat_set");

        TSampledValueControl smvCB = new TSampledValueControl();
        smvCB.setName("smv1");
        smvCB.setDatSet("smv_dat_set");

        LN0 ln0 = new LN0();
        ln0.getDataSet().add(tDataSet2);
        ln0.getSampledValueControl().add(smvCB);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);

        TExtRef tExtRef = new TExtRef();
        tExtRef.setSrcCBName("smv1");
        tExtRef.setServiceType(TServiceType.SMV);

        // When
        List<TFCDA> result = ln0Adapter.getFCDAs(tExtRef);

        // Then
        assertThat(result).isEmpty();

    }

    @Test
    void getFCDAs_when_DataSet_not_present_should_throw_Exception() {
        // Given
        LN0 ln0 = new LN0();
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);

        TExtRef tExtRef = new TExtRef();
        tExtRef.setSrcCBName("smv1");
        tExtRef.setServiceType(TServiceType.SMV);

        // When Then
        assertThatThrownBy(() -> ln0Adapter.getFCDAs(tExtRef))
                .isInstanceOf(ScdException.class)
                .hasMessage("Control Block smv1 not found in /LN0");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Case without InRef,LD_WITHOUT_InRef,InRef1",
            "Case with no InRef finishing with _1,LD_WITH_1_Bad_InRef,InRef4",
            "Case with several ExtRef desc finishing with _1,LD_WITH_2_InRef_same_SUFFIX,InRef5"
    })
    void updateDoInRef_should_return_error_message(String testName, String ldInst, String doiName) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", ldInst);

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();

        // Then
        assertThat(sclReportItems).hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactly("The DOI /SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"" + ldInst + "\"]/LN0/DOI[@name=\"" + doiName + "\"] can't be bound with an ExtRef");
    }

    @Test
    void updateDoInRef_when_no_Val_in_DAI_purpose_should_return_error_message() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_Without_Val_in_DAI_purpose");

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();

        // Then
        assertThat(sclReportItems).hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactly("The DOI /SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"" + "LD_Without_Val_in_DAI_purpose" + "\"]/LN0 can't be bound with an ExtRef");
    }

    @Test
    void updateDoInRef_when_DAI_name_purpose_not_compliant_should_not_treat_LN0() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_Without_purpose");

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();

        // Then
        AbstractDAIAdapter<?> finalSetSrcRef = sourceLn0.getDOIAdapterByName("InRef6").getDataAdapterByName(SETSRCREF_DA_NAME);
        assertThat(finalSetSrcRef.getCurrentElem().isSetVal()).isFalse();
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    @Tag("issue-321")
    void updateDoInRef_when_one_ExtRef_desc_matches_should_update_setSrcRef_and_not_setSrcCB() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_WITH_1_InRef_without_cbName");
        String doiNameInRef = "InRef7";
        // When
        List<TVal> daiValList = sourceLn0.getDOIAdapterByName(doiNameInRef).getDataAdapterByName(SETSRCREF_DA_NAME).getCurrentElem().getVal();
        String originalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String expectedSrcRef = "IED_NAME1LD_WITH_1_InRef/PRANCR1.Do11.sdo11";

        assertThat(daiValList).isEmpty();

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();

        // Then
        String finalSetSrcRef = getDaiValue(sourceLn0, doiNameInRef, SETSRCREF_DA_NAME);
        String finalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        assertThat(finalSetSrcRef)
                .isNotBlank()
                .isEqualTo(expectedSrcRef);
        assertThat(finalSetSrcCB).isEqualTo(originalSetSrcCB);
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_one_ExtRef_desc_matches_should_update_setSrcRef_and_setSrcCB() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_WITH_1_InRef");
        String doiNameInRef = "InRef2";
        String originalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String expectedSrcRef = "IED_NAME1LD_WITH_1_InRef/PRANCR1.Do11.sdo11";
        String expectedSrcCb = "IED_NAME1LD_WITH_1_InRef/prefixANCR1.GSE1";

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();

        // Then
        String finalSetSrcRef = getDaiValue(sourceLn0, doiNameInRef, SETSRCREF_DA_NAME);
        String finalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        assertThat(finalSetSrcRef)
                .isNotBlank()
                .isEqualTo(expectedSrcRef);
        assertThat(finalSetSrcCB)
                .isNotEqualTo(originalSetSrcCB)
                .isEqualTo(expectedSrcCb);
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_ExtRef_desc_matches_should_update_setSrcRef_and_setSrcCB_and_setTstRef_and_setTstCB() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_WITH_3_InRef");
        String doiNameInRef = "InRef3";
        String originalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String expectedSrcRef = "IED_NAME1LD_WITH_3_InRef/PRANCR1.Do11.sdo11";
        String expectedSrcCb = "IED_NAME1LD_WITH_3_InRef/prefixANCR1.GSE1";
        String originalSetTstCB = getDaiValue(sourceLn0, doiNameInRef, SETTSTCB_DA_NAME);
        String expectedTstRef = "IED_NAME1LD_WITH_3_InRef/PRANCR1.Do11.sdo11";
        String expectedTstCB = "IED_NAME1LD_WITH_3_InRef/prefixANCR3.GSE3";

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();
        // Then

        String finalSetSrcRef = getDaiValue(sourceLn0, doiNameInRef, SETSRCREF_DA_NAME);
        String finalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String finalSetTstRef = getDaiValue(sourceLn0, doiNameInRef, SETTSTREF_DA_NAME);
        String finalSetTstCB = getDaiValue(sourceLn0, doiNameInRef, SETTSTCB_DA_NAME);
        assertThat(finalSetSrcRef)
                .isNotBlank()
                .isEqualTo(expectedSrcRef);
        assertThat(finalSetSrcCB)
                .isNotEqualTo(originalSetSrcCB)
                .isEqualTo(expectedSrcCb);
        assertThat(finalSetTstRef)
                .isNotBlank()
                .isEqualTo(expectedTstRef);
        assertThat(finalSetTstCB)
                .isNotEqualTo(originalSetTstCB)
                .isEqualTo(expectedTstCB);
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_DAI_purpose_of_Ldevice_LDEPF_ends_with_BOOLEAN_should_match_desc_that_ends_with_enum_and_update_setSrcRef_and_setSrcCB_and_setTstRef_and_setTstCB() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LDEPF");
        String doiNameInRef = "InRef3";
        String originalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String expectedSrcRef = "IED_NAME1LDEPF/PRANCR1.Do11.sdo11";
        String expectedSrcCb = "IED_NAME1LDEPF/prefixANCR1.GSE1";
        String originalSetTstCB = getDaiValue(sourceLn0, doiNameInRef, SETTSTCB_DA_NAME);
        String expectedTstRef = "IED_NAME1LDEPF/PRANCR1.Do11.sdo11";
        String expectedTstCB = "IED_NAME1LDEPF/prefixANCR3.GSE3";

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();
        // Then

        String finalSetSrcRef = getDaiValue(sourceLn0, doiNameInRef, SETSRCREF_DA_NAME);
        String finalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String finalSetTstRef = getDaiValue(sourceLn0, doiNameInRef, SETTSTREF_DA_NAME);
        String finalSetTstCB = getDaiValue(sourceLn0, doiNameInRef, SETTSTCB_DA_NAME);
        assertThat(finalSetSrcRef)
                .isNotBlank()
                .isEqualTo(expectedSrcRef);
        assertThat(finalSetSrcCB)
                .isNotEqualTo(originalSetSrcCB)
                .isEqualTo(expectedSrcCb);
        assertThat(finalSetTstRef)
                .isNotBlank()
                .isEqualTo(expectedTstRef);
        assertThat(finalSetTstCB)
                .isNotEqualTo(originalSetTstCB)
                .isEqualTo(expectedTstCB);
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_ExtRef_desc_matches_and_dais_not_updatable_should_not_update_setSrcRef_and_setSrcCB_and_setTstRef_and_setTstCB() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_test.xml");
        LN0Adapter sourceLn0 = findLn0(scd, "IED_NAME1", "LD_WITH_3_InRef");
        String doiNameInRef = "InRef3";
        findDai(sourceLn0, "InRef3" + "." + SETSRCREF_DA_NAME).update(0L, "OLD_VAL");
        findDai(sourceLn0, "InRef3" + "." + SETTSTREF_DA_NAME).update(0L, "OLD_VAL");
        findDai(sourceLn0, "InRef3" + "." + SETSRCREF_DA_NAME).getCurrentElem().setValImport(false);
        findDai(sourceLn0, "InRef3" + "." + SETSRCCB_DA_NAME).getCurrentElem().setValImport(false);
        findDai(sourceLn0, "InRef3" + "." + SETTSTREF_DA_NAME).getCurrentElem().setValImport(false);
        findDai(sourceLn0, "InRef3" + "." + SETTSTCB_DA_NAME).getCurrentElem().setValImport(false);
        String expectedVal = "OLD_VAL";

        // When
        List<SclReportItem> sclReportItems = sourceLn0.updateDoInRef();
        // Then

        String finalSetSrcRef = getDaiValue(sourceLn0, doiNameInRef, SETSRCREF_DA_NAME);
        String finalSetSrcCB = getDaiValue(sourceLn0, doiNameInRef, SETSRCCB_DA_NAME);
        String finalSetTstRef = getDaiValue(sourceLn0, doiNameInRef, SETTSTREF_DA_NAME);
        String finalSetTstCB = getDaiValue(sourceLn0, doiNameInRef, SETTSTCB_DA_NAME);

        assertThat(finalSetSrcRef).isEqualTo(expectedVal);
        assertThat(finalSetSrcCB).isEqualTo(expectedVal);
        assertThat(finalSetTstRef).isEqualTo(expectedVal);
        assertThat(finalSetTstCB).isEqualTo(expectedVal);
        assertThat(sclReportItems)
                .hasSize(4)
                .extracting(SclReportItem::message)
                .containsExactly("The DAI setSrcRef cannot be updated", "The DAI setSrcCB cannot be updated",
                        "The DAI setTstRef cannot be updated", "The DAI setTstCB cannot be updated");
    }


    @Test
    @Tag("issue-321")
    void streamControlBlocks_should_return_all_GSEControlBlocks() {
        // Given
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        TLDevice tlDevice = new TLDevice();
        LDeviceAdapter lDeviceAdapter = mock(LDeviceAdapter.class);
        when(lDeviceAdapter.hasDataSetCreationCapability(any())).thenReturn(true);
        when(lDeviceAdapter.hasControlBlockCreationCapability(any())).thenReturn(true);
        when(lDeviceAdapter.getParentAdapter()).thenReturn(iedAdapter);
        LN0 ln0 = new LN0();
        tlDevice.setLN0(ln0);
        when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        LN0Adapter ln0Adapter = new LN0Adapter(lDeviceAdapter, ln0);
        // When
        ln0Adapter.createDataSetIfNotExists("datSet1", ControlBlockEnum.GSE);
        // When
        ln0Adapter.createDataSetIfNotExists("datSet2", ControlBlockEnum.SAMPLED_VALUE);
        // When
        ln0Adapter.createControlBlockIfNotExists("cbNameGSE", "cbId1", "datSet1", ControlBlockEnum.GSE);
        // When
        ln0Adapter.createControlBlockIfNotExists("cbNameSMV", "cbId2", "datSet2", ControlBlockEnum.SAMPLED_VALUE);
        // When
        Stream<ControlBlockAdapter> result = ln0Adapter.streamControlBlocks(ControlBlockEnum.GSE);
        // Then
        assertThat(result)
            .hasSize(1)
            .extracting(ControlBlockAdapter::getName)
            .containsExactly("cbNameGSE");
    }

}
