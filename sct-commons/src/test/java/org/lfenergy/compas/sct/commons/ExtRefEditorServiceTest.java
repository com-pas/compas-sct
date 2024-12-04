// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.epf.*;
import org.lfenergy.compas.sct.commons.scl.ExtRefService;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

class ExtRefEditorServiceTest {

    ExtRefEditorService extRefEditorService;

    @BeforeEach
    void init() {
        extRefEditorService = new ExtRefEditorService(new IedService(), new LdeviceService(), new LnService(), new DataTypeTemplatesService());
    }

    @Test
    void manageBindingForLDEPF_whenFlowKindIsInternalAndAllExtRefInSameBay_should_return_noReportAndExtRefUpdateSuccessfully() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");
        TChannel channel = new TChannel();
        channel.setBayScope(TCBScopeType.BAY_INTERNAL);
        channel.setChannelType(TChannelType.DIGITAL);
        channel.setChannelNum("1");
        channel.setChannelShortLabel("MR.PX1");
        channel.setChannelLevMod(TChannelLevMod.POSITIVE_OR_RISING);
        channel.setChannelLevModQ(TChannelLevMod.OTHER);
        channel.setIEDType("BCU");
        channel.setIEDRedundancy(TIEDredundancy.NONE);
        channel.setIEDSystemVersionInstance("1");
        channel.setLDInst("LDPX");
        channel.setLNClass("PTRC");
        channel.setLNInst("0");
        channel.setDOName("Str");
        channel.setDOInst("0");
        channel.setDAName("general");

        EPF epf = new EPF();
        Channels channels = new Channels();
        channels.getChannel().add(channel);
        epf.setChannels(channels);
        // When
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).isEmpty();
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.getIedName()).isEqualTo("IED_NAME1");
        TExtRef extRef2 = findExtRef(scd, "IED_NAME2", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef2.getIedName()).isEqualTo("IED_NAME2");
        TExtRef extRef3 = findExtRef(scd, "IED_NAME3", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef3.getIedName()).isEqualTo("IED_NAME1");

        assertExtRefIsBoundAccordingTOLDEPF(extRef1, channel);
        assertExtRefIsBoundAccordingTOLDEPF(extRef2, channel);
        assertExtRefIsBoundAccordingTOLDEPF(extRef3, channel);

        AbstractLNAdapter<?> lnRbdr = findLn(scd, "IED_NAME1", "LDEPF", "RBDR", "1", "");
        assertThat(getDaiValue(lnRbdr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("MR.PX1");
        assertThat(getDaiValue(lnRbdr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("Positive or Rising");
        assertThat(getDaiValue(lnRbdr, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnRbdr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME1LDPX/PTRC0.Str.general");

        AbstractLNAdapter<?> lnBrbdr = findLn(scd, "IED_NAME1", "LDEPF", "RBDR", "1", "b");
        assertThat(getDaiValue(lnBrbdr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("MR.PX1");
        assertThat(getDaiValue(lnBrbdr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("Other");
        assertThat(getDaiValue(lnBrbdr, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnBrbdr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME1LDPX/PTRC0.Str.q");
    }

    @Test
    void manageBindingForLDEPF_when_internalBindingMatchEPFChannel_should_update_successfully_the_ExtRef_And_DAI_In_RBDR_bRBDR_LNodes_without_report() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_extref_with_IedType_BCU.xml");
        TChannel channel = new TChannel();
        channel.setBayScope(TCBScopeType.BAY_INTERNAL);
        channel.setChannelType(TChannelType.DIGITAL);
        channel.setChannelNum("1");
        channel.setChannelShortLabel("MR.PX1");
        channel.setChannelLevMod(TChannelLevMod.POSITIVE_OR_RISING);
        channel.setChannelLevModQ(TChannelLevMod.OTHER);
        channel.setIEDType("BCU");
        channel.setIEDRedundancy(TIEDredundancy.NONE);
        channel.setIEDSystemVersionInstance("1");
        channel.setLDInst("LDPX");
        channel.setLNClass("PTRC");
        channel.setLNInst("0");
        channel.setDOName("Str");
        channel.setDOInst("0");
        channel.setDAName("general");

        EPF epf = new EPF();
        Channels channels = new Channels();
        channels.getChannel().add(channel);
        epf.setChannels(channels);
        // When
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).isEmpty();
        SclTestMarshaller.assertIsMarshallable(new SclRootAdapter(scd).getCurrentElem());
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.getIedName()).isEqualTo("IED_NAME1");
        assertExtRefIsBoundAccordingTOLDEPF(extRef1, channel);

        AbstractLNAdapter<?> lnRbdr = findLn(scd, "IED_NAME1", "LDEPF", "RBDR", "1", "");
        assertThat(getDaiValue(lnRbdr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("MR.PX1");
        assertThat(getDaiValue(lnRbdr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("Positive or Rising");
        assertThat(getDaiValue(lnRbdr, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnRbdr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME1LDPX/PTRC0.Str.general");

        AbstractLNAdapter<?> lnBrbdr = findLn(scd, "IED_NAME1", "LDEPF", "RBDR", "1", "b");
        assertThat(getDaiValue(lnBrbdr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("MR.PX1");
        assertThat(getDaiValue(lnBrbdr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("Other");
        assertThat(getDaiValue(lnBrbdr, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnBrbdr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME1LDPX/PTRC0.Str.q");
    }

    @Test
    void manageBindingForLDEPF_when_manyIedSourceFound_should_return_reportMassages() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_manyIedSources_in_same_bay.xml");
        TChannel channel = new TChannel();
        channel.setBayScope(TCBScopeType.BAY_INTERNAL);
        channel.setChannelType(TChannelType.DIGITAL);
        channel.setChannelNum("1");
        channel.setChannelShortLabel("MR.PX1");
        channel.setChannelLevMod(TChannelLevMod.POSITIVE_OR_RISING);
        channel.setChannelLevModQ(TChannelLevMod.OTHER);
        channel.setIEDType("BCU");
        channel.setIEDRedundancy(TIEDredundancy.NONE);
        channel.setIEDSystemVersionInstance("1");
        channel.setLDInst("LDPX");
        channel.setLNClass("PTRC");
        channel.setLNInst("0");
        channel.setDOName("Str");
        channel.setDOInst("0");
        channel.setDAName("general");

        EPF epf = new EPF();
        Channels channels = new Channels();
        channels.getChannel().add(channel);
        epf.setChannels(channels);
        // When
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).hasSize(2)
                .extracting(SclReportItem::message)
                .isEqualTo(List.of("There is more than one IED source to bind the signal /IED@name=IED_NAME2/LDevice@inst=LDEPF/LN0/ExtRef@desc=DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1",
                        "There is more than one IED source to bind the signal /IED@name=IED_NAME3/LDevice@inst=LDEPF/LN0/ExtRef@desc=DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1"));
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.isSetIedName()).isTrue();
        TExtRef extRef2 = findExtRef(scd, "IED_NAME2", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef2.isSetIedName()).isFalse();
        TExtRef extRef3 = findExtRef(scd, "IED_NAME3", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef3.isSetIedName()).isFalse();

        AbstractLNAdapter<?> lnRbdr = findLn(scd, "IED_NAME2", "LDEPF", "RBDR", "1", "");
        assertThat(getDaiValue(lnRbdr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isEqualTo("dU_old_val");
        assertThat(getDaiValue(lnRbdr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isEqualTo("setVal_old_val");
        assertThat(getDaiValue(lnRbdr, MOD_DO_NAME, STVAL_DA_NAME))
                .isEqualTo("off");
        assertThat(getDaiValue(lnRbdr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isEqualTo("setSrcRef_old_val");

        AbstractLNAdapter<?> lnBrbdr = findLn(scd, "IED_NAME2", "LDEPF", "RBDR", "1", "b");
        assertThat(getDaiValue(lnBrbdr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isEqualTo("dU_old_val");
        assertThat(getDaiValue(lnBrbdr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isEqualTo("setVal_old_val");
        assertThat(getDaiValue(lnBrbdr, MOD_DO_NAME, STVAL_DA_NAME))
                .isEqualTo("off");
        assertThat(getDaiValue(lnBrbdr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isEqualTo("setSrcRef_old_val");
    }

    @Test
    void manageBindingForLDEPF_when_extRefMatchFlowKindInternalOrExternal_should_update_successfully_the_ExtRef_And_DAI_In_RBDR_bRBDR_LNodes_with_no_report() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");

        TChannel analogueChannel1WithBayInternalScope = new TChannel();
        analogueChannel1WithBayInternalScope.setBayScope(TCBScopeType.BAY_INTERNAL);
        analogueChannel1WithBayInternalScope.setChannelType(TChannelType.ANALOG);
        analogueChannel1WithBayInternalScope.setChannelNum("1");
        analogueChannel1WithBayInternalScope.setChannelShortLabel("V0");
        analogueChannel1WithBayInternalScope.setChannelLevMod(TChannelLevMod.NA);
        analogueChannel1WithBayInternalScope.setChannelLevModQ(TChannelLevMod.NA);
        analogueChannel1WithBayInternalScope.setBAPVariant("8");
        analogueChannel1WithBayInternalScope.setBAPIgnoredValue("N/A");
        analogueChannel1WithBayInternalScope.setIEDType("SAMU");
        analogueChannel1WithBayInternalScope.setIEDRedundancy(TIEDredundancy.A);
        analogueChannel1WithBayInternalScope.setIEDSystemVersionInstance("1");
        analogueChannel1WithBayInternalScope.setLDInst("LDTM1");
        analogueChannel1WithBayInternalScope.setLNClass("TVTR");
        analogueChannel1WithBayInternalScope.setLNInst("11");
        analogueChannel1WithBayInternalScope.setLNPrefix("U01A");
        analogueChannel1WithBayInternalScope.setDOName("VolSv");
        analogueChannel1WithBayInternalScope.setDOInst("0");
        analogueChannel1WithBayInternalScope.setDAName("instMag");
        analogueChannel1WithBayInternalScope.setBDAName("i");

        TChannel analogueChannel10WithBayExternalBayScope = new TChannel();
        analogueChannel10WithBayExternalBayScope.setBayScope(TCBScopeType.BAY_EXTERNAL);
        analogueChannel10WithBayExternalBayScope.setChannelType(TChannelType.ANALOG);
        analogueChannel10WithBayExternalBayScope.setChannelNum("10");
        analogueChannel10WithBayExternalBayScope.setChannelShortLabel("U101");
        analogueChannel10WithBayExternalBayScope.setChannelLevMod(TChannelLevMod.NA);
        analogueChannel10WithBayExternalBayScope.setChannelLevModQ(TChannelLevMod.NA);
        analogueChannel10WithBayExternalBayScope.setBAPVariant("8");
        analogueChannel10WithBayExternalBayScope.setBAPIgnoredValue("N/A");
        analogueChannel10WithBayExternalBayScope.setIEDType("SAMU");
        analogueChannel10WithBayExternalBayScope.setIEDRedundancy(TIEDredundancy.A);
        analogueChannel10WithBayExternalBayScope.setIEDSystemVersionInstance("1");
        analogueChannel10WithBayExternalBayScope.setLDInst("LDPHAS1");
        analogueChannel10WithBayExternalBayScope.setLNClass("MMXU");
        analogueChannel10WithBayExternalBayScope.setLNInst("101");
        analogueChannel10WithBayExternalBayScope.setSDOName("phsB");
        analogueChannel10WithBayExternalBayScope.setDOName("PhV");
        analogueChannel10WithBayExternalBayScope.setDOInst("0");
        analogueChannel10WithBayExternalBayScope.setDAName("cVal");
        analogueChannel10WithBayExternalBayScope.setBDAName("mag");
        analogueChannel10WithBayExternalBayScope.setSBDAName("f");

        EPF epf = new EPF();
        Channels channels = new Channels();
        channels.getChannel().add(analogueChannel1WithBayInternalScope);
        channels.getChannel().add(analogueChannel10WithBayExternalBayScope);
        epf.setChannels(channels);
        // When
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).isEmpty();
        SclTestMarshaller.assertIsMarshallable(scd);
        TExtRef extRefBindInternally = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_ANALOG CHANNEL 1_1_AnalogueValue_1_instMag_1");
        assertThat(extRefBindInternally.getIedName()).isEqualTo("IED_NAME1");
        assertExtRefIsBoundAccordingTOLDEPF(extRefBindInternally, analogueChannel1WithBayInternalScope);

        AbstractLNAdapter<?> lnRadr = findLn(scd, "IED_NAME1", "LDEPF", "RADR", "1", "");
        assertThat(getDaiValue(lnRadr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("V0");
        assertThat(getDaiValue(lnRadr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("NA");
        assertThat(getDaiValue(lnRadr, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnRadr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME1LDTM1/U01ATVTR11.VolSv.instMag");

        AbstractLNAdapter<?> lnAradr = findLn(scd, "IED_NAME1", "LDEPF", "RADR", "1", "a");
        assertThat(getDaiValue(lnAradr, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("V0");
        assertThat(getDaiValue(lnAradr, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("NA");
        assertThat(getDaiValue(lnAradr, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnAradr, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME1LDTM1/U01ATVTR11.VolSv.q");

        TExtRef extRefBindExternally = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_ANALOG CHANNEL 10_1_AnalogueValue_1_cVal_1");
        assertThat(extRefBindExternally.getIedName()).isEqualTo("IED_NAME2");
        assertExtRefIsBoundAccordingTOLDEPF(extRefBindExternally, analogueChannel10WithBayExternalBayScope);
    }

    @Test
    void manageBindingForLDEPF_when_DOI_Mod_and_DAI_stVal_notExists_should_precede() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_withoutModStValInLN0.xml");
        // When
        TChannel channel = new TChannel();
        channel.setBayScope(TCBScopeType.BAY_INTERNAL);
        channel.setChannelType(TChannelType.DIGITAL);
        channel.setChannelNum("1");
        channel.setChannelShortLabel("MR.PX1");
        channel.setChannelLevMod(TChannelLevMod.POSITIVE_OR_RISING);
        channel.setChannelLevModQ(TChannelLevMod.OTHER);
        channel.setIEDType("BCU");
        channel.setIEDRedundancy(TIEDredundancy.NONE);
        channel.setIEDSystemVersionInstance("1");
        channel.setLDInst("LDPX");
        channel.setLNClass("PTRC");
        channel.setLNInst("0");
        channel.setDOName("Str");
        channel.setDOInst("0");
        channel.setDAName("general");

        EPF epf = new EPF();
        Channels channels = new Channels();
        channels.getChannel().add(channel);
        epf.setChannels(channels);
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).isEmpty();
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.getIedName()).isEqualTo("IED_NAME1");
        TExtRef extRef2 = findExtRef(scd, "IED_NAME2", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef2.getIedName()).isEqualTo("IED_NAME2");
        TExtRef extRef3 = findExtRef(scd, "IED_NAME3", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef3.getIedName()).isEqualTo("IED_NAME1");

        assertExtRefIsBoundAccordingTOLDEPF(extRef1, channel);
        assertExtRefIsBoundAccordingTOLDEPF(extRef2, channel);
        assertExtRefIsBoundAccordingTOLDEPF(extRef3, channel);
    }

    @Test
    void manageBindingForLDEPF_when_LDEPF_NotActive_should_precede() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_with_inactive_ldevice_ldepf.xml");
        TChannel channel = new TChannel();
        channel.setBayScope(TCBScopeType.BAY_INTERNAL);
        channel.setChannelType(TChannelType.DIGITAL);
        channel.setChannelNum("1");
        channel.setChannelShortLabel("MR.PX1");
        channel.setChannelLevMod(TChannelLevMod.POSITIVE_OR_RISING);
        channel.setChannelLevModQ(TChannelLevMod.OTHER);
        channel.setIEDType("BCU");
        channel.setIEDRedundancy(TIEDredundancy.NONE);
        channel.setIEDSystemVersionInstance("1");
        channel.setLDInst("LDPX");
        channel.setLNClass("PTRC");
        channel.setLNInst("0");
        channel.setDOName("Str");
        channel.setDOInst("0");
        channel.setDAName("general");

        EPF epf = new EPF();
        Channels channels = new Channels();
        channels.getChannel().add(channel);
        epf.setChannels(channels);
        // When
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).isEmpty();
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.getIedName()).isEqualTo("IED_NAME1");
        TExtRef extRef2 = findExtRef(scd, "IED_NAME2", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef2.getIedName()).isEqualTo("IED_NAME2");
        TExtRef extRef3 = findExtRef(scd, "IED_NAME3", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef3.getIedName()).isEqualTo("IED_NAME1");

        assertExtRefIsBoundAccordingTOLDEPF(extRef1, channel);
        assertExtRefIsBoundAccordingTOLDEPF(extRef2, channel);
        assertExtRefIsBoundAccordingTOLDEPF(extRef3, channel);
    }

    @Test
    void manageBindingForLDEPF_when_DO_Mod_and_DA_stVal_NotFoundInDataTypeTemplate_should_return_error() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_withoutModStValInDataTypeTemplate.xml");
        // When
        EPF epf = new EPF();
        epf.setChannels(new Channels());
        List<SclReportItem> sclReportItems = extRefEditorService.manageBindingForLDEPF(scd, epf);
        // Then
        assertThat(sclReportItems).hasSize(3);
        assertThat(sclReportItems)
                .extracting(SclReportItem::message, SclReportItem::xpath)
                .containsExactly(
                        Tuple.tuple("DO@name=Mod/DA@name=stVal not found in DataTypeTemplate",
                                "SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LDEPF\"]"),
                        Tuple.tuple("DO@name=Mod/DA@name=stVal not found in DataTypeTemplate",
                                "SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LDEPF\"]"),
                        Tuple.tuple("DO@name=Mod/DA@name=stVal not found in DataTypeTemplate",
                                "SCL/IED[@name=\"IED_NAME3\"]/AccessPoint/Server/LDevice[@inst=\"LDEPF\"]")
                );
    }


    private void assertExtRefIsBoundAccordingTOLDEPF(TExtRef extRef, TChannel setting) {
        assertThat(extRef.getLdInst()).isEqualTo(setting.getLDInst());
        assertThat(extRef.getLnClass()).contains(setting.getLNClass());
        assertThat(extRef.getLnInst()).isEqualTo(setting.getLNInst());
        assertThat(extRef.getPrefix()).isEqualTo(setting.getLNPrefix());
        assertThat(extRef.getDoName()).isEqualTo(setting.getDOName());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExtRefInfoInvalid")
    void updateExtRefBinders(String testCase, ExtRefInfo extRefInfo, String message) {
        //Given
        SCL scd = createSclRootAdapterWithIed("IED_NAME").getCurrentElem();
        //When
        //Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefBinders(scd, extRefInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage(message);
    }

    private static Stream<Arguments> provideExtRefInfoInvalid() {
        ExtRefInfo withBindingInfo = new ExtRefInfo();
        withBindingInfo.setBindingInfo(new ExtRefBindingInfo());
        ExtRefInfo withSignalInfo = new ExtRefInfo();
        withSignalInfo.setSignalInfo(new ExtRefSignalInfo());
        ExtRefInfo withUnknownLD = new ExtRefInfo();
        withUnknownLD.setHolderIEDName("IED_NAME");
        withUnknownLD.setHolderLDInst("Unknown LD");
        ExtRefBindingInfo extRefBindingInfo = new ExtRefBindingInfo();
        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        withUnknownLD.setBindingInfo(extRefBindingInfo);
        withUnknownLD.setSignalInfo(extRefSignalInfo);
        return Stream.of(
                Arguments.of("Should throw exception when BindingInfo is null", withSignalInfo, "ExtRef Signal and/or Binding information are missing"),
                Arguments.of("Should throw exception when SignalInfo is null", withBindingInfo, "ExtRef Signal and/or Binding information are missing"),
                Arguments.of("Should throw exception when LD is not present in IED", withUnknownLD, "Unknown LDevice (Unknown LD) in IED (IED_NAME)")
        );
    }

    @Test
    void updateExtRefBinders_should_thowException_when_AbstractLnAdapterUpdateExtRefBinders_Throws_Exception() {
        //Given
        SCL scd = createIedsInScl("ANCR", "do1").getCurrentElem();
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName(IED_NAME_1);
        extRefInfo.setHolderLDInst(LD_SUIED);
        extRefInfo.setHolderLnClass("LLN0");
        ExtRefBindingInfo extRefBindingInfo = new ExtRefBindingInfo();
        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        extRefInfo.setBindingInfo(extRefBindingInfo);
        extRefInfo.setSignalInfo(extRefSignalInfo);
        //When
        //Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefBinders(scd, extRefInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("ExtRef mandatory binding data are missing");
    }

    @Test
    void updateExtRefBinders_should_succed_when_AbstractLnAdapterUpdateExtRefBinders_succed() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME");
        extRefInfo.setHolderLDInst("LD_INS2");
        extRefInfo.setHolderLnClass("ANCR");
        extRefInfo.setHolderLnInst("1");
        extRefInfo.setHolderLnPrefix(null);
        extRefInfo.getSignalInfo().setPDO("StrVal.sdo2");
        extRefInfo.getSignalInfo().setPDA("antRef.bda1.bda2.bda3");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR2");
        extRefInfo.getSignalInfo().setDesc(null);
        extRefInfo.getSignalInfo().setPServT(null);
        //When
        //Then
        assertDoesNotThrow(() -> extRefEditorService.updateExtRefBinders(scd, extRefInfo));
    }

    @Test
    @Tag("issue-321")
    void updateExtRefSource_whenSignalInfoNullOrInvalid_shouldThrowScdException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());
        assertThat(extRefInfo.getSignalInfo()).isNull();
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        //Given
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThat(extRefInfo.getSignalInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// signal invalid
    }

    @Test
    @Tag("issue-321")
    void updateExtRefSource_whenBindingInfoNullOrInvalid_shouldThrowScdException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        extRefSignalInfo.setIntAddr("INT_ADDR21");
        extRefSignalInfo.setPDA("da21.bda211.bda212.bda213");
        extRefSignalInfo.setPDO("Do21.sdo21");
        extRefInfo.setSignalInfo(extRefSignalInfo);
        assertThat(extRefInfo.getBindingInfo()).isNull();
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo))
                .isInstanceOf(ScdException.class); // binding = null
        //Given
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThat(extRefInfo.getBindingInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// binding invalid
    }

    @Test
    void updateExtRefSource_whenBindingInternalByIedName_shouldThrowScdException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        extRefSignalInfo.setIntAddr("INT_ADDR21");
        extRefSignalInfo.setPDA("da21.bda211.bda212.bda213");
        extRefSignalInfo.setPDO("Do21.sdo21");
        extRefInfo.setSignalInfo(extRefSignalInfo);

        ExtRefBindingInfo extRefBindingInfo = new ExtRefBindingInfo();
        extRefBindingInfo.setIedName("IED_NAME2"); // internal binding
        extRefBindingInfo.setLdInst("LD_INST12");
        extRefBindingInfo.setLnClass(TLLN0Enum.LLN_0.value());
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // CB not allowed
    }

    @Test
    void updateExtRefSource_whenBindingInternaByServiceType_shouldThrowScdException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        extRefSignalInfo.setIntAddr("INT_ADDR21");
        extRefSignalInfo.setPDA("da21.bda211.bda212.bda213");
        extRefSignalInfo.setPDO("Do21.sdo21");
        extRefInfo.setSignalInfo(extRefSignalInfo);

        ExtRefBindingInfo extRefBindingInfo = new ExtRefBindingInfo();
        extRefBindingInfo.setIedName("IED_NAME2"); // internal binding
        extRefBindingInfo.setLdInst("LD_INST12");
        extRefBindingInfo.setLnClass(TLLN0Enum.LLN_0.value());
        extRefBindingInfo.setServiceType(TServiceType.POLL);
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // CB not allowed
    }

    @Test
    @Tag("issue-321")
    void updateExtRefSource_whenSourceInfoNullOrInvalid_shouldThrowScdException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        extRefSignalInfo.setIntAddr("INT_ADDR21");
        extRefSignalInfo.setPDA("da21.bda211.bda212.bda213");
        extRefSignalInfo.setPDO("Do21.sdo21");
        extRefInfo.setSignalInfo(extRefSignalInfo);

        ExtRefBindingInfo extRefBindingInfo = new ExtRefBindingInfo();
        extRefBindingInfo.setIedName("IED_NAME1"); // internal binding
        extRefBindingInfo.setLdInst("LD_INST12");
        extRefBindingInfo.setLnClass(TLLN0Enum.LLN_0.value());
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());

        assertThat(extRefInfo.getSourceInfo()).isNull();
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        //Given
        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        assertThat(extRefInfo.getSourceInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// signal invalid
    }

    @Test
    void updateExtRefSource_whenBindingExternalBinding_shouldThrowScdException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo();
        extRefSignalInfo.setIntAddr("INT_ADDR21");
        extRefSignalInfo.setPDA("da21.bda211.bda212.bda213");
        extRefSignalInfo.setPDO("Do21.sdo21");
        extRefInfo.setSignalInfo(extRefSignalInfo);

        ExtRefBindingInfo extRefBindingInfo = new ExtRefBindingInfo();
        extRefBindingInfo.setIedName("IED_NAME1");
        extRefBindingInfo.setLdInst("LD_INST12");
        extRefBindingInfo.setLnClass(TLLN0Enum.LLN_0.value());
        extRefInfo.setBindingInfo(extRefBindingInfo);

        ExtRefSourceInfo sourceInfo = new ExtRefSourceInfo();
        sourceInfo.setSrcLDInst(extRefInfo.getBindingInfo().getLdInst());
        sourceInfo.setSrcLNClass(extRefInfo.getBindingInfo().getLnClass());
        sourceInfo.setSrcCBName("goose1");
        extRefInfo.setSourceInfo(sourceInfo);

        //When
        TExtRef extRef = assertDoesNotThrow(() -> extRefEditorService.updateExtRefSource(scd, extRefInfo));
        //Then
        assertThat(extRef.getSrcCBName()).isEqualTo(extRefInfo.getSourceInfo().getSrcCBName());
        assertThat(extRef.getSrcLDInst()).isEqualTo(extRefInfo.getBindingInfo().getLdInst());
        assertThat(extRef.getSrcLNClass()).contains(extRefInfo.getBindingInfo().getLnClass());
    }

    @Test
    void epfPostProcessing_when_exist_unused_channel_should_update_setSrcRef() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_postProcessing.xml");
        // When
        extRefEditorService.epfPostProcessing(scd);
        // Then
        SoftAssertions softly = new SoftAssertions();

        Optional<TDAI> setSrcRefInInRef1 = findDai(scd, "IED_NAME1", "LDEPF", "InRef1", "setSrcRef");
        Optional<TDAI> purposeInInRef1 = findDai(scd, "IED_NAME1", "LDEPF", "InRef1", "purpose");
        assertThat(purposeInInRef1).isPresent();
        softly.assertThat(purposeInInRef1.get().getVal().getFirst().getValue()).doesNotStartWith("DYN_LDEPF_DIGITAL CHANNEL");
        softly.assertThat(purposeInInRef1.get().getVal().getFirst().getValue()).doesNotStartWith("DYN_LDEPF_ANALOG CHANNEL");
        assertThat(setSrcRefInInRef1).isPresent();
        softly.assertThat(setSrcRefInInRef1.get().isSetVal()).isFalse();

        Optional<TDAI> setSrcRefInInRef2 = findDai(scd, "IED_NAME1", "LDEPF", "InRef2", "setSrcRef");
        Optional<TDAI> purposeInInRef2 = findDai(scd, "IED_NAME1", "LDEPF", "InRef2", "purpose");
        assertThat(purposeInInRef2).isPresent();
        softly.assertThat(purposeInInRef2.get().getVal().getFirst().getValue()).startsWith("DYN_LDEPF_DIGITAL CHANNEL");
        assertThat(setSrcRefInInRef2).isPresent();
        softly.assertThat(setSrcRefInInRef2.get().getVal().getFirst().getValue()).isEqualTo("IED_NAME1LDEPF/LPHD0.Proxy");

        Optional<TDAI> setSrcRefInInRef3 = findDai(scd, "IED_NAME1", "LDEPF", "InRef3", "setSrcRef");
        Optional<TDAI> purposeInInRef3 = findDai(scd, "IED_NAME1", "LDEPF", "InRef3", "purpose");
        assertThat(purposeInInRef3).isPresent();
        softly.assertThat(purposeInInRef3.get().getVal().getFirst().getValue()).startsWith("DYN_LDEPF_DIGITAL CHANNEL");
        assertThat(setSrcRefInInRef3).isPresent();
        softly.assertThat(setSrcRefInInRef3.get().getVal().getFirst().getValue()).isEqualTo("IED_NAME1LDEPF/LPHD0.Proxy");

        Optional<TDAI> setSrcRefInInRef4 = findDai(scd, "IED_NAME1", "LDEPF", "InRef4", "setSrcRef");
        Optional<TDAI> purposeInInRef4 = findDai(scd, "IED_NAME1", "LDEPF", "InRef4", "purpose");
        assertThat(purposeInInRef4).isPresent();
        softly.assertThat(purposeInInRef4.get().getVal().getFirst().getValue()).startsWith("DYN_LDEPF_ANALOG CHANNEL");
        assertThat(setSrcRefInInRef4).isPresent();
        softly.assertThat(setSrcRefInInRef4.get().getVal().getFirst().getValue()).isEqualTo("IED_NAME1LDEPF/LPHD0.Proxy");
        softly.assertAll();
    }
}
