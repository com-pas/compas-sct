// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.SclTestMarshaller;

import static org.junit.jupiter.api.Assertions.*;

public class IEDAdapterTest {

    public static final String SCD_IED_U_TEST = "/ied-test-schema-conf/ied_unit_test.xml";


    @Test
    public void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TIED tied = new TIED();
        tied.setName("IED_NAME");
        sclRootAdapter.getCurrentElem().getIED().add(tied);
        IEDAdapter iAdapter = sclRootAdapter.getIEDAdapter("IED_NAME");
        assertTrue(iAdapter.amChildElementRef());

        IEDAdapter fAdapter = new IEDAdapter(sclRootAdapter);
        assertThrows(IllegalArgumentException.class,
                () ->fAdapter.setCurrentElem(new TIED()));

        assertThrows(ScdException.class,
                () -> sclRootAdapter.getIEDAdapter("IED_NAME1"));

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
    void updateLDName() {
    }
}