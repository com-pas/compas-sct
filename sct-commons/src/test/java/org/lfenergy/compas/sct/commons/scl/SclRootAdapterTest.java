// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SclRootAdapterTest {


    @Test
    void testConstruction() {
        AtomicReference<SclRootAdapter> sclRootAdapterAtomicReference = new AtomicReference<>();
        assertDoesNotThrow(() ->
                sclRootAdapterAtomicReference.set(new SclRootAdapter("hID", "hVersion", "hRevision"))
        );

        SclRootAdapter sclRootAdapter = sclRootAdapterAtomicReference.get();
        assertThrows(ScdException.class,
                () ->  sclRootAdapter.addHeader("hID1","hVersion1","hRevision1"));

        assertTrue(sclRootAdapter.amChildElementRef());
        assertEquals(SclRootAdapter.RELEASE, sclRootAdapter.getSclRelease());
        assertEquals(SclRootAdapter.VERSION, sclRootAdapter.getSclVersion());
        assertEquals(SclRootAdapter.REVISION, sclRootAdapter.getSclRevision());
        assertIsMarshallable(sclRootAdapter.getCurrentElem());

        SCL scd = new SCL();
        assertThrows(IllegalArgumentException.class, () -> new SclRootAdapter(scd));
    }

    @Test
    void addIED() throws Exception {

        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd1_to_add_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd2_to_add_test.xml");

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        SCL icd = new SCL();
        assertThrows(ScdException.class, () -> sclRootAdapter.addIED(icd, "IED_NAME1"));
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

    @Test
    void getIEDAdapterByName_should_return_ied(){
        // Given
        SclRootAdapter sclRootAdapter = createSclRootAdapterWithIed("IED_NAME");
        // When
        IEDAdapter resultIed = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        // Then
        assertThat(resultIed.getName()).isEqualTo("IED_NAME");
    }

    @Test
    void getIEDAdapterByName_should_throw_exception(){
        // Given
        SclRootAdapter sclRootAdapter = createSclRootAdapterWithIed("IED_NAME");
        // When & Then
        assertThatThrownBy(() -> sclRootAdapter.getIEDAdapterByName("NON_EXISTING_IED"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name 'NON_EXISTING_IED' not found in SCD");
    }

    @Test
    void findIEDAdapterByName_should_return_ied(){
        // Given
        SclRootAdapter sclRootAdapter = createSclRootAdapterWithIed("IED_NAME");
        // When
        Optional<IEDAdapter> resultOptionalIed = sclRootAdapter.findIedAdapterByName("IED_NAME");
        // Then
        assertThat(resultOptionalIed).isPresent();
        assertThat(resultOptionalIed.get().getName()).isEqualTo("IED_NAME");
    }

    @Test
    void findIEDAdapterByName_should_return_empty(){
        // Given
        SclRootAdapter sclRootAdapter = createSclRootAdapterWithIed("IED_NAME");
        // When
        Optional<IEDAdapter> resultOptionalIed = sclRootAdapter.findIedAdapterByName("NON_EXISTING_IED");
        // Then
        assertThat(resultOptionalIed).isEmpty();
    }

    private SclRootAdapter createSclRootAdapterWithIed(String iedName) {
        SCL scl = new SCL();
        scl.setHeader(new THeader());
        TIED ied = new TIED();
        ied.setName(iedName);
        scl.getIED().add(ied);
        return new SclRootAdapter(scl);
    }
}
