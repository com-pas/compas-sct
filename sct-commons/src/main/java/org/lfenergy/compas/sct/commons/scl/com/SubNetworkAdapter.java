// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SubNetworkAdapter extends SclElementAdapter<CommunicationAdapter, TSubNetwork> {

    public SubNetworkAdapter(CommunicationAdapter parentAdapter, TSubNetwork currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSubNetwork().contains(currentElem);
    }

    public ConnectedAPAdapter addConnectedAP(String iedName, String apName) {
        TConnectedAP tConnectedAP = currentElem.getConnectedAP().stream()
                .filter(cap -> Objects.equals(cap.getApName(),apName) &&
                        Objects.equals(cap.getIedName(),iedName))
                .findFirst()
                .orElse(null);

        if(tConnectedAP == null){
            tConnectedAP = new TConnectedAP();
            tConnectedAP.setApName(apName);
            tConnectedAP.setIedName(iedName);
            currentElem.getConnectedAP().add(tConnectedAP);
        }
        return new ConnectedAPAdapter(this,tConnectedAP);
    }

    public String getName() {
        return currentElem.getName();
    }

    public String getType() {
        return currentElem.getType();
    }

    public List<ConnectedAPAdapter> getConnectedAPAdapters() {
        return currentElem.getConnectedAP()
                .stream()
                .map(ap -> new ConnectedAPAdapter(this,ap))
                .collect(Collectors.toList());
    }
}
