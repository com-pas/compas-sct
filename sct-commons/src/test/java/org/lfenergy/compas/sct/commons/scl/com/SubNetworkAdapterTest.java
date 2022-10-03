// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubNetworkAdapterTest {

    @Test
    void testAmChildElementRef() {
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName("sName");
        tSubNetwork.setType("sType");
        communicationAdapter.getCurrentElem().getSubNetwork().add(tSubNetwork);

        SubNetworkAdapter subNetworkAdapter = assertDoesNotThrow(
                () -> new SubNetworkAdapter(communicationAdapter,tSubNetwork)
        );

        assertEquals("sName",subNetworkAdapter.getName());
        assertEquals("sType",subNetworkAdapter.getType());

    }


    @Test
    void testAddConnectedAP() {
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        assertTrue(subNetworkAdapter.getCurrentElem().getConnectedAP().isEmpty());
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        assertEquals(1,subNetworkAdapter.getCurrentElem().getConnectedAP().size());
        TConnectedAP tConnectedAP = subNetworkAdapter.getCurrentElem().getConnectedAP().get(0);
        assertEquals(DTO.HOLDER_IED_NAME, tConnectedAP.getIedName());
        assertEquals(DTO.AP_NAME, tConnectedAP.getApName());

        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        assertEquals(1,subNetworkAdapter.getCurrentElem().getConnectedAP().size());
    }


    @Test
    void getConnectedAPAdapters() {
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        assertTrue(subNetworkAdapter.getCurrentElem().getConnectedAP().isEmpty());
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        assertEquals(1,subNetworkAdapter.getConnectedAPAdapters().size());

        assertDoesNotThrow( () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME,DTO.AP_NAME));

        assertThrows(
                ScdException.class,
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME,DTO.AP_NAME + "1")
        );
    }

    @Test
    void addPrivate() {
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(subNetworkAdapter.getCurrentElem().getPrivate().isEmpty());
        subNetworkAdapter.addPrivate(tPrivate);
        assertEquals(1, subNetworkAdapter.getCurrentElem().getPrivate().size());
    }

    @ParameterizedTest
    @CsvSource(value = {"sName;sType;SubNetwork[@name=\"sName\"]", ";;SubNetwork[not(@name)]"}
            , delimiter = ';')
    void elementXPath(String sName, String sType, String message) {
        // Given
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName(sName);
        tSubNetwork.setType(sType);
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, tSubNetwork);
        // When
        String elementXPath = subNetworkAdapter.elementXPath();
        // Then
        assertThat(elementXPath).isEqualTo(message);

    }

}