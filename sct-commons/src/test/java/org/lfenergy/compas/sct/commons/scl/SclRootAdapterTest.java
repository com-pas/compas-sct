// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SclRootAdapterTest {


    @Test
    void testConstruction() {
        AtomicReference<SclRootAdapter> sclRootAdapter = new AtomicReference<>();
        assertDoesNotThrow(() ->
                sclRootAdapter.set(new SclRootAdapter("hID", "hVersion", "hRevision"))
        );

        assertThrows(ScdException.class,
                () ->  sclRootAdapter.get().addHeader("hID1","hVersion1","hRevision1"));

        assertTrue(sclRootAdapter.get().amChildElementRef());
        assertEquals(SclRootAdapter.RELEASE,sclRootAdapter.get().getSclRelease());
        assertEquals(SclRootAdapter.VERSION,sclRootAdapter.get().getSclVersion());
        assertEquals(SclRootAdapter.REVISION,sclRootAdapter.get().getSclRevision());
        assertIsMarshallable(sclRootAdapter.get().getCurrentElem());

        assertThrows(IllegalArgumentException.class, () -> new SclRootAdapter(new SCL()));
    }

    @Test
    void addIED() throws Exception {

        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd1_to_add_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd2_to_add_test.xml");

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        assertThrows(ScdException.class, () -> sclRootAdapter.addIED(new SCL(), "IED_NAME1"));
        assertDoesNotThrow(() -> sclRootAdapter.addIED(icd1, "IED_NAME1"));
        assertThrows(ScdException.class, () -> sclRootAdapter.addIED(icd1, "IED_NAME1"));
        assertDoesNotThrow(() -> sclRootAdapter.addIED(icd2, "IED_NAME2"));
        assertIsMarshallable(scd);
    }

    @Test
    void addPrivate() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(sclRootAdapter.getCurrentElem().getPrivate().isEmpty());
        sclRootAdapter.addPrivate(tPrivate);
        assertEquals(1, sclRootAdapter.getCurrentElem().getPrivate().size());
        assertIsMarshallable(scd);
    }
}
