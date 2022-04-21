// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.junit.jupiter.api.Assertions.*;

class RootSDIAdapterTest {
    @Test
    void testConstructor() {
        TDOI doi = new TDOI();
        doi.setName("Do");
        DOIAdapter doiAdapter = new DOIAdapter(null,doi);
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        doi.getSDIOrDAI().add(tsdi);
        RootSDIAdapter rootSDIAdapter = assertDoesNotThrow(() -> new RootSDIAdapter(doiAdapter,tsdi));

        // test tree map
        TSDI tSdi2 = new TSDI();
        tSdi2.setName("sdo2");
        tsdi.getSDIOrDAI().add(tSdi2);
        assertDoesNotThrow(() -> rootSDIAdapter.getStructuredDataAdapterByName("sdo2"));
        assertThrows(ScdException.class, () -> rootSDIAdapter.getStructuredDataAdapterByName("sdo3"));
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        assertDoesNotThrow(() -> rootSDIAdapter.getDataAdapterByName("angRef"));
        assertThrows(ScdException.class, () -> rootSDIAdapter.getStructuredDataAdapterByName("bda"));
        assertThrows(ScdException.class, () -> rootSDIAdapter.getDataAdapterByName("bda"));
    }

    @Test
    void testInnerDAIAdapter(){
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);

        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        RootSDIAdapter.DAIAdapter daiAdapter = assertDoesNotThrow(() -> new RootSDIAdapter.DAIAdapter(rootSDIAdapter,tdai));

        // test tree map
        assertThrows(UnsupportedOperationException.class, () -> daiAdapter.getDataAdapterByName("toto"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> daiAdapter.getStructuredDataAdapterByName("toto")
        );
    }

}