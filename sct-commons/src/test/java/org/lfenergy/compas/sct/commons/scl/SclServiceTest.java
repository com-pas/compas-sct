// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDo;

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
    void getDAI_should_return_all_dai() throws Exception {
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
    void getDAI_should_aggregate_attribute_from_DAI() throws Exception {
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
    void getDAI_when_LDevice_not_found_should_throw_exception() throws Exception {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        // when & then
        assertThrows(ScdException.class,
            () -> SclService.getDAI(scd, "IED_NAME1", "UNKNOWNLD", new ResumedDataTemplate(), true));
    }

    @Test
    void getDAI_should_filter_updatable_DA() throws Exception {
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
    void getDAI_should_filter_updatable_BDA() throws Exception {
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
    void getDAI_should_filter_updatable_DA_with_sGroup_Val() throws Exception {
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
    void getDAI_should_filter_updatable_DA_with_sGroup_Val_without_ConfSg() throws Exception {
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
