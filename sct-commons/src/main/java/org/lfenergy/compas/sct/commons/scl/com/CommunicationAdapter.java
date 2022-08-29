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

    /**
     * Constructor
     * @param parentAdapter
     */
    public CommunicationAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    /**
     * Constructor
     * @param parentAdapter input
     * @param currentElem input
     */
    public CommunicationAdapter(SclRootAdapter parentAdapter, TCommunication currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    public boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getCommunication();
    }

    /**
     * Add Subnetwork node in Communication one.
     * For that :
     * <ul>
     *     <li> coherence is checked first one AccessPoint's name between given
     *      * data and IED/Services/AccessPoint</li>
     *      <li>If good, Subnetworks are created</li>
     *      <li>And then AccessPoint are created two in the Subnetwork</li>
     * </ul>.
     * @param snName Subnetwork name
     * @param snType Subnetwork type
     * @param iedName IED name
     * @param apName AccessPoint name
     * @return SubNetworkAdapter object
     * @throws ScdException
     */
    public SubNetworkAdapter addSubnetwork(String snName, String snType,
                                           String iedName, String apName) throws ScdException {

        IEDAdapter iedAdapter = parentAdapter.getIEDAdapterByName(iedName);
        if (!iedAdapter.findAccessPointByName(apName)) {
            throw new ScdException("Unknown AccessPoint :" + apName + " in IED :" + iedName);
        }
        Optional<SubNetworkAdapter> opSubNetworkAdapter = getSubnetworkByName(snName);
        if (opSubNetworkAdapter.isEmpty()) { // create new subnetwork
            TSubNetwork subNetwork = new TSubNetwork();
            subNetwork.setName(snName);
            subNetwork.setType(snType);
            currentElem.getSubNetwork().add(subNetwork);
            opSubNetworkAdapter = Optional.of(new SubNetworkAdapter(this, subNetwork));
        }

        opSubNetworkAdapter.get().addConnectedAP(iedName,apName);

        return opSubNetworkAdapter.get();
    }

    /**
     * Gets Subnetwork  by name from Communication in an adapter wrapper
     * @param snName Subnetwork name
     * @return Optional SubNetworkAdapter object
     */
    public Optional<SubNetworkAdapter> getSubnetworkByName(String snName) {
        return currentElem.getSubNetwork()
                .stream()
                .filter(tSubNetwork -> tSubNetwork.getName().equals(snName))
                .findFirst()
                .map(tSubNetwork -> new SubNetworkAdapter(this, tSubNetwork));
    }

    /**
     * Gets all Subnetworks from Communication node in an adapter wrapper
     * @return list of SubNetworkAdapter object
     */
    public List<SubNetworkAdapter> getSubNetworkAdapters() {
        return currentElem.getSubNetwork()
                .stream()
                .map(tSubNetwork -> new SubNetworkAdapter(this, tSubNetwork))
                .collect(Collectors.toList());

    }
}
