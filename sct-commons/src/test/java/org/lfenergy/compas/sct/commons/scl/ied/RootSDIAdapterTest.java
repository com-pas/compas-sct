// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void addPrivate() {
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(rootSDIAdapter.getCurrentElem().getPrivate().isEmpty());
        rootSDIAdapter.addPrivate(tPrivate);
        assertEquals(1, rootSDIAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void elementXPath_sdi() {
        // Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);
        // When
        String result = rootSDIAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("SDI[@name=\"sdo1\"]");
    }

    @Test
    void elementXPath_dai() {
        // Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);

        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        RootSDIAdapter.DAIAdapter daiAdapter = assertDoesNotThrow(() -> new RootSDIAdapter.DAIAdapter(rootSDIAdapter,tdai));
        // When
        String result = daiAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DAI[@name=\"angRef\"]");
    }

}