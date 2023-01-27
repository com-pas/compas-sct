// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDo;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_SCL_FILE_TYPE;

class SclServiceTest {

    @Test
    void testAddHistoryItem() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();

        SclService.addHistoryItem(scd, "who", "what", "why");

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

        IEDAdapter iedAdapter = assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));
        assertEquals("IED_NAME1", iedAdapter.getName());
        assertNotNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());

        assertIsMarshallable(scd);
    }

    @Test
    void testAddSubnetworks() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.setApName("AP_NAME");
        connectedApDTO.setIedName("IED_NAME1");
        subNetworkDTO.addConnectedAP(connectedApDTO);

        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, Set.of(subNetworkDTO), Optional.of(icd)).get());
        assertIsMarshallable(scd);
    }

    @Test
    void testAddSubnetworksWithoutCommunicationTagInIcd() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, new HashSet<>(), Optional.of(icd)));
        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).doesNotContain("<Communication");
    }

    @Test
    void testAddSubnetworksWithFilledCommunication() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        Set<SubNetworkDTO> subNetworkDTOSet = new HashSet<>(SclService.getSubnetwork(icd));
        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, subNetworkDTOSet, Optional.of(icd)).get());

        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).contains("<Address>", "PhysConn");
    }

    @Test
    void testAddSubnetworksWithoutImportingIcdAddressAndPhysConn() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        Set<SubNetworkDTO> subNetworkDTOSet = new HashSet<>(SclService.getSubnetwork(icd));
        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, subNetworkDTOSet, Optional.empty()).get());

        String marshalledScd = assertIsMarshallable(scd);
        assertThat(marshalledScd).doesNotContain("<Address>", "PhysConn");
    }

    @Test
    void testGetSubnetwork() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.setApName("AP_NAME");
        connectedApDTO.setIedName("IED_NAME1");
        subNetworkDTO.addConnectedAP(connectedApDTO);

        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, Set.of(subNetworkDTO), Optional.of(icd)).get());

        List<SubNetworkDTO> subNetworkDTOS = assertDoesNotThrow(() -> SclService.getSubnetwork(scd));
        assertEquals(1, subNetworkDTOS.size());
    }

    @Test
    void testGetExtRefInfo() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));
        var extRefInfos = assertDoesNotThrow(() -> SclService.getExtRefInfo(scd, "IED_NAME1", "LD_INST11"));
        assertEquals(1, extRefInfos.size());

        assertEquals("IED_NAME1", extRefInfos.get(0).getHolderIEDName());

        assertThrows(ScdException.class, () -> SclService.getExtRefInfo(scd, "IED_NAME1", "UNKNOWN_LD"));
    }

    @Test
    void getExtRefBinders_shouldThowScdException_whenExtRefNotExist() {
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
    void testUpdateExtRefBinders() {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_2_test.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd1));
        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME2", icd2));

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

        assertDoesNotThrow(
                () -> SclService.updateExtRefBinders(scd, extRefInfo)
        );

        extRefInfo.setHolderLDInst("UNKNOWN_LD");
        assertThrows(
                ScdException.class,
                () -> SclService.updateExtRefBinders(scd, extRefInfo)
        );
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
        assertFalse(extRefs.isEmpty());

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));

        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        //When

        List<ControlBlock>  controlBlocks = SclService.getExtRefSourceInfo(scd, extRefInfo);

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
        assertFalse(extRefs.isEmpty());

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

        //When Then
        assertThat(extRefInfo.getSignalInfo()).isNull();
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThat(extRefInfo.getSignalInfo()).isNotNull();
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
        //When Then
        assertThat(extRefInfo.getBindingInfo()).isNull();
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // binding = null
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThat(extRefInfo.getBindingInfo()).isNotNull();
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

        //When Then
        assertThat(extRefInfo.getSourceInfo()).isNull();
        assertThatThrownBy(() -> SclService.updateExtRefSource(scd, extRefInfo)).isInstanceOf(ScdException.class); // signal = null
        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        assertThat(extRefInfo.getSourceInfo()).isNotNull();
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
        Set<ResumedDataTemplate> allResults = SclService.getDAI(scd, "IED_NAME1", "LD_INST12", new ResumedDataTemplate(), true);

        // then
        assertThat(allResults).hasSize(733);

        List<ResumedDataTemplate> resultsWithDa = allResults.stream().filter(rdt -> StringUtils.isNotBlank(rdt.getDaRef())).collect(Collectors.toList());
        assertThat(resultsWithDa).hasSize(733);

        List<ResumedDataTemplate> resultsWithNoBda = allResults.stream().filter(rdt -> rdt.getBdaNames().isEmpty()).collect(Collectors.toList());
        assertThat(resultsWithNoBda).hasSize(3);
        List<ResumedDataTemplate> resultsWithBdaDepth1 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 1).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth1).isEmpty();
        List<ResumedDataTemplate> resultsWithBdaDepth2 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 2).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth2).hasSize(1);
        List<ResumedDataTemplate> resultsWithBdaDepth3 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 3).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth3).hasSize(729);


        List<ResumedDataTemplate> resultsWithDo = allResults.stream().filter(rdt -> StringUtils.isNotBlank(rdt.getDoRef())).collect(Collectors.toList());
        assertThat(resultsWithDo).hasSize(733);

        List<ResumedDataTemplate> resultsWithNoSdo = allResults.stream().filter(rdt -> rdt.getSdoNames().isEmpty()).collect(Collectors.toList());
        assertThat(resultsWithNoSdo).hasSize(3);
        List<ResumedDataTemplate> resultsWithSdoDepth1 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 1).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth1).isEmpty();
        List<ResumedDataTemplate> resultsWithSdoDepth2 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 2).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth2).hasSize(730);
        List<ResumedDataTemplate> resultsWithSdoDepth3 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 3).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth3).isEmpty();
    }

    @Test
    void getDAI_should_aggregate_attribute_from_DAI() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_aggregate_DAI.xml");

        // when
        Set<ResumedDataTemplate> dais = SclService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new ResumedDataTemplate(), false);

        // then
        ResumedDataTemplate lln0 = ResumedDataTemplate.builder().prefix("").lnType("lntype1").lnClass("LLN0").lnInst("").build();
        ResumedDataTemplate lln0DoA = lln0.toBuilder().doName(createDo("DoA", TPredefinedCDCEnum.DPL)).build();
        ResumedDataTemplate lln0DoB = lln0.toBuilder().doName(createDo("DoB", TPredefinedCDCEnum.ACD)).build();

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

                ResumedDataTemplate.builder().prefix("").lnType("lntype2").lnClass("LPHD").lnInst("0")
                        .doName(createDo("PhyNam", TPredefinedCDCEnum.DPS))
                        .daName(createDa("aDa", TFCEnum.BL, false, Map.of())).build()
        );
    }

    @Test
    void getDAI_when_LDevice_not_found_should_throw_exception() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        // when & then
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        assertThrows(ScdException.class,
                () -> SclService.getDAI(scd, "IED_NAME1", "UNKNOWNLD", resumedDataTemplate, true));
    }

    @Test
    void getDAI_should_filter_updatable_DA() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<ResumedDataTemplate> dais = SclService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new ResumedDataTemplate(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultSimpleDa = dais.stream()
                .filter(rdtt -> rdtt.getBdaNames().isEmpty()) // test only simple DA
                .map(ResumedDataTemplate::getLNRef).collect(Collectors.toList());
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
        Set<ResumedDataTemplate> dais = SclService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new ResumedDataTemplate(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultStructDa = dais.stream()
                .filter(rdtt -> !rdtt.getBdaNames().isEmpty()) // test only struct DA
                .map(ResumedDataTemplate::getLNRef).collect(Collectors.toList());
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
        Set<ResumedDataTemplate> dais = SclService.getDAI(scd, "VirtualBCU", "LDCAP", new ResumedDataTemplate(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultSimpleDa = dais.stream()
                .filter(rdtt -> rdtt.getBdaNames().isEmpty()) // test only simple DA
                .map(ResumedDataTemplate::getLNRef).collect(Collectors.toList());
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
        Set<ResumedDataTemplate> dais = SclService.getDAI(scd, "VirtualBCU", "LDMOD", new ResumedDataTemplate(), true);

        // then
        assertThat(dais)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void testInitScl() {
        SclRootAdapter sclRootAdapter = assertDoesNotThrow(
                () -> SclService.initScl(Optional.empty(), "hVersion", "hRevision")
        );
        assertIsMarshallable(sclRootAdapter.getCurrentElem());
    }

    @Test
    void testInitScl_With_hId_shouldNotThrowError() {
        UUID hid = UUID.randomUUID();
        SclRootAdapter sclRootAdapter = assertDoesNotThrow(
                () -> SclService.initScl(Optional.of(hid), "hVersion", "hRevision")
        );
        assertIsMarshallable(sclRootAdapter.getCurrentElem());
    }

    @Test
    void testInitScl_Create_Private_SCL_FILETYPE() {
        UUID hid = UUID.randomUUID();
        SclRootAdapter rootAdapter = assertDoesNotThrow(
                () -> SclService.initScl(Optional.of(hid), "hVersion", "hRevision")
        );
        assertThat(rootAdapter.getCurrentElem().getPrivate()).isNotEmpty();
        assertThat(rootAdapter.getCurrentElem().getPrivate().get(0).getType()).isEqualTo(COMPAS_SCL_FILE_TYPE.getPrivateType());
        assertIsMarshallable(rootAdapter.getCurrentElem());
    }

    @Test
    void testUpdateHeader() {

        SclRootAdapter sclRootAdapter = assertDoesNotThrow(
                () -> SclService.initScl(Optional.empty(), "hVersion", "hRevision")
        );
        UUID hId = UUID.fromString(sclRootAdapter.getHeaderAdapter().getHeaderId());
        HeaderDTO headerDTO = DTO.createHeaderDTO(hId);
        SclService.updateHeader(sclRootAdapter.getCurrentElem(), headerDTO);
        assertIsMarshallable(sclRootAdapter.getCurrentElem());
    }

    @Test
    void testUpdateDAI() {
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        rDtt.setLnType("unknownID");
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");

        assertThrows(ScdException.class, () -> SclService.updateDAI(
                scd, "IED", "LD", rDtt
        ));
        rDtt.setLnType("LNO1");
        rDtt.setLnClass(TLLN0Enum.LLN_0.value());
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        rDtt.setDoName(doTypeName);
        rDtt.setDaName(new DaTypeName("antRef.bda1.bda2.bda3"));
        TVal tVal = new TVal();
        tVal.setValue("newValue");
        rDtt.setDaiValues(List.of(tVal));
        assertDoesNotThrow(() -> SclService.updateDAI(scd, "IED_NAME", "LD_INS1", rDtt));
        assertIsMarshallable(scd);
    }

    @Test
    void testGetEnumTypeElements() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        assertThrows(ScdException.class, () -> SclService.getEnumTypeElements(scd, "unknwnID"));

        var enumList = assertDoesNotThrow(
                () -> SclService.getEnumTypeElements(scd, "RecCycModKind")
        );
        assertFalse(enumList.isEmpty());
    }

    @Test
    void testImportSTDElementsInSCD() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        SclRootAdapter expectedScdAdapter = assertDoesNotThrow(() -> SclService.importSTDElementsInSCD(
                scdRootAdapter, Set.of(std), DTO.comMap));
        assertThat(expectedScdAdapter.getCurrentElem().getIED()).hasSize(1);
        assertThat(expectedScdAdapter.getCurrentElem().getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork()).hasSize(2);
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_with_Multiple_STD() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_lnode_with_many_compas_icdheader.xml");
        SCL std0 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1SCU2.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        SclRootAdapter expectedScdAdapter = assertDoesNotThrow(() -> SclService.importSTDElementsInSCD(
                scdRootAdapter, Set.of(std0, std1, std2), DTO.comMap));
        assertThat(expectedScdAdapter.getCurrentElem().getIED()).hasSize(3);
        assertThat(expectedScdAdapter.getCurrentElem().getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork()).hasSize(2);
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork().get(0).getConnectedAP()).hasSizeBetween(1, 3);
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork().get(1).getConnectedAP()).hasSizeBetween(1, 3);
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_Several_STD_Match_Compas_ICDHeader() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        //When Then
        Set<SCL> stds = Set.of(std, std1);
        assertThrows(ScdException.class, () -> SclService.importSTDElementsInSCD(scdRootAdapter, stds, DTO.comMap));
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_Compas_ICDHeader_Not_Match() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_with_same_ICDSystemVersionUUID.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        //When Then
        Set<SCL> stds = Set.of(std);
        assertThrows(ScdException.class, () -> SclService.importSTDElementsInSCD(scdRootAdapter, stds, DTO.comMap));
        assertIsMarshallable(scd);
    }

    @Test
    void testImportSTDElementsInSCD_No_STD_Match() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        //When Then
        Set<SCL> stds = new HashSet<>();
        assertThrows(ScdException.class, () -> SclService.importSTDElementsInSCD(scdRootAdapter, stds, DTO.comMap));
        assertIsMarshallable(scd);
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
                Arguments.of("MissingDOBeh",scl1, scl1Errors),
                Arguments.of("MissingLDevicePrivate",scl2, scl2Errors),
                Arguments.of("MissingLDevicePrivateAttribute",scl3, scl3Errors),
                Arguments.of("MissingDOMod",scl4, scl4Errors)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sclProviderMissingRequiredObjects")
    void updateLDeviceStatus_shouldReturnReportWithError_MissingRequiredObject(String testCase, SCL scl, Tuple... errors) {
        // Given
        assertTrue(getLDeviceStatusValue(scl, "IedName1", "LDSUIED").isPresent());
        assertEquals("off", getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue());
        String before = MarshallerWrapper.marshall(scl);
        // When
        SclReport sclReport = SclService.updateLDeviceStatus(scl);
        // Then
        String after = MarshallerWrapper.marshall(sclReport.getSclRootAdapter().getCurrentElem());
        assertFalse(sclReport.isSuccess());
        assertThat(sclReport.getSclReportItems())
                .hasSize(1)
                .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
                .containsExactly(errors);
        assertEquals("off", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName1", "LDSUIED").get().getValue());
        assertEquals(before, after);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("sclProviderBasedLDeviceStatus")
    void updateLDeviceStatus_shouldReturnReportWithError_WhenLDeviceStatusActiveOrUntestedOrInactive(String testCase, SCL scl, Tuple... errors) {
        // Given
        assertEquals("off", getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue());
        assertEquals("on", getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue());
        assertFalse(getLDeviceStatusValue(scl, "IedName3", "LDSUIED").isPresent());
        String before = MarshallerWrapper.marshall(scl);
        // When
        SclReport sclReport = SclService.updateLDeviceStatus(scl);
        // Then
        String after = MarshallerWrapper.marshall(sclReport.getSclRootAdapter().getCurrentElem());
        assertFalse(sclReport.isSuccess());
        assertThat(sclReport.getSclReportItems())
                .hasSize(3)
                .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
                .containsExactly(errors);
        assertEquals("off", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName1", "LDSUIED").get().getValue());
        assertEquals("on", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName2", "LDSUIED").get().getValue());
        assertFalse(getLDeviceStatusValue(scl, "IedName3", "LDSUIED").isPresent());
        assertEquals(before, after);
    }

    @Test
    void updateLDeviceStatus_shouldReturnReportWithError_WhenAllLDeviceInactive_Test2() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test2_LD_STATUS_INACTIVE.scd");
        assertEquals("off", getLDeviceStatusValue(scl, "IedName1", "LDSUIED").get().getValue());
        assertEquals("on", getLDeviceStatusValue(scl, "IedName2", "LDSUIED").get().getValue());
        assertFalse(getLDeviceStatusValue(scl, "IedName3", "LDSUIED").isPresent());
        // When
        SclReport sclReport = SclService.updateLDeviceStatus(scl);
        // Then
        assertFalse(sclReport.isSuccess());
        assertThat(sclReport.getSclReportItems())
                .hasSize(2)
                .extracting(SclReportItem::getMessage, SclReportItem::getXpath)
                .containsExactly(Tuple.tuple("The LDevice cannot be set to 'off' but has not been selected into SSD.",
                                "/SCL/IED[@name=\"IedName1\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"),
                        Tuple.tuple("The LDevice is not qualified into STD but has been selected into SSD.",
                                "/SCL/IED[@name=\"IedName2\"]/AccessPoint/Server/LDevice[@inst=\"LDSUIED\"]/LN0"));
        assertEquals("off", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName1", "LDSUIED").get().getValue());
        assertEquals("on", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName2", "LDSUIED").get().getValue());
        assertTrue(getLDeviceStatusValue(scl, "IedName3", "LDSUIED").isPresent());
        assertEquals("off", getLDeviceStatusValue(scl, "IedName3", "LDSUIED").get().getValue());
    }


    @Test
    void updateLDeviceStatus_shouldReturnUpdatedFile() {
        // Given
        SCL givenScl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_Template.scd");
        assertTrue(getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED").isPresent());
        assertEquals("off", getLDeviceStatusValue(givenScl, "IedName1", "LDSUIED").get().getValue());

        assertTrue(getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED").isPresent());
        assertEquals("on", getLDeviceStatusValue(givenScl, "IedName2", "LDSUIED").get().getValue());

        assertFalse(getLDeviceStatusValue(givenScl, "IedName3", "LDSUIED").isPresent());

        // When
        SclReport sclReport = SclService.updateLDeviceStatus(givenScl);
        // Then
        assertTrue(sclReport.isSuccess());
        assertTrue(getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName1", "LDSUIED").isPresent());
        assertEquals("on", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName1", "LDSUIED").get().getValue());

        assertTrue(getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName2", "LDSUIED").isPresent());
        assertEquals("off", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName2", "LDSUIED").get().getValue());

        assertTrue(getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName3", "LDSUIED").isPresent());
        assertEquals("off", getLDeviceStatusValue(sclReport.getSclRootAdapter().getCurrentElem(), "IedName3", "LDSUIED").get().getValue());
    }

    private Optional<TVal> getLDeviceStatusValue(SCL scl, String iedName, String ldInst) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        Optional<LDeviceAdapter> lDeviceAdapter = iedAdapter.findLDeviceAdapterByLdInst(ldInst);
        LN0Adapter ln0Adapter = lDeviceAdapter.get().getLN0Adapter();
        Optional<DOIAdapter> doiAdapter = ln0Adapter.getDOIAdapters().stream()
                .filter(doiAdapter1 -> doiAdapter1.getCurrentElem().getName().equals("Mod"))
                .findFirst();
        if (doiAdapter.isEmpty()) return Optional.empty();
        return doiAdapter.get().getCurrentElem().getSDIOrDAI().stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals("stVal"))
                .map(tdai -> tdai.getVal().get(0))
                .findFirst();
    }


    @Test
    void analyzeDataGroups_should_success() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter1 = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxAttributes(9L);
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxGOOSE(3L);
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxSMV(2L);
        iedAdapter1.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxReports(1L);
        // When
        SclReport sclReport = SclService.analyzeDataGroups(scd);
        //Then
        assertThat(sclReport.getSclReportItems()).isEmpty();

    }

    @Test
    void analyzeDataGroups_should_return_errors_messages() throws Exception {

        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME2");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getConfDataSet().setMaxAttributes(1L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getConfDataSet().setMax(3L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getSMVsc().setMax(1L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getGOOSE().setMax(2L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getConfReportControl().setMax(0L);
        // When
        SclReport sclReport = SclService.analyzeDataGroups(scd);
        //Then
        assertThat(sclReport.getSclReportItems()).hasSize(11)
                .extracting(SclReportItem::getMessage)
                .containsExactlyInAnyOrder(
                        "There are too much FCDA for the Client IED IED_NAME1",
                                "The Client IED IED_NAME1 subscribes to too much GOOSE Control Blocks.",
                                "The Client IED IED_NAME1 subscribes to too much REPORT Control Blocks.",
                                "The Client IED IED_NAME1 subscribes to too much SMV Control Blocks.",
                                "There are too much FCDA for the DataSet DATASET6 for the LDevice LD_INST21 in IED IED_NAME2",
                                "There are too much FCDA for the DataSet DATASET6 for the LDevice LD_INST22 in IED IED_NAME2",
                                "There are too much FCDA for the DataSet DATASET5 for the LDevice LD_INST22 in IED IED_NAME2",
                                "There are too much DataSets for the IED IED_NAME2",
                                "There are too much Report Control Blocks for the IED IED_NAME2",
                                "There are too much GOOSE Control Blocks for the IED IED_NAME2",
                                "There are too much SMV Control Blocks for the IED IED_NAME2");
    }


}
