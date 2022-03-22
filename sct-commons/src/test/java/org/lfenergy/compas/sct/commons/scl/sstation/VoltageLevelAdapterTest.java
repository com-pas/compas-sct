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

class VoltageLevelAdapterTest {

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
    }

    @Test
    void testAmChildElementRef() {
       assertTrue(vLevelAdapter.amChildElementRef());
    }

    @Test
    void testSetCurrentElemInAdapter() throws ScdException {
        TVoltageLevel tVoltageLevel1 = new TVoltageLevel();
        assertThrows(IllegalArgumentException.class,
                () ->vLevelAdapter.setCurrentElem(tVoltageLevel1));
    }

    @Test
    void testGetBayAdapter() {
        TBay tBay = new TBay();
        tBay.setName("BAY");
        vLevelAdapter.getCurrentElem().getBay().add(tBay);
        assertFalse(vLevelAdapter.getBayAdapter("BAY1").isPresent());
    }
}