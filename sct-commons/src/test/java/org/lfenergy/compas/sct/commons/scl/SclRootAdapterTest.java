// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclHelper;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller.assertIsMarshallable;

class SclRootAdapterTest {

    @Test
    @Tag("issue-321")
    void testConstruction() {
        // Given
        AtomicReference<SclRootAdapter> sclRootAdapterAtomicReference = new AtomicReference<>();
        assertThatCode(() -> sclRootAdapterAtomicReference.set(new SclRootAdapter("hID", "hVersion", "hRevision")))
                .doesNotThrowAnyException();
        SclRootAdapter sclRootAdapter = sclRootAdapterAtomicReference.get();
        // When Then
        // addHeader is not the purpose of this test
        assertThatCode(() -> sclRootAdapter.addHeader("hID1","hVersion1","hRevision1"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("SCL already contains header");
        assertThat(sclRootAdapter.amChildElementRef()).isTrue();
        assertThat(sclRootAdapter.getSclRelease()).isEqualTo(SclRootAdapter.RELEASE);
        assertThat(sclRootAdapter.getSclVersion()).isEqualTo(SclRootAdapter.VERSION);
        assertThat(sclRootAdapter.getSclRevision()).isEqualTo(SclRootAdapter.REVISION);
        assertIsMarshallable(sclRootAdapter.getCurrentElem());
        // Given
        SCL scd = new SCL();
        // When Then
        assertThatCode(() -> new SclRootAdapter(scd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid SCD: no tag Header found");
    }

    @Test
    // Test name should be modified to reflect each test case.
    @Tag("issue-321")
    void testAddIED() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd1_to_add_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd2_to_add_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        SCL icd = new SCL();
        //When Then
        assertThatCode(() -> sclRootAdapter.addIED(icd, "IED_NAME1"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("No IED to import from ICD file");
        //When Then
        assertThatCode(() -> sclRootAdapter.addIED(icd1, "IED_NAME1"))
                .doesNotThrowAnyException();
        //When Then
        assertThatCode(() -> sclRootAdapter.addIED(icd1, "IED_NAME1"))
                .isInstanceOf(ScdException.class)
                .hasMessageContaining("SCL file already contains IED: IED_NAME1");
        //When Then
        assertThatCode(() -> sclRootAdapter.addIED(icd2, "IED_NAME2"))
                .doesNotThrowAnyException();
        assertIsMarshallable(scd);
    }

    @Test
    void addPrivate_should_create_private() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(sclRootAdapter.getCurrentElem().getPrivate()).isEmpty();
        //When
        sclRootAdapter.addPrivate(tPrivate);
        //Then
        assertThat(sclRootAdapter.getCurrentElem().getPrivate()).hasSize(1);
        assertIsMarshallable(scd);
    }

    @Test
    void getIEDAdapterByName_should_return_ied(){
        // Given
        SclRootAdapter sclRootAdapter = SclHelper.createSclRootAdapterWithIed("IED_NAME");
        // When
        IEDAdapter resultIed = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        // Then
        assertThat(resultIed.getName()).isEqualTo("IED_NAME");
    }

    @Test
    void getIEDAdapterByName_should_throw_exception(){
        // Given
        SclRootAdapter sclRootAdapter = SclHelper.createSclRootAdapterWithIed("IED_NAME");
        // When & Then
        assertThatThrownBy(() -> sclRootAdapter.getIEDAdapterByName("NON_EXISTING_IED"))
            .isInstanceOf(ScdException.class)
            .hasMessage("IED.name 'NON_EXISTING_IED' not found in SCD");
    }

    @Test
    void findIEDAdapterByName_should_return_ied(){
        // Given
        SclRootAdapter sclRootAdapter = SclHelper.createSclRootAdapterWithIed("IED_NAME");
        // When
        Optional<IEDAdapter> resultOptionalIed = sclRootAdapter.findIedAdapterByName("IED_NAME");
        // Then
        assertThat(resultOptionalIed).isPresent();
        assertThat(resultOptionalIed.get().getName()).isEqualTo("IED_NAME");
    }

    @Test
    void findIEDAdapterByName_should_return_empty(){
        // Given
        SclRootAdapter sclRootAdapter = SclHelper.createSclRootAdapterWithIed("IED_NAME");
        // When
        Optional<IEDAdapter> resultOptionalIed = sclRootAdapter.findIedAdapterByName("NON_EXISTING_IED");
        // Then
        assertThat(resultOptionalIed).isEmpty();
    }

    @Test
    void findConnectedApAdapter_should_return_adapter(){
        // Given
        SclRootAdapter sclRootAdapter = SclHelper.createSclRootWithConnectedAp("iedName", "apName");
        // When
        Optional<ConnectedAPAdapter> result = sclRootAdapter.findConnectedApAdapter("iedName", "apName");
        // Then
        assertThat(result).get().extracting(ConnectedAPAdapter::getIedName, ConnectedAPAdapter::getApName)
            .containsExactly("iedName", "apName");
    }

    @Test
    void findConnectedApAdapter_should_return_empty(){
        // Given
        SclRootAdapter sclRootAdapter = SclHelper.createSclRootWithConnectedAp("iedName", "apName");
        // When
        Optional<ConnectedAPAdapter> result = sclRootAdapter.findConnectedApAdapter("iedName2", "apName2");
        // Then
        assertThat(result).isEmpty();
    }

}
