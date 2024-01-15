// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Tag;
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
import org.lfenergy.compas.sct.commons.scl.ied.DOIAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.LD_SUIED;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.getDAIAdapters;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_SCL_FILE_TYPE;

@ExtendWith(MockitoExtension.class)
class SclServiceTest {

    @InjectMocks
    SclService sclService;

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
    void addHistoryItem_should_add_history_elements() throws ScdException {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        //When
        sclService.addHistoryItem(scd, "who", "what", "why");
        //Then
        assertThat(scd.getHeader()).isNotNull();
        THeader.History history = scd.getHeader().getHistory();
        assertThat(history).isNotNull();
        assertThat(history.getHitem()).hasSize(1);
        THitem tHitem = history.getHitem().get(0);
        assertThat(tHitem.getWho()).isEqualTo("who");
        assertThat(tHitem.getWhat()).isEqualTo("what");
        assertThat(tHitem.getWhy()).isEqualTo("why");
        assertThat(tHitem.getRevision()).isEqualTo(SclRootAdapter.REVISION);
        assertThat(tHitem.getVersion()).isEqualTo(SclRootAdapter.VERSION);
        assertIsMarshallable(scd);
    }

    @Test
    void addIED_should_add_ied_element() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //When Then
        assertDoesNotThrow(() -> sclService.addIED(scd, "IED_NAME1", icd));
        assertThat(scd.getIED().size()).isNotZero();
        assertThat(scd.getIED().get(0).getName()).isEqualTo("IED_NAME1");
        assertThat(scd.getDataTypeTemplates()).isNotNull();
        assertIsMarshallable(scd);
    }

    @Test
    @Tag("issue-321")
    void addSubnetworks_should_add_subnetwork() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //When Then
        assertThatCode(() -> sclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO("IED_NAME1","AP_NAME");
        subNetworkDTO.addConnectedAP(connectedApDTO);
        //When Then
        assertThatCode(() -> sclService.addSubnetworks(scd, List.of(subNetworkDTO), icd)).doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    @Tag("issue-321")
    void addSubnetworks_whenNoCommunicationTagInIcd_should_not_add_subnetwork() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //When Then
        assertThatCode(() -> sclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> sclService.addSubnetworks(scd, List.of(), icd)).doesNotThrowAnyException();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).doesNotContain("<Communication");
    }

    @Test
    void addSubnetworks_shouldNotUpdateScd_when_noCommunicationInICDExist() {
        //Givens
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/scd_without_communication.xml");
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/std_without_communication.xml");
        assertThat(scd.getCommunication()).isNull();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, icd, "IED_NAME1")).doesNotThrowAnyException();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(scd.getCommunication()).isNull();
        assertThat(marshalledScd).doesNotContain("<Communication");
        assertIsMarshallable(scd);
    }

    @Test
    void addSubnetworks_shouldAddSubNetwork_and_ConnectedAp_and_updateConnectedApIEDName() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/scd_without_communication.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scl_update_communication/std_with_communication.xml");
        assertThat(scd.getCommunication()).isNull();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, std, "IED_NAME1")).doesNotThrowAnyException();
        assertThat(scd.getCommunication()).isNotNull();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).contains("<Communication");
        // assertion succeeds as subNetwork.connectedAP.iedName field is ignored.
        assertThat(scd.getCommunication())
                .usingRecursiveComparison()
                .ignoringFields("subNetwork.connectedAP.iedName")
                .isEqualTo(std.getCommunication());
        assertThat(scd.getCommunication().getSubNetwork().get(0).getConnectedAP().get(0).getIedName())
                .isEqualTo("IED_NAME1");
        assertIsMarshallable(scd);
    }


    @Test
    void addSubnetworks_shouldCopyAddressAndPhysConnFromIcd() {
        //Givens
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/scd_without_communication.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scl_update_communication/std_with_full_filled_communication.xml");
        assertThat(scd.getCommunication()).isNull();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, std, "IED_NAME1")).doesNotThrowAnyException();
        assertThat(scd.getCommunication()).isNotNull();
        // assertion succeeds as subNetwork.connectedAP.iedName and subNetwork.connectedAP.gse fields are ignored.
        // Only subNetwork.connectedAP.address and subNetwork.connectedAP.physConn added: see https://github.com/com-pas/compas-sct/issues/76
        assertThat(scd.getCommunication())
                .usingRecursiveComparison()
                .ignoringFields("subNetwork.connectedAP.iedName", "subNetwork.connectedAP.gse")
                .isEqualTo(std.getCommunication());
        assertIsMarshallable(scd);
    }

    @Test
    void addSubnetworks_shouldDoNothing_when_subNetworkAlreadyExist() {
        //Givens
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/scd_without_communication.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scl_update_communication/std_with_communication.xml");
        std.getCommunication().getSubNetwork().get(0).getConnectedAP().get(0).getPhysConn().clear();
        std.getCommunication().getSubNetwork().get(0).getConnectedAP().get(0).setAddress(null);
        std.getCommunication().getSubNetwork().get(0).getConnectedAP().get(0).unsetGSE();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, std, "IED_NAME1")).doesNotThrowAnyException();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).contains("<Communication");
    }

    @Test
    void addSubnetworks_shouldThrowError_When_IedNameNotExistInScd() {
        //Givens
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/scd_without_communication.xml");
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl_update_communication/std_with_communication.xml");
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, icd, "UnknownIedName"))
                .isInstanceOf(ScdException.class)
                .hasMessage("Unknown AccessPoint :ConnectedAP_Name in IED :UnknownIedName");
        assertIsMarshallable(scd);
    }

    @Test
    void testInitScl_With_headerId_shouldNotThrowError() {
        //Given
        UUID headerId = UUID.randomUUID();
        //When
        SCL scd = assertDoesNotThrow(() -> sclService.initScl(headerId, "hVersion", "hRevision"));
        //Then
        assertIsMarshallable(scd);
    }

    @Test
    void initScl_should_create_Private_SCL_FILETYPE() {
        // Given
        UUID headerId = UUID.randomUUID();
        // When Then
        SCL scd = assertDoesNotThrow(() -> sclService.initScl(headerId, "hVersion", "hRevision"));
        assertThat(scd.getPrivate()).isNotEmpty();
        assertThat(scd.getPrivate().get(0).getType()).isEqualTo(COMPAS_SCL_FILE_TYPE.getPrivateType());
        assertIsMarshallable(scd);
    }

    @Test
    @Tag("issue-321")
    void updateHeader_should_update_header_tag() {
        //When Then
        SCL scd = assertDoesNotThrow(() -> sclService.initScl(UUID.randomUUID(), "hVersion", "hRevision"));
        //Given
        UUID headerId = UUID.fromString(scd.getHeader().getId());
        HeaderDTO headerDTO = DTO.createHeaderDTO(headerId);
        //When
        sclService.updateHeader(scd, headerDTO);
        //Then
        assertIsMarshallable(scd);
    }

    @Test
    @Tag("issue-321")
    void updateDAI_should_not_throw_error() {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("unknownID");
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        //When Then
        assertThatThrownBy(() -> sclService.updateDAI(scd, "IED", "LD", dataAttributeRef))
                .isInstanceOf(ScdException.class);
        //Given
        dataAttributeRef.setLnType("LNO1");
        dataAttributeRef.setLnClass(TLLN0Enum.LLN_0.value());
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        dataAttributeRef.setDoName(doTypeName);
        dataAttributeRef.setDaName(new DaTypeName("antRef.bda1.bda2.bda3"));
        TVal tVal = new TVal();
        tVal.setValue("newValue");
        dataAttributeRef.setDaiValues(List.of(tVal));
        //When Then
        assertThatCode(() -> sclService.updateDAI(scd, "IED_NAME", "LD_INS1", dataAttributeRef))
                .doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_whenCalledWithOneSTD_shouldNotThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, List.of(std)))
                .doesNotThrowAnyException();
        assertThat(scd.getIED()).hasSize(1);
        assertThat(scd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_whenCalledWithMultipleSTD_shouldNotThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_lnode_with_many_compas_icdheader.xml");
        SCL std0 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU2.xml");
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, List.of(std0, std1, std2)))
                .doesNotThrowAnyException();
        assertThat(scd.getIED()).hasSize(3);
        assertThat(scd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertThat(scd.getCommunication().getSubNetwork().get(0).getConnectedAP()).hasSizeBetween(1, 3);
        assertThat(scd.getCommunication().getSubNetwork().get(1).getConnectedAP()).hasSizeBetween(1, 3);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_whenManySTDMatchCompasICDHeader_shouldThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        List<SCL> stds = List.of(std1, std2);
        //When Then
        assertThatThrownBy(() -> sclService.importSTDElementsInSCD(scd, stds))
                .isInstanceOf(ScdException.class);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_whenSCDFileContainsSameICDHeaderInTwoDifferentFunctions_should_not_throw_exception() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_with_same_compas_icd_header_in_different_functions.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, List.of(std))).doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_whenCompasICDHeaderNotMatch__shouldThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_with_same_ICDSystemVersionUUID.xml");
        List<SCL> stdList = List.of(std);
        //When Then
        assertThatThrownBy(() -> sclService.importSTDElementsInSCD(scd, stdList))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("COMPAS-ICDHeader is not the same in Substation and in IED");
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_whenNoSTDMatch_shouldThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        List<SCL> stdList = List.of();
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, stdList))
                .isInstanceOf(ScdException.class)
                .hasMessage("There is no STD file found corresponding to headerId = f8dbc8c1-2db7-4652-a9d6-0b414bdeccfa, headerVersion = 01.00.00, headerRevision = 01.00.00 and ICDSystemVersionUUID = IED4d4fe1a8cda64cf88a5ee4176a1a0eef");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sclProviderMissingRequiredObjects")
    void updateLDeviceStatus_whenMissingRequiredObject_shouldReturnReportWithError(String testCase, SCL scl, Tuple... errors) {
        // Given
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        String before = MarshallerWrapper.marshall(scl);
        // When
        List<SclReportItem> sclReportItems = sclService.updateLDeviceStatus(scl);
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
    void updateLDeviceStatus_whenLDeviceStatusActiveOrUntestedOrInactive_shouldReturnReportWithError(String testCase, SCL scl, Tuple... errors) {
        // Given
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED")).isEmpty();
        String before = MarshallerWrapper.marshall(scl);
        // When
        List<SclReportItem> sclReportItems = sclService.updateLDeviceStatus(scl);
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
    void updateLDeviceStatus_whenAllLDeviceInactive_shouldReturnReportWithError() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test2_LD_STATUS_INACTIVE.scd");
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        assertThat(getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue()).isEqualTo("on");
        assertThat(getLDeviceStatusValue(scl, "IedName3", "LDSUIED")).isEmpty();
        // When
        List<SclReportItem> sclReportItems = sclService.updateLDeviceStatus(scl);
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
        List<SclReportItem> sclReportItems = sclService.updateLDeviceStatus(givenScl);
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
    void updateLDeviceStatus_when_DAI_Mod_DO_stVal_whateverIsUpdatableOrNot_shouldReturnUpdatedFile() {
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
        List<SclReportItem> sclReportItems = sclService.updateLDeviceStatus(givenScl);
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
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertIsMarshallable(givenScl);
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
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(getValFromDaiName(givenScl, "IED_NAME1", ldInst, doName, daName)).isNotPresent();
    }

    @Test
    @Tag("issue-321")
    void updateDoInRef_when_ExtRefNotCoherent_shouldReturnReportWithError() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_issue_231_test_ko.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
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
    void manageMonitoringLns_should_update_and_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        LDeviceAdapter lDeviceAdapter = new SclRootAdapter(scd).getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_SUIED);
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(4)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "1"), Tuple.tuple("LGOS", "2"),
                        Tuple.tuple("LSVS", "1"), Tuple.tuple("LSVS", "2"));
        assertIsMarshallable(scd);
    }

    @Test
    void manageMonitoringLns_when_no_extRef_should_not_update_and_not_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter11 = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LD_INST11");
        lDeviceAdapter11.getLN0Adapter().getCurrentElem().setInputs(null);
        LDeviceAdapter lDeviceAdapter21 = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LD_INST21");
        lDeviceAdapter21.getLN0Adapter().getCurrentElem().setInputs(null);
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_SUIED);
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(2)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "3"), Tuple.tuple("LSVS", "9"));
        assertIsMarshallable(scd);
    }

    @Test
    void manageMonitoringLns_when_dai_not_updatable_should_not_update_and_not_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_SUIED);
        getDAIAdapters(lDeviceAdapter, "LGOS", "GoCBRef", "setSrcRef")
                .forEach(daiAdapter -> daiAdapter.getCurrentElem().setValImport(false));
        getDAIAdapters(lDeviceAdapter, "LSVS", "SvCBRef", "setSrcRef")
                .forEach(daiAdapter -> daiAdapter.getCurrentElem().setValImport(false));
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
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
        assertIsMarshallable(scd);
    }

}
