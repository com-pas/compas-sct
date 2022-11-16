// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCompasFlow;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findExtRef;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLDevice;

class ExtRefServiceTest {

    @Test
    void updateAllExtRefIedNames_should_update_iedName_and_ExtRefiedName() throws Exception {
        // Given : An ExtRef with a matching compas:Flow
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_success.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        TExtRef extRef = findExtRef(sclReport, "IED_NAME1", "LD_INST11", "STAT_LDSUIED_LPDO 1 Sortie_13_BOOLEAN_18_stVal_1");
        assertThat(extRef.getIedName()).isEqualTo("IED_NAME2");

        TInputs inputs = findLDevice(sclReport, "IED_NAME1", "LD_INST11")
            .getLN0Adapter()
            .getCurrentElem()
            .getInputs();
        assertThat(PrivateService.extractCompasPrivate(inputs, TCompasFlow.class))
            .map(TCompasFlow::getExtRefiedName)
            .hasValue("IED_NAME2");
    }

    @Test
    void updateAllExtRefIedNames_should_return_success_status() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_success.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReport.isSuccess())
            .overridingErrorMessage(String.valueOf(sclReport.getSclReportItems()))
            .isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateAllExtRefIedNamesErrors")
    void updateAllExtRefIedNames_should_report_errors(String testCase, SCL scl, SclReportItem... errors) {
        // Given : scl parameter
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scl);
        // Then : the sclReport should report all errors described in the comments in the SCD file
        assertThat(sclReport).isNotNull();
        assertThat(sclReport.isSuccess()).isFalse();
        assertThat(sclReport.getSclReportItems()).containsExactlyInAnyOrder(errors);
    }

    public static Stream<Arguments> updateAllExtRefIedNamesErrors() throws Exception {
        return
            Stream.of(Arguments.of(
                    "Errors on ExtRefs",
                    SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml"),
                    new SclReportItem[]{
                        SclReportItem.fatal(
                            "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                "/LN0/Inputs/ExtRef[@desc=\"No matching compas:Flow\"]",
                            "The signal ExtRef has no matching compas:Flow Private"),
                        SclReportItem.fatal(
                            "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                "/LN0/Inputs/ExtRef[@desc=\"Matching two compas:Flow\"]",
                            "The signal ExtRef has more than one matching compas:Flow Private"),
                        SclReportItem.fatal(
                            "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST13\"]",
                            "The LDevice status is neither \"on\" nor \"off\""),
                        SclReportItem.fatal(
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
                        SclReportItem.fatal(
                            "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                "/LN0/Inputs/ExtRef[@desc=\"Source LDevice is undefined for this ExtRef\"]",
                            "The signal ExtRef source LDevice /SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST23\"] status is " +
                                "undefined"),
                        SclReportItem.fatal(
                            "/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                                "/LN0/Inputs/ExtRef[@desc=\"Source LDevice is neither on nor off for this ExtRef\"]",
                            "The signal ExtRef source LDevice /SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST24\"] " +
                                "status is neither \"on\" nor \"off\"")
                    }),
                Arguments.of(
                    "Errors on IEDs",
                    SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_ied_errors.xml"),
                    new SclReportItem[]{
                        SclReportItem.fatal(
                            "/SCL/IED[@name=\"IED_NAME1\"], /SCL/IED[@name=\"IED_NAME2\"]",
                            "/IED/Private/compas:ICDHeader[@ICDSystemVersionUUID] must be unique but the same ICDSystemVersionUUID was found on several IED."),
                        SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME3\"]", "IED has no Private COMPAS-ICDHeader element"),
                        SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME4\"]", "IED private COMPAS-ICDHeader as no icdSystemVersionUUID or iedName attribute"),
                        SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME5\"]", "IED private COMPAS-ICDHeader as no icdSystemVersionUUID or iedName attribute")
                    })
            );
    }

    @Test
    void updateAllExtRefIedNames_when_not_bindable_should_clear_binding() throws Exception {
        // Given : see comments in SCD file
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThatExtRefBindingInfoIsMissing(findExtRef(sclReport, "IED_NAME1", "LD_INST12", "ExtRef target LDevice status is off"));
        assertThatExtRefBindingInfoIsMissing(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "Match compas:Flow but FlowStatus is INACTIVE"));
        assertThatExtRefBindingInfoIsMissing(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "ExtRef does not match any ICDSystemVersionUUID"));
        assertThatExtRefBindingInfoIsMissing(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "ExtRefldinst does not match any LDevice inst in source IED"));
        assertThatExtRefBindingInfoIsMissing(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "ExtRef does not match any LN in source LDevice"));
        assertThatExtRefBindingInfoIsMissing(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "Source LDevice is off for this ExtRef"));
    }

    private void assertThatExtRefBindingInfoIsMissing(TExtRef extRef) {
        assertThat(extRef.isSetIedName()).isFalse();
        assertThat(extRef.isSetLdInst()).isFalse();
        assertThat(extRef.isSetPrefix()).isFalse();
        assertThat(extRef.isSetLnClass()).isFalse();
        assertThat(extRef.isSetLnInst()).isFalse();
        assertThat(extRef.isSetDoName()).isFalse();
        assertThat(extRef.isSetDaName()).isFalse();
        assertThat(extRef.isSetServiceType()).isFalse();
        assertThat(extRef.isSetSrcLDInst()).isFalse();
        assertThat(extRef.isSetPrefix()).isFalse();
        assertThat(extRef.isSetSrcLNClass()).isFalse();
        assertThat(extRef.isSetLnInst()).isFalse();
        assertThat(extRef.isSetSrcCBName()).isFalse();
    }

    @Test
    void updateAllExtRefIedNames_when_lDevice_off_should_remove_binding() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReport).isNotNull();
        LDeviceAdapter lDeviceAdapter = getLDeviceByLdName("IED_NAME1LD_INST12", sclReport.getSclRootAdapter());
        assertThat(lDeviceAdapter.getLDeviceStatus()).hasValue("off");
        assertThat(lDeviceAdapter.getLN0Adapter().getInputsAdapter().getCurrentElem().getExtRef())
            .allSatisfy(this::assertExtRefIsNotBound);
    }

    @Test
    void updateAllExtRefIedNames_when_FlowStatus_INACTIVE_should_remove_binding() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReport).isNotNull();
        LDeviceAdapter lDeviceAdapter = getLDeviceByLdName("IED_NAME1LD_INST11", sclReport.getSclRootAdapter());
        assertThat(lDeviceAdapter.getLDeviceStatus()).hasValue("on");
        Optional<TExtRef> optionalTExtRef = lDeviceAdapter.getCurrentElem().getLN0().getInputs().getExtRef().stream()
            .filter(tExtRef -> "Match compas:Flow but FlowStatus is INACTIVE".equals(tExtRef.getDesc()))
            .findFirst();
        assertThat(optionalTExtRef).isPresent();
        TExtRef extRef = optionalTExtRef.get();
        assertExtRefIsNotBound(extRef);
    }

    @Test
    void createDataSetAndControlBlocks_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd);
        // Then
        //TODO: This is only the first part. Test will be updated in issue #84
        assertThat(sclReport.getSclReportItems()).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME1");
        // Then
        //TODO: This is only the first part. Test will be updated in issue #84
        assertThat(sclReport.getSclReportItems()).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, "non_existing_IED_name"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_provided_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11");
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_and_targetLDeviceInst_is_provided_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", "LD_INST11"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_not_found_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME1", "non_existing_LDevice_inst"))
            .isInstanceOf(ScdException.class)
            .hasMessage("LDevice.inst 'non_existing_LDevice_inst' not found in IED 'IED_NAME1'");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetLDeviceInst_is_provided_without_targetIedName_should_throw_exception() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, null, "LD_INST11"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name parameter is missing");
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

    private LDeviceAdapter getLDeviceByLdName(String ldName, SclRootAdapter sclRootAdapter) {
        Optional<LDeviceAdapter> optionalLDeviceAdapter = sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(lDeviceAdapter -> ldName.equals(lDeviceAdapter.getLdName()))
            .findFirst();
        assertThat(optionalLDeviceAdapter).isPresent();
        return optionalLDeviceAdapter.get();
    }

}
