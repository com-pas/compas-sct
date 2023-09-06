// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.MonitoringLnClassEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter.MOD_DO_TYPE_NAME;
import static org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter.STVAL_DA_TYPE_NAME;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.LDEVICE_LDEPF;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;
import static org.lfenergy.compas.sct.commons.util.Utils.copySclElement;

class LDeviceAdapterTest {

    private IEDAdapter iAdapter;

    @BeforeEach
    public void init() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
    }

    @Test
    void updateLDName_when_ldName_pass_33_characters_should_throw_exception() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        lDeviceAdapter.updateLDName();
        assertThat(lDeviceAdapter.getLdName()).isEqualTo("IED_NAMELD_INS1");
        assertThat(lDeviceAdapter.getInst()).isEqualTo("LD_INS1");
        String iedName = new Random().ints(97, 122)
                .limit(27)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        iAdapter.setIEDName(iedName);
        // When Then
        assertThatThrownBy(lDeviceAdapter::updateLDName)
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("has more than 33 characters");
    }

    @Test
    void updateLDName_when_ldName_less_than_33_characters_should_not_throw_exception() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        lDeviceAdapter.updateLDName();
        assertThat(lDeviceAdapter.getLdName()).isEqualTo("IED_NAMELD_INS1");
        assertThat(lDeviceAdapter.getInst()).isEqualTo("LD_INS1");
        String iedName = new Random().ints(97, 122)
                .limit(26)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        iAdapter.setIEDName(iedName);
        // When Then
        assertThatCode(lDeviceAdapter::updateLDName).doesNotThrowAnyException();
        assertThat(lDeviceAdapter.getLdName()).isEqualTo(iedName+"LD_INS1");
    }

    @Test
    @Tag("issue-321")
    void testGetLNAdapters()  {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());
        assertThat(lDeviceAdapter.getLNAdapters()).hasSize(1);
        // When Then
        assertDoesNotThrow(() -> lDeviceAdapter.getLNAdapter("ANCR","1",null));
        // When Then
        assertThatThrownBy(() -> lDeviceAdapter.getLNAdapter("ANCR","1","pr"))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void findLnAdapter_shouldReturnAdapter(){
        // Given
        LDeviceAdapter lDeviceAdapter = iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get();
        // When
        Optional<LNAdapter> lnAdapter = lDeviceAdapter.findLnAdapter("ANCR", "1", null);
        // Then
        assertThat(lnAdapter).get().extracting(LNAdapter::getLNClass, LNAdapter::getLNInst, LNAdapter::getPrefix)
            .containsExactly("ANCR", "1", "");
    }

    @Test
    void getExtRefBinders_whenExist_shouldReturnExtRefBindingInfo() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        signalInfo.setPDO("Do.sdo1");
        signalInfo.setPDA("da.bda1.bda2.bda3");
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).hasSize(1);
    }

    @Test
    void getExtRefBinders_when_PLN_NotMatch_shouldReturnEmptyList() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("CSWI");
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).isEmpty();
    }

    @Test
    void getExtRefBinders_when_PLN_NotSet_shouldReturnEmptyList() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).hasSize(2);
    }

    @Test
    @Tag("issue-321")
    void getExtRefInfo_should_return_expected_list_of_ExtRefInfo() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        // When
        List<ExtRefInfo> extRefInfoList = assertDoesNotThrow(lDeviceAdapter::getExtRefInfo);
        // Then
        assertThat(extRefInfoList).hasSize(2);
    }

    @Test
    @Tag("issue-321")
    void TestGetDAI() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS1"));
        // When
        var dataAttributeRefs = lDeviceAdapter.getDAI(new DataAttributeRef(),true);
        // Then
        assertThat(dataAttributeRefs).hasSize(4);

        // Given
        DataAttributeRef filter = new DataAttributeRef();
        filter.setLnClass(TLLN0Enum.LLN_0.value());
        // When
        dataAttributeRefs = lDeviceAdapter.getDAI(filter,true);
        // Then
        assertThat(dataAttributeRefs).hasSize(4);

        // Given
        lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());
        filter.setLnClass("ANCR");
        filter.setLnInst("1");
        // When
        dataAttributeRefs = lDeviceAdapter.getDAI(filter,true);
        // Then
        assertThat(dataAttributeRefs).hasSize(2);
    }

    @Test
    @Tag("issue-321")
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(lDeviceAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        lDeviceAdapter.addPrivate(tPrivate);
        // Then
        assertThat(lDeviceAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {"ldInst;LDevice[@inst=\"ldInst\"]", ";LDevice[not(@inst)]"}
            , delimiter = ';')
    void elementXPath_should_return_expected_xpath_value(String ldInst, String message) {
        // Given
        TLDevice tlDevice = new TLDevice();
        tlDevice.setInst(ldInst);
        LDeviceAdapter lDeviceAdapter = new LDeviceAdapter(null, tlDevice);
        // When
        String elementXPathResult = lDeviceAdapter.elementXPath();
        // Then
        assertThat(elementXPathResult).isEqualTo(message);
    }

    @Test
    void getLDeviceStatus_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        Optional<LDeviceAdapter> optionalLDeviceAdapter = sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(lDeviceAdapter -> "IED_NAME1LD_INST13".equals(lDeviceAdapter.getLdName()))
            .findFirst();
        assertThat(optionalLDeviceAdapter).isPresent();
        LDeviceAdapter lDeviceAdapter = optionalLDeviceAdapter.get();
        // When
        Optional<String> result = lDeviceAdapter.getLDeviceStatus();
        // Then
        assertThat(result)
            .isPresent()
            .hasValue("test");
    }

    @Test
    void getLNAdaptersIncludingLN0_should_return_expected_list_of_AbstractLNAdapter() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        //When
        List<AbstractLNAdapter<?>> lnAdapters = lDeviceAdapter.getLNAdaptersIncludingLN0();
        //Then
        assertThat(lnAdapters)
                .hasSize(2)
                .hasAtLeastOneElementOfType(LN0Adapter.class)
                .hasAtLeastOneElementOfType(LNAdapter.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHasDataSetCreationCapabilityTrue")
    void hasDataSetCreationCapability_when_attribute_exists_should_return_true(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(tServices);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        // When
        boolean hasCapability = lDeviceAdapter.hasDataSetCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isTrue();
    }

    private static Stream<Arguments> provideHasDataSetCreationCapabilityTrue() {
        TServices tServicesLogConf = new TServices();
        TLogSettings tLogSettingsConf = new TLogSettings();
        tLogSettingsConf.setDatSet(TServiceSettingsEnum.CONF);
        tServicesLogConf.setLogSettings(tLogSettingsConf);

        TServices tServicesLogDyn = new TServices();
        TLogSettings tLogSettingsDyn = new TLogSettings();
        tLogSettingsDyn.setDatSet(TServiceSettingsEnum.DYN);
        tServicesLogDyn.setLogSettings(tLogSettingsDyn);

        TServices tServicesGseConf = new TServices();
        TGSESettings tgseSettingsConf = new TGSESettings();
        tgseSettingsConf.setDatSet(TServiceSettingsEnum.CONF);
        tServicesGseConf.setGSESettings(tgseSettingsConf);

        TServices tServicesGseDyn = new TServices();
        TGSESettings tgseSettingsDyn = new TGSESettings();
        tgseSettingsDyn.setDatSet(TServiceSettingsEnum.DYN);
        tServicesGseDyn.setGSESettings(tgseSettingsDyn);

        TServices tServicesReportConf = new TServices();
        TReportSettings treportSettingsConf = new TReportSettings();
        treportSettingsConf.setDatSet(TServiceSettingsEnum.CONF);
        tServicesReportConf.setReportSettings(treportSettingsConf);

        TServices tServicesReportDyn = new TServices();
        TReportSettings treportSettingsDyn = new TReportSettings();
        treportSettingsDyn.setDatSet(TServiceSettingsEnum.DYN);
        tServicesReportDyn.setReportSettings(treportSettingsDyn);

        TServices tServicesSmvConf = new TServices();
        TSMVSettings tsmvSettingsConf = new TSMVSettings();
        tsmvSettingsConf.setDatSet(TServiceSettingsEnum.CONF);
        tServicesSmvConf.setSMVSettings(tsmvSettingsConf);

        TServices tServicesSmvDyn = new TServices();
        TSMVSettings tsmvSettingsDyn = new TSMVSettings();
        tsmvSettingsDyn.setDatSet(TServiceSettingsEnum.DYN);
        tServicesSmvDyn.setSMVSettings(tsmvSettingsDyn);

        return Stream.of(
            Arguments.of("AccessPoint has creation capability when LogSetting.datSet=CONF", tServicesLogConf, LOG),
            Arguments.of("AccessPoint has creation capability when LogSetting.datSet=DYN", tServicesLogDyn, LOG),
            Arguments.of("AccessPoint has creation capability when GseSetting.datSet=CONF", tServicesGseConf, GSE),
            Arguments.of("AccessPoint has creation capability when GseSetting.datSet=DYN", tServicesGseDyn, GSE),
            Arguments.of("AccessPoint has creation capability when ReportSetting.datSet=CONF", tServicesReportConf, REPORT),
            Arguments.of("AccessPoint has creation capability when ReportSetting.datSet=DYN", tServicesReportDyn, REPORT),
            Arguments.of("AccessPoint has creation capability when SmvSetting.datSet=CONF", tServicesSmvConf, SAMPLED_VALUE),
            Arguments.of("AccessPoint has creation capability when SmvSetting.datSet=DYN", tServicesSmvDyn, SAMPLED_VALUE)
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHasDataSetCreationCapabilityFalse")
    void hasDataSetCreationCapability_when_wrong_attribute_should_return_false(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(tServices);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");

        // When
        boolean hasCapability = lDeviceAdapter.hasDataSetCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isFalse();
    }

    private static Stream<Arguments> provideHasDataSetCreationCapabilityFalse() {
        TServices tServicesLogFix = new TServices();
        TLogSettings tLogSettingsFix = new TLogSettings();
        tLogSettingsFix.setDatSet(TServiceSettingsEnum.FIX);
        tServicesLogFix.setLogSettings(tLogSettingsFix);

        TServices tServicesGseFix = new TServices();
        TGSESettings tgseSettingsFix = new TGSESettings();
        tgseSettingsFix.setDatSet(TServiceSettingsEnum.FIX);
        tServicesGseFix.setGSESettings(tgseSettingsFix);

        TServices tServicesReportFix = new TServices();
        TReportSettings treportSettingsFix = new TReportSettings();
        treportSettingsFix.setDatSet(TServiceSettingsEnum.FIX);
        tServicesReportFix.setReportSettings(treportSettingsFix);

        TServices tServicesSmvFix = new TServices();
        TSMVSettings tsmvSettingsFix = new TSMVSettings();
        tsmvSettingsFix.setDatSet(TServiceSettingsEnum.FIX);
        tServicesSmvFix.setSMVSettings(tsmvSettingsFix);

        return Stream.of(
            Arguments.of("AccessPoint has no creation capability when LogSetting.datSet=FIX", tServicesLogFix, LOG),
            Arguments.of("AccessPoint has no creation capability when GseSetting.datSet=FIX", tServicesGseFix, GSE),
            Arguments.of("AccessPoint has no creation capability when ReportSetting.datSet=FIX", tServicesReportFix, REPORT),
            Arguments.of("AccessPoint has no creation capability when SmvSetting.datSet=FIX", tServicesSmvFix, SAMPLED_VALUE)
        );
    }


    @ParameterizedTest
    @EnumSource(ControlBlockEnum.class)
    void hasDataSetCreationCapability_when_no_existing_services_attribute_should_return_false(ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(new TServices());

        // When
        boolean hasCapability = lDeviceAdapter.hasDataSetCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isFalse();
    }

    @Test
    void hasDataSetCreationCapability_when_parameter_is_null_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(new TServices());

        // When & Then
        Assertions.assertThatThrownBy(() -> lDeviceAdapter.hasDataSetCreationCapability(null))
            .isInstanceOf(NullPointerException.class);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHasControlBlockCreationCapabilityTrue")
    void hasControlBlockCreationCapability_when_attribute_exists_should_return_true(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(tServices);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        // When
        boolean hasCapability = lDeviceAdapter.hasControlBlockCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isTrue();
    }

    private static Stream<Arguments> provideHasControlBlockCreationCapabilityTrue() {
        TServices tServicesLogConf = new TServices();
        TLogSettings tLogSettingsConf = new TLogSettings();
        tLogSettingsConf.setCbName(TServiceSettingsNoDynEnum.CONF);
        tServicesLogConf.setLogSettings(tLogSettingsConf);

        TServices tServicesGseConf = new TServices();
        TGSESettings tgseSettingsConf = new TGSESettings();
        tgseSettingsConf.setCbName(TServiceSettingsNoDynEnum.CONF);
        tServicesGseConf.setGSESettings(tgseSettingsConf);

        TServices tServicesReportConf = new TServices();
        TReportSettings treportSettingsConf = new TReportSettings();
        treportSettingsConf.setCbName(TServiceSettingsNoDynEnum.CONF);
        tServicesReportConf.setReportSettings(treportSettingsConf);

        TServices tServicesSmvConf = new TServices();
        TSMVSettings tsmvSettingsConf = new TSMVSettings();
        tsmvSettingsConf.setCbName(TServiceSettingsNoDynEnum.CONF);
        tServicesSmvConf.setSMVSettings(tsmvSettingsConf);


        return Stream.of(
            Arguments.of("AccessPoint has creation capability when LogSetting.cbName=CONF", tServicesLogConf, LOG),
            Arguments.of("AccessPoint has creation capability when GseSetting.cbName=CONF", tServicesGseConf, GSE),
            Arguments.of("AccessPoint has creation capability when ReportSetting.cbName=CONF", tServicesReportConf, REPORT),
            Arguments.of("AccessPoint has creation capability when SmvSetting.cbName=CONF", tServicesSmvConf, SAMPLED_VALUE)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHasControlBlockCreationCapabilityFalse")
    void hasControlBlockCreationCapability_when_wrong_attribute_should_return_false(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(tServices);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");

        // When
        boolean hasCapability = lDeviceAdapter.hasControlBlockCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isFalse();
    }

    private static Stream<Arguments> provideHasControlBlockCreationCapabilityFalse() {
        TServices tServicesLogFix = new TServices();
        TLogSettings tLogSettingsFix = new TLogSettings();
        tLogSettingsFix.setCbName(TServiceSettingsNoDynEnum.FIX);
        tServicesLogFix.setLogSettings(tLogSettingsFix);

        TServices tServicesGseFix = new TServices();
        TGSESettings tgseSettingsFix = new TGSESettings();
        tgseSettingsFix.setCbName(TServiceSettingsNoDynEnum.FIX);
        tServicesGseFix.setGSESettings(tgseSettingsFix);

        TServices tServicesReportFix = new TServices();
        TReportSettings treportSettingsFix = new TReportSettings();
        treportSettingsFix.setCbName(TServiceSettingsNoDynEnum.FIX);
        tServicesReportFix.setReportSettings(treportSettingsFix);

        TServices tServicesSmvFix = new TServices();
        TSMVSettings tsmvSettingsFix = new TSMVSettings();
        tsmvSettingsFix.setCbName(TServiceSettingsNoDynEnum.FIX);
        tServicesSmvFix.setSMVSettings(tsmvSettingsFix);

        return Stream.of(
            Arguments.of("AccessPoint has no creation capability when LogSetting.cbName=FIX", tServicesLogFix, LOG),
            Arguments.of("AccessPoint has no creation capability when GseSetting.cbName=FIX", tServicesGseFix, GSE),
            Arguments.of("AccessPoint has no creation capability when ReportSetting.cbName=FIX", tServicesReportFix, REPORT),
            Arguments.of("AccessPoint has no creation capability when SmvSetting.cbName=FIX", tServicesSmvFix, SAMPLED_VALUE)
        );
    }

    @ParameterizedTest
    @EnumSource(ControlBlockEnum.class)
    void hasControlBlockCreationCapability_when_no_existing_services_attribute_should_return_false(ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(new TServices());

        // When
        boolean hasCapability = lDeviceAdapter.hasControlBlockCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isFalse();
    }

    @Test
    void hasControlBlockCreationCapability_when_parameter_is_null_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).setServices(new TServices());

        // When & Then
        Assertions.assertThatThrownBy(() -> lDeviceAdapter.hasControlBlockCreationCapability(null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideLnClassAndDoType")
    void manageMonitoringLns_when_no_extRef_should_not_update_ln(String testCase, MonitoringLnClassEnum lnClassEnum, String doName) {
        // Given
        LDeviceAdapter lDeviceAdapter = createIedsInScl(lnClassEnum.value(), doName).getIEDAdapterByName(IED_NAME_1).getLDeviceAdapterByLdInst(LD_SUIED);
        // When
        Optional<SclReportItem> sclReportItem = lDeviceAdapter.manageMonitoringLns(new ArrayList<>(), doName, lnClassEnum);
        // Then
        assertThat(sclReportItem).isNotPresent();
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(1);
        assertThat(lDeviceAdapter.getLNAdapters().get(0).getLNInst()).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideLnClassAndDoType")
    void manageMonitoringLns_when_no_init_ln_should_not_create_ln(String testCase, MonitoringLnClassEnum lnClassEnum, String doName) {
        // Given
        LDeviceAdapter lDeviceAdapter = createIedsInScl(lnClassEnum.value(), doName).getIEDAdapterByName(IED_NAME_1).getLDeviceAdapterByLdInst(LD_SUIED);
        lDeviceAdapter.getCurrentElem().unsetLN();
        // When
        Optional<SclReportItem> sclReportItem = lDeviceAdapter.manageMonitoringLns(List.of(new TExtRef()), doName, lnClassEnum);
        // Then
        assertThat(sclReportItem).isPresent();
        assertThat(sclReportItem.get())
                .extracting(SclReportItem::message)
                .isEqualTo("There is no LN " + lnClassEnum.value() + " present in LDevice");
        assertThat(lDeviceAdapter.getLNAdapters()).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideLnClassAndDoType")
    void manageMonitoringLns_when_one_extRef_and_dai_updatable_should_update_ln(String testCase, MonitoringLnClassEnum lnClassEnum, String doName, TServiceType tServiceType) {
        // Given
        LDeviceAdapter lDeviceAdapter = createIedsInScl(lnClassEnum.value(), doName).getIEDAdapterByName(IED_NAME_1).getLDeviceAdapterByLdInst(LD_SUIED);
        TExtRef tExtRef = createExtRefExample("CB_Name", tServiceType);

        // When
        Optional<SclReportItem> sclReportItem = lDeviceAdapter.manageMonitoringLns(List.of(tExtRef), doName, lnClassEnum);
        // Then
        assertThat(sclReportItem).isNotPresent();
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(1)
                .map(LNAdapter::getLNInst)
                .isEqualTo(List.of("1"));
        assertThat(getDaiValues(lDeviceAdapter, lnClassEnum.value(), doName, "setSrcRef"))
                .hasSize(1)
                .extracting(TVal::getValue)
                .containsExactly("LD_Name/LLN0.CB_Name");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideLnClassAndDoType")
    void manageMonitoringLns_when_one_extRef_and_dai_not_updatable_should_not_update_ln(String testCase, MonitoringLnClassEnum lnClassEnum, String doName, TServiceType tServiceType) {
        // Given
        SclRootAdapter sclRootAdapter = createIedsInScl(lnClassEnum.value(), doName);
        sclRootAdapter.getDataTypeTemplateAdapter().getDOTypeAdapterById("REF").get().getDAAdapterByName("setSrcRef")
                .get().getCurrentElem().setValImport(false);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName(IED_NAME_1).getLDeviceAdapterByLdInst(LD_SUIED);
        TExtRef tExtRef = createExtRefExample("CB_Name", tServiceType);

        // When
        Optional<SclReportItem> sclReportItem = lDeviceAdapter.manageMonitoringLns(List.of(tExtRef), doName, lnClassEnum);
        // Then
        assertThat(sclReportItem).isPresent();
        assertThat(sclReportItem.get())
                .extracting(SclReportItem::message)
                .isEqualTo("The DAI cannot be updated");
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(1);
        assertThat(lDeviceAdapter.getLNAdapters().get(0).getLNInst()).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideLnClassAndDoType")
    void manageMonitoringLns_when_2_extRef_and_dai_updatable_should_update_ln(String testCase, MonitoringLnClassEnum lnClassEnum, String doName, TServiceType tServiceType) {
        // Given
        LDeviceAdapter lDeviceAdapter = createIedsInScl(lnClassEnum.value(), doName).getIEDAdapterByName(IED_NAME_1).getLDeviceAdapterByLdInst(LD_SUIED);
        TLN copiedLN1 = copySclElement(lDeviceAdapter.getLNAdapters().get(0).getCurrentElem(), TLN.class);
        copiedLN1.setInst("23");
        TLN copiedLN2 = copySclElement(lDeviceAdapter.getLNAdapters().get(0).getCurrentElem(), TLN.class);
        copiedLN2.setInst("24");
        lDeviceAdapter.getCurrentElem().getLN().addAll(List.of(copiedLN1, copiedLN2));
        TExtRef tExtRef1 = createExtRefExample("CB_Name_1", tServiceType);
        TExtRef tExtRef2 = createExtRefExample("CB_Name_2", tServiceType);

        // When
        Optional<SclReportItem> sclReportItem = lDeviceAdapter.manageMonitoringLns(List.of(tExtRef1, tExtRef2), doName, lnClassEnum);
        // Then
        assertThat(sclReportItem).isNotPresent();
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(2)
                .map(LNAdapter::getLNInst)
                .isEqualTo(List.of("1", "2"));
        assertThat(getDaiValues(lDeviceAdapter, lnClassEnum.value(), doName, "setSrcRef"))
                .hasSize(2)
                .extracting(TVal::getValue)
                .containsExactly("LD_Name/LLN0.CB_Name_1", "LD_Name/LLN0.CB_Name_2");
    }

    @Test
    void getExtRefBuyReferenceForActifLDEPF_when_LDEPF_active_and_bay_exists_should_return_existingExtRef() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_extrefbayRef.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LDEPF");
        // When
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<ExtRefInfo.ExtRefBayReference> extRefBayReferences = lDeviceAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems);
        // Then
        assertThat(extRefBayReferences).hasSize(1);
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void getExtRefBayReferenceForActifLDEPF_when_NoPrivateBuyNorIcdHeader_should_return_fatal_errors() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_extrefbayRef.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        iedAdapter.getCurrentElem().getPrivate().clear();
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LDEPF");
        // When
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<ExtRefInfo.ExtRefBayReference> extRefBayReferences = lDeviceAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems);
        // Then
        assertThat(extRefBayReferences).isEmpty();
        assertThat(sclReportItems).hasSize(2);
        assertThat(sclReportItems)
                .extracting(SclReportItem::message)
                .contains("The IED has no Private Bay", "The IED has no Private compas:ICDHeader");
    }

    @Test
    void getExtRefBayReferenceForActifLDEPF_when_DOI_Mod_notExists_should_return_fatal_errors() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_extrefbayRef.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME3").getLDeviceAdapterByLdInst("LDEPF");
        // When
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<ExtRefInfo.ExtRefBayReference> extRefBayReferences = lDeviceAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems);
        // Then
        assertThat(extRefBayReferences).isEmpty();
        assertThat(sclReportItems).hasSize(1);
        assertThat(sclReportItems)
                .extracting(SclReportItem::message)
                .containsExactly("There is no DOI@name=" + MOD_DO_TYPE_NAME + "/DAI@name=" + STVAL_DA_TYPE_NAME + "/Val for LDevice@inst" + LDEVICE_LDEPF);
    }

    @Test
    void getExtRefBayReferenceForActifLDEPF_when_LDEPF_NotActive_should_not_return_existingExtRef() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_extrefbayRef.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME2").getLDeviceAdapterByLdInst("LDEPF");
        // When
        List<SclReportItem> sclReportItems = new ArrayList<>();
        List<ExtRefInfo.ExtRefBayReference> extRefBayReferences = lDeviceAdapter.getExtRefBayReferenceForActifLDEPF(sclReportItems);
        // Then
        assertThat(extRefBayReferences).isEmpty();
        assertThat(sclReportItems).isEmpty();
    }

    private static Stream<Arguments> provideLnClassAndDoType() {
        return Stream.of(
                Arguments.of("Case GOOSE : ln LGOS", MonitoringLnClassEnum.LGOS, DO_GOCBREF, TServiceType.GOOSE),
                Arguments.of("Case SMV : ln LSVS", MonitoringLnClassEnum.LSVS, DO_SVCBREF, TServiceType.SMV)
        );
    }
}

