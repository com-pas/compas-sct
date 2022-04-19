// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.dto.DTO;

import static org.junit.jupiter.api.Assertions.*;

class ConnectedAPAdapterTest {

    @Test
    void testAmChildElementRef() {
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        TConnectedAP tConnectedAP = new TConnectedAP();
        tConnectedAP.setIedName(DTO.HOLDER_IED_NAME);
        tConnectedAP.setApName(DTO.AP_NAME);
        subNetworkAdapter.getCurrentElem().getConnectedAP().add(tConnectedAP);

        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () ->subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME,DTO.AP_NAME)
        );

        assertEquals(DTO.HOLDER_IED_NAME,connectedAPAdapter.getIedName());
        assertEquals(DTO.AP_NAME,connectedAPAdapter.getApName());
    }

    @Test
    void addPrivate() throws Exception {
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        TConnectedAP tConnectedAP = new TConnectedAP();
        tConnectedAP.setIedName(DTO.HOLDER_IED_NAME);
        tConnectedAP.setApName(DTO.AP_NAME);
        subNetworkAdapter.getCurrentElem().getConnectedAP().add(tConnectedAP);
        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(connectedAPAdapter.getCurrentElem().getPrivate().isEmpty());
        connectedAPAdapter.addPrivate(tPrivate);
        assertEquals(1, connectedAPAdapter.getCurrentElem().getPrivate().size());
    }
}