// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;

class LNAdapterTest {

    private static final String NEW_VALUE = "newValue";
    private static DoTypeName DO_TYPE_NAME;
    private static DaTypeName DA_TYPE_NAME;
    private static String DATA_TYPE_REF;
    private static DataAttributeRef RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3;
    private static DataAttributeRef DATA_ATTRIBUTE_REF_DO_DA2;


    @BeforeEach
    void setUp() {
        // As DoTypeName, DaTypeName and DataAttributeRef are mutable, make sure to re-init them before each test
        DO_TYPE_NAME = createDo("Do.sdo1.d", TPredefinedCDCEnum.WYE);
        DA_TYPE_NAME = createDa("antRef.bda1.bda2.bda3", TFCEnum.CF, true, Map.of(0L, NEW_VALUE));
        DATA_TYPE_REF = DO_TYPE_NAME + "." + DA_TYPE_NAME;
        RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3 = createDataAttributeRef(DO_TYPE_NAME, DA_TYPE_NAME);
        DATA_ATTRIBUTE_REF_DO_DA2 = createDataAttributeRef(createDo("Do", TPredefinedCDCEnum.WYE), createDa("da2", TFCEnum.CF, true, Map.of(0L, NEW_VALUE)));
    }

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

        assertThat(extRef.getLnClass()).contains(extRefInfo.getSourceInfo().getSrcLNClass());

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
    void updateDAI_should_throw_ScdException_when_DataAttributeRef_is_empty() {
        // Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        LN0Adapter lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");

        // When Then
        assertThatThrownBy(() -> lnAdapter.updateDAI(dataAttributeRef))
                .isInstanceOf(ScdException.class)
                .hasMessage("Cannot update undefined DAI");
    }

    @Test
    void updateDAI_should_throw_ScdException_when_DataAttributeRef_DA_name_is_not_defined() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        DataAttributeRef dataAttributeRef = createDataAttributeRef(DO_TYPE_NAME, null);

        // When Then
        assertThatThrownBy(() -> lnAdapter.updateDAI(dataAttributeRef))
                .isInstanceOf(ScdException.class)
                .hasMessage("Cannot update undefined DAI");
    }

    @Test
    void updateDAI_should_throw_ScdException_when_DataAttributeRef_DO_name_is_not_defined() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        DataAttributeRef dataAttributeRef = createDataAttributeRef(null, DA_TYPE_NAME);

        // When Then
        assertThatThrownBy(() -> lnAdapter.updateDAI(dataAttributeRef))
                .isInstanceOf(ScdException.class)
                .hasMessage("Cannot update undefined DAI");
    }

    @Test
    void updateDAI_should_not_update_DAI_Val_when_DTT_Fc_not_allowed_to_update() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        DataAttributeRef dataAttributeRef = DataAttributeRef.copyFrom(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);
        dataAttributeRef.setFc(TFCEnum.BL);
        final String OLD_VALUE = "Completed-diff";
        assertThat(dataAttributeRef.isUpdatable()).isFalse();
        assertThat(getValue(findDai(lnAdapter, RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.getDataAttributes())))
                .isEqualTo(OLD_VALUE);
        // When
        lnAdapter.updateDAI(dataAttributeRef);

        // Then
        TDAI tdai = findDai(lnAdapter, DATA_TYPE_REF).getCurrentElem();
        assertThat(getValue(tdai)).isEqualTo(OLD_VALUE);

        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_should_update_DOI_SDI_DAI_that_exists_when_data_updatable() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        assertThat(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.isUpdatable()).isTrue();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        AbstractDAIAdapter<?> daiAdapter = findDai(lnAdapter, DATA_TYPE_REF);
        assertThat(getValue(daiAdapter)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_should_update_DOI_DAI_that_exists_when_data_updatable() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        assertThat(DATA_ATTRIBUTE_REF_DO_DA2.isUpdatable()).isTrue();

        // When
        lnAdapter.updateDAI(DATA_ATTRIBUTE_REF_DO_DA2);

        // Then
        AbstractDAIAdapter<?> daiAdapter = findDai(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDataAttributes());
        assertThat(getValue(daiAdapter)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_should_create_DOI_SDI_DAI_elements_with_new_value_when_data_updatable() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS2");
        assertThat(lnAdapter.getCurrentElem().getDOI()).isEmpty();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        TDAI tdai = findDai(lnAdapter, DATA_TYPE_REF).getCurrentElem();
        assertThat(getValue(tdai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_should_create_DOI_DAI_elements_with_new_value_when_data_updatable() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS2");
        DataAttributeRef dataAttributeRef = DATA_ATTRIBUTE_REF_DO_DA2;
        assertThat(lnAdapter.getCurrentElem().getDOI()).isEmpty();

        // When
        lnAdapter.updateDAI(dataAttributeRef);

        // Then
        AbstractDAIAdapter<?> daiAdapter = findDai(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDataAttributes());
        assertThat(getValue(daiAdapter)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_should_not_update_DAI_values_when_not_updatable() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        LN0Adapter lnAdapter = (LN0Adapter) AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        DataAttributeRef dataAttributeRef = DataAttributeRef.copyFrom(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);
        dataAttributeRef.setValImport(false);
        assertThat(dataAttributeRef.isUpdatable()).isFalse();

        // When & Then
        assertThatCode(() -> lnAdapter.updateDAI(dataAttributeRef)).doesNotThrowAnyException();
    }

    @Test
    void updateDAI_when_nothing_instantiated_should_create_DOI_and_DAI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        lnAdapter.getCurrentElem().getDOI().clear();
        assertThat(lnAdapter.getCurrentElem().getDOI()).isEmpty();

        // When
        lnAdapter.updateDAI(DATA_ATTRIBUTE_REF_DO_DA2);

        // Then
        TDAI dai = findDai(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_when_only_DOI_instantiated_should_create_DOI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        lnAdapter.getDOIAdapterByName(DATA_ATTRIBUTE_REF_DO_DA2.getDoRef()).getSDIOrDAI().clear();
        assertThat(findDoiOrSdi(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDoName().getName()).getSDIOrDAI()).isEmpty();

        // When
        lnAdapter.updateDAI(DATA_ATTRIBUTE_REF_DO_DA2);

        // Then
        TDAI dai = findDai(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_when_already_instantiated_should_do_nothing() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        assertThat(findDai(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDataAttributes())).isNotNull();

        // When
        lnAdapter.updateDAI(DATA_ATTRIBUTE_REF_DO_DA2);

        // Then
        TDAI dai = findDai(lnAdapter, DATA_ATTRIBUTE_REF_DO_DA2.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_with_SDO_and_BDA_when_no_elements_instantiated_should_create_SDI_DAI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        lnAdapter.getCurrentElem().getDOI().clear();
        assertThat(lnAdapter.getCurrentElem().getDOI()).isEmpty();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        TDAI dai = findDai(lnAdapter, RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_with_SDO_and_BDA_when_only_DOI_is_instantiated_should_create_DOI_SDI_DAI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        lnAdapter.getDOIAdapterByName(DO_TYPE_NAME.getName()).getCurrentElem().getSDIOrDAI().clear();
        assertThat(findDoiOrSdi(lnAdapter, DO_TYPE_NAME.getName()).getSDIOrDAI()).isEmpty();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        TDAI dai = findDai(lnAdapter, RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void updateDAI_with_SDO_and_BDA_when_some_SDI_instantiated_should_create_missing_SDI_DAI(int numberOfSdi) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        String refOfExistingSDIs = String.join(".", Arrays.asList(DATA_TYPE_REF.split("\\.")).subList(0, numberOfSdi + 1));
        findDoiOrSdi(lnAdapter, refOfExistingSDIs).getSDIOrDAI().clear();
        assertThat(findDoiOrSdi(lnAdapter, refOfExistingSDIs).getSDIOrDAI()).isEmpty();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        TDAI dai = findDai(lnAdapter, RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_with_SDO_and_BDA_when_only_DAI_is_missing_should_create_DAI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        AbstractDAIAdapter<?> daiToRemove = findDai(lnAdapter, DATA_TYPE_REF);
        ((IDataParentAdapter) daiToRemove.getParentAdapter()).getSDIOrDAI().clear();
        assertThat(findDoiOrSdi(lnAdapter, DATA_TYPE_REF.substring(0, DATA_TYPE_REF.lastIndexOf("."))).getSDIOrDAI()).isEmpty();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        TDAI dai = findDai(lnAdapter, RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void updateDAI_with_SDO_and_BDA_when_already_instantiated_should_do_nothing() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        AbstractLNAdapter<?> lnAdapter = findLn0(scd, "IED_NAME", "LD_INS1");
        assertThat(findDai(lnAdapter, DATA_TYPE_REF)).isNotNull();

        // When
        lnAdapter.updateDAI(RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3);

        // Then
        TDAI dai = findDai(lnAdapter, RDDT_DO_SDO1_D_ANTREF_BDA1_BDA2_BDA3.getDataAttributes()).getCurrentElem();
        assertThat(dai.isValImport()).isTrue();
        assertThat(getValue(dai)).isEqualTo(NEW_VALUE);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void testGetDAI() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/scd_with_dai_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get());
        LNAdapter lnAdapter = lDeviceAdapter.getLNAdapter("PIOC", "1", null);
        var dataAttributeRefs = lnAdapter.getDAI(new DataAttributeRef(), true);
        assertFalse(dataAttributeRefs.isEmpty());
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
        List<ControlBlock> tControls = lnAdapter.getControlBlocks(tDataSets, TServiceType.GOOSE);

        //Then
        assertThat(tControls).isNotEmpty()
                .hasSize(1);
    }

    @Test
    void getTControlsByType_should_return_list_of_report_control_blocks() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        LNAdapter lnAdapter = findLn(scd, "IED4d4fe1a8cda64cf88a5ee4176a1a0eef", "LDSUIED", "LPAI", "1", null);
        // When
        List<TReportControl> tControlsByType = lnAdapter.getTControlsByType(TReportControl.class);
        // Then
        assertThat(tControlsByType).isSameAs(lnAdapter.getCurrentElem().getReportControl());
    }

    @ParameterizedTest
    @MethodSource("provideGetTControlsByTypeException")
    void getTControlsByType_should_throw_when_unsupported_controlBlock_for_ln(Class<? extends TControl> tControlClass) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        LNAdapter lnAdapter = findLn(scd, "IED4d4fe1a8cda64cf88a5ee4176a1a0eef", "LDSUIED", "LPAI", "1", null);
        // When & Then
        assertThatThrownBy(() -> lnAdapter.getTControlsByType(tControlClass))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> provideGetTControlsByTypeException() {
        return Stream.of(
                Arguments.of(TGSEControl.class),
                Arguments.of(TSampledValueControl.class)
        );
    }

}
