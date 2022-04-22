// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Optional;

public class ConnectedAPAdapter extends SclElementAdapter<SubNetworkAdapter, TConnectedAP> {

    public ConnectedAPAdapter(SubNetworkAdapter parentAdapter, TConnectedAP currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getConnectedAP().contains(currentElem);
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    public String getIedName() {
        return currentElem.getIedName();
    }

    public String getApName() {
        return currentElem.getApName();
    }

    public void copyAddressAndPhysConnFromIcd(Optional<SCL> icd) {
        if (icd.isPresent() && icd.get().getCommunication() != null) {
            icd.stream()
                    .map(SCL::getCommunication)
                    .findFirst()
                    .flatMap(com -> com.getSubNetwork().stream()
                            .flatMap(subNet -> subNet.getConnectedAP().stream()
                                    .filter(connAP -> connAP.getApName().equals(currentElem.getApName())))
                            .findFirst()).ifPresent(connectedAP -> {
                        currentElem.setAddress(connectedAP.getAddress());
                        currentElem.getPhysConn().addAll(connectedAP.getPhysConn());
                    });

        }
    }
}
