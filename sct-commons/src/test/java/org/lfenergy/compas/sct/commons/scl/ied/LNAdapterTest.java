// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class LNAdapterTest {

    @Test
    void testAmChildElementRef() {
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);

        assertEquals(TLN.class, lnAdapter.getElementClassType());
        assertEquals(DTO.LN_TYPE, lnAdapter.getLnType());
        assertEquals(DTO.HOLDER_LN_CLASS, lnAdapter.getLNClass());
        assertFalse(lnAdapter.hasInputs());
        tln.setInputs(new TInputs());
        assertTrue(lnAdapter.hasInputs());
        assertFalse(lnAdapter.isLN0());

        assertEquals(DTO.HOLDER_LN_INST, lnAdapter.getLNInst());
        assertEquals(DTO.HOLDER_LN_PREFIX, lnAdapter.getPrefix());
        assertTrue(lnAdapter.getCurrentElem().getReportControl().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> new LNAdapter(lnAdapter.getParentAdapter(), new TLN()));
    }

    @Test
    void testGetExtRefs() {
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);

        assertTrue(lnAdapter.getExtRefs(null).isEmpty());
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo(extRef);
        tInputs.getExtRef().add(DTO.createExtRef());
        tln.setInputs(tInputs);
        assertEquals(1, lnAdapter.getExtRefs(null).size());
        assertEquals(1, lnAdapter.getExtRefs(extRefSignalInfo).size());
    }

    @Test
    void findDataSetByName_should_return_dataset() {
        // Given
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName(DTO.CB_DATASET_REF);
        tln.getDataSet().add(tDataSet);
        // Then
        Optional<DataSetAdapter> foundDataSet = lnAdapter.findDataSetByName(DTO.CB_DATASET_REF);
        // When
        assertThat(foundDataSet).isPresent();
    }

    @Test
    void findDataSetByName_when_not_found_should_return_empty() {
        // Given
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName(DTO.CB_DATASET_REF);
        tln.getDataSet().add(tDataSet);
        // Then
        Optional<DataSetAdapter> foundDataSet = lnAdapter.findDataSetByName("Non existent dataset");
        // When
        assertThat(foundDataSet).isEmpty();
    }

    @Test
    void updateExtRefBindingInfo_shouldUpdateBindingInfo_whenBindingInfoNull() {
        //Given
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        TExtRef extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());
        assertNull(extRef.getIedName());
        LNAdapter lnAdapter = initLNAdapter(new TLN());
        extRefInfo.setBindingInfo(null);
        extRefInfo.setSourceInfo(null);
        extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());
        assertNull(extRef.getIedName());
        //When
        lnAdapter.updateExtRefBindingInfo(extRef, extRefInfo);
        //Then
        assertNull(extRef.getIedName());
        assertNull(extRef.getSrcLDInst());
    }

    @Test
    void updateExtRefBindingInfo_shouldUpdateBindingInfo_whenNotBindingInfoNull() {
        //Given
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        TExtRef extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());
        assertNull(extRef.getIedName());
        LNAdapter lnAdapter = initLNAdapter(new TLN());
        extRefInfo.getBindingInfo().setServiceType(null);
        //When
        lnAdapter.updateExtRefBindingInfo(extRef, extRefInfo);
        //Then
        assertEquals(extRefInfo.getBindingInfo().getServiceType(), extRefInfo.getSignalInfo().getPServT());
        assertEquals(extRefInfo.getBindingInfo().getIedName(), extRef.getIedName());
        assertEquals(extRefInfo.getSourceInfo().getSrcLDInst(), extRef.getSrcLDInst());
    }

    @Test
    void updateExtRefBinders_shouldUpdateExtRefs() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());
        LNAdapter lnAdapter = (LNAdapter) AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass("ANCR")
                .withLnInst("1")
                .build();
        ExtRefInfo info = DTO.createExtRefInfo();
        info.getSignalInfo().setPDO("StrVal.sdo2");
        info.getSignalInfo().setPDA("antRef.bda1.bda2.bda3");
        info.getSignalInfo().setIntAddr("INT_ADDR2");
        info.getSignalInfo().setDesc(null);
        info.getSignalInfo().setPServT(null);
        //When Then
        assertDoesNotThrow(() -> lnAdapter.updateExtRefBinders(info));
        List<TExtRef> tExtRefs = lnAdapter.getExtRefs(null);
        assertEquals(1, tExtRefs.size());
        assertEquals(info.getBindingInfo().getIedName(), tExtRefs.get(0).getIedName());

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("extRefInfoAndMessage")
    void updateExtRefBinders_shouldThrowsException(String testCase, ExtRefInfo info, String expectedMessage) {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        LNAdapter lnAdapter = (LNAdapter) AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass("ANCR")
                .withLnInst("1")
                .build();

        assertThatThrownBy(() -> lnAdapter.updateExtRefBinders(info))
                .isInstanceOf(ScdException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> extRefInfoAndMessage() {
        return Stream.of(
                Arguments.of("whenBindingInfoNotValid", new ExtRefInfo(), "ExtRef mandatory binding data are missing"),
                Arguments.of("whenNoExtRefFound", DTO.createExtRefInfo(), "Unknown ExtRef [pDO(FACntRs1.res),intAddr(INT_ADDR)] in IED_NAME/LD_INST_H.ANCR")
        );
    }


    @Test
    void updateExtRefBinders_shouldUpdateExtRefs_whenManyExtRefMatch() {
        //Given
        TExtRef tExtRef = DTO.createExtRef();
        TInputs inputs = new TInputs();
        inputs.getExtRef().add(tExtRef);
        inputs.getExtRef().add(tExtRef);
        LN0 ln0 = new LN0();
        ln0.setInputs(inputs);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);
        ExtRefInfo info = DTO.createExtRefInfo();
        //When Then
        assertDoesNotThrow(() -> ln0Adapter.updateExtRefBinders(info));
    }

    @Test
    void should_throw_ScdException_when_the_given_binding_info_does_not_match_the_found_TExtRef_binding_info() {

        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME1"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INST11").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        ExtRefInfo extRefInfo = DTO.createExtRefInfo();

        //When
        //Then
        assertThrows(ScdException.class, () -> lnAdapter.extractExtRefFromExtRefInfo(extRefInfo));
    }

    @Test
    void should_throw_ScdException_when_the_given_binding_info_does_not_refer_to_an_existing_IED_LDevice_and_LNode_in_the_SCL() {

        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME1"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INST11"));
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo.setSourceInfo(null);
        extRefInfo.getSignalInfo().setDesc(null);
        extRefInfo.getSignalInfo().setPLN(null);
        extRefInfo.getSignalInfo().setPServT(null);
        extRefInfo.getSignalInfo().setPDA("da11.bda111.bda112.bda113");
        extRefInfo.getSignalInfo().setPDO("Do11.sdo11");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR11");

        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        extRefInfo.setBindingInfo(bindingInfo);

        //When
        //Then
        assertThrows(ScdException.class, () -> lnAdapter.extractExtRefFromExtRefInfo(extRefInfo));
    }

    @Test
    void should_check_with_success_extRefInfo_coherence() {

        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        LDeviceAdapter lDeviceAdapter = iAdapter.getLDeviceAdapterByLdInst("LD_INST11");

        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo.getSignalInfo().setDesc(null);
        extRefInfo.getSignalInfo().setPLN(null);
        extRefInfo.getSignalInfo().setPServT(null);
        extRefInfo.getSignalInfo().setPDA("da11.bda111.bda112.bda113");
        extRefInfo.getSignalInfo().setPDO("Do11.sdo11");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR11");

        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        bindingInfo.setServiceType(null);
        bindingInfo.setIedName("IED_NAME1");
        bindingInfo.setLdInst("LD_INST12");
        bindingInfo.setLnInst("1");
        bindingInfo.setLnClass("ANCR");
        bindingInfo.setPrefix("PR");

        extRefInfo.setBindingInfo(bindingInfo);

        ExtRefSourceInfo sourceInfo = new ExtRefSourceInfo();
        sourceInfo.setSrcCBName("rpt1");
        extRefInfo.setSourceInfo(sourceInfo);

        //When
        //Then
        assertDoesNotThrow(() -> lnAdapter.checkExtRefInfoCoherence(extRefInfo));
    }

    @Test
    void should_extract_ExtRef_from_ExtRefInfo() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        LDeviceAdapter lDeviceAdapter = iAdapter.getLDeviceAdapterByLdInst("LD_INST11");

        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo.setSourceInfo(null);
        extRefInfo.getSignalInfo().setDesc(null);
        extRefInfo.getSignalInfo().setPLN(null);
        extRefInfo.getSignalInfo().setPServT(null);
        extRefInfo.getSignalInfo().setPDA("da11.bda111.bda112.bda113");
        extRefInfo.getSignalInfo().setPDO("Do11.sdo11");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR11");

        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        bindingInfo.setServiceType(null);
        bindingInfo.setIedName("IED_NAME1");
        bindingInfo.setLdInst("LD_INST12");
        bindingInfo.setLnInst("1");
        bindingInfo.setLnClass("ANCR");
        bindingInfo.setPrefix("PR");

        extRefInfo.setBindingInfo(bindingInfo);

        //When
        TExtRef extRef = assertDoesNotThrow(() -> lnAdapter.extractExtRefFromExtRefInfo(extRefInfo));

        //Then
        assertEquals(extRefInfo.getSignalInfo().getPDO(), extRef.getPDO());
    }

    @ParameterizedTest
    @MethodSource("provideIncompleteExtRefInfo")
    void should_throw_exception_when_trying_update_extRefSource_with_wrong_arguments(ExtRefInfo incompleteExtrefInfo) {

        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);

        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME1"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INST11").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        //When Then
        assertThatThrownBy(() -> lnAdapter.updateExtRefSource(incompleteExtrefInfo))
                .isInstanceOf(IllegalArgumentException.class);

    }

    private static Stream<Arguments> provideIncompleteExtRefInfo() {

        ExtRefInfo extRefInfoEmpty = new ExtRefInfo();

        ExtRefInfo extRefInfoWithOnlySignalInfo = new ExtRefInfo();
        extRefInfoWithOnlySignalInfo.setSignalInfo(new ExtRefSignalInfo());

        ExtRefInfo extRefInfoWithSignalInfoAndBindingInfo = new ExtRefInfo();
        extRefInfoWithSignalInfoAndBindingInfo.setSignalInfo(new ExtRefSignalInfo());
        extRefInfoWithSignalInfoAndBindingInfo.setBindingInfo(new ExtRefBindingInfo());

        return Stream.of(
                Arguments.of(extRefInfoEmpty),
                Arguments.of(extRefInfoWithOnlySignalInfo),
                Arguments.of(extRefInfoWithSignalInfoAndBindingInfo)
        );
    }

    @Test
    void testUpdateExtRefSource() {

        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME1"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INST11").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        ExtRefInfo extRefInfo = givenCompleteExtRefInfo();

        //When
        TExtRef extRef = assertDoesNotThrow(() -> lnAdapter.updateExtRefSource(extRefInfo));

        //Then
        assertThat(extRef.getSrcCBName()).isEqualTo(extRefInfo.getSourceInfo().getSrcCBName());
        assertThat(extRef.getSrcLDInst()).isEqualTo(extRefInfo.getSourceInfo().getSrcLDInst());

        assertThat(extRef.getLnClass().contains(extRefInfo.getSourceInfo().getSrcLNClass())).isTrue();

    }

    private ExtRefInfo givenCompleteExtRefInfo() {
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        extRefInfo.getSignalInfo().setPDA("da11.bda111.bda112.bda113");
        extRefInfo.getSignalInfo().setPDO("Do11.sdo11");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR11");
        extRefInfo.getBindingInfo().setIedName("IED_NAME1");
        extRefInfo.getBindingInfo().setLdInst("LD_INST12");
        extRefInfo.getBindingInfo().setLnInst("1");
        extRefInfo.getBindingInfo().setLnClass("ANCR");
        extRefInfo.getBindingInfo().setPrefix("PR");
        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        extRefInfo.getSourceInfo().setSrcCBName("rpt1");
        extRefInfo.getSourceInfo().setSrcLDInst(extRefInfo.getBindingInfo().getLdInst());
        extRefInfo.getSourceInfo().setSrcLNInst(extRefInfo.getBindingInfo().getLnInst());
        extRefInfo.getSourceInfo().setSrcLNClass(extRefInfo.getBindingInfo().getLnClass());
        extRefInfo.getSourceInfo().setSrcPrefix(extRefInfo.getBindingInfo().getPrefix());
        return extRefInfo;
    }

    @Test
    void testAddDOI() {
        TLN tln = new TLN();
        tln.getLnClass().add(DTO.HOLDER_LN_CLASS);
        tln.setPrefix(DTO.HOLDER_LN_PREFIX);
        tln.setInst(DTO.HOLDER_LN_INST);
        LNAdapter lnAdapter = new LNAdapter(null, tln);

        DOIAdapter doiAdapter = lnAdapter.addDOI("Do");
        assertEquals("Do", doiAdapter.getCurrentElem().getName());
    }

    @Test
    void testGetLNodeName() {
        TLN tln = new TLN();
        tln.getLnClass().add(DTO.HOLDER_LN_CLASS);
        tln.setPrefix(DTO.HOLDER_LN_PREFIX);
        tln.setInst(DTO.HOLDER_LN_INST);
        AbstractLNAdapter<?> lnAdapter = new LNAdapter(null, tln);
        String exp = DTO.HOLDER_LN_PREFIX + DTO.HOLDER_LN_CLASS + DTO.HOLDER_LN_INST;
        assertEquals(exp, lnAdapter.getLNodeName());

        LN0 ln0 = new LN0();
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        lnAdapter = new LN0Adapter(null, ln0);
        assertEquals(TLLN0Enum.LLN_0.value(), lnAdapter.getLNodeName());
    }

    @Test
    void testUpdateDAI() {
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        assertThrows(ScdException.class, () -> lnAdapter.updateDAI(rDtt));
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        rDtt.setDoName(doTypeName);
        assertThrows(ScdException.class, () -> lnAdapter.updateDAI(rDtt));
        rDtt.setDaName(new DaTypeName("antRef.bda1.bda2.bda3"));
        TVal tVal = new TVal();
        tVal.setValue("newValue");
        rDtt.setDaiValues(List.of(tVal));
        assertDoesNotThrow(() -> lnAdapter.updateDAI(rDtt));

        lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());
        AbstractLNAdapter<?> lnAdapter2 = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        rDtt.setValImport(true);
        rDtt.setFc(TFCEnum.SE);
        assertTrue(rDtt.isUpdatable());
        assertDoesNotThrow(() -> lnAdapter2.updateDAI(rDtt));

        System.out.println(MarshallerWrapper.marshall(scd));

    }

    @Test
    void testGetDAI() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/scd_with_dai_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LNAdapter lnAdapter = lDeviceAdapter.getLNAdapter("PIOC", "1", null);
        var rDtts = lnAdapter.getDAI(new ResumedDataTemplate(), true);
        assertFalse(rDtts.isEmpty());
    }

    private LNAdapter initLNAdapter(TLN tln) {
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        tln.getLnClass().add(DTO.HOLDER_LN_CLASS);
        tln.setInst(DTO.HOLDER_LN_INST);
        tln.setLnType(DTO.LN_TYPE);
        tln.setPrefix(DTO.HOLDER_LN_PREFIX);
        Mockito.when(tlDevice.getLN()).thenReturn(List.of(tln));
        return assertDoesNotThrow(() -> new LNAdapter(lDeviceAdapter, tln));
    }

    @Test
    void addPrivate() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/scd_with_dai_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LNAdapter lnAdapter = lDeviceAdapter.getLNAdapter("PIOC", "1", null);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(lnAdapter.getCurrentElem().getPrivate().isEmpty());
        lnAdapter.addPrivate(tPrivate);
        assertEquals(1, lnAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void elementXPath() {
        // Given
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);
        // When
        String result = lnAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("LN[@lnClass=\"LN_CLASS_H\" and @inst=\"1\" and @lnType=\"LN_TYPE\"]");
    }

    @Test
    void getControlBlocks_should_find_ControlBlock_by_name() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        LDeviceAdapter lDeviceAdapter = iAdapter.getLDeviceAdapterByLdInst("LD_INST12");

        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        List<TDataSet> tDataSets = lnAdapter.getCurrentElem().getDataSet();
        //When
        var tControls = lnAdapter.getControlBlocks(tDataSets, TServiceType.GOOSE);
       // var tControls = lnAdapter.getControlBlocks("dataset121", TGSEControl.class);

        //Then
        assertThat(tControls).isNotEmpty()
                .hasSize(1);
    }
}
