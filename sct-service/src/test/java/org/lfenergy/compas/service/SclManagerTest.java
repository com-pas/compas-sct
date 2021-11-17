// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.service;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
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
import org.lfenergy.compas.sct.commons.dto.IedDTO;
import org.lfenergy.compas.sct.commons.dto.LNodeDTO;
import org.lfenergy.compas.sct.commons.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
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

        assertEquals("IED_NAME1",extRefInfos.get(0).getIedName());

        assertThrows(ScdException.class, () -> sclManager.getExtRefInfo(scd,"IED_NAME1","UNKNOWN_LD"));
    }

    @Test
    void testGetExtRefBinders() throws Exception {
        SclRootAdapter sclRootAdapter=  new SclRootAdapter("hId",SclRootAdapter.VERSION,SclRootAdapter.REVISION);
        SCL scd = sclRootAdapter.getCurrentElem();
        assertNull(sclRootAdapter.getCurrentElem().getDataTypeTemplates());
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_1_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_2_test.xml");
        SCL icd3 = SclTestMarshaller.getSCLFromFile("/import-ieds/ied_3_test.xml");
        SclManager sclManager = new SclManager();
        assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME1",icd1));
        assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME2",icd2));
        assertDoesNotThrow(() -> sclManager.addIED(scd,"IED_NAME3",icd3));

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


}