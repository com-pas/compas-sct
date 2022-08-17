// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Optional;
import java.util.stream.Stream;

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

    @Override
    protected String elementXPath() {
        return String.format("VoltageLevel[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    public Optional<BayAdapter> getBayAdapter(String bayName) {
        return currentElem.getBay()
                .stream()
                .filter(tBay -> tBay.getName().equals(bayName))
                .map(tBay -> new BayAdapter(this, tBay))
                .findFirst();
    }

    public Stream<BayAdapter> streamBayAdapters() {
        if (!currentElem.isSetBay()){
            return Stream.empty();
        }
        return currentElem.getBay().stream().map(tBay -> new BayAdapter(this, tBay));
    }

}
