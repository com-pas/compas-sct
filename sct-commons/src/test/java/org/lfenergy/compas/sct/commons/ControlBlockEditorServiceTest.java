// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlockTarget;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.cbcom.*;
import org.lfenergy.compas.sct.commons.scl.ControlService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DataSetAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.FCDARecord;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.lfenergy.compas.scl2007b4.model.TFCEnum.ST;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;

class ControlBlockEditorServiceTest {

    ControlBlockEditorService controlBlockEditorService;

    private Set<FcdaForDataSetsCreation> allowedFcdas;

    @BeforeEach
    void init() {
        controlBlockEditorService = new ControlBlockEditorService(new ControlService());
        allowedFcdas = new HashSet<>(CsvUtils.parseRows("FcdaCandidates.csv", StandardCharsets.UTF_8, FcdaForDataSetsCreation.class));
    }

    @Test
    void analyzeDataGroups_should_success() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter1 = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxAttributes(9L);
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxGOOSE(3L);
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxSMV(2L);
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxReports(1L);
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.analyzeDataGroups(scd);
        //Then
        assertThat(sclReportItems).isEmpty();

    }

    @Test
    void analyzeDataGroups_should_return_errors_messages() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME2");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getConfDataSet().setMaxAttributes(1L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getConfDataSet().setMax(3L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getSMVsc().setMax(1L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getGOOSE().setMax(2L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getConfReportControl().setMax(0L);
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.analyzeDataGroups(scd);
        //Then
        assertThat(sclReportItems).hasSize(11)
                .extracting(SclReportItem::message)
                .containsExactlyInAnyOrder(
                        "The Client IED IED_NAME1 subscribes to too much FCDA: 9 > 8 max",
                        "The Client IED IED_NAME1 subscribes to too much GOOSE Control Blocks: 3 > 2 max",
                        "The Client IED IED_NAME1 subscribes to too much Report Control Blocks: 1 > 0 max",
                        "The Client IED IED_NAME1 subscribes to too much SMV Control Blocks: 2 > 1 max",
                        "There are too much FCDA for the DataSet dataset6 for the LDevice LD_INST21 in IED IED_NAME2: 2 > 1 max",
                        "There are too much FCDA for the DataSet dataset6 for the LDevice LD_INST22 in IED IED_NAME2: 2 > 1 max",
                        "There are too much FCDA for the DataSet dataset5 for the LDevice LD_INST22 in IED IED_NAME2: 2 > 1 max",
                        "There are too much DataSets for the IED IED_NAME2: 6 > 3 max",
                        "There are too much Report Control Blocks for the IED IED_NAME2: 1 > 0 max",
                        "There are too much GOOSE Control Blocks for the IED IED_NAME2: 3 > 2 max",
                        "There are too much SMV Control Blocks for the IED IED_NAME2: 3 > 1 max");
    }

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_srcXXX_attributes_on_ExtRef() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        controlBlockEditorService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
        // Then
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scl);
        List<TExtRef> extRefs = scdRootAdapter
                .streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .map(LDeviceAdapter::getLN0Adapter)
                .map(AbstractLNAdapter::getExtRefs).flatMap(List::stream)
                .collect(Collectors.toList());
        assertThat(extRefs)
                .isNotEmpty()
                .noneMatch(TExtRef::isSetSrcLDInst)
                .noneMatch(TExtRef::isSetSrcPrefix)
                .noneMatch(TExtRef::isSetSrcLNInst)
                .noneMatch(TExtRef::isSetSrcCBName)
                .noneMatch(TExtRef::isSetSrcLNClass);
        assertIsMarshallable(scl);
    }


    private static Stream<Arguments> provideAllowedFcdaListEmptyOrNull() {
        return Stream.of(
                Arguments.of("Set of allowed FCDA is null", null),
                Arguments.of("Set of allow FCDA is Empty", Collections.EMPTY_SET)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideAllowedFcdaListEmptyOrNull")
    void createDataSetAndControlBlocks_should_Throw_Exception_when_list_allowed_fcda_not_initialized(String testName, Set<FcdaForDataSetsCreation> fcdaForDataSets) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When Then
        assertThatCode(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, fcdaForDataSets))
                .isInstanceOf(ScdException.class)
                .hasMessage("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
    }

    @Test
    void createDataSetAndControlBlocks_should_create_DataSet() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, allowedFcdas);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideAllowedFcdaListEmptyOrNull")
    void createDataSetAndControlBlocks_with_targetIedName_should_Throw_Exception_when_list_allowed_fcda_not_initialized(String testName, Set<FcdaForDataSetsCreation> fcdaForDataSets) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When Then
        assertThatCode(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, "IED_NAME1", fcdaForDataSets))
                .isInstanceOf(ScdException.class)
                .hasMessage("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, "IED_NAME1", allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, "IED_NAME2", allowedFcdas);
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(streamAllDataSets(scd)).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideAllowedFcdaListEmptyOrNull")
    void createDataSetAndControlBlocks_with_targetIedName_and_targetLDeviceInst_should_Throw_Exception_when_list_allowed_fcda_not_initialized(String testName, Set<FcdaForDataSetsCreation> fcdaForDataSets) {
        // Given
        SCL scd = new SCL();
        // When Then
        assertThatCode(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11", fcdaForDataSets))
                .isInstanceOf(ScdException.class)
                .hasMessage("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11", allowedFcdas);
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_and_targetLDeviceInst_is_provided_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", "LD_INST11", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, "IED_NAME1", "non_existing_LDevice_inst", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("LDevice.inst 'non_existing_LDevice_inst' not found in IED 'IED_NAME1'");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetLDeviceInst_is_provided_without_targetIedName_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockEditorService.createDataSetAndControlBlocks(scd, null, "LD_INST11", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name parameter is missing");
    }


    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_sort_FCDA_inside_DataSet_and_avoid_duplicates() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success_test_fcda_sort.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
    void configureNetworkForAllControlBlocks_should_create_GSE_elements() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        TGSE gse1 = getCommunicationGSE(scd, "IED_NAME2", "CB_LD_INST21_GSI");
        assertThat(gse1.getLdInst()).isEqualTo("LD_INST21");
        assertThat(SclDuration.from(gse1.getMinTime())).isEqualTo(new SclDuration("10", "s", "m"));
        assertThat(SclDuration.from(gse1.getMaxTime())).isEqualTo(new SclDuration("2000", "s", "m"));
        assertThat(gse1.getAddress().getP()).extracting(TP::getType, TP::getValue).containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "1"),
                Tuple.tuple("APPID", "0000"),
                Tuple.tuple("MAC-Address", "01-0C-CD-01-00-00"),
                Tuple.tuple("VLAN-ID", "12D")
        );
        TGSE gse2 = getCommunicationGSE(scd, "IED_NAME2", "CB_LD_INST21_GMI");
        assertThat(gse2.getLdInst()).isEqualTo("LD_INST21");
        assertThat(SclDuration.from(gse2.getMinTime())).isEqualTo(new SclDuration("10", "s", "m"));
        assertThat(SclDuration.from(gse2.getMaxTime())).isEqualTo(new SclDuration("2000", "s", "m"));
        assertThat(gse2.getAddress().getP()).extracting(TP::getType, TP::getValue).containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "1"),
                Tuple.tuple("APPID", "0001"),
                Tuple.tuple("MAC-Address", "01-0C-CD-01-00-01"),
                Tuple.tuple("VLAN-ID", "12D")
        );
        TGSE gse3 = getCommunicationGSE(scd, "IED_NAME3", "CB_LD_INST31_GSE");
        assertThat(gse3.getLdInst()).isEqualTo("LD_INST31");
        assertThat(SclDuration.from(gse3.getMinTime())).isEqualTo(new SclDuration("10", "s", "m"));
        assertThat(SclDuration.from(gse3.getMaxTime())).isEqualTo(new SclDuration("2000", "s", "m"));
        assertThat(gse3.getAddress().getP()).extracting(TP::getType, TP::getValue).containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "2"),
                Tuple.tuple("APPID", "0002"),
                Tuple.tuple("MAC-Address", "01-0C-CD-01-00-02"),
                Tuple.tuple("VLAN-ID", "12E")
        );
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void configureNetworkForAllControlBlocks_should_create_SMV_elements() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        TSMV smv1 = getCommunicationSMV(scd, "IED_NAME2", "CB_LD_INST21_SVI");
        assertThat(smv1.getLdInst()).isEqualTo("LD_INST21");
        assertThat(smv1.getAddress().getP()).extracting(TP::getType, TP::getValue).containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "3"),
                Tuple.tuple("APPID", "4000"),
                Tuple.tuple("MAC-Address", "01-0C-CD-04-00-00"),
                Tuple.tuple("VLAN-ID", "12F")
        );
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        TSMV smv2 = getCommunicationSMV(scd, "IED_NAME3", "CB_LD_INST31_SVE");
        assertThat(smv2.getLdInst()).isEqualTo("LD_INST31");
        assertThat(smv2.getAddress().getP()).extracting(TP::getType, TP::getValue).containsExactlyInAnyOrder(
                Tuple.tuple("VLAN-PRIORITY", "4"),
                Tuple.tuple("APPID", "4001"),
                Tuple.tuple("MAC-Address", "01-0C-CD-04-00-01"),
                Tuple.tuple("VLAN-ID", "130")
        );
        MarshallerWrapper.assertValidateXmlSchema(scd);
    }

    @Test
    void configureNetworkForAllControlBlocks_should_create_GSE_with_incremental_appid_and_mac_addresses() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        cbCom.getAppIdRanges().getAppIdRange().get(0).setStart("0009");
        cbCom.getAppIdRanges().getAppIdRange().get(0).setEnd("000B");
        cbCom.getMacRanges().getMacRange().get(0).setStart("01-02-03-04-00-FF");
        cbCom.getMacRanges().getMacRange().get(0).setEnd("01-02-03-04-01-01");
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(streamAllConnectedApGseP(scd, "APPID"))
                .containsExactlyInAnyOrder("0009", "000A", "000B");
        assertThat(streamAllConnectedApGseP(scd, "MAC-Address"))
                .containsExactlyInAnyOrder("01-02-03-04-00-FF", "01-02-03-04-01-00", "01-02-03-04-01-01");
    }

    @ParameterizedTest
    @MethodSource("provideConfigureNetworkForAllControlBlocksErrors")
    void configureNetworkForAllControlBlocks_should_fail_when_no_settings_for_this_controlBlock(CBCom cbCom, String expectedMessage, String expectedXPath) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems)
                .contains(SclReportItem.error(expectedXPath, expectedMessage));
    }

    public static Stream<Arguments> provideConfigureNetworkForAllControlBlocksErrors() {
        CBCom cbComWithNoVlan = createCbCom();
        cbComWithNoVlan.getVlans().getVlan().clear();
        CBCom cbComWithMissingVlanId = createCbCom();
        cbComWithMissingVlanId.getVlans().getVlan().get(0).setVlanId(null);
        CBCom cbComWithNotEnoughAppId = createCbCom();
        cbComWithNotEnoughAppId.getAppIdRanges().getAppIdRange().get(0).setStart("0000");
        cbComWithNotEnoughAppId.getAppIdRanges().getAppIdRange().get(0).setEnd("00001");
        CBCom cbComWithNotEnoughMacAddress = createCbCom();
        cbComWithNotEnoughMacAddress.getMacRanges().getMacRange().get(0).setStart("01-0C-CD-01-00-00");
        cbComWithNotEnoughMacAddress.getMacRanges().getMacRange().get(0).setEnd("01-0C-CD-01-00-01");

        return Stream.of(
                Arguments.of(cbComWithNoVlan, "Cannot configure communication for this ControlBlock because: No controlBlock communication settings found with these Criteria[cbType=GOOSE, systemVersionWithoutV=01.00.009.001, iedType=BCU, iedRedundancy=A, iedSystemVersionInstance=1, bayIntOrExt=BAY_INTERNAL]",
                        "/SCL/IED[@name=\"IED_NAME2\"]/AccessPoint[@name=\"AP_NAME\"]/Server/LDevice[@inst=\"LD_INST21\"]/LN0/GSEControl[@name=\"CB_LD_INST21_GMI\"]"),
                Arguments.of(cbComWithMissingVlanId, "Cannot configure communication for this ControlBlock because no Vlan Id was provided in the settings",
                        "/SCL/IED[@name=\"IED_NAME2\"]/AccessPoint[@name=\"AP_NAME\"]/Server/LDevice[@inst=\"LD_INST21\"]/LN0/GSEControl[@name=\"CB_LD_INST21_GMI\"]"),
                Arguments.of(cbComWithNotEnoughAppId, "Cannot configure communication for this ControlBlock because range of appId is exhausted",
                        "/SCL/IED[@name=\"IED_NAME3\"]/AccessPoint[@name=\"AP_NAME\"]/Server/LDevice[@inst=\"LD_INST31\"]/LN0/GSEControl[@name=\"CB_LD_INST31_GSE\"]"),
                Arguments.of(cbComWithNotEnoughMacAddress, "Cannot configure communication for this ControlBlock because range of MAC Address is exhausted",
                        "/SCL/IED[@name=\"IED_NAME3\"]/AccessPoint[@name=\"AP_NAME\"]/Server/LDevice[@inst=\"LD_INST31\"]/LN0/GSEControl[@name=\"CB_LD_INST31_GSE\"]")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBlankCriteria")
    void configureNetworkForAllControlBlocks_when_setting_files_has_blank_criteria_should_return_error(Consumer<TVlan> setCriteriaBlank) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        setCriteriaBlank.accept(cbCom.getVlans().getVlan().get(0));
        //When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        //Then
        assertThat(sclReportItems).hasSize(1);
        assertThat(sclReportItems.get(0)).extracting(SclReportItem::isError, SclReportItem::xpath)
                .containsExactly(true, "Control Block Communication setting files");
        assertThat(sclReportItems.get(0).message()).matches("Error in Control Block communication setting file: vlan is missing attribute .*");
    }

    private static Stream<Arguments> provideBlankCriteria() {
        return Stream.of(Arguments.of(
                (Consumer<TVlan>) tVlan -> tVlan.setXY(null),
                (Consumer<TVlan>) tVlan -> tVlan.setZW(null),
                (Consumer<TVlan>) tVlan -> tVlan.setIEDType(null),
                (Consumer<TVlan>) tVlan -> tVlan.setIEDRedundancy(null),
                (Consumer<TVlan>) tVlan -> tVlan.setIEDSystemVersionInstance(null),
                (Consumer<TVlan>) tVlan -> tVlan.setBayIntOrExt(null)
        ));
    }

    @ParameterizedTest
    @MethodSource("provideMalformedNumbers")
    void configureNetworkForAllControlBlocks_when_malformed_numbers_should_return_error(Consumer<TVlan> setMalformedNumber) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        setMalformedNumber.accept(cbCom.getVlans().getVlan().get(0));
        //When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        //Then
        assertThat(sclReportItems).hasSize(1);
        assertThat(sclReportItems.get(0)).extracting(SclReportItem::isError, SclReportItem::xpath)
                .containsExactly(true, "Control Block Communication setting files");
        assertThat(sclReportItems.get(0).message()).matches("Error in Control Block communication setting file: .+ must be an integer( or 'none')?, but got : XXX");
    }

    private static Stream<Arguments> provideMalformedNumbers() {
        return Stream.of(
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setIEDSystemVersionInstance("XXX")),
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setVlanId("XXX")),
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setVlanPriority("XXX")),
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setMinTime("XXX")),
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setMaxTime("XXX"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideOutOfBoundNumbers")
    void configureNetworkForAllControlBlocks_when_out_of_bound_numbers_should_return_error(Consumer<TVlan> setMalformedNumber) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        setMalformedNumber.accept(cbCom.getVlans().getVlan().get(0));
        //When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        //Then
        assertThat(sclReportItems).hasSize(1);
        assertThat(sclReportItems.get(0)).extracting(SclReportItem::isError, SclReportItem::xpath)
                .containsExactly(true, "Control Block Communication setting files");
        assertThat(sclReportItems.get(0).message()).matches("Error in Control Block communication setting file: VLAN (ID|PRIORITY) must be between 0 and [0-9]+, but got : .*");
    }

    private static Stream<Arguments> provideOutOfBoundNumbers() {
        return Stream.of(
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setVlanId("4096")), // VlanId > MAX_VLAN_ID
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setVlanId("-1")), // VlanId < 0
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setVlanPriority("8")), // VlanPriority > MAX_VLAN_PRIORITY
                Arguments.of((Consumer<TVlan>) tVlan -> tVlan.setVlanPriority("-1")) // VlanPriority < 0
        );
    }

    @Test
    void configureNetworkForAllControlBlocks_when_missing_connectedAp_should_return_error() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        scd.getCommunication().getSubNetwork().get(0).getConnectedAP().remove(0);
        CBCom cbCom = createCbCom();
        //When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        //Then
        assertThat(sclReportItems).hasSize(3);
        assertThat(sclReportItems).extracting(SclReportItem::isError).containsOnly(true);
        assertThat(sclReportItems).extracting(SclReportItem::message).containsOnly("Cannot configure communication for ControlBlock because no ConnectedAP found for AccessPoint");
        assertThat(sclReportItems).extracting(SclReportItem::xpath).allMatch(xpath -> xpath.startsWith("""
                /SCL/IED[@name="IED_NAME2"]/AccessPoint[@name="AP_NAME"]/Server/LDevice[@inst="LD_INST21"]/LN0/"""));
    }

    @ParameterizedTest
    @MethodSource("provideRemovePrivateInfo")
    void configureNetworkForAllControlBlocks_when_missing_IED_privates_should_return_error(Consumer<TIED> removePrivateInfo) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        CBCom cbCom = createCbCom();
        removePrivateInfo.accept(scd.getIED().get(1));
        //When
        List<SclReportItem> sclReportItems = controlBlockEditorService.configureNetworkForAllControlBlocks(scd, cbCom);
        //Then
        assertThat(sclReportItems).hasSize(3);
        assertThat(sclReportItems).extracting(SclReportItem::isError, SclReportItem::xpath)
                .containsOnly(Tuple.tuple(true, """
                        /SCL/IED[@name="IED_NAME2"]/AccessPoint[@name="AP_NAME"]/Server/LDevice[@inst="LD_INST21"]"""));
        assertThat(sclReportItems).extracting(SclReportItem::message).allSatisfy(message -> assertThat(message).containsAnyOf("COMPAS-ICDHeader", "COMPAS-SystemVersion"));
    }

    private static Stream<Arguments> provideRemovePrivateInfo() {
        return Stream.of(
                Arguments.of((Consumer<TIED>) tied -> PrivateUtils.removePrivates(tied, PrivateEnum.COMPAS_SYSTEM_VERSION)),
                Arguments.of((Consumer<TIED>) tied -> PrivateUtils.extractCompasPrivate(tied, TCompasSystemVersion.class).ifPresent(tCompasSystemVersion -> tCompasSystemVersion.setMainSystemVersion(null))),
                Arguments.of((Consumer<TIED>) tied -> PrivateUtils.extractCompasPrivate(tied, TCompasSystemVersion.class).ifPresent(tCompasSystemVersion -> tCompasSystemVersion.setMinorSystemVersion(null))),
                Arguments.of((Consumer<TIED>) tied -> PrivateUtils.removePrivates(tied, PrivateEnum.COMPAS_ICDHEADER)),
                Arguments.of((Consumer<TIED>) tied -> PrivateUtils.extractCompasPrivate(tied, TCompasICDHeader.class).ifPresent(tCompasICDHeader -> tCompasICDHeader.setIEDSystemVersioninstance(null)))
        );
    }

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_controlBlocks_and_Dataset_on_ln0() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        controlBlockEditorService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
        // Then
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scl);
        List<LDeviceAdapter> lDevices = scdRootAdapter.streamIEDAdapters().flatMap(IEDAdapter::streamLDeviceAdapters).toList();
        List<LN0> ln0s = lDevices.stream().map(LDeviceAdapter::getLN0Adapter).map(LN0Adapter::getCurrentElem).toList();
        assertThat(ln0s)
                .isNotEmpty()
                .noneMatch(TAnyLN::isSetDataSet)
                .noneMatch(TAnyLN::isSetLogControl)
                .noneMatch(TAnyLN::isSetReportControl)
                .noneMatch(LN0::isSetGSEControl)
                .noneMatch(LN0::isSetSampledValueControl);
        assertIsMarshallable(scl);
    }

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_controlBlocks_and_Dataset_on_ln() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        controlBlockEditorService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
        // Then
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scl);
        List<TLN> lns = scdRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .map(LDeviceAdapter::getLNAdapters).flatMap(List::stream)
                .map(LNAdapter::getCurrentElem).collect(Collectors.toList());
        assertThat(lns)
                .isNotEmpty()
                .noneMatch(TAnyLN::isSetDataSet)
                .noneMatch(TAnyLN::isSetLogControl)
                .noneMatch(TAnyLN::isSetReportControl);
        assertIsMarshallable(scl);
    }

    private static TGSE getCommunicationGSE(SCL scd, String iedName, String cbName) {
        return new SclRootAdapter(scd).findConnectedApAdapter(iedName, "AP_NAME").orElseThrow()
                .getCurrentElem()
                .getGSE().stream()
                .filter(tgse -> cbName.equals(tgse.getCbName()))
                .findFirst().orElseThrow();
    }

    private static TSMV getCommunicationSMV(SCL scd, String iedName, String cbName) {
        return new SclRootAdapter(scd).findConnectedApAdapter(iedName, "AP_NAME").orElseThrow()
                .getCurrentElem()
                .getSMV().stream()
                .filter(tsmv -> cbName.equals(tsmv.getCbName()))
                .findFirst().orElseThrow();
    }

    private static CBCom createCbCom() {
        CBCom cbCom = new CBCom();
        cbCom.setMacRanges(new MacRanges());
        cbCom.setAppIdRanges(new AppIdRanges());
        cbCom.setVlans(new Vlans());
        cbCom.getMacRanges().getMacRange().add(newRange(TCBType.GOOSE, "01-0C-CD-01-00-00", "01-0C-CD-01-01-FF"));
        cbCom.getMacRanges().getMacRange().add(newRange(TCBType.SV, "01-0C-CD-04-00-00", "01-0C-CD-04-FF-FF"));
        cbCom.getAppIdRanges().getAppIdRange().add(newRange(TCBType.GOOSE, "0000", "4000"));
        cbCom.getAppIdRanges().getAppIdRange().add(newRange(TCBType.SV, "4000", "7FFF"));
        cbCom.getVlans().getVlan().addAll(List.of(
                newVlan(TCBType.GOOSE, TBayIntOrExt.BAY_INTERNAL, "301", "1"),
                newVlan(TCBType.GOOSE, TBayIntOrExt.BAY_EXTERNAL, "302", "2"),
                newVlan(TCBType.SV, TBayIntOrExt.BAY_INTERNAL, "303", "3"),
                newVlan(TCBType.SV, TBayIntOrExt.BAY_EXTERNAL, "304", "4")
        ));
        return cbCom;
    }

    private static TRange newRange(TCBType tcbType, String start, String end) {
        TRange macRangeGSE = new TRange();
        macRangeGSE.setCBType(tcbType);
        macRangeGSE.setStart(start);
        macRangeGSE.setEnd(end);
        return macRangeGSE;
    }

    private static TVlan newVlan(TCBType tcbType, TBayIntOrExt tBayIntOrExt, String vlanId, String vlanPriority) {
        TVlan gseVlan = new TVlan();
        gseVlan.setCBType(tcbType);
        gseVlan.setXY("01.00");
        gseVlan.setZW("009.001");
        gseVlan.setIEDType(TIEDType.BCU);
        gseVlan.setIEDRedundancy(TIEDRedundancy.A);
        gseVlan.setIEDSystemVersionInstance("1");
        gseVlan.setBayIntOrExt(tBayIntOrExt);
        gseVlan.setVlanId(vlanId);
        gseVlan.setVlanPriority(vlanPriority);
        gseVlan.setMinTime("10");
        gseVlan.setMaxTime("2000");
        return gseVlan;
    }

    /**
     * Help comparing TDurationInMilliSec
     */
    record SclDuration(String value, String unit, String multiplier) {
        static SclDuration from(TDurationInMilliSec tDurationInMilliSec) {
            return new SclDuration(tDurationInMilliSec.getValue().toString(), tDurationInMilliSec.getUnit(), tDurationInMilliSec.getMultiplier());
        }
    }
}
