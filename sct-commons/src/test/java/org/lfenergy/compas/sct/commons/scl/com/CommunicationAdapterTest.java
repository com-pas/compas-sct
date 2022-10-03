// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommunicationAdapterTest {

    @Test
    void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setCommunication(new TCommunication());
        CommunicationAdapter cAdapter = sclRootAdapter.getCommunicationAdapter(true);
        assertTrue(cAdapter.amChildElementRef());

        CommunicationAdapter finalCAdapter = new CommunicationAdapter(sclRootAdapter);
        TCommunication tCommunication = new TCommunication();
        assertThrows(IllegalArgumentException.class,
                () -> finalCAdapter.setCurrentElem(tCommunication));
    }

    @Test
    void testAddSubnetwork() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TIED tied = new TIED();
        tied.setName("IED_NAME");
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName("apName");
        TAccessPoint tAccessPoint2 = new TAccessPoint();
        tAccessPoint2.setName("apName2");
        tied.getAccessPoint().add(tAccessPoint);
        tied.getAccessPoint().add(tAccessPoint2);
        sclRootAdapter.getCurrentElem().getIED().add(tied);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);
        assertDoesNotThrow(
                () -> communicationAdapter.addSubnetwork("snName", "snType",
                        "IED_NAME", "apName")
        );
        assertDoesNotThrow(
                () -> communicationAdapter.addSubnetwork("snName", "snType",
                        "IED_NAME", "apName2")
        );

        assertThrows(
                ScdException.class,
                () -> communicationAdapter.addSubnetwork("snName", "snType",
                        "IED_NAME1", "apName")
        );
        assertThrows(
                ScdException.class,
                () -> communicationAdapter.addSubnetwork("snName", "snType",
                        "IED_NAME", "apName1")
        );
    }

    @Test
    void testFindSubnetworkByName() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName("snName");
        communicationAdapter.getCurrentElem().getSubNetwork().add(tSubNetwork);

        assertTrue(communicationAdapter.getSubnetworkByName("snName").isPresent());
        assertFalse(communicationAdapter.getSubnetworkByName("snName_NO").isPresent());
    }

    @Test
    void testGetSubNetworkAdapters(){
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        assertTrue(communicationAdapter.getSubNetworkAdapters().isEmpty());
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName("snName");
        communicationAdapter.getCurrentElem().getSubNetwork().add(tSubNetwork);
        assertFalse(communicationAdapter.getSubNetworkAdapters().isEmpty());
    }

    @Test
    void addPrivate() {
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(communicationAdapter.getCurrentElem().getPrivate().isEmpty());
        communicationAdapter.addPrivate(tPrivate);
        assertEquals(1, communicationAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void elementXPath() {
        // Given
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        // When
        String result = communicationAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Communication");
    }

}