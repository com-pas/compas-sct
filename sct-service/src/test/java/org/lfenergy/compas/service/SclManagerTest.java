// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.service;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.sct.commons.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.dto.ConnectedApDTO;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.dto.LNodeDTO;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.service.testhelpers.SclTestMarshaller;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SclManagerTest {

    @Test
    void testAddHistoryItem() throws ScdException {
        SclRootAdapter sclRootAdapter=  new SclRootAdapter("hId",SclRootAdapter.VERSION,SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        SclManager sclManager = new SclManager();
        sclManager.addHistoryItem(scd,"who","what","why");

        assertNotNull(scd.getHeader());
        THeader.History history = scd.getHeader().getHistory();
        assertNotNull(history);
        assertEquals(1,history.getHitem().size());
        THitem tHitem = history.getHitem().get(0);
        assertEquals("who",tHitem.getWho());
        assertEquals("what",tHitem.getWhat());
        assertEquals("why",tHitem.getWhy());
        assertEquals(SclRootAdapter.REVISION,tHitem.getRevision());
        assertEquals(SclRootAdapter.VERSION,tHitem.getVersion());
    }

    @Test
    void testAddIED() throws Exception {

        SclRootAdapter sclRootAdapter=  new SclRootAdapter("hId",SclRootAdapter.VERSION,SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");

        SclManager sclManager = new SclManager();
        IEDAdapter iedAdapter = assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME1",icd));
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
        SCL icd = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");

        SclManager sclManager = new SclManager();
        assertDoesNotThrow(() -> sclManager.addIED(scd, "IED_NAME1", icd));

        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.setApName("AP_NAME");
        connectedApDTO.setIedName("IED_NAME1");
        subNetworkDTO.addConnectedAP(connectedApDTO);

        assertDoesNotThrow(() -> sclManager.addSubnetworks(scd, Set.of(subNetworkDTO)));
        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        System.out.println(marshallerWrapper.marshall(scd));
    }


    @Test
    void testGetSubnetwork() throws Exception {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hId", SclRootAdapter.VERSION, SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");

        SclManager sclManager = new SclManager();
        assertDoesNotThrow(() -> sclManager.addIED(scd, "IED_NAME1", icd));

        SubNetworkDTO subNetworkDTO = new SubNetworkDTO();
        subNetworkDTO.setName("sName1");
        subNetworkDTO.setType("IP");
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.setApName("AP_NAME");
        connectedApDTO.setIedName("IED_NAME1");
        subNetworkDTO.addConnectedAP(connectedApDTO);

        assertDoesNotThrow(() -> sclManager.addSubnetworks(scd, Set.of(subNetworkDTO)));

        List<SubNetworkDTO> subNetworkDTOS = assertDoesNotThrow(()-> sclManager.getSubnetwork(scd));
        assertEquals(1,subNetworkDTOS.size());
    }

    @Test
    void testGetExtRefInfo() throws Exception {
        SclRootAdapter sclRootAdapter=  new SclRootAdapter("hId",SclRootAdapter.VERSION,SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");

        SclManager sclManager = new SclManager();
        assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME1",icd));
        var extRefInfos = assertDoesNotThrow(() -> sclManager.getExtRefInfo(scd,"IED_NAME1","LD_INST11"));
        assertEquals(1,extRefInfos.size());

        assertEquals("IED_NAME1",extRefInfos.get(0).getHolderIedName());

        assertThrows(ScdException.class, () -> sclManager.getExtRefInfo(scd,"IED_NAME1","UNKNOWN_LD"));
    }

    @Test
    void testGetExtRefBinders() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_binders_test.xml");
        SclManager sclManager = new SclManager();

        ExtRefSignalInfo signalInfo = createSignalInfo(
                "Do11.sdo11","da11.bda111.bda112.bda113","INT_ADDR11"
        );

        List<ExtRefBindingInfo> potentialBinders = assertDoesNotThrow(
                () -> sclManager.getExtRefBinders(
                        scd,"IED_NAME1","LD_INST11","LLN0","",signalInfo
                )
        );

        assertThrows(
                ScdException.class,
                () -> sclManager.getExtRefBinders(
                        scd,"IED_NAME1","UNKNOWN_LD","LLN0","",signalInfo
                )
        );
    }

    @Test
    void testUpdateExtRefBinders() throws Exception {
        SclRootAdapter sclRootAdapter=  new SclRootAdapter("hId",SclRootAdapter.VERSION,SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_2_test.xml");
        SclManager sclManager = new SclManager();
        assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME1",icd1));
        assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME2",icd2));

        ExtRefSignalInfo signalInfo = createSignalInfo(
                "Do11.sdo11","da11.bda111.bda112.bda113","INT_ADDR11"
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
        extRefInfo.setSignalInfo(signalInfo);
        extRefInfo.setBindingInfo(bindingInfo);
        lNodeDTO.getExtRefs().add(extRefInfo);

        assertDoesNotThrow(
            () -> sclManager.updateExtRefBinders(scd,"IED_NAME1","LD_INST11",lNodeDTO)
        );

        assertThrows(
                ScdException.class,
                () -> sclManager.updateExtRefBinders(
                        scd,"IED_NAME1","UNKNOWN_LD",lNodeDTO
                )
        );
    }

    @Test
    void testGetExtRefSourceInfo() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        String iedName = "IED_NAME2";
        String ldInst = "LD_INST21";
        String lnClass = TLLN0Enum.LLN_0.value();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapter(iedName);
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.getLDeviceAdapterByLdInst(ldInst).get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        List<TExtRef> extRefs = ln0Adapter.getExtRefs(null);
        assertFalse(extRefs.isEmpty());

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));
        extRefInfo.setHolderIedName(iedName);
        extRefInfo.setHolderLdInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        SclManager sclManager = new SclManager();
        var controlBlocks = sclManager.getExtRefSourceInfo(scd,extRefInfo);
        assertEquals(2,controlBlocks.size());
        controlBlocks.forEach(controlBlock -> assertTrue(
                controlBlock.getName().equals("goose1") || controlBlock.getName().equals("smv1")
                )
        );
    }

    @Test
    void testUpdateExtRefSource() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setHolderIedName("IED_NAME2");
        extRefInfo.setHolderLdInst("LD_INST21");
        extRefInfo.setHolderLnClass(TLLN0Enum.LLN_0.value());

        SclManager sclManager = new SclManager();
        assertThrows(IllegalArgumentException.class, () -> sclManager.updateExtRefSource(scd,extRefInfo)); // signal = null
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThrows(IllegalArgumentException.class, () -> sclManager.updateExtRefSource(scd,extRefInfo)); // signal invalid

        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR21");
        extRefInfo.getSignalInfo().setPDA("da21.bda211.bda212.bda213");
        extRefInfo.getSignalInfo().setPDO("Do21.sdo21");
        assertThrows(IllegalArgumentException.class, () -> sclManager.updateExtRefSource(scd,extRefInfo)); // binding = null
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThrows(IllegalArgumentException.class, () -> sclManager.updateExtRefSource(scd,extRefInfo)); // binding invalid

        extRefInfo.getBindingInfo().setIedName("IED_NAME2"); // internal binding
        extRefInfo.getBindingInfo().setLdInst("LD_INST12");
        extRefInfo.getBindingInfo().setLnClass(TLLN0Enum.LLN_0.value());
        assertThrows(IllegalArgumentException.class, () -> sclManager.updateExtRefSource(scd,extRefInfo)); // CB not allowed

        extRefInfo.getBindingInfo().setIedName("IED_NAME1");

        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        extRefInfo.getSourceInfo().setSrcLDInst(extRefInfo.getBindingInfo().getLdInst());
        extRefInfo.getSourceInfo().setSrcLNClass(extRefInfo.getBindingInfo().getLnClass());
        extRefInfo.getSourceInfo().setSrcCBName("goose1");
        TExtRef extRef = assertDoesNotThrow( () -> sclManager.updateExtRefSource(scd,extRefInfo));
        assertEquals(extRefInfo.getSourceInfo().getSrcCBName(),extRef.getSrcCBName());
    }


    private ExtRefSignalInfo createSignalInfo(String pDO, String pDA, String intAddr){

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
        SCL scd = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");
        SclManager sclManager = new SclManager();

        Set<LNodeDTO> nodeDTOS = assertDoesNotThrow(
                ()-> sclManager.getDAI(
                        scd,"IED_NAME1","LD_INST12",new ResumedDataTemplate(),true
                )
        );
        assertEquals(2,nodeDTOS.size());
        LNodeDTO[] ArrayLNodeDTO = nodeDTOS.toArray(new LNodeDTO[0]);
        assertFalse(ArrayLNodeDTO[0].getResumedDataTemplates().isEmpty());
        assertFalse(ArrayLNodeDTO[1].getResumedDataTemplates().isEmpty());

        assertThrows(
                ScdException.class,
                ()-> sclManager.getDAI(
                        scd,"IED_NAME1","UNKNOWNLD",new ResumedDataTemplate(),true
                )
        );
    }
}