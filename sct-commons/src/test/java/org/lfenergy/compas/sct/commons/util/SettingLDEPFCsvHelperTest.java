// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettingData;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SettingLDEPFCsvHelperTest {

    private InputStreamReader reader;

    @BeforeEach
     void setup() {
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        reader = new InputStreamReader(inputStream);
    }

    @Test
    void readCsvFile_should_return_settings() {
        // When Then
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        assertThat(settingLDEPFCsvHelper.getSettings()).hasSize(161);
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_NoSetting_whenNotMatchExtRefDescValue() {
        // Given
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("desc1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        var result = settingLDEPFCsvHelper.getLDEPFSettingDataMatchExtRef(tExtRef);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_digitalSetting_whenMatchExtRef() {
        // Given
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        var result = settingLDEPFCsvHelper.getLDEPFSettingDataMatchExtRef(tExtRef);
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getChannelDigitalNum()).isEqualTo(1);
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_analogSetting_whenMatchExtRef() {
        // Given
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 1_1_AnalogueValue_Y_instMag_1");
        tExtRef.getPLN().add("TVTR");
        tExtRef.setPDO("VolSv");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        var result = settingLDEPFCsvHelper.getLDEPFSettingDataMatchExtRef(tExtRef);
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getChannelAnalogNum()).isEqualTo(1);
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_NoSetting_whenNotMatchExtRefPLNValue() {
        // Given
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("TOTO");
        tExtRef.setPDO("Str");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        var result = settingLDEPFCsvHelper.getLDEPFSettingDataMatchExtRef(tExtRef);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_NoSetting_when_AllSettingsNotMatchExtRefWithUnknownPDOValue() {
        // Given
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("TOTO");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        var result = settingLDEPFCsvHelper.getLDEPFSettingDataMatchExtRef(tExtRef);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getIedSources_should_return_oneIedSource_whenFlowKindIsInternalAndMatchAllVerification() {
        // Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData digitalSetting = LDEPFSettingData.builder()
                //required attribute to verify Kind of Flow
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                //required attributes to verify ExtRef
                .channelDigitalNum(10)
                .channelAnalogNum(null)
                //required attributes to verify IED source for ExtRef
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                //required attributes to verify LDEVICE source for ExtRef
                .ldInst("LDPX")
                //required attributes to verify LNODE source for ExtRef
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                //required attributes to verify DO object source for ExtRef
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).hasSize(1);
        assertThat(iedSources.get(0).getName()).isEqualTo("IED_NAME1");
    }

    @Test
    void getIedSources_should_return_OneIedSource_whenFlowKindIsInternal() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData digitalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(1)
                .channelAnalogNum(null)
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).hasSize(1);
    }


    @Test
    void getIedSources_should_return_TwoIedSources_whenFlowKindIsExternalAndTwoIedSourcesFoundWithTheSameBayUUID() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_External_iedSources_in_same_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData digitalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(10)
                .channelAnalogNum(null)
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        setCompasBayUUID(sclRootAdapter, "IED_NAME2", "bayUUID2");
        setCompasBayUUID(sclRootAdapter, "IED_NAME3", "bayUUID2");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).hasSize(2);
    }

    @Test
    void getIedSources_should_return_TwoIedSources_whenFlowKindIsExternalAndTwoIedSourcesFoundWithTheSameICDHeader() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData digitalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(10)
                .channelAnalogNum(null)
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        setCompasBayUUID(sclRootAdapter, "IED_NAME2", "bayUUID2");
        setCompasBayUUID(sclRootAdapter, "IED_NAME3", "bayUUID3");
        sclRootAdapter.getIEDAdapterByName("IED_NAME3").getCompasICDHeader().get().setIEDSystemVersioninstance(digitalSetting.getIedInstance());
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).hasSize(2);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {
            "NO_EXISTING_INST_OF_LDEVICE_SOURCE",
            "LDEVICE_INVALID"
    })
    void getIedSources_should_return_NoIedSource_whenLDevice(String ldInst) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData digitalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                .channelDigitalNum(10)
                .channelAnalogNum(null)
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                //required attributes to match LDEVICE source for ExtRef
                .ldInst(ldInst)
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }

    @Test
    void getIedSources_should_return_NoIedSource_whenFlowKindIsInternalAndNoExistingLNodeSource() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        var digitalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                .channelDigitalNum(10)
                .channelAnalogNum(null)
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                //required attributes to verify LNODE source for ExtRef
                .lnClass("PTRC").lnInst("100000").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "buyUUID1");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }


    @Test
    void getIedSources_should_return_NoIedSource_whenFlowKindIsInternalAndLNodeSourceIsOff() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        var digitalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                .channelDigitalNum(10)
                .channelAnalogNum(null)
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                //required attributes to verify LNODE source for ExtRef
                .lnClass("PTRC").lnInst("2").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        // When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }

    @Test
    void getIedSources_should_return_OneIedSource_whenFlowKindIsInternalAndDataTypeTemplateIsValid() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData analogSetting1 = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                .channelDigitalNum(null).channelAnalogNum(1)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDTM1").lnClass("TVTR").lnInst("11").lnPrefix("U01A")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("VolSv").doInst("0").daName("instMag").bdaName("i").build();
        TCompasBay compasBay = getCompasBayUUID(sclRootAdapter, "IED_NAME1");
        //When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, analogSetting1);
        // Then
        assertThat(iedSources).hasSize(1);
        assertThat(compasBay.getUUID()).isEqualTo(getCompasBayUUID(sclRootAdapter, iedSources.get(0).getName()).getUUID());
    }

    @Test
    void getIedSources_should_return_IedSource_whenFlowKindIsExternalAndDataTypeTemplateIsValid() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSettingData analogSetting10 = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f")
                .build();
        TCompasBay compasBay = getCompasBayUUID(sclRootAdapter, "IED_NAME1");
        //When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, analogSetting10);
        // Then
        assertThat(iedSources).hasSize(1);
        assertThat(compasBay.getUUID()).isNotEqualTo(getCompasBayUUID(sclRootAdapter, iedSources.get(0).getName()).getUUID());
    }

    @ParameterizedTest
    @MethodSource("provideInternalAndExternalLDEPFSetting")
    void getIedSources_should_return_IedSource_whenInternalAndExternalFlowKindAreApplied(final LDEPFSettingData setting, final boolean isInSameBay) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TCompasBay compasBay = getCompasBayUUID(sclRootAdapter, "IED_NAME1");
        //When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, setting);
        // Then
        assertThat(iedSources).hasSize(1);
        assertThat(getCompasBayUUID(sclRootAdapter, iedSources.get(0).getName()).getUUID()
                .equals(compasBay.getUUID())).isEqualTo(isInSameBay);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDoName")
    void getIedSources_should_return_NoSource_whenInvalidDoName(final SCL scd, final LDEPFSettingData setting) {
        // Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TCompasBay compasBay = getCompasBayUUID(new SclRootAdapter(scd), "IED_NAME1");
        //When
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, setting);
        // Then
        assertThat(iedSources).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidSdoName")
    void getIedSources_should_return_NoSource_whenInvalidSdoName(final SCL scd, final LDEPFSettingData setting) {
        // Given
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TCompasBay compasBay = getCompasBayUUID(new SclRootAdapter(scd), "IED_NAME1");
        //When
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, setting);
        // Then
        assertThat(iedSources).isEmpty();
        SclTestMarshaller.assertIsMarshallable(scd);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDaName")
    void getIedSources_should_return_NoSource_whenInvalidDaName(final SCL scd, final LDEPFSettingData setting) {
        // Given
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TCompasBay compasBay = getCompasBayUUID(new SclRootAdapter(scd), "IED_NAME1");
        //When
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, setting);
        // Then
        assertThat(iedSources).isEmpty();
        SclTestMarshaller.assertIsMarshallable(scd);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBdaName")
    void getIedSources_should_return_NoSource_whenInvalidBdaName(final SCL scd, final LDEPFSettingData setting) {
        // Given
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TCompasBay compasBay = getCompasBayUUID(new SclRootAdapter(scd), "IED_NAME1");
        //When
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, setting);
        // Then
        assertThat(iedSources).isEmpty();
        SclTestMarshaller.assertIsMarshallable(scd);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidSbdaName")
    void getIedSources_should_return_NoSource_whenInvalidSbdaName(final SCL scd, final LDEPFSettingData setting) {
        // Given
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TCompasBay compasBay = getCompasBayUUID(new SclRootAdapter(scd), "IED_NAME1");
        //When
        List<TIED> iedSources = settingLDEPFCsvHelper.getIedSources(sclRootAdapter, compasBay, setting);
        // Then
        assertThat(iedSources).isEmpty();
        SclTestMarshaller.assertIsMarshallable(scd);
    }

    static Stream<Arguments> provideInternalAndExternalLDEPFSetting() {
        LDEPFSettingData internalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                .channelDigitalNum(null).channelAnalogNum(1)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDTM1").lnClass("TVTR").lnInst("11").lnPrefix("U01A")
                .doName("VolSv").doInst("0").daName("instMag").bdaName("i").build();
        LDEPFSettingData externalSetting = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f")
                .build();
        return Stream.of(
                Arguments.of(Named.of("internal setting",
                        internalSetting), Named.of("identical same bay validation", true)),
                Arguments.of(Named.of("external setting",
                        externalSetting), Named.of("identical same bay validation", false))
        );
    }

    static Stream<Arguments> provideInvalidDoName() {
        SCL validScd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SCL scdWithInvalidDoName = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateWithInvalidDoName.xml");
        LDEPFSettingData.LDEPFSettingDataBuilder settingBuilder = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1)).ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f");
        LDEPFSettingData validAnalogSetting = settingBuilder.build();
        LDEPFSettingData analogSettingWithInvalidDoName = settingBuilder.doName("WrongDoName").build();
        return Stream.of(
                Arguments.of(Named.of("scd with invalid Do Name in dataTypeTemplate", scdWithInvalidDoName),
                        Named.of("valid setting", validAnalogSetting)),
                Arguments.of(Named.of("valid scd", validScd),
                        Named.of("setting with invalid Do Name", analogSettingWithInvalidDoName))
        );
    }


    static Stream<Arguments> provideInvalidSdoName() {
        SCL validScd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SCL scdWithInvalidSdoName = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateWithInvalidSdoName.xml");
        LDEPFSettingData.LDEPFSettingDataBuilder settingBuilder = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1)).ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f");
        LDEPFSettingData validAnalogSetting = settingBuilder.build();
        LDEPFSettingData analogSettingWithInvalidSdoName = settingBuilder.sdoName("wrongSdoName").build();
        return Stream.of(
                Arguments.of(Named.of("scd with invalid Sdo Name in dataTypeTemplate", scdWithInvalidSdoName),
                        Named.of("valid setting", validAnalogSetting)),
                Arguments.of(Named.of("valid scd", validScd),
                        Named.of("setting with invalid Sdo Name", analogSettingWithInvalidSdoName))
        );
    }

    static Stream<Arguments> provideInvalidDaName() {
        SCL validScd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SCL scdWithInvalidDaName = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateWithInvalidDaName.xml");
        LDEPFSettingData.LDEPFSettingDataBuilder settingBuilder = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1)).ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f");
        LDEPFSettingData validAnalogSetting = settingBuilder.build();
        LDEPFSettingData analogSettingWithInvalidDaName = settingBuilder.daName("wrongDaName").build();
        return Stream.of(
                Arguments.of(Named.of("scd with invalid da Name in dataTypeTemplate", scdWithInvalidDaName),
                        Named.of("valid setting", validAnalogSetting)),
                Arguments.of(Named.of("valid scd", validScd),
                        Named.of("setting with invalid da Name", analogSettingWithInvalidDaName))
        );
    }

    static Stream<Arguments> provideInvalidBdaName() {
        SCL validScd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SCL scdWithInvalidBdaName = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateWithInvalidBdaName.xml");
        LDEPFSettingData.LDEPFSettingDataBuilder settingBuilder = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1)).ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f");
        LDEPFSettingData validAnalogSetting = settingBuilder.build();
        LDEPFSettingData analogSettingWithInvalidBdaName = settingBuilder.bdaName("wrongBdaName").build();
        return Stream.of(
                Arguments.of(Named.of("scd with invalid bda Name in dataTypeTemplate", scdWithInvalidBdaName),
                        Named.of("valid setting", validAnalogSetting)),
                Arguments.of(Named.of("valid scd", validScd),
                        Named.of("setting with invalid bda Name", analogSettingWithInvalidBdaName))
        );
    }

    static Stream<Arguments> provideInvalidSbdaName() {
        SCL validScd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SCL scdWithInvalidSBdaName = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateWithInvalidSBdaName.xml");
        LDEPFSettingData.LDEPFSettingDataBuilder settingBuilder = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_EXTERNAL)
                .channelDigitalNum(null).channelAnalogNum(10)
                .iedType("SAMU").iedRedundancy("A").iedInstance(BigInteger.valueOf(1)).ldInst("LDPHAS1").lnClass("MMXU").lnInst("101").lnPrefix("")
                //required attributes to verify DataTypeTemplate (DoType and DaType objects) linked to IedSource for ExtRef
                .doName("PhV").doInst("0").sdoName("phsB").daName("cVal").bdaName("mag").sbdaName("f");
        LDEPFSettingData validAnalogSetting = settingBuilder.build();
        LDEPFSettingData analogSettingWithInvalidSBdaName = settingBuilder.sbdaName("wrongSbdaName").build();
        return Stream.of(
                Arguments.of(Named.of("scd with invalid sbda Name in dataTypeTemplate", scdWithInvalidSBdaName),
                        Named.of("valid setting", validAnalogSetting)),
                Arguments.of(Named.of("valid scd", validScd),
                        Named.of("setting with invalid sbda Name", analogSettingWithInvalidSBdaName))
        );
    }

    private TCompasBay getCompasBayUUID(SclRootAdapter sclRootAdapter, String iedName) {
        return sclRootAdapter.getIEDAdapterByName(iedName).getPrivateCompasBay().get();
    }
    private TCompasBay setCompasBayUUID(SclRootAdapter sclRootAdapter, String iedName, String uuid) {
        TCompasBay compasBay = sclRootAdapter.getIEDAdapterByName(iedName).getPrivateCompasBay().get();
        compasBay.setUUID(uuid);
        return compasBay;
    }

    
}