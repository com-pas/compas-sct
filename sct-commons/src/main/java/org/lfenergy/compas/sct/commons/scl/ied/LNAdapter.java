// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TLN;


public class LNAdapter extends AbstractLNAdapter<TLN>{

    public LNAdapter(LDeviceAdapter parentAdapter, TLN currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected Class<TLN> getElementClassType() {
        return TLN.class;
    }

    @Override
    public String getLNClass() {

        if(!currentElem.getLnClass().isEmpty()){
            return currentElem.getLnClass().get(0);
        }
        return null;
    }

    @Override
    public String getLNInst() {
        return currentElem.getInst();
    }

    @Override
    public String getPrefix() {
        return currentElem.getPrefix();
    }

    @Override
    protected boolean amChildElementRef() {
        // the contains method compares object ref by default
        // as there's no equals method in TLN
        return parentAdapter.getCurrentElem().getLN().contains(currentElem);
    }
}
