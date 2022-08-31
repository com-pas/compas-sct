// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TLN;


public class LNAdapter extends AbstractLNAdapter<TLN>{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public LNAdapter(LDeviceAdapter parentAdapter, TLN currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Gets LNode class type
     * @return <em>TLN.class</em>
     */
    @Override
    protected Class<TLN> getElementClassType() {
        return TLN.class;
    }

    /**
     * Gets LNClass enum value of current LNode
     * @return LNClass value
     */
    @Override
    public String getLNClass() {

        if(!currentElem.getLnClass().isEmpty()){
            return currentElem.getLnClass().get(0);
        }
        return null;
    }

    /**
     * Gets LNInst value of current LNode
     * @return LNInst value
     */
    @Override
    public String getLNInst() {
        return currentElem.getInst();
    }

    /**
     * Gets Prefix value of current LNode
     * @return Prefix value
     */
    @Override
    public String getPrefix() {
        return currentElem.getPrefix();
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        // the contains method compares object ref by default
        // as there's no equals method in TLN
        return parentAdapter.getCurrentElem().getLN().contains(currentElem);
    }
}
