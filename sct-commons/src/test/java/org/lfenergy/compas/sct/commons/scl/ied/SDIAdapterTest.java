// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SDIAdapterTest {

    @Test
    @Tag("issue-321")
    void testConstructor() {
        //Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);
        TSDI tSdi1 = new TSDI();
        tSdi1.setName("sdo2");
        tsdi.getSDIOrDAI().add(tSdi1);
        //When Then
        SDIAdapter sdiAdapter = assertDoesNotThrow(() -> new SDIAdapter(rootSDIAdapter,tSdi1));
        // test tree map
        TSDI tSdi2 = new TSDI();
        tSdi2.setName("sdo2");
        tSdi1.getSDIOrDAI().add(tSdi2);
        //When Then
        assertThatCode(() -> sdiAdapter.getStructuredDataAdapterByName("sdo2"))
                .doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> sdiAdapter.getStructuredDataAdapterByName("sdo3"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown SDI (sdo3) in this DOI or SDI (sdo2)");
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tSdi1.getSDIOrDAI().add(tdai);
        //When Then
        assertThatCode(() -> sdiAdapter.getDataAdapterByName("angRef")).doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> sdiAdapter.getStructuredDataAdapterByName("bda"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown SDI (bda) in this DOI or SDI (sdo2)");
        //When Then
        assertThatCode(() -> sdiAdapter.getDataAdapterByName("bda"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown DAI (bda) in this SDI (sdo2)");
    }


    @Test
    @Tag("issue-321")
    void innerDAIAdapter_should_not_throw_exception(){
        //Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        //When
        SDIAdapter sdiAdapter = new SDIAdapter(null,tsdi);
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        //When Then
        assertDoesNotThrow(() -> new SDIAdapter.DAIAdapter(sdiAdapter,tdai));
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        //Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        SDIAdapter sdiAdapter = new SDIAdapter(null,tsdi);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(sdiAdapter.getCurrentElem().getPrivate()).isEmpty();
        //When
        sdiAdapter.addPrivate(tPrivate);
        //Then
        assertThat(sdiAdapter.getCurrentElem().getPrivate()).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource(value = {"sdo1;SDI[@name=\"sdo1\"]", ";SDI[not(@name)]"}, delimiter = ';')
    void elementXPath_should_return_expected_xpath_value(String sdo, String message) {
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
