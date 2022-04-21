// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.junit.jupiter.api.Assertions.*;

class SubstationAdapterTest {

    @Test
    void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TSubstation tSubstation = new TSubstation();
        tSubstation.setName("SUBSTATION");
        sclRootAdapter.getCurrentElem().getSubstation().add(tSubstation);
        SubstationAdapter ssAdapter = sclRootAdapter.getSubstationAdapter("SUBSTATION");
        assertTrue(ssAdapter.amChildElementRef());

        SubstationAdapter ssfAdapter = new SubstationAdapter(sclRootAdapter);
        TSubstation tSubstation1 = new TSubstation();
        assertThrows(IllegalArgumentException.class,
                () ->ssfAdapter.setCurrentElem(tSubstation1));

        assertThrows(ScdException.class,
                () -> sclRootAdapter.getSubstationAdapter("SUBSTATION1"));

    }
}