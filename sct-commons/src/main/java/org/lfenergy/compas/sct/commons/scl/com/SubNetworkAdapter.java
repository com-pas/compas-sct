// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SubNetworkAdapter extends SclElementAdapter<CommunicationAdapter, TSubNetwork> {

    /**
     * Constructor
     * @param parentAdapter input
     * @param currentElem input
     */
    public SubNetworkAdapter(CommunicationAdapter parentAdapter, @NonNull TSubNetwork currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSubNetwork().contains(currentElem);
    }

    /**
     * Create a Connected Access Point for this subnetwork.
     * Note : this method doesn't check the validity on neither the IED name nor the access point name.
     * @param iedName IED name
     * @param apName IED name
     * @return ConnectedAPAdapter object
     */
    public ConnectedAPAdapter addConnectedAP(@NonNull String iedName, @NonNull String apName) {
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

    /**
     * Gets Subnetwork name
     * @return Subnetwork name
     */
    public String getName() {
        return currentElem.getName();
    }

    /**
     *  Gets Subnetwork type
     * @return Subnetwork type
     */
    public String getType() {
        return currentElem.getType();
    }

    /**
     * Gets all ConnectedAP from Subnetwork
     * @return List of ConnectedAPAdapter
     */
    public List<ConnectedAPAdapter> getConnectedAPAdapters() {
        return currentElem.getConnectedAP()
                .stream()
                .map(ap -> new ConnectedAPAdapter(this,ap))
                .collect(Collectors.toList());
    }

    /**
     * Gets ConnectedAP from Subnetwork
     * @param iedName IED name
     * @param apName AccessPoint name
     * @return ConnectedAPAdapter object
     * @throws ScdException
     */
    public ConnectedAPAdapter getConnectedAPAdapter(String iedName, String apName) throws ScdException {
        return currentElem.getConnectedAP()
                .stream()
                .filter(ap -> ap.getIedName().equals(iedName) && ap.getApName().equals(apName))
                .map(ap -> new ConnectedAPAdapter(this,ap))
                .findFirst()
                .orElseThrow(
                    () -> new ScdException(
                        String.format(
                            "Unknown connected AP (%s,%s) for subnetwork %s", iedName, apName, getName()
                        )
                    )
                );
    }
}
