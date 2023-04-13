// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.SettingLDEPFCsvHelper;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettingsSupplier.LDEPFSetting;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class LDEPFSettingsSupplierTest {

    @Test
    void getLDEPFSettings_should_return_settings() {
        //Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName)
                , "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        LDEPFSettingsSupplier settingsSupplier = new SettingLDEPFCsvHelper(reader);
        // When
        // Then
        assertThat(settingsSupplier.getSettings()).hasSize(161);
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_digitalSetting_whenMatchExtRef() {
        // Given
        LDEPFSetting digitalSetting  = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSetting analogSetting  = LDEPFSetting.builder()
                .channelDigitalNum("1")
                .channelAnalogNum(null)
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting, analogSetting);
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 10_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        // When
        var result = settingsSupplier.getLDEPFSettingMatchingExtRef(tExtRef);
        // Then
        assertThat(result).contains(digitalSetting);
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_analogSetting_whenMatchExtRef() {
        // Given
        LDEPFSetting digitalSetting  = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSetting analogSetting  = LDEPFSetting.builder()
                .channelDigitalNum(null)
                .channelAnalogNum("10")
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting, analogSetting);
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 10_1_AnalogueValue_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        // When
        var result = settingsSupplier.getLDEPFSettingMatchingExtRef(tExtRef);
        // Then
        assertThat(result).contains(analogSetting);
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_NoSetting_whenNotMatchExtRefDescValue() {
        // Given
        LDEPFSetting digitalSetting  = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSetting analogSetting  = LDEPFSetting.builder()
                .channelDigitalNum(null)
                .channelAnalogNum("10")
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting, analogSetting);
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("desc");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        // When
        var result = settingsSupplier.getLDEPFSettingMatchingExtRef(tExtRef);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_NoSetting_whenNotMatchExtRefPLNValue() {
        // Given
        LDEPFSetting digitalSetting  = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSetting analogSetting  = LDEPFSetting.builder()
                .channelDigitalNum(null)
                .channelAnalogNum("10")
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting, analogSetting);
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 10_1_AnalogueValue_Y_general_1");
        tExtRef.getPLN().add("TOTO");
        tExtRef.setPDO("Str");
        // When
        var result = settingsSupplier.getLDEPFSettingMatchingExtRef(tExtRef);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getLDPFSettingMatchingExtRef_should_return_NoSetting_when_AllSettingsNotMatchExtRefWithUnknownPDOValue() {
        // Given
        LDEPFSetting digitalSetting  = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSetting analogSetting  = LDEPFSetting.builder()
                .channelDigitalNum(null)
                .channelAnalogNum("10")
                .daName("general").lnClass("PTRC").doName("Str").build();
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting, analogSetting);
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 10_1_AnalogueValue_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("TOTO");
        // When
        var result = settingsSupplier.getLDEPFSettingMatchingExtRef(tExtRef);
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getIedSources_should_return_oneIedSource_whenMatchAllVerification() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSetting digitalSetting = LDEPFSetting.builder()
                //required attributes to verify ExtRef
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                //required attributes to verify IED source for ExtRef
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
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
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).hasSize(1);
        assertThat(iedSources.get(0).getName()).isEqualTo("IED_NAME1");
    }


    @Test
    void getIedSources_should_return_TwoIedSources_whenTwoSourcesFoundHasTheSameBayUUID() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSetting digitalSetting = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        setCompasBayUUID(sclRootAdapter, "IED_NAME2", "bayUUID1");
        // When
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).hasSize(2);
    }

    @Test
    void getIedSources_should_return_TwoIedSources_whenTwoSourcesFoundHasTheSameICDHeader() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSetting digitalSetting = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        sclRootAdapter.getIEDAdapterByName("IED_NAME3").getCompasICDHeader().get().setIEDSystemVersioninstance(digitalSetting.iedInstance());
        // When
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
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
        LDEPFSetting digitalSetting = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                //required attributes to match LDEVICE source for ExtRef
                .ldInst(ldInst)
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        // When
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }

    @Test
    void getIedSources_should_return_NoIedSource_whenNoExistingLNodeSource() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        var digitalSetting = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                //required attributes to verify LNODE source for ExtRef
                .lnClass("PTRC").lnInst("100000").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "buyUUID1");
        // When
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }


    @Test
    void getIedSources_should_return_NoIedSource_whenLNodeSourceIsOff() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        var digitalSetting = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                //required attributes to verify LNODE source for ExtRef
                .lnClass("PTRC").lnInst("2").lnPrefix(null)
                .doName("Str").doInst("0")
                .daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        // When
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }


    @Test
    void getIedSources_should_return_NoIedSource_whenNoValidDataTypeTemplate() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDEPFSetting digitalSetting = LDEPFSetting.builder()
                .channelDigitalNum("10")
                .channelAnalogNum(null)
                .rteIedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                //required attributes to verify DO object source for ExtRef
                .doName("Str").doInst("0").daName("general")
                .build();
        TCompasBay compasBay = setCompasBayUUID(sclRootAdapter, "IED_NAME1", "bayUUID1");
        sclRootAdapter.getDataTypeTemplateAdapter().getLNodeTypeAdapters()
                .stream()
                .filter(lNodeTypeAdapter1 -> lNodeTypeAdapter1.getDOAdapterByName("Str").isPresent())
                .findFirst()
                .map(SclElementAdapter::getCurrentElem).get()
                .getDO().removeIf(tdo -> tdo.getName().equals("Str"));
        // When
        LDEPFSettingsSupplier settingsSupplier = () -> List.of(digitalSetting);
        var iedSources = settingsSupplier.getIedSources(sclRootAdapter, compasBay, digitalSetting);
        // Then
        assertThat(iedSources).isEmpty();
    }

    private TCompasBay setCompasBayUUID(SclRootAdapter sclRootAdapter, String iedName, String uuid) {
        TCompasBay compasBay = sclRootAdapter.getIEDAdapterByName(iedName).getPrivateCompasBay().get();
        compasBay.setUUID(uuid);
        return compasBay;
    }

}