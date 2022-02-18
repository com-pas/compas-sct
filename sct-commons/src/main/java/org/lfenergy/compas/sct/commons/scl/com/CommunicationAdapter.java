
// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;


import org.lfenergy.compas.scl2007b4.model.TCommunication;

import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class CommunicationAdapter extends SclElementAdapter<SclRootAdapter, TCommunication> {

    public CommunicationAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    public CommunicationAdapter(SclRootAdapter parentAdapter, TCommunication currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    public boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getCommunication();
    }

    public SubNetworkAdapter addSubnetwork(String snName, String snType,
                                           String iedName, String apName) throws ScdException {

        IEDAdapter iedAdapter = parentAdapter.getIEDAdapterByName(iedName);
        if(!iedAdapter.findAccessPointByName(apName)){
            throw new ScdException("Unknown AccessPoint :" + apName + " in IED :" + iedName);
        }
        Optional<SubNetworkAdapter> opSubNetworkAdapter = getSubnetworkByName(snName);
        if(!opSubNetworkAdapter.isPresent()){ // create new subnetwork
            TSubNetwork subNetwork = new TSubNetwork();
            subNetwork.setName(snName);
            subNetwork.setType(snType);
            currentElem.getSubNetwork().add(subNetwork);
            opSubNetworkAdapter = Optional.of(new SubNetworkAdapter(this,subNetwork));
        }

        opSubNetworkAdapter.get().addConnectedAP(iedName,apName);

        return opSubNetworkAdapter.get();
    }

    public Optional<SubNetworkAdapter> getSubnetworkByName(String snName) {
        return currentElem.getSubNetwork()
                .stream()
                .filter(tSubNetwork -> tSubNetwork.getName().equals(snName))
                .findFirst()
                .map(tSubNetwork -> new SubNetworkAdapter(this,tSubNetwork));
    }

    public List<SubNetworkAdapter> getSubNetworkAdapters() {
        return currentElem.getSubNetwork()
                .stream()
                .map(tSubNetwork -> new SubNetworkAdapter(this,tSubNetwork))
                .collect(Collectors.toList());

    }
}
