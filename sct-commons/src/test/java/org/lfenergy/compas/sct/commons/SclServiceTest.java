// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.DataTypeTemplateReader;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.DOIAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.LD_LDSUIED;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.getDAIAdapters;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertSclValidateXsd;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_SCL_FILE_TYPE;

class SclServiceTest {

    private final IedService iedService = new IedService();
    private final LnService lnService = new LnService();
    private final ExtRefReaderService extRefReaderService = new ExtRefReaderService();
    private final DataTypeTemplateReader dataTypeTemplatesService = new DataTypeTemplatesService();
    private final LdeviceService ldeviceService = new LdeviceService(lnService);
    private SclService sclService;

    @BeforeEach
    void setUp() {
        sclService = new SclService(iedService, ldeviceService, lnService, extRefReaderService, dataTypeTemplatesService);
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
        THitem tHitem = history.getHitem().getFirst();
        assertThat(tHitem.getWho()).isEqualTo("who");
        assertThat(tHitem.getWhat()).isEqualTo("what");
        assertThat(tHitem.getWhy()).isEqualTo("why");
        assertThat(tHitem.getRevision()).isEqualTo(SclRootAdapter.REVISION);
        assertThat(tHitem.getVersion()).isEqualTo(SclRootAdapter.VERSION);
        assertSclValidateXsd(scd);
    }

    @Test
    void addIED_should_add_ied_element() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromResource("scl-srv-import-ieds/ied_1_test.xml");
        //When Then
        assertDoesNotThrow(() -> sclService.addIED(scd, "IED_NAME1", icd));
        assertThat(scd.getIED().size()).isNotZero();
        assertThat(scd.getIED().getFirst().getName()).isEqualTo("IED_NAME1");
        assertThat(scd.getDataTypeTemplates()).isNotNull();
        assertSclValidateXsd(scd);
    }

    @Test
    @Tag("issue-321")
    void addSubnetworks_should_add_subnetwork() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromResource("scl-srv-import-ieds/ied_1_test.xml");
        //When Then
        assertThatCode(() -> sclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO("IED_NAME1","AP_NAME");
        subNetworkDTO.addConnectedAP(connectedApDTO);
        //When Then
        assertThatCode(() -> sclService.addSubnetworks(scd, List.of(subNetworkDTO), icd)).doesNotThrowAnyException();
        assertSclValidateXsd(scd);
    }

    @Test
    @Tag("issue-321")
    void addSubnetworks_whenNoCommunicationTagInIcd_should_not_add_subnetwork() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromResource("scl-srv-import-ieds/ied_1_test.xml");
        //When Then
        assertThatCode(() -> sclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> sclService.addSubnetworks(scd, List.of(), icd)).doesNotThrowAnyException();
        String marshalledScd = SclTestMarshaller.marshal(scd);
        assertThat(marshalledScd).doesNotContain("<Communication");
    }

    @Test
    void addSubnetworks_shouldNotUpdateScd_when_noCommunicationInSTDExist() {
        //Givens
        SCL scd = SclTestMarshaller.getSCLFromResource("scl_update_communication/scd_without_communication.xml");
        SCL icd = SclTestMarshaller.getSCLFromResource("scl_update_communication/std_without_communication.xml");
        assertThat(scd.getCommunication()).isNull();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, icd, "IED_NAME1")).doesNotThrowAnyException();
        String marshalledScd = SclTestMarshaller.marshal(scd);
        assertThat(scd.getCommunication()).isNull();
        assertThat(marshalledScd).doesNotContain("<Communication");
        assertSclValidateXsd(scd);
    }

    @Test
    void addSubnetworks_shouldAddSubNetwork_and_ConnectedAp_and_updateConnectedApIEDName() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scl_update_communication/scd_without_communication.xml");
        SCL std = SclTestMarshaller.getSCLFromResource("scl_update_communication/std_with_communication.xml");
        assertThat(scd.getCommunication()).isNull();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, std, "IED_NAME1")).doesNotThrowAnyException();
        assertThat(scd.getCommunication()).isNotNull();
        String marshalledScd = SclTestMarshaller.marshal(scd);
        assertThat(marshalledScd).contains("<Communication");
        // assertion succeeds as subNetwork.connectedAP.iedName field is ignored.
        assertThat(scd.getCommunication())
                .usingRecursiveComparison()
                .ignoringFields("subNetwork.connectedAP.iedName")
                .isEqualTo(std.getCommunication());
        assertThat(scd.getCommunication().getSubNetwork().getFirst().getConnectedAP().getFirst().getIedName())
                .isEqualTo("IED_NAME1");
        assertSclValidateXsd(scd);
    }


    @Test
    void addSubnetworks_shouldCopyAddressAndPhysConnFromStd() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scl_update_communication/scd_without_communication.xml");
        SCL std = SclTestMarshaller.getSCLFromResource("scl_update_communication/std_with_full_filled_communication.xml");
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
        assertSclValidateXsd(scd);
    }

    @Test
    void addSubnetworks_shouldDoNothing_when_subNetworkAlreadyExist() {
        //Givens
        SCL scd = SclTestMarshaller.getSCLFromResource("scl_update_communication/scd_without_communication.xml");
        SCL std = SclTestMarshaller.getSCLFromResource("scl_update_communication/std_with_communication.xml");
        std.getCommunication().getSubNetwork().getFirst().getConnectedAP().getFirst().getPhysConn().clear();
        std.getCommunication().getSubNetwork().getFirst().getConnectedAP().getFirst().setAddress(null);
        std.getCommunication().getSubNetwork().getFirst().getConnectedAP().getFirst().unsetGSE();
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, std, "IED_NAME1")).doesNotThrowAnyException();
        String marshalledScd = SclTestMarshaller.marshal(scd);
        assertThat(marshalledScd).contains("<Communication");
    }

    @Test
    void addSubnetworks_shouldThrowError_When_IedNameNotExistInScd() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scl_update_communication/scd_without_communication.xml");
        SCL icd = SclTestMarshaller.getSCLFromResource("scl_update_communication/std_with_communication.xml");
        //When
        //Then
        assertThatCode(() -> sclService.addSubnetworks(scd, icd, "UnknownIedName"))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'UnknownIedName' not found in SCD");
    }

    @Test
    void testInitScl_With_headerId_shouldNotThrowError() {
        //Given
        UUID headerId = UUID.randomUUID();
        //When
        SCL scd = assertDoesNotThrow(() -> sclService.initScl(headerId, "hVersion", "hRevision"));
        //Then
        assertSclValidateXsd(scd);
    }

    @Test
    void initScl_should_create_Private_SCL_FILETYPE() {
        // Given
        UUID headerId = UUID.randomUUID();
        // When Then
        SCL scd = assertDoesNotThrow(() -> sclService.initScl(headerId, "hVersion", "hRevision"));
        assertThat(scd.getPrivate()).isNotEmpty();
        assertThat(scd.getPrivate().getFirst().getType()).isEqualTo(COMPAS_SCL_FILE_TYPE.getPrivateType());
        assertSclValidateXsd(scd);
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
        assertSclValidateXsd(scd);
    }

    @Test
    @Tag("issue-321")
    void updateDAI_should_not_throw_error() {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("unknownID");
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
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
        assertSclValidateXsd(scd);
    }

    @Test
    void testImportSTDElementsInSCD_whenCalledWithOneSTD_shouldNotThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, List.of(std)))
                .doesNotThrowAnyException();
        assertThat(scd.getIED()).hasSize(1);
        assertThat(scd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertSclValidateXsd(scd);
    }

    @Test
    void importSTDElementsInSCD_whenCalledWithMultipleSTD_shouldNotThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/scd_lnode_with_many_compas_icdheader.xml");
        SCL std0 = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std_SITESITE1SCU1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std_SITESITE1SCU2.xml");
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, List.of(std0, std1, std2)))
                .doesNotThrowAnyException();
        assertThat(scd.getIED()).hasSize(3);
        assertThat(scd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(scd.getCommunication().getSubNetwork()).hasSize(2);
        assertThat(scd.getCommunication().getSubNetwork().getFirst().getConnectedAP()).hasSizeBetween(1, 3);
        assertThat(scd.getCommunication().getSubNetwork().get(1).getConnectedAP()).hasSizeBetween(1, 3);
        assertSclValidateXsd(scd);
    }

    @Test
    void importSTDElementsInSCD_whenManySTDMatchCompasICDHeader_shouldThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/scd.xml");
        SCL std1 = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        SCL std2 = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        List<SCL> stds = List.of(std1, std2);
        //When Then
        assertThatThrownBy(() -> sclService.importSTDElementsInSCD(scd, stds))
                .isInstanceOf(ScdException.class);
        assertSclValidateXsd(scd);
    }

    @Test
    void importSTDElementsInSCD_whenSCDFileContainsSameICDHeaderInTwoDifferentFunctions_should_not_throw_exception() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/scd_with_same_compas_icd_header_in_different_functions.xml");
        SCL std = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, List.of(std))).doesNotThrowAnyException();
        assertSclValidateXsd(scd);
    }

    @Test
    void importSTDElementsInSCD_whenCompasICDHeaderNotMatch__shouldThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std_with_same_ICDSystemVersionUUID.xml");
        List<SCL> stdList = List.of(std);
        //When Then
        assertThatThrownBy(() -> sclService.importSTDElementsInSCD(scd, stdList))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("COMPAS-ICDHeader is not the same in Substation and in IED");
        assertSclValidateXsd(scd);
    }

    @Test
    void importSTDElementsInSCD_whenNoSTDMatch_shouldThrowException() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/ssd.xml");
        List<SCL> stdList = List.of();
        //When Then
        assertThatCode(() -> sclService.importSTDElementsInSCD(scd, stdList))
                .isInstanceOf(ScdException.class)
                .hasMessage("There is no STD file found corresponding to headerId = f8dbc8c1-2db7-4652-a9d6-0b414bdeccfa, headerVersion = 01.00.00, headerRevision = 01.00.00 and ICDSystemVersionUUID = IED4d4fe1a8cda64cf88a5ee4176a1a0eef");
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
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_issue_231_test_ok.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertSclValidateXsd(givenScl);
        assertThat(getValFromDaiName(givenScl, "IED_NAME1", ldInst, doName, daName)
                .map(TVal::getValue))
                .hasValue(expected);
        assertSclValidateXsd(givenScl);
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
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_issue_231_test_ok.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isError)).isTrue();
        assertThat(getValFromDaiName(givenScl, "IED_NAME1", ldInst, doName, daName)).isNotPresent();
    }

    @Test
    void updateDoInRef_when_No_ExtRef_for_InRef_should_not_return_error() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_no_Extref.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_ExtRef_bind_and_desc_suffix_not_match_should_not_return_error() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_extref_bind_desc_suffix_not_match.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_ExtRef_bind_should_succeed() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_extref_bind.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(givenScl.getIED())
                .filteredOn(tied -> tied.getName().equals("IED_NAME1"))
                .flatExtracting(TIED::getAccessPoint)
                .extracting(TAccessPoint::getServer)
                .flatExtracting(TServer::getLDevice)
                .filteredOn(tlDevice -> tlDevice.getInst().equals("LD1"))
                .map(TLDevice::getLN0)
                .flatExtracting(TAnyLN::getDOI)
                .filteredOn(tdoi -> tdoi.getName().equals("InRef1"))
                .flatExtracting(TDOI::getSDIOrDAI)
                .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                .containsExactly("IED_NAME1LD2/PRANCR1.Do11.sdo11");
    }

    @Test
    void updateDoInRef_when_ExtRef_bind_should_not_return_error() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_extref_not_bind.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void updateDoInRef_when_Inref_purpose_not_set_should_return_report_with_warning() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromResource("scd-test-update-inref/scd_update_inref_when_inref_purpose_not_set.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.updateDoInRef(givenScl);
        // Then
        assertThat(sclReportItems.stream().filter(sclReportItem -> !sclReportItem.isError()).map(SclReportItem::message))
                .hasSize(1)
                .containsExactly("The DOI /SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_Without_Val_in_DAI_purpose\"]/LN0 can't be bound with an ExtRef");
    }

    private Optional<TVal> getValFromDaiName(SCL scl, String iedName, String ldInst, String doiName, String daiName) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        Optional<LDeviceAdapter> lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst(ldInst);
        LN0Adapter ln0Adapter = lDeviceAdapter.orElseThrow().getLN0Adapter();
        Optional<DOIAdapter> doiAdapter = ln0Adapter.getDOIAdapters().stream()
                .filter(doiAdapter1 -> doiAdapter1.getCurrentElem().getName().equals(doiName))
                .findFirst();
        return doiAdapter.flatMap(adapter -> adapter.getCurrentElem().getSDIOrDAI().stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals(daiName) && !tdai.getVal().isEmpty())
                .map(tdai -> tdai.getVal().getFirst())
                .findFirst());
    }

    @Test
    void manageMonitoringLns_should_update_and_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        assertSclValidateXsd(scd);
        assertThat(scd.getIED())
                .filteredOn(tied -> tied.getName().equals("IED_NAME1"))
                .flatExtracting(TIED::getAccessPoint)
                .extracting(TAccessPoint::getServer)
                .flatExtracting(TServer::getLDevice)
                .filteredOn(tlDevice -> tlDevice.getInst().equals(LD_LDSUIED))
                .extracting(TLDevice::getLN)
                .allSatisfy(tlns -> {
                    // Expected number of LGOS and LSVS
                    assertThat(tlns).hasSize(4).extracting(tln -> tln.getLnClass().getFirst(), TLN::getInst).containsExactlyInAnyOrder(
                            Tuple.tuple("LGOS", "1"), Tuple.tuple("LGOS", "2"),
                            Tuple.tuple("LSVS", "1"), Tuple.tuple("LSVS", "2"));

                    // LGOS setSrcRef values
                    assertThat(tlns)
                            .filteredOn(tln -> tln.getLnClass().contains("LGOS") && tln.getInst().equals("1")
                                    || tln.getLnClass().contains("LGOS") && tln.getInst().equals("2"))
                            .flatExtracting(TAnyLN::getDOI)
                            .filteredOn(tdoi -> tdoi.getName().equals("GoCBRef"))
                            .flatExtracting(TDOI::getSDIOrDAI)
                            .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                            .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                            .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.goose1", "IED_NAME2LD_INST22/LLN0.goose1");
                    // LSVS setSrcRef values
                    assertThat(tlns)
                            .filteredOn(tln -> (tln.getLnClass().contains("LSVS") && tln.getInst().equals("1")
                                    || tln.getLnClass().contains("LSVS") && tln.getInst().equals("2")))
                            .flatExtracting(TAnyLN::getDOI)
                            .filteredOn(tdoi -> tdoi.getName().equals("SvCBRef"))
                            .flatExtracting(TDOI::getSDIOrDAI)
                            .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                            .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                            .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.smv2", "IED_NAME2LD_INST22/LLN0.smv2");
                });
    }

    @Test
    void manageMonitoringLns_should_update_and_create_lsvs_and_goose_when_many_ExtRef_for_same_iedSource() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos_with_many_extref_for_same_ied_source.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        assertSclValidateXsd(scd);
        assertThat(scd.getIED())
                .filteredOn(tied -> tied.getName().equals("IED_NAME1"))
                .flatExtracting(TIED::getAccessPoint)
                .extracting(TAccessPoint::getServer)
                .flatExtracting(TServer::getLDevice)
                .filteredOn(tlDevice -> tlDevice.getInst().equals(LD_LDSUIED))
                .extracting(TLDevice::getLN)
                .allSatisfy(tlns -> {
                    // Expected number of LGOS and LSVS
                    assertThat(tlns).hasSize(4).extracting(tln -> tln.getLnClass().getFirst(), TLN::getInst).containsExactlyInAnyOrder(
                            Tuple.tuple("LGOS", "1"), Tuple.tuple("LGOS", "2"),
                            Tuple.tuple("LSVS", "1"), Tuple.tuple("LSVS", "2"));
                    // LGOS setSrcRef values
                    assertThat(tlns)
                            .filteredOn(tln -> tln.getLnClass().contains("LGOS") && tln.getInst().equals("1")
                                    || tln.getLnClass().contains("LGOS") && tln.getInst().equals("2"))
                            .flatExtracting(TAnyLN::getDOI)
                            .filteredOn(tdoi -> tdoi.getName().equals("GoCBRef"))
                            .flatExtracting(TDOI::getSDIOrDAI)
                            .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                            .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                            .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.goose1", "IED_NAME2LD_INST22/LLN0.goose1");
                    // LSVS setSrcRef values
                    assertThat(tlns)
                            .filteredOn(tln -> tln.getLnClass().contains("LSVS") && tln.getInst().equals("1")
                                    || tln.getLnClass().contains("LSVS") && tln.getInst().equals("2"))
                            .flatExtracting(TAnyLN::getDOI)
                            .filteredOn(tdoi -> tdoi.getName().equals("SvCBRef"))
                            .flatExtracting(TDOI::getSDIOrDAI)
                            .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                            .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                            .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.smv2", "IED_NAME2LD_INST22/LLN0.smv2");
                });
    }

    @Test
    void manageMonitoringLns_when_no_extRef_should_not_update_and_not_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter11 = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LD_INST11");
        lDeviceAdapter11.getLN0Adapter().getCurrentElem().setInputs(null);
        LDeviceAdapter lDeviceAdapter21 = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst("LD_INST21");
        lDeviceAdapter21.getLN0Adapter().getCurrentElem().setInputs(null);
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_LDSUIED);
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(2)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "1"), Tuple.tuple("LSVS", "1"));
        assertSclValidateXsd(scd);
    }

    @Test
    void manageMonitoringLns_when_dai_not_updatable_should_not_update_and_not_create_lsvs_and_goose() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LDeviceAdapter lDeviceAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1").getLDeviceAdapterByLdInst(LD_LDSUIED);
        getDAIAdapters(lDeviceAdapter, "LGOS", "1", "GoCBRef", "setSrcRef")
                .forEach(daiAdapter -> daiAdapter.getCurrentElem().setValImport(false));
        getDAIAdapters(lDeviceAdapter, "LSVS", "1", "SvCBRef", "setSrcRef")
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
                        Tuple.tuple("LGOS", "1"), Tuple.tuple("LSVS", "1"));
        assertSclValidateXsd(scd);
    }

    @Test
    void manageMonitoringLns_should_return_error_when_target_lns_not_found() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos.xml");
        scd.getIED().stream()
                .filter(tied -> tied.getName().equals("IED_NAME1"))
                .flatMap(tied -> tied.getAccessPoint().stream())
                .flatMap(tAccessPoint -> tAccessPoint.getServer().getLDevice().stream())
                .filter(tlDevice -> tlDevice.getInst().equals(LD_LDSUIED))
                .findFirst()
                .ifPresent(tlDevice -> tlDevice.getLN().clear());
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).hasSize(2)
                .extracting(SclReportItem::isError, SclReportItem::xpath, SclReportItem::message)
                .containsExactly(tuple(false, "IED_NAME1/LDSUIED/LGOS", "There is no LN LGOS present in LDevice"), tuple(false, "IED_NAME1/LDSUIED/LSVS", "There is no LN LSVS present in LDevice"));
    }

    @Test
    void manageMonitoringLns_should_update_and_create_lsvs_and_goose_when_contains_privates() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos_with_privates.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).isEmpty();
        assertSclValidateXsd(scd);

        assertThat(scd.getIED())
                .filteredOn(tied -> tied.getName().equals("IED_NAME1"))
                .flatExtracting(TIED::getAccessPoint)
                .extracting(TAccessPoint::getServer)
                .flatExtracting(TServer::getLDevice)
                .filteredOn(tlDevice -> tlDevice.getInst().equals(LD_LDSUIED))
                .extracting(TLDevice::getLN)
                .allSatisfy(tlns -> {
                            // Expected number of LGOS and LSVS
                            assertThat(tlns).hasSize(4).extracting(tln -> tln.getLnClass().getFirst(), TLN::getInst).containsExactlyInAnyOrder(
                                            Tuple.tuple("LGOS", "1"), Tuple.tuple("LGOS", "2"),
                                            Tuple.tuple("LSVS", "1"), Tuple.tuple("LSVS", "2"));
                            // LGOS setSrcRef values
                            assertThat(tlns)
                                    .filteredOn(tln -> tln.getLnClass().contains("LGOS") && tln.getInst().equals("1")
                                            || tln.getLnClass().contains("LGOS") && tln.getInst().equals("2"))
                                    .flatExtracting(TAnyLN::getDOI)
                                    .filteredOn(tdoi -> tdoi.getName().equals("GoCBRef"))
                                    .flatExtracting(TDOI::getSDIOrDAI)
                                    .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                                    .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                                    .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.goose1", "IED_NAME2LD_INST22/LLN0.goose1");
                            // LSVS setSrcRef values
                            assertThat(tlns)
                                    .filteredOn(tln -> tln.getLnClass().contains("LSVS") && tln.getInst().equals("1")
                                            || tln.getLnClass().contains("LSVS") && tln.getInst().equals("2"))
                                    .flatExtracting(TAnyLN::getDOI)
                                    .filteredOn(tdoi -> tdoi.getName().equals("SvCBRef"))
                                    .flatExtracting(TDOI::getSDIOrDAI)
                                    .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                                    .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                                    .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.smv2", "IED_NAME2LD_INST22/LLN0.smv2");
                        });
    }

    @Test
    void manageMonitoringLns_should_clean_remove_all_lsvs_lgos_with_instance_not_equals_one(){
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("monitoring_lns/scd_monitoring_lsvs_lgos_with_inst_one_and_not_one.xml");
        // When
        List<SclReportItem> sclReportItems = sclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems).hasSize(1)
                .extracting(SclReportItem::isError, SclReportItem::xpath, SclReportItem::message)
                .containsExactly(tuple(false, "IED_NAME1/LDSUIED/LSVS", "There is no LN LSVS present in LDevice"));

        assertThat(scd.getIED())
                .filteredOn(tied -> tied.getName().equals("IED_NAME1"))
                .flatExtracting(TIED::getAccessPoint)
                .extracting(TAccessPoint::getServer)
                .flatExtracting(TServer::getLDevice)
                .filteredOn(tlDevice -> tlDevice.getInst().equals(LD_LDSUIED))
                .extracting(TLDevice::getLN)
                .allSatisfy(tlns -> {
                    // Expected number of LGOS and LSVS
                    assertThat(tlns).hasSize(2).extracting(tln -> tln.getLnClass().getFirst(), TLN::getInst).containsExactlyInAnyOrder(
                            Tuple.tuple("LGOS", "1"), Tuple.tuple("LGOS", "2"));
                    // LGOS setSrcRef values
                    assertThat(tlns)
                            .filteredOn(tln -> tln.getLnClass().contains("LGOS") && tln.getInst().equals("1")
                                    || tln.getLnClass().contains("LGOS") && tln.getInst().equals("2"))
                            .flatExtracting(TAnyLN::getDOI)
                            .filteredOn(tdoi -> tdoi.getName().equals("GoCBRef"))
                            .flatExtracting(TDOI::getSDIOrDAI)
                            .filteredOn(tUnNaming -> tUnNaming.getClass().equals(TDAI.class) && ((TDAI)tUnNaming).getName().equals("setSrcRef"))
                            .map(tUnNaming -> ((TDAI)tUnNaming).getVal().getFirst().getValue())
                            .containsExactlyInAnyOrder("IED_NAME2LD_INST21/LLN0.goose1", "IED_NAME2LD_INST22/LLN0.goose1");
                });
    }
}
