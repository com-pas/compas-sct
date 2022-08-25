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

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TCommunication Communication}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter#addSubnetwork <em>add Subnetwork</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter#addPrivate <em>add Private</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.sct.commons.scl.SclRootAdapter
 * @see org.lfenergy.compas.scl2007b4.model.TCommunication
 */
public class CommunicationAdapter extends SclElementAdapter<SclRootAdapter, TCommunication> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     */
    public CommunicationAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public CommunicationAdapter(SclRootAdapter parentAdapter, TCommunication currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    public boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getCommunication();
    }

    /**
     * Returns the <em><b>SubNetworkAdapter</b></em> element
     * @param snName input
     * @param snType input
     * @param iedName input
     * @param apName input
     * @return SubNetworkAdapter Current reference
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
     * Returns the value of the <em><b>SubNetworkAdapter</b></em> containment reference list.
     * @param snName Sub Network Name
     * @return the value of the <em><b>SubNetworkAdapter</b></em> containment reference list.
     */
    public Optional<SubNetworkAdapter> getSubnetworkByName(String snName) {
        return currentElem.getSubNetwork()
                .stream()
                .filter(tSubNetwork -> tSubNetwork.getName().equals(snName))
                .findFirst()
                .map(tSubNetwork -> new SubNetworkAdapter(this, tSubNetwork));
    }

    /**
     * Returns the value of the <em><b>SubNetworkAdapter</b></em> containment reference list.
     * @return the value of the <em><b>SubNetworkAdapter</b></em> containment reference list.
     */
    public List<SubNetworkAdapter> getSubNetworkAdapters() {
        return currentElem.getSubNetwork()
                .stream()
                .map(tSubNetwork -> new SubNetworkAdapter(this, tSubNetwork))
                .collect(Collectors.toList());

    }
}
