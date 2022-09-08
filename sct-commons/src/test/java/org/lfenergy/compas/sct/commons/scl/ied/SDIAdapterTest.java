// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SDIAdapterTest {
    @Test
    void testConstructor() {
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);

        TSDI tSdi1 = new TSDI();
        tSdi1.setName("sdo2");
        tsdi.getSDIOrDAI().add(tSdi1);
        // test amChildElement
        SDIAdapter sdiAdapter = assertDoesNotThrow(() -> new SDIAdapter(rootSDIAdapter,tSdi1));

        // test tree map
        TSDI tSdi2 = new TSDI();
        tSdi2.setName("sdo2");
        tSdi1.getSDIOrDAI().add(tSdi2);
        assertDoesNotThrow(() -> sdiAdapter.getStructuredDataAdapterByName("sdo2"));
        assertThrows(ScdException.class, () -> sdiAdapter.getStructuredDataAdapterByName("sdo3"));
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tSdi1.getSDIOrDAI().add(tdai);
        assertDoesNotThrow(() -> sdiAdapter.getDataAdapterByName("angRef"));
        assertThrows(ScdException.class, () -> sdiAdapter.getStructuredDataAdapterByName("bda"));
        assertThrows(ScdException.class, () -> sdiAdapter.getDataAdapterByName("bda"));
    }


    @Test
    void testInnerDAIAdapter(){
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        SDIAdapter sdiAdapter = new SDIAdapter(null,tsdi);

        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        SDIAdapter.DAIAdapter daiAdapter = assertDoesNotThrow(() -> new SDIAdapter.DAIAdapter(sdiAdapter,tdai));

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
        SDIAdapter sdiAdapter = new SDIAdapter(null,tsdi);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(sdiAdapter.getCurrentElem().getPrivate().isEmpty());
        sdiAdapter.addPrivate(tPrivate);
        assertEquals(1, sdiAdapter.getCurrentElem().getPrivate().size());
    }

    @ParameterizedTest
    @CsvSource(value = {"sdo1;SDI[@name=\"sdo1\"]", ";SDI[not(@name)]"}
            , delimiter = ';')
    void elementXPath(String sdo, String message) {
        // Given
        TSDI tsdi = new TSDI();
        tsdi.setName(sdo);
        SDIAdapter sdiAdapter = new SDIAdapter(null,tsdi);
        // When
        String sdiAdapterResult = sdiAdapter.elementXPath();
        // Then
        assertThat(sdiAdapterResult).isEqualTo(message);
    }

}