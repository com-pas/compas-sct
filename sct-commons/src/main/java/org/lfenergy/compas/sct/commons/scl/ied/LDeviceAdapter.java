// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Collection;

public class LDeviceAdapter extends SclElementAdapter<IEDAdapter, TLDevice> {

    public LDeviceAdapter(IEDAdapter parentAdapter, TLDevice currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .anyMatch(tlDevice -> currentElem.getInst().equals(tlDevice.getInst()));
    }


    public void updateLDName() throws ScdException {
        String newLdName = parentAdapter.getCurrentElem().getName() + currentElem.getInst();
        if(newLdName.length() > 33){
            throw new ScdException(newLdName + "(IED.name + LDevice.inst) has more than 33 characters");
        }
        // renaming ldName
        currentElem.setLdName(newLdName);
    }
}
