// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDo;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.LD_SUIED;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.getDAIAdapters;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_SCL_FILE_TYPE;

class SclServiceTest {

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
        SclService.addHistoryItem(scd, "who", "what", "why");
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
        assertThat(scd.getIED()).isEmpty();
        //When Then
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        assertThat(scd.getIED().size()).isNotZero();
        assertThat(scd.getIED().get(0).getName()).isEqualTo("IED_NAME1");
        assertThat(scd.getDataTypeTemplates()).isNotNull();
        assertIsMarshallable(scd);
    }

    @Test
    void addSubnetworks_should_add_subnetwork() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.setApName("AP_NAME");
        connectedApDTO.setIedName("IED_NAME1");
        subNetworkDTO.addConnectedAP(connectedApDTO);
        //When Then
        assertThatCode(() ->  SclService.addSubnetworks(scd, Set.of(subNetworkDTO), Optional.of(icd)).get()).doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    void addSubnetworks_should_not_add_subnetwork_when_withoutCommunicationTagInIcd() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        //When Then
        assertThatCode(() ->  SclService.addSubnetworks(scd, new HashSet<>(), Optional.of(icd))).doesNotThrowAnyException();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).doesNotContain("<Communication");
    }

    @Test
    void addSubnetworks_should_add_subnetwork_when_withFilledCommunication() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        //TODO not should appear in given section, only one When step
        Set<SubNetworkDTO> subNetworkDTOSet = new HashSet<>(SclService.getSubnetwork(icd));
        //When Then
        assertThatCode(() ->  SclService.addSubnetworks(scd, subNetworkDTOSet, Optional.of(icd))).doesNotThrowAnyException();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).contains("<Address>", "PhysConn");
    }

    @Test
    void addSubnetworks_should_add_subnetwork_element_when_withoutImportingIcdAddressAndPhysConn() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        //When Then
        Set<SubNetworkDTO> subNetworkDTOSet = new HashSet<>(SclService.getSubnetwork(icd));
        assertThatCode(() ->  SclService.addSubnetworks(scd, subNetworkDTOSet, Optional.empty())).doesNotThrowAnyException();
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).doesNotContain("<Address>", "PhysConn");
    }

    @Test
    void getSubnetwork_should_return_list() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.setApName("AP_NAME");
        connectedApDTO.setIedName("IED_NAME1");
        subNetworkDTO.addConnectedAP(connectedApDTO);
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addSubnetworks(scd, Set.of(subNetworkDTO), Optional.of(icd))).doesNotThrowAnyException();
        //When
        List<SubNetworkDTO> subNetworkDTOS = SclService.getSubnetwork(scd);
        //Then
        assertThat(subNetworkDTOS).hasSize(1);
    }

    @Test
    void getExtRefInfo_should_throw_exception_when_unknown_ldInst() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //TODO not should appear in given section, only one When step
        assertThatCode(() ->  SclService.addIED(scd, "IED_NAME1", icd)).doesNotThrowAnyException();
        //TODO not should appear in given section, only one When step
        var extRefInfos =  SclService.getExtRefInfo(scd, "IED_NAME1", "LD_INST11");
        assertThat(extRefInfos).hasSize(1);
        assertThat(extRefInfos.get(0).getHolderIEDName()).isEqualTo("IED_NAME1");
        //When Then
        assertThatThrownBy(() -> SclService.getExtRefInfo(scd, "IED_NAME1", "UNKNOWN_LD"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown LDevice (UNKNOWN_LD) in IED (IED_NAME1)");
    }

    @Test
    void getExtRefBinders_shouldThrowScdException_whenExtRefNotExist() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");

        ExtRefSignalInfo signalInfo = createSignalInfo("Do11.sdo11", "da11.bda111.bda112.bda113", "INT_ADDR11");
        signalInfo.setPLN("ANCR");
        //When Then
        assertThatThrownBy(
                () -> SclService.getExtRefBinders(scd, "IED_NAME1", "UNKNOWN_LD", "LLN0", "", "", signalInfo))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void getExtRefBinders_shouldReturnSortedListBindingInfo_whenExtRefAndDOExist() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");

        ExtRefSignalInfo signalInfo = createSignalInfo(
                "Do11.sdo11", "da11.bda111.bda112.bda113", "INT_ADDR11"
        );
        signalInfo.setPLN("ANCR");

        // When
        List<ExtRefBindingInfo> potentialBinders = SclService.getExtRefBinders(scd, "IED_NAME1", "LD_INST11", "LLN0", "", "", signalInfo);

        // Then
        assertThat(potentialBinders).hasSize(4);
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getIedName)
                .containsExactly("IED_NAME1", "IED_NAME1", "IED_NAME2", "IED_NAME3");
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getLdInst)
                .containsExactly("LD_INST11", "LD_INST12", "LD_INST22", "LD_INST31");
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getLnClass)
                .containsExactly("ANCR", "ANCR", "ANCR", "ANCR");
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getLnInst)
                .containsExactly("1", "1", "2", "3");
    }

    @Test
    void updateExtRefBinders_should_not_throw_exception() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertThat(sclRootAdapter.getCurrentElem().getDataTypeTemplates()).isNull();
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_2_test.xml");

        //TODO not should appear in given section, only one When step
        assertThatCode(() -> SclService.addIED(scd, "IED_NAME1", icd1)).doesNotThrowAnyException();
        //TODO not should appear in given section, only one When step
        assertThatCode(() -> SclService.addIED(scd, "IED_NAME2", icd2)).doesNotThrowAnyException();

        ExtRefSignalInfo signalInfo = createSignalInfo(
                "Do11.sdo11", "da11.bda111.bda112.bda113", "INT_ADDR11"
        );
        signalInfo.setPServT(null);
        signalInfo.setPLN(null);
        signalInfo.setDesc(null);
        // Signal for external binding (in IED 2 LD_INST22 - PIOC)
        ExtRefBindingInfo bindingInfo = new ExtRefBindingInfo();
        bindingInfo.setIedName("IED_NAME2");
        bindingInfo.setLdInst("LD_INST22");
        bindingInfo.setLnClass("PIOC");
        bindingInfo.setLnInst("1");
        bindingInfo.setLnType("LN2");
        bindingInfo.setDoName(new DoTypeName(signalInfo.getPDO()));
        bindingInfo.setDaName(new DaTypeName(signalInfo.getPDA()));
        bindingInfo.setServiceType(signalInfo.getPServT());
        LNodeDTO lNodeDTO = new LNodeDTO();
        lNodeDTO.setNodeClass(TLLN0Enum.LLN_0.value());
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME1");
        extRefInfo.setHolderLDInst("LD_INST11");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());
        extRefInfo.setSignalInfo(signalInfo);
        extRefInfo.setBindingInfo(bindingInfo);
        lNodeDTO.getExtRefs().add(extRefInfo);

        //TODO not should appear in given section, only one When step
        assertThatCode(() -> SclService.updateExtRefBinders(scd, extRefInfo)).doesNotThrowAnyException();
        extRefInfo.setHolderLDInst("UNKNOWN_LD");
        //When Then
        assertThatThrownBy(() -> SclService.updateExtRefBinders(scd, extRefInfo)).isInstanceOf(ScdException.class);
        assertIsMarshallable(scd);
    }

    @Test
    void getExtRefSourceInfo_shouldReturnEmptyList_whenExtRefMatchNoFCDA() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        String iedName = "IED_NAME2";
        String ldInst = "LD_INST21";
        String lnClass = TLLN0Enum.LLN_0.value();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst(ldInst).get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        List<TExtRef> extRefs = ln0Adapter.getExtRefs(null);
        assertThat(extRefs.isEmpty()).isFalse();

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));

        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        //When
        List<ControlBlock> controlBlocks = SclService.getExtRefSourceInfo(scd, extRefInfo);

        //Then
        assertThat(controlBlocks).isEmpty();
    }

    @Test
    void getExtRefSourceInfo_shouldReturnListOfControlBlocks_whenExtRefMatchFCDA() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/issue_175_scd_get_cbs_test.xml");
        String iedName = "IED_NAME2";
        String ldInst = "LD_INST21";
        String lnClass = TLLN0Enum.LLN_0.value();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst(ldInst).get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        List<TExtRef> extRefs = ln0Adapter.getExtRefs(null);
        assertThat(extRefs).isNotEmpty();

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));

        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        //When
        List<ControlBlock> controlBlocks = SclService.getExtRefSourceInfo(scd, extRefInfo);

        //Then
        assertThat(controlBlocks).hasSize(1);
        assertThat(controlBlocks.get(0).getName()).isEqualTo("goose2");
    }

    @Test
    void updateExtRefSource_shouldThrowScdException_whenSignalInfoNullOrInvalid() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        //TODO not should appear in given section, only one When step
        //When Then
        assertThat(extRefInfo.getSignalInfo()).isNull();
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThat(extRefInfo.getSignalInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// signal invalid
    }

    @Test
    void updateExtRefSource_shouldThrowScdException_whenBindingInfoNullOrInvalid() {
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

        //TODO only one When step (updateExtRefSource called twice)
        //When Then
        assertThat(extRefInfo.getBindingInfo()).isNull();
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // binding = null
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThat(extRefInfo.getBindingInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// binding invalid
    }

    @Test
    void updateExtRefSource_shouldThrowScdException_whenBindingInternalByIedName() {
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
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // CB not allowed
    }

    @Test
    void updateExtRefSource_shouldThrowScdException_whenBindingInternaByServiceType() {
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
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // CB not allowed
    }

    @Test
    void updateExtRefSource_shouldThrowScdException_whenSourceInfoNullOrInvalid() {
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

        //TODO only one When step (updateExtRefSource called twice)
        //When Then
        assertThat(extRefInfo.getSourceInfo()).isNull();
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        assertThat(extRefInfo.getSourceInfo()).isNotNull();
        //When Then
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class);// signal invalid
    }

    @Test
    void updateExtRefSource_shouldThrowScdException_whenBindingExternalBinding() {
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
        TExtRef extRef = assertDoesNotThrow(() -> SclService.updateExtRefSource(scd, extRefInfo));
        //Then
        assertThat(extRef.getSrcCBName()).isEqualTo(extRefInfo.getSourceInfo().getSrcCBName());
        assertThat(extRef.getSrcLDInst()).isEqualTo(extRefInfo.getBindingInfo().getLdInst());
        assertThat(extRef.getSrcLNClass()).contains(extRefInfo.getBindingInfo().getLnClass());
    }

    private ExtRefSignalInfo createSignalInfo(String pDO, String pDA, String intAddr) {

        final String DESC = "DESC";
        final String P_LN = TLLN0Enum.LLN_0.value();
        final String P_SERV_T = "Report";

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setDesc(DESC);
        signalInfo.setPDA(pDA);
        signalInfo.setPDO(pDO);
        signalInfo.setPLN(P_LN);
        signalInfo.setPServT(TServiceType.fromValue(P_SERV_T));
        signalInfo.setIntAddr(intAddr);

        return signalInfo;
    }

    @Test
    void getDAI_should_return_all_dai() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        // when
        Set<DataAttributeRef> allResults = SclService.getDAI(scd, "IED_NAME1", "LD_INST12", new DataAttributeRef(), true);

        // then
        assertThat(allResults).hasSize(733);

        List<DataAttributeRef> resultsWithDa = allResults.stream().filter(rdt -> StringUtils.isNotBlank(rdt.getDaRef())).collect(Collectors.toList());
        assertThat(resultsWithDa).hasSize(733);

        List<DataAttributeRef> resultsWithNoBda = allResults.stream().filter(rdt -> rdt.getBdaNames().isEmpty()).collect(Collectors.toList());
        assertThat(resultsWithNoBda).hasSize(3);
        List<DataAttributeRef> resultsWithBdaDepth1 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 1).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth1).isEmpty();
        List<DataAttributeRef> resultsWithBdaDepth2 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 2).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth2).hasSize(1);
        List<DataAttributeRef> resultsWithBdaDepth3 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 3).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth3).hasSize(729);


        List<DataAttributeRef> resultsWithDo = allResults.stream().filter(rdt -> StringUtils.isNotBlank(rdt.getDoRef())).collect(Collectors.toList());
        assertThat(resultsWithDo).hasSize(733);

        List<DataAttributeRef> resultsWithNoSdo = allResults.stream().filter(rdt -> rdt.getSdoNames().isEmpty()).collect(Collectors.toList());
        assertThat(resultsWithNoSdo).hasSize(3);
        List<DataAttributeRef> resultsWithSdoDepth1 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 1).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth1).isEmpty();
        List<DataAttributeRef> resultsWithSdoDepth2 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 2).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth2).hasSize(730);
        List<DataAttributeRef> resultsWithSdoDepth3 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 3).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth3).isEmpty();
    }

    @Test
    void getDAI_should_aggregate_attribute_from_DAI() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_aggregate_DAI.xml");

        // when
        Set<DataAttributeRef> dais = SclService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new DataAttributeRef(), false);

        // then
        DataAttributeRef lln0 = DataAttributeRef.builder().prefix("").lnType("lntype1").lnClass("LLN0").lnInst("").build();
        DataAttributeRef lln0DoA = lln0.toBuilder().doName(createDo("DoA", TPredefinedCDCEnum.DPL)).build();
        DataAttributeRef lln0DoB = lln0.toBuilder().doName(createDo("DoB", TPredefinedCDCEnum.ACD)).build();

        assertThat(dais).containsExactlyInAnyOrder(
                lln0DoA.toBuilder().daName(createDa("daNotInDai", TFCEnum.CF, false, Map.of(0L, "0"))).build(),
                lln0DoA.toBuilder().daName(createDa("daNotInDai2", TFCEnum.CF, true, Map.of())).build(),
                lln0DoA.toBuilder().daName(createDa("daiOverrideVal", TFCEnum.CF, false, Map.of(0L, "1"))).build(),
                lln0DoA.toBuilder().daName(createDa("daiOverrideValImport", TFCEnum.CF, true, Map.of())).build(),
                lln0DoA.toBuilder().daName(createDa("daiOverrideValImport2", TFCEnum.CF, false, Map.of())).build(),

                lln0DoB.toBuilder().daName(createDa("structDa.daNotInDai", TFCEnum.ST, false, Map.of(0L, "0"))).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daNotInDai2", TFCEnum.ST, true, Map.of())).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daiOverrideVal", TFCEnum.ST, false, Map.of(0L, "1"))).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daiOverrideValImport", TFCEnum.ST, true, Map.of())).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daiOverrideValImport2", TFCEnum.ST, false, Map.of())).build(),

                DataAttributeRef.builder().prefix("").lnType("lntype2").lnClass("LPHD").lnInst("0")
                        .doName(createDo("PhyNam", TPredefinedCDCEnum.DPS))
                        .daName(createDa("aDa", TFCEnum.BL, false, Map.of())).build()
        );
    }

    @Test
    void getDAI_when_LDevice_not_found_should_throw_exception() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        // when & then
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        assertThatThrownBy(() -> SclService.getDAI(scd, "IED_NAME1", "UNKNOWNLD", dataAttributeRef, true))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void getDAI_should_filter_updatable_DA() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = SclService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new DataAttributeRef(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultSimpleDa = dais.stream()
                .filter(dataAttributeRef -> dataAttributeRef.getBdaNames().isEmpty()) // test only simple DA
                .map(DataAttributeRef::getLNRef).collect(Collectors.toList());
        assertThat(resultSimpleDa).containsExactlyInAnyOrder(
                // ...AndTrueInDai : If ValImport is True in DAI, DA is updatable
                "LLN0.DoA.valImportNotSetAndTrueInDai",
                "LLN0.DoA.valImportTrueAndTrueInDai",
                "LLN0.DoA.valImportFalseAndTrueInDai",
                // valImportTrue : If ValImport is True in DA and DAI does not exist, DA is updatable
                "LLN0.DoA.valImportTrue",
                // valImportTrueAndNotSetInDai : If ValImport is True in DA and DAI exists but DAI ValImport is not set, DA is updatable
                "LLN0.DoA.valImportTrueAndNotSetInDai",
                // Only these FC are updatable
                "LLN0.DoA.fcCF",
                "LLN0.DoA.fcDC",
                "LLN0.DoA.fcSG",
                "LLN0.DoA.fcSP",
                "LLN0.DoA.fcST",
                "LLN0.DoA.fcSE"
        );
    }

    @Test
    void getDAI_should_filter_updatable_BDA() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = SclService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new DataAttributeRef(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultStructDa = dais.stream()
                .filter(dataAttributeRef -> !dataAttributeRef.getBdaNames().isEmpty()) // test only struct DA
                .map(DataAttributeRef::getLNRef).collect(Collectors.toList());
        assertThat(resultStructDa).containsExactlyInAnyOrder(
                // ...AndTrueInDai : If ValImport is True in DAI, BDA is updatable
                "LLN0.DoB.structValImportNotSet.bValImportFalseAndTrueInDai",
                "LLN0.DoB.structValImportNotSet.bValImportNotSetAndTrueInDai",
                "LLN0.DoB.structValImportNotSet.bValImportTrueAndTrueInDai",
                "LLN0.DoB.structValImportTrue.bValImportFalseAndTrueInDai",
                "LLN0.DoB.structValImportTrue.bValImportNotSetAndTrueInDai",
                "LLN0.DoB.structValImportTrue.bValImportTrueAndTrueInDai",
                "LLN0.DoB.structValImportFalse.bValImportFalseAndTrueInDai",
                "LLN0.DoB.structValImportFalse.bValImportNotSetAndTrueInDai",
                "LLN0.DoB.structValImportFalse.bValImportTrueAndTrueInDai",
                // bValImportTrue : If ValImport is True in BDA and DAI does not exist, BDA is updatable
                "LLN0.DoB.structValImportFalse.bValImportTrue",
                "LLN0.DoB.structValImportTrue.bValImportTrue",
                "LLN0.DoB.structValImportNotSet.bValImportTrue",
                // bValImportTrueAndNotSetInDai : If ValImport is True in BDA and DAI exists but DAI ValImport is not set, BDA is updatable
                "LLN0.DoB.structValImportTrue.bValImportTrueAndNotSetInDai",
                "LLN0.DoB.structValImportNotSet.bValImportTrueAndNotSetInDai",
                "LLN0.DoB.structValImportFalse.bValImportTrueAndNotSetInDai",
                // Only these FC are updatable
                "LLN0.DoB.structWithFcCF.bda1",
                "LLN0.DoB.structWithFcDC.bda1",
                "LLN0.DoB.structWithFcSG.bda1",
                "LLN0.DoB.structWithFcSP.bda1",
                "LLN0.DoB.structWithFcST.bda1",
                "LLN0.DoB.structWithFcSE.bda1"
        );
    }

    @Test
    void getDAI_should_filter_updatable_DA_with_sGroup_Val() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = SclService.getDAI(scd, "VirtualBCU", "LDCAP", new DataAttributeRef(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultSimpleDa = dais.stream()
                .filter(dataAttributeRef -> dataAttributeRef.getBdaNames().isEmpty()) // test only simple DA
                .map(DataAttributeRef::getLNRef).collect(Collectors.toList());
        assertThat(resultSimpleDa).containsExactlyInAnyOrder(
                "LLN0.DoD.sGroupValImportNotSet",
                "LLN0.DoD.sGroupValImportTrue"
        );
    }

    @Test
    void getDAI_should_filter_updatable_DA_with_sGroup_Val_without_ConfSg() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = SclService.getDAI(scd, "VirtualBCU", "LDMOD", new DataAttributeRef(), true);

        // then
        assertThat(dais)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void initScl_shouldNotThrowError() {
        SCL scd = assertDoesNotThrow(() -> SclService.initScl(Optional.empty(), "hVersion", "hRevision"));
        assertIsMarshallable(scd);
    }

    @Test
    void initScl_shouldNotThrowError_when_hId_provided() {
        // Given
        UUID hid = UUID.randomUUID();
        // When Then
        SCL scd = assertDoesNotThrow(() -> SclService.initScl(Optional.of(hid), "hVersion", "hRevision"));
        assertIsMarshallable(scd);
    }

    @Test
    void initScl_should_create_Private_SCL_FILETYPE() {
        // Given
        UUID hid = UUID.randomUUID();
        // When Then
        SCL scd = assertDoesNotThrow(() -> SclService.initScl(Optional.of(hid), "hVersion", "hRevision"));
        assertThat(scd.getPrivate()).isNotEmpty();
        assertThat(scd.getPrivate().get(0).getType()).isEqualTo(COMPAS_SCL_FILE_TYPE.getPrivateType());
        assertIsMarshallable(scd);
    }

    @Test
    void updateHeader_should_update_header_tag() {
        //Given
        SCL scd = assertDoesNotThrow(() -> SclService.initScl(Optional.empty(), "hVersion", "hRevision"));
        UUID hId = UUID.fromString(scd.getHeader().getId());
        HeaderDTO headerDTO = DTO.createHeaderDTO(hId);
        //When
        SclService.updateHeader(scd, headerDTO);
        //Then
        assertIsMarshallable(scd);
    }

    @Test
    void updateDAI_should_not_throw_error() {
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnType("unknownID");
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        //TODO not should appear in given section
        assertThatThrownBy(() -> SclService.updateDAI(scd, "IED", "LD", dataAttributeRef))
                .isInstanceOf(ScdException.class);
        dataAttributeRef.setLnType("LNO1");
        dataAttributeRef.setLnClass(TLLN0Enum.LLN_0.value());
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        dataAttributeRef.setDoName(doTypeName);
        dataAttributeRef.setDaName(new DaTypeName("antRef.bda1.bda2.bda3"));
        TVal tVal = new TVal();
        tVal.setValue("newValue");
        dataAttributeRef.setDaiValues(List.of(tVal));
        //When Then
        assertThatCode(() -> SclService.updateDAI(scd, "IED_NAME", "LD_INS1", dataAttributeRef)).doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    void getEnumTypeElements_should_return_list() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        //TODO not should appear in given section
        assertThatThrownBy(() -> SclService.getEnumTypeElements(scd, "unknwnID"))
                .isInstanceOf(ScdException.class);
        //When Then
        var enumList = assertDoesNotThrow(() -> SclService.getEnumTypeElements(scd, "RecCycModKind"));
        assertThat(enumList).isNotEmpty();
    }

    @Test
    void importSTDElementsInSCD_should_std_files_is_scd() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        SCL expectedScd = assertDoesNotThrow(() -> SclService.importSTDElementsInSCD(scd, List.of(std), DTO.comMap));
        assertThat(expectedScd.getIED()).hasSize(1);
        assertThat(expectedScd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(expectedScd.getCommunication().getSubNetwork()).hasSize(2);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_shouldNotThrowException_when_with_Multiple_STD() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_lnode_with_many_compas_icdheader.xml");
        SCL std0 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU2.xml");

        SCL expectedScd = assertDoesNotThrow(() -> SclService.importSTDElementsInSCD(scd, List.of(std0, std1, std2), DTO.comMap));
        assertThat(expectedScd.getIED()).hasSize(3);
        assertThat(expectedScd.getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(expectedScd.getCommunication().getSubNetwork()).hasSize(2);
        assertThat(expectedScd.getCommunication().getSubNetwork().get(0).getConnectedAP()).hasSizeBetween(1, 3);
        assertThat(expectedScd.getCommunication().getSubNetwork().get(1).getConnectedAP()).hasSizeBetween(1, 3);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_shouldThrowException_when_Several_STD_Match_Compas_ICDHeader() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        List<SCL> stds = List.of(std, std1);
        assertThatThrownBy(() -> SclService.importSTDElementsInSCD(scd, stds, DTO.comMap))
                .isInstanceOf(ScdException.class);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_should_not_throw_exception_when_SCD_file_contains_same_ICDHeader_in_two_different_functions() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_with_same_compas_icd_header_in_different_functions.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        //When Then
        assertThatCode(() -> SclService.importSTDElementsInSCD(scd, List.of(std), DTO.comMap)).doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_should_throw_exception_when_Compas_ICDHeader_Not_Match() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_with_same_ICDSystemVersionUUID.xml");
        //When Then
        List<SCL> stds = List.of(std);
        assertThatThrownBy(() -> SclService.importSTDElementsInSCD(scd, stds, DTO.comMap))
                .isInstanceOf(ScdException.class);
        assertIsMarshallable(scd);
    }

    @Test
    void importSTDElementsInSCD_should_throw_exception_when_No_STD_Match() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        //When Then
        assertThatCode(() -> SclService.importSTDElementsInSCD(scd, List.of(), DTO.comMap))
                .isInstanceOf(ScdException.class)
                .hasMessage("There is no STD file found corresponding to headerId = f8dbc8c1-2db7-4652-a9d6-0b414bdeccfa, headerVersion = 01.00.00, headerRevision = 01.00.00 and ICDSystemVersionUUID = IED4d4fe1a8cda64cf88a5ee4176a1a0eef");
    }

    @Test
    void removeControlBlocksAndDatasetAndExtRefSrc_should_remove_controlBlocks_and_Dataset_on_ln0() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scl-remove-controlBlocks-dataSet-extRefSrc/scl-with-control-blocks.xml");
        // When
        SclService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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
        SclService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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
        SclService.removeAllControlBlocksAndDatasetsAndExtRefSrcBindings(scl);
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
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED")).isPresent();
        assertThat(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue()).isEqualTo("off");
        String before = MarshallerWrapper.marshall(scl);
        // When
        List<SclReportItem> sclReportItems = SclService.updateLDeviceStatus(scl);
        // Then
        String after = MarshallerWrapper.marshall(scl);
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isFalse();
        assertThat(sclReportItems)
                .hasSize(1)
                .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
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
        List<SclReportItem> sclReportItems = SclService.updateLDeviceStatus(scl);
        // Then
        String after = MarshallerWrapper.marshall(scl);
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isFalse();
        assertThat(sclReportItems)
                .hasSize(3)
                .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
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
        List<SclReportItem> sclReportItems = SclService.updateLDeviceStatus(scl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isFalse();
        assertThat(sclReportItems)
                .hasSize(2)
                .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
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
        List<SclReportItem> sclReportItems = SclService.updateLDeviceStatus(givenScl);
        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isTrue();
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
        List<SclReportItem> sclReportItems = SclService.updateLDeviceStatus(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isTrue();
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
        List<SclReportItem> sclReportItems = SclService.updateDoInRef(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isTrue();
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
        List<SclReportItem> sclReportItems = SclService.updateDoInRef(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isTrue();
        assertThat(getValFromDaiName(givenScl, "IED_NAME1", ldInst, doName, daName)).isNotPresent();
    }

    @Test
    void updateDoInRef_shouldReturnReportWithError_when_ExtRef_not_coherent() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-test-update-inref/scd_update_inref_issue_231_test_ko.xml");

        // When
        List<SclReportItem> sclReportItems = SclService.updateDoInRef(givenScl);

        // Then
        assertThat(sclReportItems.stream().noneMatch(SclReportItem::isFatal)).isTrue();
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
        List<SclReportItem> sclReportItems = SclService.analyzeDataGroups(scd);
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
        List<SclReportItem> sclReportItems = SclService.analyzeDataGroups(scd);
        //Then
        assertThat(sclReportItems).hasSize(11)
                .extracting(SclReportItem::getMessage)
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
        List<SclReportItem> sclReportItems = SclService.manageMonitoringLns(scd);
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
        List<SclReportItem> sclReportItems = SclService.manageMonitoringLns(scd);
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
        List<SclReportItem> sclReportItems = SclService.manageMonitoringLns(scd);
        //Then
        assertThat(sclReportItems)
                .isNotEmpty()
                .hasSize(2)
                .extracting(SclReportItem::getMessage)
                .containsExactly("The DAI cannot be updated", "The DAI cannot be updated");
        assertThat(lDeviceAdapter.getLNAdapters())
                .hasSize(2)
                .extracting(LNAdapter::getLNClass, LNAdapter::getLNInst).containsExactlyInAnyOrder(
                        Tuple.tuple("LGOS", "3"), Tuple.tuple("LSVS", "9"));
        SclTestMarshaller.assertIsMarshallable(scd);
    }

}
