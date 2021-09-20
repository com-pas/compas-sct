// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;


public class IEDAdapter extends SclElementAdapter<SclRootAdapter, TIED> {

    public IEDAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    public IEDAdapter(SclRootAdapter parentAdapter, TIED currentElem) {
        super(parentAdapter, currentElem);
    }
    public IEDAdapter(SclRootAdapter parentAdapter, String iedName) throws ScdException {
        super(parentAdapter);
        TIED ied = parentAdapter.getCurrentElem().getIED()
                .stream()
                .filter(tied -> tied.getName().equals(iedName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown IED name :" + iedName));
        setCurrentElem(ied);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getIED().contains(currentElem);
    }
}
