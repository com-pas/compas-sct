// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings;
import org.lfenergy.compas.sct.commons.dto.ControlBlockTarget;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.ControlBlockAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DataSetAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.FCDARecord;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.lfenergy.compas.scl2007b4.model.TFCEnum.ST;
import static org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.Settings;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newDurationInMilliSec;

class ExtRefServiceTest {

    private static final int APP_ID_MIN = 0;
    private static final long APP_ID_MAX = 0x3FFFL;
    private static final String MAC_ADDRESS_MIN = "01-0C-CD-01-00-00";
    private static final String MAC_ADDRESS_MAX = "01-0C-CD-01-01-FF";

    @Test
    void updateAllExtRefIedNames_should_update_iedName_and_ExtRefIedName() {
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
    void updateAllExtRefIedNames_should_return_success_status() {
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

    public static Stream<Arguments> updateAllExtRefIedNamesErrors() {
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
    void updateAllExtRefIedNames_when_not_bindable_should_clear_binding() {
        // Given : see comments in SCD file
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertExtRefIsNotBound(findExtRef(sclReport, "IED_NAME1", "LD_INST12", "ExtRef target LDevice status is off"));
        assertExtRefIsNotBound(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "Match compas:Flow but FlowStatus is INACTIVE"));
        assertExtRefIsNotBound(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "ExtRef does not match any ICDSystemVersionUUID"));
        assertExtRefIsNotBound(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "ExtRefldinst does not match any LDevice inst in source IED"));
        assertExtRefIsNotBound(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "ExtRef does not match any LN in source LDevice"));
        assertExtRefIsNotBound(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "Source LDevice is off for this ExtRef"));
    }

    @Test
    void updateAllExtRefIedNames_when_lDevice_off_should_remove_binding() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReport).isNotNull();
        LDeviceAdapter lDeviceAdapter = findLDeviceByLdName(sclReport.getSclRootAdapter(), "IED_NAME1LD_INST12");
        assertThat(lDeviceAdapter.getLDeviceStatus()).hasValue("off");
        assertThat(lDeviceAdapter.getLN0Adapter().getInputsAdapter().getCurrentElem().getExtRef())
            .allSatisfy(this::assertExtRefIsNotBound);
    }

    @Test
    void updateAllExtRefIedNames_when_FlowStatus_INACTIVE_should_remove_binding() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_extref_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then
        assertThat(sclReport).isNotNull();
        LDeviceAdapter lDeviceAdapter = findLDeviceByLdName(sclReport.getSclRootAdapter(), "IED_NAME1LD_INST11");
        assertThat(lDeviceAdapter.getLDeviceStatus()).hasValue("on");
        Optional<TExtRef> optionalTExtRef = lDeviceAdapter.getCurrentElem().getLN0().getInputs().getExtRef().stream()
            .filter(tExtRef -> "Match compas:Flow but FlowStatus is INACTIVE".equals(tExtRef.getDesc()))
            .findFirst();
        assertThat(optionalTExtRef).isPresent();
        TExtRef extRef = optionalTExtRef.get();
        assertExtRefIsNotBound(extRef);
    }

    @Test
    void createDataSetAndControlBlocks_should_create_DataSet() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();
        assertThat(streamAllDataSets(sclReport.getSclRootAdapter())).hasSize(6);

        // Check dataSet names
        findDataSet(sclReport, "IED_NAME2", "LD_INST21", "DS_LD_INST21_CYCI");
        findDataSet(sclReport, "IED_NAME2", "LD_INST21", "DS_LD_INST21_DQCI");
        findDataSet(sclReport, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GMI");
        findDataSet(sclReport, "IED_NAME2", "LD_INST21", "DS_LD_INST21_SVI");
        findDataSet(sclReport, "IED_NAME3", "LD_INST31", "DS_LD_INST31_GSE");
        findDataSet(sclReport, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GSI");

        // Check one DataSet content
        DataSetAdapter aDataSet = findDataSet(sclReport.getSclRootAdapter(), "IED_NAME2", "LD_INST21", "DS_LD_INST21_GSI");
        assertThat(aDataSet.getCurrentElem().getFCDA()).hasSize(4);
        assertThat(aDataSet.getCurrentElem().getFCDA().stream().map(FCDARecord::toFCDARecord))
            .containsExactly(
                new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameST", ST),
                new FCDARecord("LD_INST21", "ANCR", "1", "", "DoWithInst1", "daNameST", ST),
                new FCDARecord("LD_INST21", "ANCR", "1", "", "DoWithInst2.subDo", "daNameST", ST),
                new FCDARecord("LD_INST21", "ANCR", "1", "", "OtherDoName", "daNameST", ST)
            );

    }

    @Test
    void createDataSetAndControlBlocks_should_create_ControlBlocks() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();

        // Check ControlBlock names, id and datSet
        assertControlBlockExists(sclReport, "IED_NAME2", "LD_INST21", "CB_LD_INST21_CYCI", "DS_LD_INST21_CYCI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_CYCI", REPORT);
        assertControlBlockExists(sclReport, "IED_NAME2", "LD_INST21", "CB_LD_INST21_DQCI", "DS_LD_INST21_DQCI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_DQCI", REPORT);
        assertControlBlockExists(sclReport, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GMI", "DS_LD_INST21_GMI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_GMI", GSE);
        assertControlBlockExists(sclReport, "IED_NAME2", "LD_INST21", "CB_LD_INST21_SVI", "DS_LD_INST21_SVI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_SVI", SAMPLED_VALUE);
        assertControlBlockExists(sclReport, "IED_NAME3", "LD_INST31", "CB_LD_INST31_GSE", "DS_LD_INST31_GSE", "IED_NAME3LD_INST31/LLN0.CB_LD_INST31_GSE", GSE);
        assertControlBlockExists(sclReport, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GSI", "DS_LD_INST21_GSI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_GSI", GSE);

        // Check one ControlBlock content (ReportControl with sourceDA.fc=MX)
        ControlBlockAdapter reportControlBlock = findControlBlock(sclReport.getSclRootAdapter(), "IED_NAME2", "LD_INST21", "CB_LD_INST21_CYCI", REPORT);
        assertThat(reportControlBlock.getCurrentElem()).isInstanceOf(TReportControl.class);
        TReportControl tReportControl = (TReportControl) reportControlBlock.getCurrentElem();
        assertThat(tReportControl).extracting(TReportControl::getConfRev, TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed,
            TControlWithTriggerOpt::getIntgPd)
            .containsExactly(1L, true, 0L, true, 60000L);

        assertThat(tReportControl.getTrgOps())
            .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isDupd, TTrgOps::isPeriod, TTrgOps::isGi)
            .containsExactly(false, false, false, true, true);

        assertThat(tReportControl.getRptEnabled().getMax()).isEqualTo(1L);
        assertThat(tReportControl.getRptEnabled().getClientLN().stream().map(ControlBlockTarget::from))
            .containsExactly(
                new ControlBlockTarget("AP_NAME", "IED_NAME1", "LD_INST11", "", "LLN0", "", ""));
    }

    @Test
    void createDataSetAndControlBlocks_should_set_ExtRef_srcXXX_attributes() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();

        // assert all ExtRef.srcPrefix srcLNClass srcLNInst are not set
        assertThat(streamAllExtRef(sclReport.getSclRootAdapter()))
            .extracting(TExtRef::getSrcPrefix, TExtRef::isSetSrcLNClass, TExtRef::getSrcLNInst)
            .containsOnly(Tuple.tuple(null, false, null));

        // check some ExtRef
        assertThat(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "test bay internal"))
            .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
            .containsExactly("CB_LD_INST21_GSI", "LD_INST21");
        assertThat(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "test bay external"))
            .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
            .containsExactly("CB_LD_INST31_GSE", "LD_INST31");
        assertThat(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "test ServiceType is SMV, no daName and DO contains ST and MX, but only ST is FCDA candidate"))
            .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
            .containsExactly("CB_LD_INST21_SVI", "LD_INST21");
        assertThat(findExtRef(sclReport, "IED_NAME1", "LD_INST11", "test ServiceType is Report_daReportMX_1"))
            .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
            .containsExactly("CB_LD_INST21_CYCI", "LD_INST21");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME1");
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();
        assertThat(streamAllDataSets(sclReport.getSclRootAdapter())).hasSize(6);
        List<LN0> ln0s = streamAllLn0Adapters(sclReport.getSclRootAdapter()).map(SclElementAdapter::getCurrentElem).toList();
        assertThat(ln0s).flatMap(TLN0::getGSEControl).hasSize(3);
        assertThat(ln0s).flatMap(TLN0::getSampledValueControl).hasSize(1);
        assertThat(ln0s).flatMap(TLN0::getReportControl).hasSize(2);
        MarshallerWrapper.assertValidateXmlSchema(sclReport.getSclRootAdapter().getCurrentElem());
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_and_no_ext_ref_should_do_nothing() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME2");
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();
        assertThat(streamAllDataSets(sclReport.getSclRootAdapter())).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, "non_existing_IED_name"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11");
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_and_targetLDeviceInst_is_provided_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", "LD_INST11"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> ExtRefService.createDataSetAndControlBlocks(scd, "IED_NAME1", "non_existing_LDevice_inst"))
            .isInstanceOf(ScdException.class)
            .hasMessage("LDevice.inst 'non_existing_LDevice_inst' not found in IED 'IED_NAME1'");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetLDeviceInst_is_provided_without_targetIedName_should_throw_exception() {
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

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_sort_FCDA_inside_DataSet_and_avoid_duplicates() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success_test_fcda_sort.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When
        SclReport sclReport = ExtRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReport.getSclReportItems()).isEmpty();
        DataSetAdapter dataSetAdapter = findDataSet(sclRootAdapter, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GSI");
        assertThat(dataSetAdapter.getCurrentElem().getFCDA())
            .map(TFCDA::getLnInst, TFCDA::getDoName)
            .containsExactly(
                Tuple.tuple("1", "FirstDo"),
                Tuple.tuple("1", "SecondDo"),
                Tuple.tuple("1", "ThirdDo"),
                Tuple.tuple("02", "FirstDo")
            );
    }

    @Test
    void configureNetworkForAllControlBlocks_should_create_GSE_elements() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");

        TDurationInMilliSec minTime = newDurationInMilliSec(10);
        TDurationInMilliSec maxTime = newDurationInMilliSec(2000);
        ControlBlockNetworkSettings controlBlockNetworkSettings = controlBlockAdapter -> new Settings(0x1D6, (byte) 4, minTime, maxTime);

        // When
        SclReport sclReport = ExtRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings,
            APP_ID_MIN, APP_ID_MAX, MAC_ADDRESS_MIN, MAC_ADDRESS_MAX, GSE);
        // Then
        assertThat(sclReport.isSuccess()).isTrue();
        TConnectedAP connectedAP = sclReport.getSclRootAdapter().findConnectedApAdapter("IED_NAME2", "AP_NAME").get().getCurrentElem();
        TGSE gse = connectedAP.getGSE().stream()
            .filter(tgse -> "CB_LD_INST21_GSI".equals(tgse.getCbName()))
            .findFirst().get();
        assertThat(gse.getLdInst()).isEqualTo("LD_INST21");
        assertThat(gse.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("10"));
        assertThat(gse.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("2000"));
        assertThat(gse.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "4"),
                Tuple.tuple("APPID", "0000"),
                Tuple.tuple("MAC-Address", MAC_ADDRESS_MIN),
                Tuple.tuple("VLAN-ID", "1D6")
            );

        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void configureNetworkForAllControlBlocks_should_create_GSE_with_incremental_appid_and_mac_addresses() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");

        TDurationInMilliSec minTime = newDurationInMilliSec(10);
        TDurationInMilliSec maxTime = newDurationInMilliSec(2000);
        ControlBlockNetworkSettings controlBlockNetworkSettings = controlBlockAdapter -> new Settings(0x1D6, (byte) 4, minTime, maxTime);

        // When
        SclReport sclReport = ExtRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings,
            0x9L, APP_ID_MAX, "01-0C-CD-01-00-FF", MAC_ADDRESS_MAX, GSE);
        // Then
        assertThat(sclReport.isSuccess()).isTrue();
        assertThat(streamAllConnectedApGseP(scd, "APPID"))
            .containsExactlyInAnyOrder("0009", "000A", "000B");
        assertThat(streamAllConnectedApGseP(scd, "MAC-Address"))
            .containsExactlyInAnyOrder("01-0C-CD-01-00-FF", "01-0C-CD-01-01-00", "01-0C-CD-01-01-01");
    }

    @Test
    void configureNetworkForAllControlBlocks_should_create_SMV_elements() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        ControlBlockNetworkSettings controlBlockNetworkSettings = controlBlockAdapter -> new Settings(0x1D6, (byte) 4, null, null);

        // When
        SclReport sclReport = ExtRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings,
            0x400AL, 0x7FFFL, "01-0C-CD-04-00-00", "01-0C-CD-04-FF-FF", SAMPLED_VALUE);
        // Then
        assertThat(sclReport.isSuccess()).isTrue();
        TConnectedAP connectedAP = scd.getCommunication().getSubNetwork().stream()
            .map(TSubNetwork::getConnectedAP)
            .flatMap(List::stream)
            .filter(tConnectedAP -> "IED_NAME2".equals(tConnectedAP.getIedName()))
            .filter(tConnectedAP -> "AP_NAME".equals(tConnectedAP.getApName()))
            .findFirst().get();
        TSMV smv = connectedAP.getSMV().stream()
            .filter(tsmv -> "CB_LD_INST21_SVI".equals(tsmv.getCbName()))
            .findFirst().get();
        assertThat(smv.getLdInst()).isEqualTo("LD_INST21");
        assertThat(smv.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "4"),
                Tuple.tuple("APPID", "400A"),
                Tuple.tuple("MAC-Address", "01-0C-CD-04-00-00"),
                Tuple.tuple("VLAN-ID", "1D6")
            );

        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void configureNetworkForAllControlBlocks_with_CSV_file_should_create_GSE_elements() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        String fileName = "ControlBlockCommunicationTemplates.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader csvReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        // When
        SclReport sclReport = ExtRefService.configureNetworkForAllControlBlocks(scd, csvReader,
            APP_ID_MIN, APP_ID_MAX, MAC_ADDRESS_MIN, MAC_ADDRESS_MAX, GSE);
        // Then
        assertThat(sclReport.isSuccess()).isTrue();
        TGSE gseBayInternal = sclReport.getSclRootAdapter().findConnectedApAdapter("IED_NAME2", "AP_NAME").get().getCurrentElem()
            .getGSE().stream()
            .filter(tgse -> "CB_LD_INST21_GSI".equals(tgse.getCbName()))
            .findFirst().get();
        assertThat(gseBayInternal.getLdInst()).isEqualTo("LD_INST21");
        assertThat(gseBayInternal.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("10"));
        assertThat(gseBayInternal.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("2000"));
        assertThat(gseBayInternal.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "4"),
                Tuple.tuple("APPID", "0000"),
                Tuple.tuple("MAC-Address", MAC_ADDRESS_MIN),
                Tuple.tuple("VLAN-ID", "12C")
            );

        TGSE gseBayExternal = sclReport.getSclRootAdapter().findConnectedApAdapter("IED_NAME3", "AP_NAME").get().getCurrentElem()
            .getGSE().stream()
            .filter(tgse -> "CB_LD_INST31_GSE".equals(tgse.getCbName()))
            .findFirst().get();
        assertThat(gseBayExternal.getLdInst()).isEqualTo("LD_INST31");
        assertThat(gseBayExternal.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("15"));
        assertThat(gseBayExternal.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("5000"));
        assertThat(gseBayExternal.getAddress().getP())
            .extracting(TP::getType, TP::getValue)
            .containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "5"),
                Tuple.tuple("APPID", String.format("%04d", APP_ID_MIN + 2)),
                Tuple.tuple("MAC-Address", Utils.longToMacAddress(Utils.macAddressToLong(MAC_ADDRESS_MIN) + 2)),
                Tuple.tuple("VLAN-ID", "12D")
            );

        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @ParameterizedTest
    @MethodSource("provideConfigureNetworkForAllControlBlocksErrors")
    void configureNetworkForAllControlBlocks_should_fail_when_no_settings_for_this_controlBlock(ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                                                long aapIdMax, String macAddressMax,
                                                                                                String expectedMessage) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");

        // When
        SclReport sclReport = ExtRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings,
            APP_ID_MIN, aapIdMax, MAC_ADDRESS_MIN, macAddressMax, GSE);
        // Then
        assertThat(sclReport.isSuccess()).isFalse();
        assertThat(sclReport.getSclReportItems())
            .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
            .contains(Tuple.tuple(expectedMessage,
                "/SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST21\"]/LN0/GSEControl[@name=\"CB_LD_INST21_GMI\"]"));
    }

    public static Stream<Arguments> provideConfigureNetworkForAllControlBlocksErrors() {
        Settings settingsWithNullVlanId = new Settings(null, (byte) 1, newDurationInMilliSec(1), newDurationInMilliSec(2));
        Settings settings = new Settings(1, (byte) 1, newDurationInMilliSec(1), newDurationInMilliSec(2));
        return Stream.of(
            Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> null, APP_ID_MAX, MAC_ADDRESS_MAX,
                "Cannot configure network for this ControlBlock because no settings was provided"),
            Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> settingsWithNullVlanId, APP_ID_MAX, MAC_ADDRESS_MAX,
                "Cannot configure network for this ControlBlock because no Vlan Id was "
                + "provided in the settings"),
            Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> settings, APP_ID_MIN, MAC_ADDRESS_MAX,
                "Cannot configure network for this ControlBlock because range of appId is exhausted"),
            Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> settings, APP_ID_MAX, MAC_ADDRESS_MIN,
                "Cannot configure network for this ControlBlock because range of MAC Address is exhausted")
        );
    }

}
