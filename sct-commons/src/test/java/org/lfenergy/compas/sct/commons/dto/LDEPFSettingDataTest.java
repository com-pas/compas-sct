// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LDEPFSettingDataTest {

    @Test
    void isMatchExtRef_with_DigitalSettingData_should_return_true_when_ExtRefDescMatch() {
        //Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelDigitalNum(22).lnClass("PTRC")
                .doName("Str").daName("general").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 22_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        //When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        //Then
        assertThat(isMatchExtRef).isTrue();
    }

    @Test
    void isMatchExtRef_with_DigitalSettingData_should_return_false_when_ExtRefDesc_DigitalNum_NotMatch() {
        //Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelDigitalNum(22).lnClass("PTRC")
                .doName("Str").daName("general").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 23_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        //When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        //Then
        assertThat(isMatchExtRef).isFalse();
    }


    @Test
    void isMatchExtRef_with_DigitalSettingData_should_return_false_when_ExtRefDesc_DaName_NotMatch() {
        //Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelDigitalNum(22).lnClass("PTRC")
                .doName("Str").daName("general").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 22_1_BOOLEEN_Y_daname_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        //When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        //Then
        assertThat(isMatchExtRef).isFalse();
    }


    @Test
    void isMatchExtRef_with_AnalogSettingData_should_return_true_when_ExtRefDescMatch() {
        //Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelAnalogNum(22).lnClass("PTRC")
                .doName("Str").daName("instMag").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 22_1_AnalogueValue_Y_instMag_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        //When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        //Then
        assertThat(isMatchExtRef).isTrue();
    }

    @Test
    void isMatchExtRef_with_AnalogSettingData_should_return_false_when_ExtRefDesc_DigitalNum_NotMatch() {
        //Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelAnalogNum(22).lnClass("PTRC")
                .doName("Str").daName("instMag").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 23_1_AnalogueValue_Y_instMag_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        //When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        //Then
        assertThat(isMatchExtRef).isFalse();
    }


    @Test
    void isMatchExtRef_with_AnalogSettingData_should_return_false_when_ExtRefDesc_DaName_NotMatch() {
        //Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelAnalogNum(22).lnClass("PTRC")
                .doName("Str").daName("instMag").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_ANALOG CHANNEL 22_1_AnalogueValue_Y_daname_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        //When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        //Then
        assertThat(isMatchExtRef).isFalse();
    }

    @Test
    void isMatchExtRef_should_return_true_when_ExtRefMatch() {
        // Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelDigitalNum(22).lnClass("PTRC")
                .doName("Str").daName("general").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 22_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("Str");
        // When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        // Then
        assertThat(isMatchExtRef).isTrue();
    }

    @Test
    void isMatchExtRef_should_return_false_when_ExtRefPLN_NotMatch() {
        // Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelDigitalNum(22).lnClass("PTRC")
                .doName("Str").daName("general").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 22_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("TOTO");
        tExtRef.setPDO("Str");
        // When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        // Then
        assertThat(isMatchExtRef).isFalse();
    }

    @Test
    void isMatchExtRef_should_return_false_when_ExtRefPDO_NotMatch() {
        // Given
        LDEPFSettingData ldepfSettingData = LDEPFSettingData.builder()
                .channelDigitalNum(22).lnClass("PTRC")
                .doName("Str").daName("general").build();
        TExtRef tExtRef = new TExtRef();
        tExtRef.setDesc("DYN_LDEPF_DIGITAL CHANNEL 22_1_BOOLEEN_Y_general_1");
        tExtRef.getPLN().add("PTRC");
        tExtRef.setPDO("TOTO");
        // When
        Boolean isMatchExtRef = ldepfSettingData.isMatchExtRef(tExtRef);
        // Then
        assertThat(isMatchExtRef).isFalse();
    }

}