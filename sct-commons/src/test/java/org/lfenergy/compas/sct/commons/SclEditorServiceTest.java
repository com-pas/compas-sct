// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.LD_SUIED;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.getDAIAdapters;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_SCL_FILE_TYPE;

@ExtendWith(MockitoExtension.class)
class SclEditorServiceTest {

    @InjectMocks
    SclEditorService sclEditorService;

    private static Stream<Arguments> sclProviderMissingRequiredObjects() {
        SCL scl1 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_KO_MissingBeh.scd");
        SCL scl2 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_KO_MissingLDevicePrivate.scd");
        SCL scl3 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_KO_MissingLDevicePrivateAttribute.scd");
        SCL scl4 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_KO_MissingMod.scd");
        Tuple[] scl1Errors = new Tuple[]{Tuple.tuple("The LDevice doesn't have a DO @name='Beh' OR its associated DA@fc='ST' AND DA@name='stVal'",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0")};
        Tuple[] scl2Errors = new Tuple[]{Tuple.tuple("The LDevice doesn't have a Private compas:LDevice.",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0")};
        Tuple[] scl3Errors = new Tuple[]{Tuple.tuple("The Private compas:LDevice doesn't have the attribute 'LDeviceStatus'",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0")};
        Tuple[] scl4Errors = new Tuple[]{Tuple.tuple("The LDevice doesn't have a DO @name='Mod'",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0")};
        return Stream.of(
                Arguments.of("MissingDOBeh", scl1, scl1Errors),
                Arguments.of("MissingLDevicePrivate", scl2, scl2Errors),
                Arguments.of("MissingLDevicePrivateAttribute", scl3, scl3Errors),
                Arguments.of("MissingDOMod", scl4, scl4Errors)
        );
    }

    private static Stream<Arguments> sclProviderBasedLDeviceStatus() {
        SCL scl1 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_LD_STATUS_ACTIVE.scd");
        SCL scl2 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_LD_STATUS_UNTESTED.scd");
        SCL scl3 = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test1_LD_STATUS_INACTIVE.scd");
        Tuple[] scl1Errors = new Tuple[]{Tuple.tuple("The LDevice cannot be set to 'off' but has not been selected into SSD.",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                Tuple.tuple("The LDevice cannot be set to 'on' but has been selected into SSD.",
                        "/SCL/IED[@name=\"IedName2\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                Tuple.tuple("The LDevice cannot be activated or desactivated because its BehaviourKind Enum contains NOT 'on' AND NOT 'off'.",
                        "/SCL/IED[@name=\"IedName3\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"
                )};
        Tuple[] scl2Errors = new Tuple[]{Tuple.tuple("The LDevice cannot be set to 'off' but has not been selected into SSD.",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                Tuple.tuple("The LDevice cannot be set to 'on' but has been selected into SSD.",
                        "/SCL/IED[@name=\"IedName2\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                Tuple.tuple("The LDevice cannot be activated or desactivated because its BehaviourKind Enum contains NOT 'on' AND NOT 'off'.",
                        "/SCL/IED[@name=\"IedName3\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"
                )};
        Tuple[] scl3Errors = new Tuple[]{Tuple.tuple("The LDevice is not qualified into STD but has been selected into SSD.",
                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                Tuple.tuple("The LDevice cannot be set to 'on' but has been selected into SSD.",
                        "/SCL/IED[@name=\"IedName2\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                Tuple.tuple("The LDevice cannot be activated or desactivated because its BehaviourKind Enum contains NOT 'on' AND NOT 'off'.",
                        "/SCL/IED[@name=\"IedName3\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"
                )};
        return Stream.of(
                Arguments.of("ACTIVE", scl1, scl1Errors),
                Arguments.of("UNTESTED", scl2, scl2Errors),
                Arguments.of("INACTIVE", scl3, scl3Errors)
        );
    }

    @Test
    void testAddHistoryItem() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();

        sclEditorService.addHistoryItem(scd, "who", "what", "why");

        assertNotNull(scd.getHeader());
        THeader.History history = scd.getHeader().getHistory();
        assertNotNull(history);
        assertEquals(1, history.getHitem().size());
        THitem tHitem = history.getHitem().get(0);
        assertEquals("who", tHitem.getWho());
        assertEquals("what", tHitem.getWhat());
        assertEquals("why", tHitem.getWhy());
        assertEquals(SclRootAdapter.REVISION, tHitem.getRevision());
        assertEquals(SclRootAdapter.VERSION, tHitem.getVersion());
        assertIsMarshallable(scd);
    }

    @Test
    void testAddIED() {

        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> sclEditorService.addIED(scd, "IED_NAME1", icd));
        assertEquals("IED_NAME1", scd.getIED().get(0).getName());
        assertNotNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());

        assertIsMarshallable(scd);
    }

    @Test
    void testAddSubnetworks() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> sclEditorService.addIED(scd, "IED_NAME1", icd));

        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO("IED_NAME1", "AP_NAME");
        subNetworkDTO.addConnectedAP(connectedApDTO);

        assertDoesNotThrow(() -> sclEditorService.addSubnetworks(scd, List.of(subNetworkDTO), icd));
        assertIsMarshallable(scd);
    }

    @Test
    void testAddSubnetworksWithoutCommunicationTagInIcd() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> sclEditorService.addIED(scd, "IED_NAME1", icd));

        assertDoesNotThrow(() -> sclEditorService.addSubnetworks(scd, List.of(), icd));
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).doesNotContain("<Communication");
    }

    @Test
    void testInitScl_With_headerId_shouldNotThrowError() {
        UUID headerId = UUID.randomUUID();
        SCL scd = assertDoesNotThrow(() -> sclEditorService.initScl(headerId, "hVersion", "hRevision"));
        assertIsMarshallable(scd);
    }

    @Test
    void initScl_should_create_Private_SCL_FILETYPE() {
        // Given
        UUID headerId = UUID.randomUUID();
        // When Then
        SCL scd = assertDoesNotThrow(() -> sclEditorService.initScl(headerId, "hVersion", "hRevision"));
        assertThat(scd.getPrivate()).isNotEmpty();
        assertThat(scd.getPrivate().get(0).getType()).isEqualTo(COMPAS_SCL_FILE_TYPE.getPrivateType());
        assertIsMarshallable(scd);
    }

    @Test
    void updateHeader_should_update_header_tag() {
        //Given
        SCL scd = assertDoesNotThrow(() -> sclEditorService.initScl(UUID.randomUUID(), "hVersion", "hRevision"));
        UUID hId = UUID.fromString(scd.getHeader().getId());
        HeaderDTO headerDTO = DTO.createHeaderDTO(hId);
        //When
        sclEditorService.updateHeader(scd, headerDTO);
        //Then
        assertIsMarshallable(scd);
    }

    @Test
    void testUpdateDAI() {
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("unknownID");
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");

        assertThrows(ScdException.class, () -> sclEditorService.updateDAI(
                scd, "IED", "LD", dataAttributeRef
        ));
        dataAttributeRef.setLnType("LNO1");
        dataAttributeRef.setLnClass(TLLN0Enum.LLN_0.value());
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        dataAttributeRef.setDoName(doTypeName);
        dataAttributeRef.setDaName(new DaTypeName("antRef.bda1.bda2.bda3"));
        TVal tVal = new TVal();
        tVal.setValue("newValue");
        dataAttributeRef.setDaiValues(List.of(tVal));
        assertDoesNotThrow(() -> sclEditorService.updateDAI(scd, "IED_NAME", "LD_INS1", dataAttributeRef));
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");

        assertDoesNotThrow(() -> sclEditorService.importSTDElementsInSCD(scd, List.of(std), DTO.SUB_NETWORK_TYPES));
        assertThat(scd.getIED()).hasSize(1);
        assertThat(scd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_with_Multiple_STD() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_lnode_with_many_compas_icdheader.xml");
        SCL std0 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU2.xml");

        assertDoesNotThrow(() -> sclEditorService.importSTDElementsInSCD(scd, List.of(std0, std1, std2), DTO.SUB_NETWORK_TYPES));
        assertThat(scd.getIED()).hasSize(3);
        assertThat(scd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertThat(scd.getCommunication().getSubNetwork().get(0).getConnectedAP()).hasSizeBetween(1, 3);
        assertThat(scd.getCommunication().getSubNetwork().get(1).getConnectedAP()).hasSizeBetween(1, 3);
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_Several_STD_Match_Compas_ICDHeader() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        List<SCL> stds = List.of(std, std1);
        assertThrows(ScdException.class, () -> sclEditorService.importSTDElementsInSCD(scd, stds, DTO.SUB_NETWORK_TYPES));
        assertIsMarshallable(scd);
    }

    @Test
    void test_importSTDElementsInSCD_should_not_throw_exception_when_SCD_file_contains_same_ICDHeader_in_two_different_functions() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_with_same_compas_icd_header_in_different_functions.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        assertDoesNotThrow(() -> sclEditorService.importSTDElementsInSCD(scd, List.of(std), DTO.SUB_NETWORK_TYPES));
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_Compas_ICDHeader_Not_Match() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_with_same_ICDSystemVersionUUID.xml");
        List<SCL> stdList = List.of(std);
        //When Then
        assertThrows(ScdException.class, () -> sclEditorService.importSTDElementsInSCD(scd, stdList, DTO.SUB_NETWORK_TYPES));
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_No_STD_Match() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        List<SCL> stdList = List.of();
        //When Then
        assertThrows(ScdException.class, () -> sclEditorService.importSTDElementsInSCD(scd, stdList, DTO.SUB_NETWORK_TYPES));
        assertIsMarshallable(scd);
    }

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_controlBlocks_and_Dataset_on_ln0() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        sclEditorService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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
        sclEditorService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_srcXXX_attributes_on_ExtRef() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        sclEditorService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("sclProviderMissingRequiredObjects")
    void updateLDeviceStatus_shouldReturnReportWithError_MissingRequiredObject(String testCase, SCL scl, Tuple... errors) {
        // Given
        assertTrue(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").isPresent());
        assertEquals("off", getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue());
        String before = MarshallerWrapper.marshall(scl);
        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateLDeviceStatus(scl);
        // Then
        String after = MarshallerWrapper.marshall(scl);
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems)
                .hasSize(1)
                .extracting(SclReportItem::message, SclReportItem::xpath)
                .containsExactly(errors);
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(before).isEqualTo(after);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sclProviderBasedLDeviceStatus")
    void updateLDeviceStatus_shouldReturnReportWithError_WhenLDeviceStatusActiveOrUntestedOrInactive(String testCase, SCL scl, Tuple... errors) {
        // Given
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED")).isEmpty();
        String before = MarshallerWrapper.marshall(scl);
        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateLDeviceStatus(scl);
        // Then
        String after = MarshallerWrapper.marshall(scl);
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems)
                .hasSize(3)
                .extracting(SclReportItem::message, SclReportItem::xpath)
                .containsExactly(errors);
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED")).isEmpty();
        assertThat(before).isEqualTo(after);
    }

    @Test
    void updateLDeviceStatus_shouldReturnReportWithError_WhenAllLDeviceInactive_Test2() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test2_LD_STATUS_INACTIVE.scd");
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED")).isEmpty();
        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateLDeviceStatus(scl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isFalse();
        assertThat(sclReportItems)
                .hasSize(2)
                .extracting(SclReportItem::message, SclReportItem::xpath)
                .containsExactly(Tuple.tuple("The LDevice cannot be set to 'off' but has not been selected into SSD.",
                                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                        Tuple.tuple("The LDevice is not qualified into STD but has been selected into SSD.",
                                "/SCL/IED[@name=\"IedName2\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"));
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED").get().getValue()).isEqualTo("off");
    }


    @Test
    void updateLDeviceStatus_shouldReturnUpdatedFile() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_Template.scd");
        assertThat(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(givenScl, "IedName3", "LDSUIED")).isEmpty();

        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateLDeviceStatus(givenScl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(givenScl, "IedName3", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(givenScl, "IedName3", "LDSUIED").get().getValue()).isEqualTo("off");
    }

    @Test
    void updateLDeviceStatus_shouldReturnUpdatedFile_when_DAI_Mod_DO_stVal_whatever_it_is_updatable_or_not() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue_165_enhance_68_Test_Dai_Updatable.scd");
        assertThat(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("off");
        assertThat(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("on");
        assertThat(getLDeviceStatusValue(givenScl, "IedName3", "LDSUIED"))
                .map(TVal::getValue)
                .isNotPresent();
        assertThat(getLDeviceStatusValue(givenScl, "IedName4", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("on");
        assertThat(getLDeviceStatusValue(givenScl, "IedName5", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("on");
        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateLDeviceStatus(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("on");

        assertThat(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("off");

        assertThat(getLDeviceStatusValue(givenScl, "IedName3", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("off");

        assertThat(getLDeviceStatusValue(givenScl, "IedName4", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("off");

        assertThat(getLDeviceStatusValue(givenScl, "IedName5", "LDSUIED"))
                .map(TVal::getValue)
                .hasValue("off");
    }

    private Optional<TVal> getLDeviceStatusValue(SCL scl, String iedName, String ldInst) {
        return getValFromDaiName(scl, iedName, ldInst, "Mod", "stVal");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Test update setSrcRef Value,LD_WITH_1_InRef,InRef2,setSrcRef,IED_NAME1LD_WITH_1_InRef/PRANCR1.Do11.sdo11",
            "Test update setSrcCB Value,LD_WITH_1_InRef,InRef2,setSrcCB,OLD_VAL",
            "Test update setSrcRef Value,LD_WITH_3_InRef,InRef3,setSrcRef,IED_NAME1LD_WITH_3_InRef/PRANCR1.Do11.sdo11",
            "Test update setSrcCB Value,LD_WITH_3_InRef,InRef3,setSrcCB,IED_NAME1LD_WITH_3_InRef/prefixANCR1.GSE1",
            "Test update setTstRef Value,LD_WITH_3_InRef,InRef3,setTstRef,IED_NAME1LD_WITH_3_InRef/PRANCR1.Do11.sdo11",
            "Test update setTstCB Value,LD_WITH_3_InRef,InRef3,setTstCB,IED_NAME1LD_WITH_3_InRef/prefixANCR3.GSE3"
    })
    void updateDoInRef_shouldReturnUpdatedFile(String testName, String ldInst, String doName, String daName, String expected) {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_issue_231_test_ok.xml");

        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateDoInRef(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        SclTestMarshaller.assertIsMarshallable(givenScl);
        assertThat(getValFromDaiName(givenScl, "IED_NAME1", ldInst, doName, daName)
                .map(TVal::getValue))
                .hasValue(expected);
        assertIsMarshallable(givenScl);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Test with only 1 ExtRef should not update srcTstCB,LD_WITH_1_InRef,InRef2,setTstRef",
            "Test with only 1 ExtRef should not update setTstCB Value,LD_WITH_1_InRef,InRef2,setTstCB",
            "Test with only 1 ExtRef should not update DO when IedName not present,LD_WITH_1_InRef_ExtRef_Without_IedName,InRef4,setSrcRef",
            "Test with only 1 ExtRef should not update DO when LdInst not present,LD_WITH_1_InRef_ExtRef_Without_LdInst,InRef5,setSrcRef",
            "Test with only 1 ExtRef should not update DO when lnClass not present,LD_WITH_1_InRef_ExtRef_Without_LnClass,InRef6,setSrcRef"
    })
    void updateDoInRef_should_not_update_DAI(String testName, String ldInst, String doName, String daName) {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_issue_231_test_ok.xml");

        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateDoInRef(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(getValFromDaiName(givenScl, "IED_NAME1", ldInst, doName, daName)).isNotPresent();
    }

    @Test
    void updateDoInRef_shouldReturnReportWithError_when_ExtRef_not_coherent() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_issue_231_test_ko.xml");

        // When
        List<SclReportItem> sclReportItems = sclEditorService.updateDoInRef(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(sclReportItems).hasSize(4);
    }

    private Optional<TVal> getValFromDaiName(SCL scl, String iedName, String ldInst, String doiName, String daiName) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        Optional<LDeviceAdapter> lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst(ldInst);
        LN0Adapter ln0Adapter = lDeviceAdapter.get().getLN0Adapter();
        Optional<DOIAdapter> doiAdapter = ln0Adapter.getDOIAdapters().stream()
                .filter(doiAdapter1 -> doiAdapter1.getCurrentElem().getName().equals(doiName))
                .findFirst();
        return doiAdapter.flatMap(adapter -> adapter.getCurrentElem().getSDIOrDAI().stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals(daiName) && !tdai.getVal().isEmpty())
                .map(tdai -> tdai.getVal().get(0))
                .findFirst());
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
        List<SclReportItem> sclReportItems = sclEditorService.analyzeDataGroups(scd);
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
        List<SclReportItem> sclReportItems = sclEditorService.analyzeDataGroups(scd);
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
    void manageMonitoringLns_should_update_and_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        // When
        List<SclReportItem> sclReportItems = sclEditorService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        LDeviceAdapter lDeviceAdapter = new SclRootAdapter(scd).getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_SUIED);
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(4)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "1"), Tuple.tuple("LGOS", "2"),
                        Tuple.tuple("LSVS", "1"), Tuple.tuple("LSVS", "2"));
        SclTestMarshaller.assertIsMarshallable(scd);
    }

    @Test
    void manageMonitoringLns_should_not_update_and_not_create_lsvs_and_goose_when_no_extRef() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter11 = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LD_INST11");
        lDeviceAdapter11.getLN0Adapter().getCurrentElem().setInputs(null);
        LDeviceAdapter lDeviceAdapter21 = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LD_INST21");
        lDeviceAdapter21.getLN0Adapter().getCurrentElem().setInputs(null);
        // When
        List<SclReportItem> sclReportItems = sclEditorService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_SUIED);
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(2)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "3"), Tuple.tuple("LSVS", "9"));
        SclTestMarshaller.assertIsMarshallable(scd);
    }

    @Test
    void manageMonitoringLns_should_not_update_and_not_create_lsvs_and_goose_when_dai_not_updatable() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_SUIED);
        getDAIAdapters(lDeviceAdapter, "LGOS", "GoCBRef", "setSrcRef")
                .forEach(daiAdapter -> daiAdapter.getCurrentElem().setValImport(false));
        getDAIAdapters(lDeviceAdapter, "LSVS", "SvCBRef", "setSrcRef")
                .forEach(daiAdapter -> daiAdapter.getCurrentElem().setValImport(false));
        // When
        List<SclReportItem> sclReportItems = sclEditorService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems)
                .isNotEmpty()
                .hasSize(2)
                .extracting(SclReportItem::message)
                .containsExactly("The DAI cannot be updated", "The DAI cannot be updated");
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(2)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "3"), Tuple.tuple("LSVS", "9"));
        SclTestMarshaller.assertIsMarshallable(scd);
    }

}
