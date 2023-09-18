// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.ILDEPFSettings;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.SettingLDEPFCsvHelper;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

@ExtendWith(MockitoExtension.class)
class ExtRefServiceTest {

    @InjectMocks
    ExtRefService extRefService;

    @Test
    void updateAllExtRefIedNames_should_update_iedName_and_ExtRefIedName() {
        // Given : An ExtRef with a matching compas:Flow
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_success.xml");
        // When
        extRefService.updateAllExtRefIedNames(scd);
        // Then
        TExtRef extRef = findExtRef(scd, "IED_NAME1", "LD_INST11", "STAT_LDSUIED_LPDO 1 Sortie_13_BOOLEAN_18_stVal_1");
        assertThat(extRef.getIedName()).isEqualTo("IED_NAME2");

        TInputs inputs = findLDevice(scd, "IED_NAME1", "LD_INST11")
                .getLN0Adapter()
                .getCurrentElem()
                .getInputs();
        Assertions.assertThat(PrivateUtils.extractCompasPrivate(inputs, TCompasFlow.class))
                .map(TCompasFlow::getExtRefiedName)
                .hasValue("IED_NAME2");
    }

    @Test
    void updateAllExtRefIedNames_should_return_success_status() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_success.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError))
                .overridingErrorMessage(String.valueOf(sclReportItems))
                .isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateAllExtRefIedNamesErrors")
    void updateAllExtRefIedNames_should_report_errors(String testCase, SCL scl, SclReportItem... errors) {
        // Given : scl parameter
        // When
        List<SclReportItem> sclReportItems = extRefService.updateAllExtRefIedNames(scl);
        // Then : the sclReport should report all errors described in the comments in the SCD file
        assertThat(sclReportItems).isNotNull();
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems).containsExactlyInAnyOrder(errors);
    }

    public static Stream<Arguments> updateAllExtRefIedNamesErrors() {
        return
                Stream.of(Arguments.of(
                                "Errors on ExtRefs",
                                SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml"),
                                new SclReportItem[]{
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"No matching compas:Flow\"]",
                                                "The signal ExtRef has no matching compas:Flow Private"),
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"Matching two compas:Flow\"]",
                                                "The signal ExtRef has more than one matching compas:Flow Private"),
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST13\"]",
                                                "The Ldevice status test does not exist. It should be among [on, off]"),
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST14\"]",
                                                "The LDevice status is undefined"),
                                        SclReportItem.warning(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"ExtRef does not match any ICDSystemVersionUUID\"]",
                                                "The signal ExtRef iedName does not match any IED/Private/compas:ICDHeader@ICDSystemVersionUUID"),
                                        SclReportItem.warning(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"ExtRefldinst does not match any LDevice inst in source IED\"]",
                                                "The signal ExtRef ExtRefldinst does not match any LDevice with same inst attribute in source IED /SCL/IED[@name=\"IED_NAME2\"]"),
                                        SclReportItem.warning(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"ExtRef does not match any LN in source LDevice\"]",
                                                "The signal ExtRef lninst, doName or daName does not match any source in LDevice " +
                                                        "/SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST21\"]"),
                                        SclReportItem.warning(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"Source LDevice is off for this ExtRef\"]",
                                                "The signal ExtRef source LDevice /SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST22\"] status is off"),
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"Source LDevice is undefined for this ExtRef\"]",
                                                "The signal ExtRef source LDevice /SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST23\"] status is " +
                                                        "undefined"),
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                                        "/LN0/Inputs/ExtRef[@desc=\"Source LDevice is neither on nor off for this ExtRef\"]",
                                                "The signal ExtRef source LDevice /SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST24\"] " +
                                                        "status is neither \"on\" nor \"off\"")
                                }),
                        Arguments.of(
                                "Errors on IEDs",
                                SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_ied_errors.xml"),
                                new SclReportItem[]{
                                        SclReportItem.error(
                                                "/SCL/IED[@name=\"IED_NAME1\"], /SCL/IED[@name=\"IED_NAME2\"]",
                                                "/IED/Private/compas:ICDHeader[@ICDSystemVersionUUID] must be unique but the same ICDSystemVersionUUID was found on several IED."),
                                        SclReportItem.error("/SCL/IED[@name=\"IED_NAME3\"]", "IED has no Private COMPAS-ICDHeader element"),
                                        SclReportItem.error("/SCL/IED[@name=\"IED_NAME4\"]", "IED private COMPAS-ICDHeader as no icdSystemVersionUUID or iedName attribute"),
                                        SclReportItem.error("/SCL/IED[@name=\"IED_NAME5\"]", "IED private COMPAS-ICDHeader as no icdSystemVersionUUID or iedName attribute")
                                })
                );
    }

    @Test
    void updateAllExtRefIedNames_when_not_bindable_should_clear_binding() {
        // Given : see comments in SCD file
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        extRefService.updateAllExtRefIedNames(scd);
        // Then
        assertExtRefIsNotBound(findExtRef(scd, "IED_NAME1", "LD_INST12", "ExtRef target LDevice status is off"));
        assertExtRefIsNotBound(findExtRef(scd, "IED_NAME1", "LD_INST11", "Match compas:Flow but FlowStatus is INACTIVE"));
        assertExtRefIsNotBound(findExtRef(scd, "IED_NAME1", "LD_INST11", "ExtRef does not match any ICDSystemVersionUUID"));
        assertExtRefIsNotBound(findExtRef(scd, "IED_NAME1", "LD_INST11", "ExtRefldinst does not match any LDevice inst in source IED"));
        assertExtRefIsNotBound(findExtRef(scd, "IED_NAME1", "LD_INST11", "ExtRef does not match any LN in source LDevice"));
        assertExtRefIsNotBound(findExtRef(scd, "IED_NAME1", "LD_INST11", "Source LDevice is off for this ExtRef"));
    }

    @Test
    void updateAllExtRefIedNames_when_lDevice_off_should_remove_binding() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReportItems).isNotNull();
        LDeviceAdapter lDeviceAdapter = findLDeviceByLdName(scd, "IED_NAME1LD_INST12");
        assertThat(lDeviceAdapter.getLDeviceStatus()).hasValue("off");
        assertThat(lDeviceAdapter.getLN0Adapter().getInputsAdapter().getCurrentElem().getExtRef())
                .allSatisfy(this::assertExtRefIsNotBound);
    }

    @Test
    void updateAllExtRefIedNames_when_FlowStatus_INACTIVE_should_remove_binding() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReportItems).isNotNull();
        LDeviceAdapter lDeviceAdapter = findLDeviceByLdName(scd, "IED_NAME1LD_INST11");
        assertThat(lDeviceAdapter.getLDeviceStatus()).hasValue("on");
        Optional<TExtRef> optionalTExtRef = lDeviceAdapter.getCurrentElem().getLN0().getInputs().getExtRef().stream()
                .filter(tExtRef -> "Match compas:Flow but FlowStatus is INACTIVE".equals(tExtRef.getDesc()))
                .findFirst();
        assertThat(optionalTExtRef).isPresent();
        TExtRef extRef = optionalTExtRef.get();
        assertExtRefIsNotBound(extRef);
    }

    private void assertExtRefIsNotBound(TExtRef extRef) {
        assertThat(extRef.isSetIedName()).isFalse();
        assertThat(extRef.isSetLdInst()).isFalse();
        assertThat(extRef.isSetPrefix()).isFalse();
        assertThat(extRef.isSetLnClass()).isFalse();
        assertThat(extRef.isSetLnInst()).isFalse();
        assertThat(extRef.isSetDoName()).isFalse();
        assertThat(extRef.isSetDaName()).isFalse();
        assertThat(extRef.isSetServiceType()).isFalse();
        assertThat(extRef.isSetSrcLDInst()).isFalse();
        assertThat(extRef.isSetSrcPrefix()).isFalse();
        assertThat(extRef.isSetSrcLNClass()).isFalse();
        assertThat(extRef.isSetSrcLNInst()).isFalse();
        assertThat(extRef.isSetSrcCBName()).isFalse();
    }

    @Test
    void filterDuplicatedExtRefs_should_remove_duplicated_extrefs() {
        // Given
        TExtRef tExtRefLnClass = createExtRefExample("CB_Name1", TServiceType.GOOSE);
        tExtRefLnClass.getSrcLNClass().add(TLLN0Enum.LLN_0.value());
        TExtRef tExtRef = createExtRefExample("CB_Name1", TServiceType.GOOSE);
        List<TExtRef> tExtRefList = List.of(tExtRef, tExtRefLnClass, createExtRefExample("CB", TServiceType.GOOSE),
                createExtRefExample("CB", TServiceType.GOOSE));
        // When
        List<TExtRef> result = extRefService.filterDuplicatedExtRefs(tExtRefList);
        // Then
        assertThat(result).hasSizeLessThan(tExtRefList.size())
                .hasSize(2);
    }

    @Test
    void filterDuplicatedExtRefs_should_not_remove_not_duplicated_extrefs() {
        // Given
        TExtRef tExtRefIedName = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefIedName.setIedName("IED_XXX");
        TExtRef tExtRefLdInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLdInst.setSrcLDInst("LD_XXX");
        TExtRef tExtRefLnInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnInst.setSrcLNInst("X");
        TExtRef tExtRefPrefix = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefPrefix.setSrcPrefix("X");
        List<TExtRef> tExtRefList = List.of(tExtRefIedName, tExtRefLdInst, tExtRefLnInst, tExtRefPrefix,
                createExtRefExample("CB_1", TServiceType.GOOSE), createExtRefExample("CB_1", TServiceType.SMV));
        // When
        List<TExtRef> result = extRefService.filterDuplicatedExtRefs(tExtRefList);
        // Then
        assertThat(result).hasSameSizeAs(tExtRefList)
                .hasSize(6);
    }

    @Test
    void manageBindingForLDEPF_whenFlowKindIsInternalAndAllExtRefInSameBay_should_return_noReportAndExtRefUpdateSuccessfully() {
        //Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_iedSources_in_different_bay.xml");

        LDEPFSettingData expectedSettingData = LDEPFSettingData.builder()
                .bayScope(TCompasFlowKind.BAY_INTERNAL)
                .channelDigitalNum(1)
                .channelAnalogNum(null)
                .channelShortLabel("MR.PX1")
                .channelLevMod("Positive or Rising")
                .channelLevModQ("Other")
                .iedType("BCU").iedRedundancy("None").iedInstance(BigInteger.valueOf(1))
                .ldInst("LDPX")
                .lnClass("PTRC").lnInst("0").lnPrefix(null)
                .doName("Str").doInst("0").daName("general")
                .build();

        // When
        List<SclReportItem> sclReportItems = extRefService.manageBindingForLDEPF(scd, settingLDEPFCsvHelper);
        // Then
        assertThat(sclReportItems).isEmpty();
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.getIedName()).isEqualTo("IED_NAME1");
        TExtRef extRef2 = findExtRef(scd, "IED_NAME2", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef2.getIedName()).isEqualTo("IED_NAME2");
        TExtRef extRef3 = findExtRef(scd, "IED_NAME3", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef3.getIedName()).isEqualTo("IED_NAME1");

        assertExtRefIsBoundAccordingTOLDEPF(extRef1, expectedSettingData);
        assertExtRefIsBoundAccordingTOLDEPF(extRef2, expectedSettingData);
        assertExtRefIsBoundAccordingTOLDEPF(extRef3, expectedSettingData);

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
    void manageBindingForLDEPF_when_extRef_withDifferentIedType_update_successfully_should_return_no_report() {
        // Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_extref_with_BCU_BPU.xml");
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        // When
        List<SclReportItem> sclReportItems = extRefService.manageBindingForLDEPF(scd, settingLDEPFCsvHelper);
        // Then
        assertThat(sclReportItems).isEmpty();
        SclTestMarshaller.assertIsMarshallable(new SclRootAdapter(scd).getCurrentElem());
        TExtRef extRef1 = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 1_1_BOOLEEN_1_general_1");
        assertThat(extRef1.getIedName()).isEqualTo("IED_NAME1");
        assertExtRefIsBoundAccordingTOLDEPF(extRef1, getLDEPFSettingByDigitalNum(settingLDEPFCsvHelper.getSettings(), 1));

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

        TExtRef extRef2 = findExtRef(scd, "IED_NAME2", "LDEPF", "DYN_LDEPF_DIGITAL CHANNEL 15_1_BOOLEEN_1_general_1");
        assertThat(extRef2.getIedName()).isEqualTo("IED_NAME2");
        assertExtRefIsBoundAccordingTOLDEPF(extRef2, getLDEPFSettingByDigitalNum(settingLDEPFCsvHelper.getSettings(), 15));

        AbstractLNAdapter<?> lnRbdr2 = findLn(scd, "IED_NAME2", "LDEPF", "RBDR", "15", "");
        assertThat(getDaiValue(lnRbdr2, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("MR.PX2");
        assertThat(getDaiValue(lnRbdr2, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("Positive or Rising");
        assertThat(getDaiValue(lnRbdr2, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnRbdr2, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME2LDPX/PTRC0.Str.general");

        AbstractLNAdapter<?> lnBrbdr2 = findLn(scd, "IED_NAME2", "LDEPF", "RBDR", "15", "b");
        assertThat(getDaiValue(lnBrbdr2, CHNUM1_DO_NAME, DU_DA_NAME))
                .isNotEqualTo("dU_old_val")
                .isEqualTo("MR.PX2");
        assertThat(getDaiValue(lnBrbdr2, LEVMOD_DO_NAME, SETVAL_DA_NAME))
                .isNotEqualTo("setVal_old_val")
                .isEqualTo("Other");
        assertThat(getDaiValue(lnBrbdr2, MOD_DO_NAME, STVAL_DA_NAME))
                .isNotEqualTo("off")
                .isEqualTo("on");
        assertThat(getDaiValue(lnBrbdr2, SRCREF_DO_NAME, SETSRCREF_DA_NAME))
                .isNotEqualTo("setSrcRef_old_val")
                .isEqualTo("IED_NAME2LDPX/PTRC0.Str.q");
    }

    @Test
    void manageBindingForLDEPF_when_manyIedSourceFound_should_return_report() {
        //Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_manyIedSources_in_same_bay.xml");
        ILDEPFSettings settings = new SettingLDEPFCsvHelper(reader);
        // When
        List<SclReportItem> sclReportItems = extRefService.manageBindingForLDEPF(scd, settings);
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
    void manageBindingForLDEPF_when_extRefInFlowKindInternalAndExternal_update_successfully_should_return_no_report() {
        //Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ldepf/scd_ldepf_dataTypeTemplateValid.xml");
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        // When
        List<SclReportItem> sclReportItems = extRefService.manageBindingForLDEPF(scd, settingLDEPFCsvHelper);
        // Then
        assertThat(sclReportItems).isEmpty();
        SclTestMarshaller.assertIsMarshallable(scd);
        TExtRef extRefBindInternally = findExtRef(scd, "IED_NAME1", "LDEPF", "DYN_LDEPF_ANALOG CHANNEL 1_1_AnalogueValue_1_instMag_1");
        assertThat(extRefBindInternally.getIedName()).isEqualTo("IED_NAME1");
        assertExtRefIsBoundAccordingTOLDEPF(extRefBindInternally, getLDEPFSettingByAnalogNum(settingLDEPFCsvHelper.getSettings(), 1));

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
        assertExtRefIsBoundAccordingTOLDEPF(extRefBindExternally, getLDEPFSettingByAnalogNum(settingLDEPFCsvHelper.getSettings(), 10));
    }

    private void assertExtRefIsBoundAccordingTOLDEPF(TExtRef extRef, LDEPFSettingData setting) {
        assertThat(extRef.getLdInst()).isEqualTo(setting.getLdInst());
        assertThat(extRef.getLnClass()).contains(setting.getLnClass());
        assertThat(extRef.getLnInst()).isEqualTo(setting.getLnInst());
        assertThat(extRef.getPrefix()).isEqualTo(setting.getLnPrefix());
        assertThat(extRef.getDoName()).isEqualTo(setting.getDoName());
    }

    private LDEPFSettingData getLDEPFSettingByDigitalNum(List<LDEPFSettingData> settings, Integer digitalNum) {
        return settings.stream()
                .filter(setting -> digitalNum.equals(setting.getChannelDigitalNum()))
                .findFirst().get();
    }

    private LDEPFSettingData getLDEPFSettingByAnalogNum(List<LDEPFSettingData> settings, Integer analogNum) {
        return settings.stream()
                .filter(setting -> analogNum.equals(setting.getChannelAnalogNum()))
                .findFirst().get();
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
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        //Given
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThat(extRefInfo.getSignalInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// signal invalid
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
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo))
                .isInstanceOf(ScdException.class); // binding = null
        //Given
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThat(extRefInfo.getBindingInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// binding invalid
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
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // CB not allowed
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
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // CB not allowed
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
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        //Given
        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        assertThat(extRefInfo.getSourceInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> extRefService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// signal invalid
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
        TExtRef extRef = assertDoesNotThrow(() -> extRefService.updateExtRefSource(scd, extRefInfo));
        //Then
        assertThat(extRef.getSrcCBName()).isEqualTo(extRefInfo.getSourceInfo().getSrcCBName());
        assertThat(extRef.getSrcLDInst()).isEqualTo(extRefInfo.getBindingInfo().getLdInst());
        assertThat(extRef.getSrcLNClass()).contains(extRefInfo.getBindingInfo().getLnClass());
    }

}
