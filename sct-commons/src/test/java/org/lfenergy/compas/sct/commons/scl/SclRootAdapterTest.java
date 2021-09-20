// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.junit.jupiter.api.Assertions.*;

public class SclRootAdapterTest {


    @Test
    public void testConstruction(){
        SclRootAdapter sclRootAdapter = new SclRootAdapter();
        assertAll("ROOT",
                () -> assertNull(sclRootAdapter.parentAdapter),
                () -> assertEquals((short)4,sclRootAdapter.getRelease()),
                () -> assertEquals("B",sclRootAdapter.getRevision()),
                () -> assertEquals("2007",sclRootAdapter.getVersion()),
                () -> assertTrue(sclRootAdapter.amChildElementRef())
        );

        assertDoesNotThrow(() -> new SclRootAdapter(new SCL()));
    }

    @Test
    public void testAddHeader(){
        SclRootAdapter sclRootAdapter = new SclRootAdapter();
        assertDoesNotThrow(() ->
                sclRootAdapter.addHeader("hID","hVersion","hRevision"));

        assertThrows(ScdException.class,
                () ->  sclRootAdapter.addHeader("hID1","hVersion1","hRevision1"));
    }

}