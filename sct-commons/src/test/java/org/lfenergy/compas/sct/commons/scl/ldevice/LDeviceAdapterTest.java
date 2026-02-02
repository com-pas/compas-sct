// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ldevice;

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
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;

class LDeviceAdapterTest {

    private IEDAdapter iedAdapter;

    @BeforeEach
    public void init() {
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        iedAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
    }

    @Test
    void updateLDName_when_ldName_should_update() {
        // Given
        SCL std = SclTestMarshaller.getSCLFromResource("std/std_sample.std");
        TIED tied = std.getIED().getFirst();
        TLDevice tlDevice = tied.getAccessPoint().getFirst().getServer().getLDevice().getFirst();
        LDeviceAdapter lDeviceAdapter = new LDeviceAdapter(new IEDAdapter(new SclRootAdapter(std), tied), tlDevice);

        //When
        lDeviceAdapter.updateLDName();

        //When
        assertThat(lDeviceAdapter.getLdName()).isEqualTo("IED4d4fe1a8cda64cf88a5ee4176a1a0eefLDSUIED");
        assertThat(lDeviceAdapter.getInst()).isEqualTo("LDSUIED");
    }

    @Test
    @Tag("issue-321")
    void testGetLNAdapters()  {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst("LD_INS2").orElseThrow());
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
        LDeviceAdapter lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst("LD_INS2").orElseThrow();
        // When
        Optional<LNAdapter> lnAdapter = lDeviceAdapter.findLnAdapter("ANCR", "1", null);
        // Then
        assertThat(lnAdapter).get().extracting(LNAdapter::getLNClass, LNAdapter::getLNInst, LNAdapter::getPrefix)
            .containsExactly("ANCR", "1", "");
    }

    @Test
    void getExtRefBinders_whenExist_shouldReturnExtRefBindingInfo() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst("LD_INS2").orElseThrow());
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
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iedAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("CSWI");
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).isEmpty();
    }

    @Test
    void getExtRefBinders_when_PLN_NotSet_shouldReturnEmptyList() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iedAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).hasSize(2);
    }

    @Test
    @Tag("issue-321")
    void getExtRefInfo_should_return_expected_list_of_ExtRefInfo() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iedAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        // When
        List<ExtRefInfo> extRefInfoList = assertDoesNotThrow(lDeviceAdapter::getExtRefInfo);
        // Then
        assertThat(extRefInfoList).hasSize(2);
    }

    @Test
    @Tag("issue-321")
    void TestGetDAI() {
        // Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iedAdapter.getLDeviceAdapterByLdInst("LD_INS1"));
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
        lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst("LD_INS2").orElseThrow());
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
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iedAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
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
    void getLNAdaptersIncludingLN0_should_return_expected_list_of_AbstractLNAdapter() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iedAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
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
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(tServices);
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
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(tServices);
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
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(new TServices());

        // When
        boolean hasCapability = lDeviceAdapter.hasDataSetCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isFalse();
    }

    @Test
    void hasDataSetCreationCapability_when_parameter_is_null_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(new TServices());

        // When & Then
        Assertions.assertThatThrownBy(() -> lDeviceAdapter.hasDataSetCreationCapability(null))
            .isInstanceOf(NullPointerException.class);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHasControlBlockCreationCapabilityTrue")
    void hasControlBlockCreationCapability_when_attribute_exists_should_return_true(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(tServices);
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
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(tServices);
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
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(new TServices());

        // When
        boolean hasCapability = lDeviceAdapter.hasControlBlockCreationCapability(controlBlockEnum);
        //Then
        assertThat(hasCapability).isFalse();
    }

    @Test
    void hasControlBlockCreationCapability_when_parameter_is_null_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst("LD_INS1");
        iedAdapter.getCurrentElem().getAccessPoint().getFirst().setServices(new TServices());

        // When & Then
        Assertions.assertThatThrownBy(() -> lDeviceAdapter.hasControlBlockCreationCapability(null))
                .isInstanceOf(NullPointerException.class);
    }

}
