// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Optional;

public class VoltageLevelAdapter extends SclElementAdapter<SubstationAdapter, TVoltageLevel> {

    public VoltageLevelAdapter(SubstationAdapter parentAdapter) {super(parentAdapter);}

    public VoltageLevelAdapter(SubstationAdapter substationAdapter, TVoltageLevel currentElem){
        super(substationAdapter, currentElem);
    }

    public VoltageLevelAdapter(SubstationAdapter parentAdapter, String vLevelName) throws ScdException {
        super(parentAdapter);
        TVoltageLevel tVoltageLevel = parentAdapter.getCurrentElem().getVoltageLevel()
                .stream()
                .filter(vLevel -> vLevel.getName().equals(vLevelName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown VoltageLevel name :" + vLevelName));
        setCurrentElem(tVoltageLevel);
    }


    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getVoltageLevel().contains(currentElem);
    }

    public Optional<BayAdapter> getBayAdapter(String bayName) {
        return currentElem.getBay()
                .stream()
                .filter(tBay -> tBay.getName().equals(bayName))
                .map(tBay -> new BayAdapter(this, tBay))
                .findFirst();
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }
}
