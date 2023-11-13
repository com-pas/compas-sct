// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DataSetAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.testhelpers.FCDARecord;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.lfenergy.compas.scl2007b4.model.TFCEnum.ST;
import static org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.lfenergy.compas.sct.commons.util.ControlBlockEnum.*;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newDurationInMilliSec;

@ExtendWith(MockitoExtension.class)
class ControlBlockServiceTest {

    @InjectMocks
    ControlBlockService controlBlockService;

    private Set<FcdaForDataSetsCreation> allowedFcdas;

    private static final long GSE_APP_ID_MIN = 0x9;
    private static final long SMV_APP_ID_MIN = 0x400A;
    private static final String GSE_MAC_ADDRESS_PREFIX = "01-02-03-04-";
    private static final String SMV_MAC_ADDRESS_PREFIX = "0A-0B-0C-0D-";
    private static final NetworkRanges GSE_NETWORK_RANGES = new NetworkRanges(GSE_APP_ID_MIN, GSE_APP_ID_MIN + 10, GSE_MAC_ADDRESS_PREFIX + "00-FF", GSE_MAC_ADDRESS_PREFIX + "01-AA");
    private static final NetworkRanges SMV_NETWORK_RANGES = new NetworkRanges(SMV_APP_ID_MIN, SMV_APP_ID_MIN + 10, SMV_MAC_ADDRESS_PREFIX + "00-FF", SMV_MAC_ADDRESS_PREFIX + "01-AA");
    private static final RangesPerCbType RANGES_PER_CB_TYPE = new RangesPerCbType(GSE_NETWORK_RANGES, SMV_NETWORK_RANGES);


    @BeforeEach
    void init() {
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
        List<SclReportItem> sclReportItems = controlBlockService.analyzeDataGroups(scd);
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
        List<SclReportItem> sclReportItems = controlBlockService.analyzeDataGroups(scd);
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
        controlBlockService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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
        assertThatCode(() -> controlBlockService.createDataSetAndControlBlocks(scd, fcdaForDataSets))
                .isInstanceOf(ScdException.class)
                .hasMessage("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
    }

    @Test
    void createDataSetAndControlBlocks_should_create_DataSet() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
        assertThatCode(() -> controlBlockService.createDataSetAndControlBlocks(scd, "IED_NAME1", fcdaForDataSets))
                .isInstanceOf(ScdException.class)
                .hasMessage("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, "IED_NAME1", allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, "IED_NAME2", allowedFcdas);
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(streamAllDataSets(scd)).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideAllowedFcdaListEmptyOrNull")
    void createDataSetAndControlBlocks_with_targetIedName_and_targetLDeviceInst_should_Throw_Exception_when_list_allowed_fcda_not_initialized(String testName, Set<FcdaForDataSetsCreation> fcdaForDataSets) {
        // Given
        SCL scd = new SCL();
        // When Then
        assertThatCode(() -> controlBlockService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11", fcdaForDataSets))
                .isInstanceOf(ScdException.class)
                .hasMessage("Accepted FCDAs list is empty, you should initialize allowed FCDA lists with CsvHelper class before");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_provided_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, "IED_NAME1", "LD_INST11", allowedFcdas);
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_is_not_found_and_targetLDeviceInst_is_provided_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockService.createDataSetAndControlBlocks(scd, "non_existing_IED_name", "LD_INST11", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'non_existing_IED_name' not found in SCD");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetIedName_and_targetLDeviceInst_is_not_found_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockService.createDataSetAndControlBlocks(scd, "IED_NAME1", "non_existing_LDevice_inst", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("LDevice.inst 'non_existing_LDevice_inst' not found in IED 'IED_NAME1'");
    }

    @Test
    void createDataSetAndControlBlocks_when_targetLDeviceInst_is_provided_without_targetIedName_should_throw_exception() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        // When & Then
        assertThatThrownBy(() -> controlBlockService.createDataSetAndControlBlocks(scd, null, "LD_INST11", allowedFcdas))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name parameter is missing");
    }



    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_sort_FCDA_inside_DataSet_and_avoid_duplicates() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success_test_fcda_sort.xml");
        // When
        List<SclReportItem> sclReportItems = controlBlockService.createDataSetAndControlBlocks(scd, allowedFcdas);
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
        List<SclReportItem> sclReportItems = controlBlockService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, RANGES_PER_CB_TYPE);
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
        List<SclReportItem> sclReportItems = controlBlockService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, RANGES_PER_CB_TYPE);
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
        List<SclReportItem> sclReportItems = controlBlockService.configureNetworkForAllControlBlocks(scd, controlBlockNetworkSettings, rangesPerCbType);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems)
                .extracting(SclReportItem::message, SclReportItem::xpath)
                .contains(Tuple.tuple(expectedMessage,
                        "/SCL/IED[@name=\"IED_NAME2\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST21\"]/LN0/GSEControl[@name=\"CB_LD_INST21_GMI\"]"));
    }

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_controlBlocks_and_Dataset_on_ln0() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        controlBlockService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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
        controlBlockService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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

}
