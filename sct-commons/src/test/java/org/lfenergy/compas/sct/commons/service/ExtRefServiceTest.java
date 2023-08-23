// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.service;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DataSetAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.service.impl.ExtRefService;
import org.lfenergy.compas.sct.commons.testhelpers.FCDARecord;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.ILDEPFSettings;
import org.lfenergy.compas.sct.commons.util.SettingLDEPFCsvHelper;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.lfenergy.compas.scl2007b4.model.TFCEnum.ST;
import static org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newDurationInMilliSec;

@ExtendWith(MockitoExtension.class)
class ExtRefServiceTest {

    @InjectMocks
    ExtRefService extRefService;

    private static final long GSE_APP_ID_MIN = 0x9;
    private static final long SMV_APP_ID_MIN = 0x400A;
    private static final String GSE_MAC_ADDRESS_PREFIX = "01-02-03-04-";
    private static final String SMV_MAC_ADDRESS_PREFIX = "0A-0B-0C-0D-";
    private static final NetworkRanges GSE_NETWORK_RANGES = new NetworkRanges(GSE_APP_ID_MIN, GSE_APP_ID_MIN + 10, GSE_MAC_ADDRESS_PREFIX + "00-FF", GSE_MAC_ADDRESS_PREFIX + "01-AA");
    private static final NetworkRanges SMV_NETWORK_RANGES = new NetworkRanges(SMV_APP_ID_MIN, SMV_APP_ID_MIN + 10, SMV_MAC_ADDRESS_PREFIX + "00-FF", SMV_MAC_ADDRESS_PREFIX + "01-AA");
    private static final RangesPerCbType RANGES_PER_CB_TYPE = new RangesPerCbType(GSE_NETWORK_RANGES, SMV_NETWORK_RANGES);

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

    @Test
    void createDataSetAndControlBlocks_should_create_DataSet() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(streamAllDataSets(scd)).hasSize(6);

        // Check dataSet names
        findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_CYCI");
        findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_DQCI");
        findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GMI");
        findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_SVI");
        findDataSet(scd, "IED_NAME3", "LD_INST31", "DS_LD_INST31_GSE");
        findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GSI");

        // Check one DataSet content
        DataSetAdapter aDataSet = findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GSI");
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
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReportItems).isEmpty();

        // Check ControlBlock names, id and datSet
        assertControlBlockExists(scd, "IED_NAME2", "LD_INST21", "CB_LD_INST21_CYCI", "DS_LD_INST21_CYCI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_CYCI", REPORT);
        assertControlBlockExists(scd, "IED_NAME2", "LD_INST21", "CB_LD_INST21_DQCI", "DS_LD_INST21_DQCI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_DQCI", REPORT);
        assertControlBlockExists(scd, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GMI", "DS_LD_INST21_GMI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_GMI", GSE);
        assertControlBlockExists(scd, "IED_NAME2", "LD_INST21", "CB_LD_INST21_SVI", "DS_LD_INST21_SVI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_SVI", SAMPLED_VALUE);
        assertControlBlockExists(scd, "IED_NAME3", "LD_INST31", "CB_LD_INST31_GSE", "DS_LD_INST31_GSE", "IED_NAME3LD_INST31/LLN0.CB_LD_INST31_GSE", GSE);
        assertControlBlockExists(scd, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GSI", "DS_LD_INST21_GSI", "IED_NAME2LD_INST21/LLN0.CB_LD_INST21_GSI", GSE);

        // Check one ControlBlock content (ReportControl with sourceDA.fc=MX)
        TReportControl tReportControl = findControlBlock(scd, "IED_NAME2", "LD_INST21", "CB_LD_INST21_CYCI", TReportControl.class);
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
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReportItems).isEmpty();

        // assert all ExtRef.srcPrefix srcLNClass srcLNInst are not set
        assertThat(streamAllExtRef(scd))
                .extracting(TExtRef::getSrcPrefix, TExtRef::isSetSrcLNClass, TExtRef::getSrcLNInst)
                .containsOnly(Tuple.tuple(null, false, null));

        // check some ExtRef
        assertThat(findExtRef(scd, "IED_NAME1", "LD_INST11", "test bay internal"))
                .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
                .containsExactly("CB_LD_INST21_GSI", "LD_INST21");
        assertThat(findExtRef(scd, "IED_NAME1", "LD_INST11", "test bay external"))
                .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
                .containsExactly("CB_LD_INST31_GSE", "LD_INST31");
        assertThat(findExtRef(scd, "IED_NAME1", "LD_INST11", "test ServiceType is SMV, no daName and DO contains ST and MX, but only ST is FCDA candidate"))
                .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
                .containsExactly("CB_LD_INST21_SVI", "LD_INST21");
        assertThat(findExtRef(scd, "IED_NAME1", "LD_INST11", "test ServiceType is Report_daReportMX_1"))
                .extracting(TExtRef::getSrcCBName, TExtRef::getSrcLDInst)
                .containsExactly("CB_LD_INST21_CYCI", "LD_INST21");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd, "IED_NAME1");
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(streamAllDataSets(scd)).hasSize(6);
        List<LN0> ln0s = streamAllLn0Adapters(scd).map(SclElementAdapter::getCurrentElem).toList();
        assertThat(ln0s).flatMap(TLN0::getGSEControl).hasSize(3);
        assertThat(ln0s).flatMap(TLN0::getSampledValueControl).hasSize(1);
        assertThat(ln0s).flatMap(TLN0::getReportControl).hasSize(2);
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_and_no_ext_ref_should_do_nothing() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd, "IED_NAME2");
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(streamAllDataSets(scd)).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> extRefService.createDataSetAndControlBlocks(scd, "non_existing_IED_name"))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11");
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_and_targetLDeviceInst_is_provided_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> extRefService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", "LD_INST11"))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> extRefService.createDataSetAndControlBlocks(scd, "IED_NAME1", "non_existing_LDevice_inst"))
                .isInstanceOf(ScdException.class)
                .hasMessage("LDevice.inst 'non_existing_LDevice_inst' not found in IED 'IED_NAME1'");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetLDeviceInst_is_provided_without_targetIedName_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> extRefService.createDataSetAndControlBlocks(scd, null, "LD_INST11"))
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
        // When
        List<SclReportItem> sclReportItems = extRefService.createDataSetAndControlBlocks(scd);
        // Then
        assertThat(sclReportItems).isEmpty();
        DataSetAdapter dataSetAdapter = findDataSet(scd, "IED_NAME2", "LD_INST21", "DS_LD_INST21_GSI");
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
    void configureNetworkForAllControlBlocks_should_create_GSE_and_SMV_elements() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");

        TDurationInMilliSec minTime = newDurationInMilliSec(10);
        TDurationInMilliSec maxTime = newDurationInMilliSec(2000);
        ControlBlockNetworkSettings controlBlockNetworkSettings = controlBlockAdapter -> new SettingsOrError(new Settings(0x1D6, (byte) 4, minTime, maxTime), null);

        // When
        List<SclReportItem> sclReportItems = extRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, RANGES_PER_CB_TYPE);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        TConnectedAP connectedAP = new SclRootAdapter(scd).findConnectedApAdapter("IED_NAME2", "AP_NAME").get().getCurrentElem();
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
                        Tuple.tuple("APPID", "0009"),
                        Tuple.tuple("MAC-Address", "01-02-03-04-00-FF"),
                        Tuple.tuple("VLAN-ID", "1D6")
                );
        TSMV smv = connectedAP.getSMV().stream()
                .filter(tsmv -> "CB_LD_INST21_SVI".equals(tsmv.getCbName()))
                .findFirst().get();
        assertThat(smv.getLdInst()).isEqualTo("LD_INST21");
        assertThat(smv.getAddress().getP()).extracting(TP::getType, TP::getValue)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("VLAN-PRIORITY", "4"),
                        Tuple.tuple("APPID", "400A"),
                        Tuple.tuple("MAC-Address", "0A-0B-0C-0D-00-FF"),
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
        ControlBlockNetworkSettings controlBlockNetworkSettings = controlBlockAdapter -> new SettingsOrError(new Settings(0x1D6, (byte) 4, minTime, maxTime), null);
        // When
        List<SclReportItem> sclReportItems = extRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, RANGES_PER_CB_TYPE);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(streamAllConnectedApGseP(scd, "APPID"))
                .containsExactlyInAnyOrder("0009", "000A", "000B");
        assertThat(streamAllConnectedApGseP(scd, "MAC-Address"))
                .containsExactlyInAnyOrder("01-02-03-04-00-FF", "01-02-03-04-01-00", "01-02-03-04-01-01");
    }

    @ParameterizedTest
    @MethodSource("provideConfigureNetworkForAllControlBlocksErrors")
    void configureNetworkForAllControlBlocks_should_fail_when_no_settings_for_this_controlBlock(ControlBlockNetworkSettings controlBlockNetworkSettings,
                                                                                                RangesPerCbType rangesPerCbType,
                                                                                                String expectedMessage) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        // When
        List<SclReportItem> sclReportItems = extRefService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems)
                .extracting(SclReportItem::message, SclReportItem::xpath)
                .contains(Tuple.tuple(expectedMessage,
                        "/SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST21\"]/LN0/GSEControl[@name=\"CB_LD_INST21_GMI\"]"));
    }

    public static Stream<Arguments> provideConfigureNetworkForAllControlBlocksErrors() {
        Settings settingsWithNullVlanId = new Settings(null, (byte) 1, newDurationInMilliSec(1), newDurationInMilliSec(2));
        Settings settings = new Settings(1, (byte) 1, newDurationInMilliSec(1), newDurationInMilliSec(2));
        return Stream.of(
                Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> new SettingsOrError(null, null),
                        RANGES_PER_CB_TYPE,
                        "Cannot configure network for this ControlBlock because no settings was provided"),
                Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> new SettingsOrError(null, "Custom error message"),
                        RANGES_PER_CB_TYPE,
                        "Cannot configure network for this ControlBlock because: Custom error message"),
                Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> new SettingsOrError(settingsWithNullVlanId, null),
                        RANGES_PER_CB_TYPE,
                        "Cannot configure network for this ControlBlock because no Vlan Id was provided in the settings"),
                Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> new SettingsOrError(settings, null),
                        new RangesPerCbType(
                                new NetworkRanges(GSE_APP_ID_MIN, GSE_APP_ID_MIN, GSE_MAC_ADDRESS_PREFIX + "00-FF", GSE_MAC_ADDRESS_PREFIX + "01-AA"),
                                SMV_NETWORK_RANGES),
                        "Cannot configure network for this ControlBlock because range of appId is exhausted"),
                Arguments.of((ControlBlockNetworkSettings) controlBlockAdapter -> new SettingsOrError(settings, null),
                        new RangesPerCbType(
                                new NetworkRanges(GSE_APP_ID_MIN, GSE_APP_ID_MIN + 10, GSE_MAC_ADDRESS_PREFIX + "00-FF", GSE_MAC_ADDRESS_PREFIX + "00-FF"),
                                SMV_NETWORK_RANGES),
                        "Cannot configure network for this ControlBlock because range of MAC Address is exhausted")
        );
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
    void manageBindingForLDEPF_should_return_noReportAndExtRefUpdateSuccessfully_whenFlowKindIsInternalAndAllExtRefInSameBay() {
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
    void manageBindingForLDEPF_should_return_no_report_when_extRef_withDifferentIedType_update_successfully() {
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
    void manageBindingForLDEPF_should_return_report_when_manyIedSourceFound() {
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
    void manageBindingForLDEPF_should_return_no_report_when_extRefInFlowKindInternalAndExternal_update_successfully() {
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

}
