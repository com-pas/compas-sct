// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;

import static org.junit.jupiter.api.Assertions.*;

public class IEDAdapterTest {

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
}