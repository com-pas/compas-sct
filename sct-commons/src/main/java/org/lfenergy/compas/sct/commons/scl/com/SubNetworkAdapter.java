// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TSubNetwork SubNetwork}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SubNetworkAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *   <li>{@link SubNetworkAdapter#addConnectedAP <em>add <b>ConnectedAP </b> under this object</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter
 * @see org.lfenergy.compas.scl2007b4.model.TSubNetwork
 */
public class SubNetworkAdapter extends SclElementAdapter<CommunicationAdapter, TSubNetwork> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
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

    @Override
    protected String elementXPath() {
        return String.format("SubNetwork[%s]",
                Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    /**
     * Create a Connected Access Point for this subnetwork.<br/>
     * <p>
     * Note : this method doesn't check the validity on neither the IED name nor the access point name.
     * </p>
     * @param iedName input
     * @param apName input
     * @return the <em><b>ConnectedAPAdapter</b></em> object
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
     * Returns the value of the <em><b>name</b></em> attribute.
     * @return the value of the <em><b>name</b></em> attribute.
     */
    public String getName() {
        return currentElem.getName();
    }

    /**
     * Returns the value of the <em><b>type</b></em> attribute.
     * @return the value of the <em><b>type</b></em> attribute.
     */
    public String getType() {
        return currentElem.getType();
    }

    /**
     * Returns the value of the <em><b>ConnectedAPAdapter</b></em> containment reference list.
     * @return the value of the <em><b>ConnectedAPAdapter</b></em> containment reference list.
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
     * @return the <em><b>ConnectedAPAdapter</b></em> object
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
