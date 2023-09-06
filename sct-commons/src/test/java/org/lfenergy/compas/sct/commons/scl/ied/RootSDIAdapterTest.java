// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TDOI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RootSDIAdapterTest {

    @Test
    @Tag("issue-321")
    void testConstructor() {
        //Given
        TDOI doi = new TDOI();
        doi.setName("Do");
        DOIAdapter doiAdapter = new DOIAdapter(null,doi);
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        doi.getSDIOrDAI().add(tsdi);
        //When Then
        RootSDIAdapter rootSDIAdapter = assertDoesNotThrow(() -> new RootSDIAdapter(doiAdapter,tsdi));
        // test tree map
        TSDI tSdi2 = new TSDI();
        tSdi2.setName("sdo2");
        tsdi.getSDIOrDAI().add(tSdi2);
        //When Then
        assertThatCode(() -> rootSDIAdapter.getStructuredDataAdapterByName("sdo2"))
                .doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> rootSDIAdapter.getStructuredDataAdapterByName("sdo3"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown SDI (sdo3) in this DOI or SDI (sdo1)");
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        //When Then
        assertThatCode(() -> rootSDIAdapter.getDataAdapterByName("angRef")).doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> rootSDIAdapter.getStructuredDataAdapterByName("bda"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown SDI (bda) in this DOI or SDI (sdo1)");
        //When Then
        assertThatCode(() -> rootSDIAdapter.getDataAdapterByName("bda"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("Unknown DAI (bda) in this SDI (sdo1)");
    }

    @Test
    void innerDAIAdapter_should_not_throw_exception(){
        //Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        //When Then
        assertThatCode(() -> new RootSDIAdapter.DAIAdapter(rootSDIAdapter,tdai)).doesNotThrowAnyException();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        //Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(rootSDIAdapter.getCurrentElem().getPrivate()).isEmpty();
        //When
        rootSDIAdapter.addPrivate(tPrivate);
        //Then
        assertThat(rootSDIAdapter.getCurrentElem().getPrivate()).hasSize(1);
    }

    @Test
    void rootSDIAdapter_elementXPath_should_return_expected_xpath_value() {
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
    @Tag("issue-321")
    void daiAdapter_elementXPath_should_return_expected_xpath_value() {
        // Given
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo1");
        RootSDIAdapter rootSDIAdapter = new RootSDIAdapter(null,tsdi);
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tsdi.getSDIOrDAI().add(tdai);
        // When Then
        RootSDIAdapter.DAIAdapter daiAdapter = assertDoesNotThrow(() -> new RootSDIAdapter.DAIAdapter(rootSDIAdapter,tdai));
        // When
        String result = daiAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DAI[@name=\"angRef\"]");
    }

}
