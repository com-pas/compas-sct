// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newAddress;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TConnectedAP ConnectedAP}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ConnectedAPAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *   <li>{@link ConnectedAPAdapter#copyAddressAndPhysConnFromIcd  <em>copy Address And PhysConn From Icd</em>}</li>
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

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getConnectedAP().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("ConnectedAP[%s and %s]",
                Utils.xpathAttributeFilter("apName", currentElem.isSetApName() ? currentElem.getApName() : null),
                Utils.xpathAttributeFilter("iedName", currentElem.isSetIedName() ? currentElem.getIedName() : null));
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
     * Copies Address and PhysicalConnection nodes from ICD file
     * @param icd ICD object
     * @see <a href="https://github.com/com-pas/compas-sct/issues/76" target="_blank">Issue !76</a>
     * Copies Address and PhysicalConnection nodes from ICD file
     */
    public void copyAddressAndPhysConnFromIcd(SCL icd) {
        if (icd != null && icd.getCommunication() != null) {
            icd.getCommunication().getSubNetwork().stream()
                    .flatMap(tSubNetwork -> tSubNetwork.getConnectedAP().stream())
                    .filter(connectedAP -> connectedAP.getApName().equals(currentElem.getApName()))
                    .findFirst()
                    .ifPresent(connectedAP -> {
                        currentElem.setAddress(connectedAP.getAddress());
                        currentElem.getPhysConn().addAll(connectedAP.getPhysConn());
                    });
        }
    }

    private Optional<TGSE> findGse(String ldInst, String cbName){
        if (!currentElem.isSetGSE()){
            return Optional.empty();
        }
        return currentElem.getGSE().stream().filter(gse -> Objects.equals(ldInst, gse.getLdInst()) && Objects.equals(cbName, gse.getCbName()))
            .findFirst();
    }

    private Optional<TSMV> findSmv(String ldInst, String cbName){
        if (!currentElem.isSetSMV()){
            return Optional.empty();
        }
        return currentElem.getSMV().stream().filter(smv -> Objects.equals(ldInst, smv.getLdInst()) && Objects.equals(cbName, smv.getCbName()))
            .findFirst();
    }

    /**
     * Create A GSE Section or update an existing GSE Section (the network configuration of a GSEControl block).
     *
     * @param ldInst ldInst
     * @param cbName cbName
     * @param listOfP list of P elements
     * @param minTime minTime
     * @param maxTime maxTime
     */
    public void updateGseOrCreateIfNotExists(String ldInst, String cbName, List<TP> listOfP, TDurationInMilliSec minTime, TDurationInMilliSec maxTime) {
        TGSE gse = findGse(ldInst, cbName)
            .orElseGet(() -> {
                TGSE newGse = new TGSE();
                newGse.setLdInst(ldInst);
                newGse.setCbName(cbName);
                currentElem.getGSE().add(newGse);
                return newGse;
            }
        );
        gse.setAddress(newAddress(listOfP));
        gse.setMinTime(minTime);
        gse.setMaxTime(maxTime);
    }

    /**
     * Create A SMV Section or update an existing SMV Section (the network configuration of a SampledValueControl block)..
     * @param ldInst ldInst
     * @param cbName cbName
     * @param listOfP list of P elements
     */
    public void updateSmvOrCreateIfNotExists(String ldInst, String cbName, List<TP> listOfP) {
        TSMV smv = findSmv(ldInst, cbName)
            .orElseGet(() -> {
                TSMV newSmv = new TSMV();
                newSmv.setLdInst(ldInst);
                newSmv.setCbName(cbName);
                currentElem.getSMV().add(newSmv);
                return newSmv;
            }
        );
        smv.setAddress(newAddress(listOfP));
    }

}
