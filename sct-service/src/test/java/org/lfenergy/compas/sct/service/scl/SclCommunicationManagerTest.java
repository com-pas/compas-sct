// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service.scl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TAccessPoint;
import org.lfenergy.compas.scl.TConnectedAP;
import org.lfenergy.compas.scl.TIED;
import org.lfenergy.compas.scl.TSubNetwork;
import org.lfenergy.compas.exception.ScdException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class SclCommunicationManagerTest {

    private static final String IED_NAME = "IED_NAME";
    private static final String AP_NAME = "AP_NAME";
    private static final String SUBNETWORK = "SUBNETWORK";


    @Test
    void shouldReturnOKWhenAddSubnetworkCaseReceiverHasNoCommunication() throws ScdException {
        SCL receiver = createSCLWithIEDAndAccessPoint();
        SclCommunicationManager sclCommunicationManager = new SclCommunicationManager(receiver);

        receiver = sclCommunicationManager.addSubnetwork(SUBNETWORK,"IP",IED_NAME,AP_NAME);
        assertNotNull(receiver.getCommunication());
        assertNotNull(receiver.getCommunication().getSubNetwork());
        assertFalse(receiver.getCommunication().getSubNetwork().isEmpty());
        TSubNetwork subNetwork = receiver.getCommunication().getSubNetwork()
                .stream()
                .filter(sn -> sn.getName().equals(SUBNETWORK))
                .findFirst()
                .orElse(null);
        assertNotNull(subNetwork);

        TConnectedAP cAP = subNetwork.getConnectedAP()
                .stream()
                .filter(connectedAP -> connectedAP.getIedName().equals(IED_NAME) && connectedAP.getApName().equals(AP_NAME) )
                .findFirst()
                .orElse(null);
        assertNotNull(cAP);

    }


    @Test
    void ShouldReturnNOKWhenAddSubnetworkCauseUnknownIED() throws ScdException {

        SCL receiver = createMinimalSCL();

        SclCommunicationManager sclCommunicationManager = new SclCommunicationManager(receiver);

        assertThrows(ScdException.class, () -> sclCommunicationManager.addSubnetwork(SUBNETWORK,"IP",IED_NAME,AP_NAME));

    }

    @Test
    void ShouldReturnNOKWhenAddSubnetworkCauseUnknownIEDAccessPoint() {
        SCL receiver = createMinimalSCL();
        TIED ied = new TIED();
        ied.setName(IED_NAME);
        receiver.getIED().add(ied);

        SclCommunicationManager sclCommunicationManager = new SclCommunicationManager(receiver);

        assertThrows(ScdException.class, () -> sclCommunicationManager.addSubnetwork(SUBNETWORK,"IP",IED_NAME,AP_NAME));
    }

    private SCL createMinimalSCL(){
        return SclManager.initialize("hID","hVersion","hRevision");
    }

    private SCL createSCLWithIEDAndAccessPoint(){
        SCL receiver = createMinimalSCL();
        TIED ied = new TIED();
        ied.setName(IED_NAME);
        TAccessPoint accessPoint = new TAccessPoint();
        accessPoint.setName(AP_NAME);
        ied.getAccessPoint().add(accessPoint);

        receiver.getIED().add(ied);
        return receiver;
    }
}