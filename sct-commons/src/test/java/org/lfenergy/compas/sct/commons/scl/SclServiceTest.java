// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.CommonConstants;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    void testAddIED() throws Exception {

        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        IEDAdapter iedAdapter = assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));
        assertEquals("IED_NAME1", iedAdapter.getName());
        assertNotNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());

        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        System.out.println(marshallerWrapper.marshall(scd));
    }

    @Test
    void testAddSubnetworks() throws Exception {
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
        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        System.out.println(marshallerWrapper.marshall(scd));
    }

    @Test
    void testAddSubnetworksWithoutCommunicationTagInIcd() throws Exception {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, new HashSet<>(), Optional.of(icd)));
        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        String marshalledScd = marshallerWrapper.marshall(scd);
        assertThat(marshalledScd).doesNotContain("<Communication");
    }

    @Test
    void testAddSubnetworksWithFilledCommunication() throws Exception {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        Set<SubNetworkDTO> subNetworkDTOSet = new HashSet<>(SclService.getSubnetwork(icd));
        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, subNetworkDTOSet, Optional.of(icd)).get());

        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        String marshalledScd = marshallerWrapper.marshall(scd);
        assertThat(marshalledScd).contains("<Address>", "PhysConn");
    }

    @Test
    void testAddSubnetworksWithoutImportingIcdAddressAndPhysConn() throws Exception {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");

        assertDoesNotThrow(() -> SclService.addIED(scd, "IED_NAME1", icd));

        Set<SubNetworkDTO> subNetworkDTOSet = new HashSet<>(SclService.getSubnetwork(icd));
        assertDoesNotThrow(() -> SclService.addSubnetworks(scd, subNetworkDTOSet, Optional.empty()).get());

        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        String marshalledScd = marshallerWrapper.marshall(scd);
        assertThat(marshalledScd).doesNotContain("<Address>", "PhysConn");
    }


    @Test
    void testGetSubnetwork() throws Exception {
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
    void testGetExtRefInfo() throws Exception {
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
    void testGetExtRefBinders() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");

        ExtRefSignalInfo signalInfo = createSignalInfo(
                "Do11.sdo11", "da11.bda111.bda112.bda113", "INT_ADDR11"
        );

        List<ExtRefBindingInfo> potentialBinders = assertDoesNotThrow(
                () -> SclService.getExtRefBinders(
                        scd, "IED_NAME1", "LD_INST11", "LLN0", "", "", signalInfo
                )
        );

        assertThrows(
                ScdException.class,
                () -> SclService.getExtRefBinders(
                        scd, "IED_NAME1", "UNKNOWN_LD", "LLN0", "", "", signalInfo
                )
        );
    }

    @Test
    void testUpdateExtRefBinders() throws Exception {
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
    }

    @Test
    void testGetExtRefSourceInfo() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        String iedName = "IED_NAME2";
        String ldInst = "LD_INST21";
        String lnClass = TLLN0Enum.LLN_0.value();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.getLDeviceAdapterByLdInst(ldInst).get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        List<TExtRef> extRefs = ln0Adapter.getExtRefs(null);
        assertFalse(extRefs.isEmpty());

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));

        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        var controlBlocks = SclService.getExtRefSourceInfo(scd, extRefInfo);
        assertEquals(2, controlBlocks.size());
        controlBlocks.forEach(controlBlock -> assertTrue(
                        controlBlock.getName().equals("goose1") || controlBlock.getName().equals("smv1")
                )
        );
    }

    @Test
    void testUpdateExtRefSource() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIEDName("IED_NAME2");
        extRefInfo.setHolderLDInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        assertThrows(ScdException.class, () -> SclService.updateExtRefSource(scd, extRefInfo)); // signal = null
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThrows(ScdException.class, () -> SclService.updateExtRefSource(scd, extRefInfo)); // signal invalid

        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR21");
        extRefInfo.getSignalInfo().setPDA("da21.bda211.bda212.bda213");
        extRefInfo.getSignalInfo().setPDO("Do21.sdo21");
        assertThrows(ScdException.class, () -> SclService.updateExtRefSource(scd, extRefInfo)); // binding = null
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThrows(ScdException.class, () -> SclService.updateExtRefSource(scd, extRefInfo)); // binding invalid

        extRefInfo.getBindingInfo().setIedName("IED_NAME2"); // internal binding
        extRefInfo.getBindingInfo().setLdInst("LD_INST12");
        extRefInfo.getBindingInfo().setLnClass(TLLN0Enum.LLN_0.value());
        assertThrows(ScdException.class, () -> SclService.updateExtRefSource(scd, extRefInfo)); // CB not allowed

        extRefInfo.getBindingInfo().setIedName("IED_NAME1");

        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        extRefInfo.getSourceInfo().setSrcLDInst(extRefInfo.getBindingInfo().getLdInst());
        extRefInfo.getSourceInfo().setSrcLNClass(extRefInfo.getBindingInfo().getLnClass());
        extRefInfo.getSourceInfo().setSrcCBName("goose1");
        TExtRef extRef = assertDoesNotThrow(() -> SclService.updateExtRefSource(scd, extRefInfo));
        assertEquals(extRefInfo.getSourceInfo().getSrcCBName(), extRef.getSrcCBName());
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
    void testGetDAI() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");


        Set<ResumedDataTemplate> resumedDataTemplates = assertDoesNotThrow(
                () -> SclService.getDAI(
                        scd, "IED_NAME1", "LD_INST12", new ResumedDataTemplate(), true
                )
        );
        assertEquals(13, resumedDataTemplates.size());

        assertThrows(
                ScdException.class,
                () -> SclService.getDAI(
                        scd, "IED_NAME1", "UNKNOWNLD", new ResumedDataTemplate(), true
                )
        );
    }

    @Test
    void testInitScl() {
        assertDoesNotThrow(
                () -> SclService.initScl(Optional.empty(), "hVersion", "hRevision")
        );
    }

    @Test
    void testInitScl_With_hId_shouldNotThrowError() {
        UUID hid = UUID.randomUUID();
        assertDoesNotThrow(
                () -> SclService.initScl(Optional.of(hid), "hVersion", "hRevision")
        );
    }

    @Test
    void testInitScl_Create_Private_SCL_FILETYPE() {
        UUID hid = UUID.randomUUID();
        SclRootAdapter rootAdapter = assertDoesNotThrow(
                () -> SclService.initScl(Optional.of(hid), "hVersion", "hRevision")
        );
        assertThat(rootAdapter.getCurrentElem().getPrivate()).isNotEmpty();
        assertThat(rootAdapter.getCurrentElem().getPrivate().get(0).getType()).isEqualTo(CommonConstants.COMPAS_SCL_FILE_TYPE);
    }

    @Test
    void testUpdateHeader() {

        SclRootAdapter sclRootAdapter = assertDoesNotThrow(
                () -> SclService.initScl(Optional.empty(), "hVersion", "hRevision")
        );
        UUID hId = UUID.fromString(sclRootAdapter.getHeaderAdapter().getHeaderId());
        HeaderDTO headerDTO = DTO.createHeaderDTO(hId);
        SclService.updateHeader(sclRootAdapter.getCurrentElem(), headerDTO);
        SclService.updateHeader(sclRootAdapter.getCurrentElem(), headerDTO);

    }

    @Test
    void testUpdateDAI() throws Exception {
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        rDtt.setLnType("unknownID");
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);

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

    }

    @Test
    void testGetEnumTypeElements() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        assertThrows(ScdException.class, () -> SclService.getEnumTypeElements(scd, "unknwnID"));

        var enumList = assertDoesNotThrow(
                () -> SclService.getEnumTypeElements(scd, "RecCycModKind")
        );
        assertFalse(enumList.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/scd-substation-import-ssd/ssd_with_2_substations.xml", "/scd-substation-import-ssd/ssd_without_substations.xml"})
    void testAddSubstation_Check_SSD_Validity(String ssdFileName) throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile(ssdFileName);

        assertThrows(ScdException.class,
                () -> SclService.addSubstation(scd, ssd));
    }

    @Test
    void testAddSubstation_SCD_Without_Substation() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        SclRootAdapter expectedScdAdapter = SclService.addSubstation(scd, ssd);

        assertNotEquals(scdRootAdapter, expectedScdAdapter);
        assertEquals(expectedScdAdapter.getCurrentElem().getSubstation(), ssdRootAdapter.getCurrentElem().getSubstation());
    }

    @Test
    void testAddSubstation_SCD_With_Different_Substation_Name() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation_name_different.xml");
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");

        assertThrows(ScdException.class,
                () -> SclService.addSubstation(scd, ssd));
    }

    @Test
    void testAddSubstation_SCD_With_Substation() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/scd_with_substation.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);
        SCL ssd = SclTestMarshaller.getSCLFromFile("/scd-substation-import-ssd/ssd.xml");
        SclRootAdapter ssdRootAdapter = new SclRootAdapter(ssd);
        SclRootAdapter expectedScdAdapter = SclService.addSubstation(scd, ssd);
        TSubstation expectedTSubstation = expectedScdAdapter.getCurrentElem().getSubstation().get(0);
        TSubstation tSubstation = ssdRootAdapter.getCurrentElem().getSubstation().get(0);

        assertNotEquals(scdRootAdapter, expectedScdAdapter);
        assertEquals(expectedTSubstation.getName(), tSubstation.getName());
        assertEquals(expectedTSubstation.getVoltageLevel().size(), tSubstation.getVoltageLevel().size());
    }

    @Test
    void testImportSTDElementsInSCD() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        SclRootAdapter expectedScdAdapter = assertDoesNotThrow( () -> SclService.importSTDElementsInSCD(
                scdRootAdapter, Set.of(std), DTO.comMap));
        assertThat(expectedScdAdapter.getCurrentElem().getIED()).hasSize(1);
        assertThat(expectedScdAdapter.getCurrentElem().getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork()).hasSize(2);
    }

    @Test
    void testImportSTDElementsInSCD_with_Multiple_STD() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd_lnode_with_many_compas_icdheader.xml");
        SCL std0 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1GTW1.xml");
        SCL std2 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_SITESITE1GTW2.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        SclRootAdapter expectedScdAdapter = assertDoesNotThrow( () -> SclService.importSTDElementsInSCD(
                scdRootAdapter, Set.of(std0, std1, std2), DTO.comMap));
        assertThat(expectedScdAdapter.getCurrentElem().getIED()).hasSize(3);
        assertThat(expectedScdAdapter.getCurrentElem().getDataTypeTemplates()).hasNoNullFieldsOrProperties();
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork()).hasSize(2);
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork().get(0).getConnectedAP()).hasSizeBetween(1,3);
        assertThat(expectedScdAdapter.getCurrentElem().getCommunication().getSubNetwork().get(1).getConnectedAP()).hasSizeBetween(1,3);
    }

    @Test
    void testImportSTDElementsInSCD_Several_STD_Match_Compas_ICDHeader() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SCL std1 = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        assertThrows(ScdException.class, () -> SclService.importSTDElementsInSCD(scdRootAdapter, Set.of(std, std1), DTO.comMap));

    }

    @Test
    void testImportSTDElementsInSCD_Compas_ICDHeader_Not_Match() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/scd.xml");
        SCL std = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std_with_same_ICDSystemVersionUUID.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        assertThrows(ScdException.class, ()-> SclService.importSTDElementsInSCD(scdRootAdapter, Set.of(std), DTO.comMap));

    }

    @Test
    void testImportSTDElementsInSCD_No_STD_Match() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/ssd.xml");
        SclRootAdapter scdRootAdapter = new SclRootAdapter(scd);

        assertThrows(ScdException.class, ()-> SclService.importSTDElementsInSCD(scdRootAdapter, new HashSet<>(), DTO.comMap));

    }
}