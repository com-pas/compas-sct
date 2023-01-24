// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;

class LDeviceAdapterTest {

    private IEDAdapter iAdapter;

    @BeforeEach
    public void init() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
    }

    @Test
    void testUpdateLDName() {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        lDeviceAdapter.updateLDName();
        assertEquals("IED_NAMELD_INS1",lDeviceAdapter.getLdName());
        iAdapter.setIEDName("VERY_VERY_VERY_VERY_VERY_VERY_LONG_IED_NAME");
        assertThrows(ScdException.class, ()-> lDeviceAdapter.updateLDName());

        assertEquals("LD_INS1", lDeviceAdapter.getInst());
    }

    @Test
    void testGetLNAdapters()  {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());

        assertEquals(1,lDeviceAdapter.getLNAdapters().size());

        assertDoesNotThrow(() -> lDeviceAdapter.getLNAdapter("ANCR","1",null));
        assertThrows(ScdException.class, () -> lDeviceAdapter.getLNAdapter("ANCR","1","pr"));
    }

    @Test
    void getExtRefBinders_shouldReturnExtRefBindingInfo_whenExist() {
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
    void getExtRefBinders_shouldReturnEmptyList_whenpLNNotMatch() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPLN("CSWI");
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).isEmpty();
    }

    @Test
    void getExtRefBinders_shouldReturnEmptyList_whenpLNNotSet() {
        //Given
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        //When Then
        assertDoesNotThrow(()-> lDeviceAdapter.getExtRefBinders(signalInfo));
        assertThat(lDeviceAdapter.getExtRefBinders(signalInfo)).hasSize(2);
    }

    @Test
    void testGetExtRefInfo() {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        List<ExtRefInfo> extRefInfoList = assertDoesNotThrow(()-> lDeviceAdapter.getExtRefInfo());
        assertEquals(2,extRefInfoList.size());
    }

    @Test
    void TestGetDAI() {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS1"));
        var rDtts = lDeviceAdapter.getDAI(new ResumedDataTemplate(),true);
        assertEquals(4,rDtts.size());



        ResumedDataTemplate filter = new ResumedDataTemplate();
        filter.setLnClass(TLLN0Enum.LLN_0.value());
        rDtts = lDeviceAdapter.getDAI(filter,true);
        assertEquals(4,rDtts.size());

        lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get());
        filter.setLnClass("ANCR");
        filter.setLnInst("1");
        rDtts = lDeviceAdapter.getDAI(filter,true);
        assertEquals(2,rDtts.size());
    }

    @Test
    void addPrivate() {
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.getLDeviceAdapterByLdInst("LD_INS2"));
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(lDeviceAdapter.getCurrentElem().getPrivate().isEmpty());
        lDeviceAdapter.addPrivate(tPrivate);
        assertEquals(1, lDeviceAdapter.getCurrentElem().getPrivate().size());
    }

    @ParameterizedTest
    @CsvSource(value = {"ldInst;LDevice[@inst=\"ldInst\"]", ";LDevice[not(@inst)]"}
            , delimiter = ';')
    void elementXPath(String ldInst, String message) {
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
    void getLNAdaptersInclundigLN0() {
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
    void hasDataSetCreationCapability_should_return_true_when_attribute_exists(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
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
    void hasDataSetCreationCapability_should_return_false_when_wrong_attribute(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
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
    void hasDataSetCreationCapability_should_return_false_when_no_existing_services_attribute(ControlBlockEnum controlBlockEnum) {
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
    void hasDataSetCreationCapability_should_throw_exception_when_parameter_is_null() {
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
    void hasControlBlockCreationCapability_should_return_true_when_attribute_exists(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
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
    void hasControlBlockCreationCapability_should_return_false_when_wrong_attribute(String testCase, TServices tServices, ControlBlockEnum controlBlockEnum) {
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
    void hasControlBlockCreationCapability_should_return_false_when_no_existing_services_attribute(ControlBlockEnum controlBlockEnum) {
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
    void hasControlBlockCreationCapability_should_throw_exception_when_parameter_is_null() {
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

}

