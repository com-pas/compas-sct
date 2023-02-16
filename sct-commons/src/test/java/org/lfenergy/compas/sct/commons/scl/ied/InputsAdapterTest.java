// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.FCDARecord;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Named.named;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;

@ExtendWith(MockitoExtension.class)
class InputsAdapterTest {

    @Test
    void constructor_should_succeed() {
        // Given
        TInputs tInputs = new TInputs();
        LN0 ln0 = new LN0();
        ln0.setInputs(tInputs);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);
        // When && Then
        assertThatNoException().isThrownBy(() -> new InputsAdapter(ln0Adapter, tInputs));
    }

    @Test
    void elementXPath_should_succeed() {
        // Given
        TInputs tInputs = new TInputs();
        InputsAdapter inputsAdapter = new InputsAdapter(null, tInputs);
        // When
        String result = inputsAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Inputs");
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_report_Target_Ied_missing_Private_compasBay_errors() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_ied_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).containsExactly(
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]",
                "IED is missing Private/compas:Bay@UUID attribute")
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_report_Source_Ied_missing_Private_compasBay_errors() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_ied_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME3", "LD_INST31");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).containsExactly(
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME3\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST31\"]/LN0/Inputs/ExtRef[@desc=\"Source IED is " +
                    "missing compas:Bay @UUID\"]",
                "Source IED is missing Private/compas:Bay@UUID attribute")
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_report_ExtRef_attribute_missing() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_extref_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).containsExactlyInAnyOrder(
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]/LN0/Inputs/" +
                    "ExtRef[@desc=\"ExtRef is missing ServiceType attribute\"]",
                "The signal ExtRef is missing ServiceType attribute"),
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]/LN0/Inputs/" +
                    "ExtRef[@desc=\"ExtRef is ServiceType Poll\"]",
                "The signal ExtRef ServiceType attribute is unexpected : POLL"),
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]/LN0/Inputs/" +
                    "ExtRef[@desc=\"ExtRef is ServiceType Report with malformed desc attribute\"]",
                "ExtRef.serviceType=Report but ExtRef.desc attribute is malformed")
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).isEmpty();
        System.out.println(MarshallerWrapper.marshall(scd));
    }

    @ParameterizedTest
    @MethodSource("provideCreateFCDA")
    void updateAllSourceDataSetsAndControlBlocks_should_create_dataset_and_fcda_for_valid_extRef(String extRefDesc, String dataSetPath,
                                                                                                 List<FCDARecord> expectedFcda) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = keepOnlyThisExtRef(sclRootAdapter, extRefDesc);
        String[] splitPath = dataSetPath.split("/");
        final int IED_NAME_PART = 0;
        final int LDEVICE_INST_PART = 1;
        final int DATASET_NAME_PART = 2;
        String expectedSourceIedName = splitPath[IED_NAME_PART];
        String expectedSourceLDeviceInst = splitPath[LDEVICE_INST_PART];
        String expectedDataSetName = splitPath[DATASET_NAME_PART];
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).isEmpty();
        DataSetAdapter dataSet = findDataSet(sclRootAdapter, expectedSourceIedName, expectedSourceLDeviceInst, expectedDataSetName);
        assertThat(dataSet.getCurrentElem().getFCDA())
            .extracting(TFCDA::getLdInst)
            .containsOnly(expectedSourceLDeviceInst);

        assertThat(dataSet.getCurrentElem().getFCDA())
            .map(FCDARecord::toFCDARecord)
            .containsExactly(expectedFcda.toArray(new FCDARecord[]{}));
    }

    public static Stream<Arguments> provideCreateFCDA() {
        return Stream.of(
            Arguments.of(named("should include signal internal to a Bay",
                    "test bay internal"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GSI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameST", TFCEnum.ST))),
            Arguments.of(named("should include signal external to a Bay",
                    "test bay external"),
                "IED_NAME3/LD_INST31/DS_LD_INST31_GSE",
                List.of(new FCDARecord("LD_INST31", "ANCR", "1", "", "DoName", "daNameST", TFCEnum.ST))),
            Arguments.of(named("keep source DA with fc = ST",
                    "test daName ST"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GSI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameST", TFCEnum.ST))),
            Arguments.of(named("keep source DA with fc = MX",
                    "test daName MX"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GMI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameMX", TFCEnum.MX))),
            Arguments.of(named("for GOOSE, should keep only valid fcda candidates",
                    "test ServiceType is GOOSE, no daName and DO contains ST and MX, but only ST is FCDA candidate"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GSI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "OtherDoName", "daNameST", TFCEnum.ST))),
            Arguments.of(named("for SMV, should keep only valid fcda candidates",
                    "test ServiceType is SMV, no daName and DO contains ST and MX, but only ST is FCDA candidate"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_SVI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "OtherDoName", "daNameST", TFCEnum.ST))),
            Arguments.of(named("for Report, should get source daName from ExtRef.desc to deduce FC ST",
                    "test ServiceType is Report_daReportST_1"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_DQCI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", null, TFCEnum.ST))),
            Arguments.of(named("for Report, should get source daName from ExtRef.desc to deduce FC MX",
                    "test ServiceType is Report_daReportMX_1"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_CYCI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", null, TFCEnum.MX))),
            Arguments.of(named("should ignore instance number when checking FCDA Candidate file",
                    "test no daName and doName with instance number"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GSI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoWithInst1", "daNameST", TFCEnum.ST))),
            Arguments.of(named("should ignore instance number when checking FCDA Candidate file (DO with SDO)",
                    "test no daName and doName with instance number and SDO"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GSI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoWithInst2.subDo", "daNameST", TFCEnum.ST))),
            Arguments.of(named("hould include UNTESTED FlowStatus",
                    "test include compas:Flow.FlowStatus UNTESTED"),
                "IED_NAME2/LD_INST21/DS_LD_INST21_GSI",
                List.of(new FCDARecord("LD_INST21", "ANCR", "1", "", "DoName", "daNameST", TFCEnum.ST)))
        );
    }

    @ParameterizedTest
    @MethodSource("provideDoNotCreateFCDA")
    void updateAllSourceDataSetsAndControlBlocks_should_not_create_FCDA_when_no_valid_source_Da_found(String extRefDesc) {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = keepOnlyThisExtRef(sclRootAdapter, extRefDesc);
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(LDeviceAdapter::hasLN0)
            .map(LDeviceAdapter::getLN0Adapter))
            .allMatch(ln0Adapter -> !ln0Adapter.getCurrentElem().isSetDataSet());
    }

    public static Stream<Arguments> provideDoNotCreateFCDA() {
        return Stream.of(
            Arguments.of(named("should not create FCDA for source Da different from MX and ST",
                "test daName BL")),
            Arguments.of(named("should not create FCDA for extref with a binding internal to the IED",
                "test ignore internal binding")),
            Arguments.of(named("should not create FCDA for extref with missing binding attributes",
                "test ignore missing bindings attributes")),
            Arguments.of(named("should not create FCDA for ExtRef with compas:Flow.FlowStatus INACTIVE",
                "test ignore when compas:Flow.FlowStatus is neither ACTIVE nor UNTESTED"))
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_when_AceessPoint_does_not_have_dataset_creation_capability_should_report_error() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = keepOnlyThisExtRef(sclRootAdapter, "test bay internal");
        TExtRef extRef = inputsAdapter.getCurrentElem().getExtRef().get(0);
        LDeviceAdapter sourceLDevice = findLDevice(sclRootAdapter, extRef.getIedName(), extRef.getLdInst());
        sourceLDevice.getAccessPoint().setServices(new TServices());
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).hasSize(1)
            .first().extracting(SclReportItem::getMessage).asString()
            .startsWith("Could not create DataSet or ControlBlock for this ExtRef : IED/AccessPoint does not have capability to create DataSet of type GSE");
    }

    private static InputsAdapter keepOnlyThisExtRef(SclRootAdapter sclRootAdapter, String extRefDesc) {
        InputsAdapter foundInputsAdapter = sclRootAdapter.streamIEDAdapters()
            .flatMap(IEDAdapter::streamLDeviceAdapters)
            .filter(LDeviceAdapter::hasLN0)
            .map(LDeviceAdapter::getLN0Adapter)
            .filter(AbstractLNAdapter::hasInputs)
            .map(LN0Adapter::getInputsAdapter)
            .filter(inputsAdapter ->
                inputsAdapter.getCurrentElem().getExtRef().stream().map(TExtRef::getDesc).anyMatch(extRefDesc::equals))
            .findFirst()
            .orElseThrow(() -> new AssertionFailedError("ExtRef not found: " + extRefDesc));
        foundInputsAdapter.getCurrentElem().getExtRef().removeIf(Predicate.not(extref -> extRefDesc.equals(extref.getDesc())));
        return foundInputsAdapter;
    }
   /* @Test
    void checkSourceDataGroupCoherence_should_fail_one_error_messages() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_coherent_extRefs.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = keepOnlyThisExtRef(sclRootAdapter, "a");
        //When
        List<SclReportItem> sclReportItems = inputsAdapter.checkSourceDataGroupCoherence();
        //Then
        assertThat(sclReportItems).hasSize(1)
                .extracting(SclReportItem::getMessage)
                .containsExactlyInAnyOrder("The Client IED IED_NAME1 subscribes to much GOOSE Control Blocks.");
    }

    @Test
    void checkSourceDataGroupCoherence_should_succed_no_error_message() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_coherent_extRefs.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = keepOnlyThisExtRef(sclRootAdapter, "a");
        sclRootAdapter.getIEDAdapterByName("IED_NAME1").getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxGOOSE(1L);
        //When
        List<SclReportItem> sclReportItems = inputsAdapter.checkSourceDataGroupCoherence();
        //Then
        assertThat(sclReportItems).isEmpty();
    }*/
}
