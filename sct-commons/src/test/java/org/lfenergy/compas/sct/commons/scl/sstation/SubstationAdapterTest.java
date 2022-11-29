// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubstationAdapterTest {

    private SclRootAdapter sclRootAdapter;
    private SubstationAdapter ssAdapter;

    @BeforeEach
    public void init() throws ScdException {
        sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TSubstation tSubstation = new TSubstation();
        tSubstation.setName("SUBSTATION");
        sclRootAdapter.getCurrentElem().getSubstation().add(tSubstation);
        ssAdapter = sclRootAdapter.getSubstationAdapter("SUBSTATION");
    }

    @Test
    void testAmChildElementRef()  {
        assertTrue(ssAdapter.amChildElementRef());
    }

    @Test
    void testController() {
        SubstationAdapter expectedSubstationAdapter = new SubstationAdapter(sclRootAdapter, sclRootAdapter.getCurrentElem().getSubstation().get(0));
        assertNotNull(expectedSubstationAdapter.getParentAdapter());
        assertNotNull(expectedSubstationAdapter.getCurrentElem());
        assertTrue(expectedSubstationAdapter.amChildElementRef());
    }

    @Test
    void testSetCurrentElement()  {
        SubstationAdapter ssfAdapter = new SubstationAdapter(sclRootAdapter);
        TSubstation tSubstation1 = new TSubstation();
        assertThrows(IllegalArgumentException.class,
                () ->ssfAdapter.setCurrentElem(tSubstation1));
    }

    @Test
    void testGetSubstationAdapter()  {
        assertThrows(ScdException.class,
                () -> sclRootAdapter.getSubstationAdapter("SUBSTATION1"));
    }

    @Test
    void testGetVoltageLevelAdapter() {
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.setName("VOLTAGE_LEVEL");
        ssAdapter.getCurrentElem().getVoltageLevel().add(tVoltageLevel);
        assertFalse(ssAdapter.getVoltageLevelAdapter("VOLTAGE_LEVEL1").isPresent());
    }

    @Test
    void addPrivate() {
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(ssAdapter.getCurrentElem().getPrivate().isEmpty());
        ssAdapter.addPrivate(tPrivate);
        assertEquals(1, ssAdapter.getCurrentElem().getPrivate().size());
    }


    @Test
    void getIedAndLDeviceNamesForLN0FromLNode_shouldReturnListOf1Pair_WhenLNodeContainsLN0() throws Exception {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_Template.scd");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        SubstationAdapter substationAdapter = sclRootAdapter.getSubstationAdapter();
        // When
        List<Pair<String, String>> iedNameLdInstList = substationAdapter.getIedAndLDeviceNamesForLN0FromLNode();
        // Then
        assertEquals(1, iedNameLdInstList.size());
        assertThat(iedNameLdInstList).containsExactly(Pair.of("IedName1", "LDSUIED"));
    }

}
