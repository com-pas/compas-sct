// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.IedDTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.SclTestMarshaller;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IEDAdapterTest {

    public static final String SCD_IED_U_TEST = "/ied-test-schema-conf/ied_unit_test.xml";


    @Test
    public void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TIED tied = new TIED();
        tied.setName(DTO.IED_NAME);

        tied.setServices(new TServices());
        sclRootAdapter.getCurrentElem().getIED().add(tied);
        IEDAdapter iAdapter = sclRootAdapter.getIEDAdapter(DTO.IED_NAME);
        assertTrue(iAdapter.amChildElementRef());
        assertNotNull(iAdapter.getServices());
        assertEquals(DTO.IED_NAME,iAdapter.getName());

        IEDAdapter fAdapter = new IEDAdapter(sclRootAdapter);
        assertThrows(IllegalArgumentException.class,
                () ->fAdapter.setCurrentElem(new TIED()));

        assertThrows(ScdException.class,
                () -> sclRootAdapter.getIEDAdapter(DTO.IED_NAME + "1"));

    }


    @Test
    void testGetLDeviceAdapters() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile(SCD_IED_U_TEST);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow( () -> sclRootAdapter.getIEDAdapter("IED_NAME"));
        assertFalse(iAdapter.getLDeviceAdapters().isEmpty());
    }

    @Test
    void testGetLDeviceAdapterByLdInst() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile(SCD_IED_U_TEST);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow( () -> sclRootAdapter.getIEDAdapter("IED_NAME"));
        assertTrue(iAdapter.getLDeviceAdapterByLdInst("LD_INS1").isPresent());
    }

    @Test
    void testUpdateLDeviceNodesType() throws Exception {

        SCL scd = SclTestMarshaller.getSCLFromFile(SCD_IED_U_TEST);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow( () -> sclRootAdapter.getIEDAdapter(DTO.IED_NAME));

        assertEquals(2,iAdapter.getLDeviceAdapters().size());
        Map<String,String> pairOldNewId = new HashMap<>();
        pairOldNewId.put("LNO1", DTO.IED_NAME + "_LNO1");
        pairOldNewId.put("LNO2", DTO.IED_NAME + "_LNO2");
        assertDoesNotThrow( () ->iAdapter.updateLDeviceNodesType(pairOldNewId));

        LDeviceAdapter lDeviceAdapter = iAdapter.getLDeviceAdapters().get(0);
        assertEquals(DTO.IED_NAME + "_LNO1",lDeviceAdapter.getLN0Adapter().getLnType());
    }

    @Test
    void testGetExtRefBinders() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapter("IED_NAME"));
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        signalInfo.setPDO("Do.sdo1");
        signalInfo.setPDA("da.bda1.bda2.bda3");
        assertDoesNotThrow(() ->iAdapter.getExtRefBinders(signalInfo));

        signalInfo.setPDO("Do.sdo1.errorSdo");
        assertThrows(ScdException.class, () ->iAdapter.getExtRefBinders(signalInfo));
    }

}