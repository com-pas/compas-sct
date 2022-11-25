// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConnectedAPAdapterTest {

    private SubNetworkAdapter subNetworkAdapter;

    @BeforeEach
    void setUp() {
        subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        TConnectedAP tConnectedAP = new TConnectedAP();
        tConnectedAP.setIedName(DTO.HOLDER_IED_NAME);
        tConnectedAP.setApName(DTO.AP_NAME);
        subNetworkAdapter.getCurrentElem().getConnectedAP().add(tConnectedAP);
    }

    @Test
    void testAmChildElementRef() {
        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME)
        );

        assertEquals(DTO.HOLDER_IED_NAME, connectedAPAdapter.getIedName());
        assertEquals(DTO.AP_NAME, connectedAPAdapter.getApName());
    }

    @Test
    void addPrivate() {
        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(connectedAPAdapter.getCurrentElem().getPrivate().isEmpty());
        connectedAPAdapter.addPrivate(tPrivate);
        assertEquals(1, connectedAPAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void testCopyAddressAndPhysConnFromIcd_withFilledCommunication() throws Exception {
        // GIVEN
        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME)
        );

        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");
        SclRootAdapter icdRootAdapter = new SclRootAdapter(icd);
        Optional<SCL> opIcd = Optional.of(icdRootAdapter.getCurrentElem());

        // WHEN
        connectedAPAdapter.copyAddressAndPhysConnFromIcd(opIcd);

        // THEN
        assertThat(connectedAPAdapter.getCurrentElem().getAddress()).isNotNull();
        assertThat(connectedAPAdapter.getCurrentElem().getPhysConn()).isNotNull();
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).isEmpty();
    }

    @Test
    void testCopyAddressAndPhysConnFromIcd_withEmptyIcd() {
        // GIVEN
        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME)
        );
        Optional<SCL> opIcd = Optional.empty();

        // WHEN
        connectedAPAdapter.copyAddressAndPhysConnFromIcd(opIcd);

        // THEN
        assertThat(connectedAPAdapter.getCurrentElem().getAddress()).isNull();
        assertThat(connectedAPAdapter.getCurrentElem().getPhysConn()).isEmpty();
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {"IED_NAME;AP_NAME;ConnectedAP[@apName=\"IED_NAME\" and @iedName=\"IED_NAME\"]", ";;ConnectedAP[not(@apName) and not(@iedName)]"}
            , delimiter = ';')
    void elementXPath(String iedName, String apName, String message) {
        // Given
        TConnectedAP tConnectedAP = new TConnectedAP();
        tConnectedAP.setApName(iedName);
        tConnectedAP.setIedName(apName);
        ConnectedAPAdapter connectedAPAdapter = new ConnectedAPAdapter(null, tConnectedAP);
        // When
        String elementXPath = connectedAPAdapter.elementXPath();
        // Then
        assertThat(elementXPath).isEqualTo(message);
    }

}
