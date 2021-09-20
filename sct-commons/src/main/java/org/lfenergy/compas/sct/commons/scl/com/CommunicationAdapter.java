
// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;


import org.lfenergy.compas.scl2007b4.model.TCommunication;

import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;


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
}
