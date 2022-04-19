// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.Optional;

public class SubstationAdapter extends SclElementAdapter<SclRootAdapter, TSubstation> {

    public SubstationAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    public SubstationAdapter(SclRootAdapter parentAdapter, TSubstation currentElem) {
        super(parentAdapter, currentElem);
    }

    public SubstationAdapter(SclRootAdapter parentAdapter, String ssName) throws ScdException {
        super(parentAdapter);
        TSubstation tSubstation = parentAdapter.getCurrentElem().getSubstation()
                .stream()
                .filter(subst -> subst.getName().equals(ssName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown Substation name :" + ssName));
        setCurrentElem(tSubstation);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSubstation().contains(currentElem);
    }

    public Optional<VoltageLevelAdapter> getVoltageLevelAdapter(String vLevelName) {
        return currentElem.getVoltageLevel()
                .stream()
                .filter(tVoltageLevel -> tVoltageLevel.getName().equals(vLevelName))
                .map(tVoltageLevel -> new VoltageLevelAdapter(this, tVoltageLevel))
                .findFirst();
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }
}
