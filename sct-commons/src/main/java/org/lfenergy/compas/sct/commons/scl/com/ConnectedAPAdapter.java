// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

public class ConnectedAPAdapter extends SclElementAdapter<SubNetworkAdapter, TConnectedAP> {

    public ConnectedAPAdapter(SubNetworkAdapter parentAdapter, TConnectedAP currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getConnectedAP().contains(currentElem);
    }

    public String getIedName() {
        return currentElem.getIedName();
    }

    public String getApName() {
        return currentElem.getApName();
    }
}
