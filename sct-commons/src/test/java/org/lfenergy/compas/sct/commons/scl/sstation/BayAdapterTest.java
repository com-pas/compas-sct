// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.junit.jupiter.api.Assertions.*;

class BayAdapterTest {

    private BayAdapter bayAdapter;
    private VoltageLevelAdapter vLevelAdapter;

    @BeforeEach
    public void init() throws ScdException {

        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TSubstation tSubstation = new TSubstation();
        tSubstation.setName("SUBSTATION");
        sclRootAdapter.getCurrentElem().getSubstation().add(tSubstation);
        SubstationAdapter ssAdapter = sclRootAdapter.getSubstationAdapter("SUBSTATION");
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.setName("VOLTAGE_LEVEL");
        ssAdapter.getCurrentElem().getVoltageLevel().add(tVoltageLevel);
        vLevelAdapter = ssAdapter.getVoltageLevelAdapter("VOLTAGE_LEVEL").get();
        TBay tBay = new TBay();
        tBay.setName("BAY");
        vLevelAdapter.getCurrentElem().getBay().add(tBay);
        bayAdapter = vLevelAdapter.getBayAdapter("BAY").get();

    }

    @Test
    void testAmChildElementRef() {
        assertTrue(bayAdapter.amChildElementRef());
    }

    @Test
    void testController() {
        BayAdapter expectedBayAdapter = new BayAdapter(vLevelAdapter);
        assertNotNull(expectedBayAdapter.getParentAdapter());
        assertNull(expectedBayAdapter.getCurrentElem());
        assertFalse(expectedBayAdapter.amChildElementRef());
    }

    @Test
    void testControllerWithVoltageLevelName() {
        assertThrows(ScdException.class,
                () -> new BayAdapter(vLevelAdapter, "BAY_1"));
    }

    @Test
    void testSetCurrentElemInAdapter() {
        TBay tBay1 = new TBay();
        assertThrows(IllegalArgumentException.class,
                () ->bayAdapter.setCurrentElem(tBay1));
    }
}