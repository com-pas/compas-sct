// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Optional;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TConnectedAP ConnectedAP}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter#addPrivate <em>add Private</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter#copyAddressAndPhysConnFromIcd  <em>copy Address And PhysConn From Icd</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.sct.commons.scl.com.SubNetworkAdapter
 * @see org.lfenergy.compas.scl2007b4.model.TConnectedAP
 */
public class ConnectedAPAdapter extends SclElementAdapter<SubNetworkAdapter, TConnectedAP> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public ConnectedAPAdapter(SubNetworkAdapter parentAdapter, TConnectedAP currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getConnectedAP().contains(currentElem);
    }

    /**
     * Returns the value of the <em><b>iedName</b></em> attribute.
     * @return the value of the <em><b>iedName</b></em> attribute.
     */
    public String getIedName() {
        return currentElem.getIedName();
    }
    /**
     * Returns the value of the <em><b>apName</b></em> attribute.
     * @return the value of the <em><b>apName</b></em> attribute.
     */
    public String getApName() {
        return currentElem.getApName();
    }

    /**
     * Copy Address And PhysConn From Icd
     * @param icd input
     * @see <a href="https://github.com/com-pas/compas-sct/issues/76">Issue !76</a>
     */
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
