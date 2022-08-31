// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.stream.Stream;

public class BayAdapter extends SclElementAdapter<VoltageLevelAdapter, TBay> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     */
    public BayAdapter(VoltageLevelAdapter parentAdapter){super(parentAdapter);}

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public BayAdapter(VoltageLevelAdapter parentAdapter, TBay currentElem){
        super (parentAdapter, currentElem);
    }

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param bayName BAY name reference
     */
    public BayAdapter(VoltageLevelAdapter parentAdapter, String bayName) throws ScdException {
        super(parentAdapter);
        TBay tBay = parentAdapter.getCurrentElem().getBay()
                .stream()
                .filter(bay -> bay.getName().equals(bayName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown Bay name :" + bayName));
        setCurrentElem(tBay);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getBay().contains(currentElem);
    }

    /**
     * Returns XPath path to current Bay
     * @return path to current Bay
     */
    @Override
    protected String elementXPath() {
        return String.format("Bay[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    /**
     * Gets Functions in Stream from current Bay
     * @return Stream of <em>FunctionAdapter</em> object from Bay
     */
    public Stream<FunctionAdapter> streamFunctionAdapters(){
        if (!currentElem.isSetFunction()){
            return Stream.empty();
        }
        return currentElem.getFunction().stream().map(tFunction -> new FunctionAdapter(this, tFunction));
    }
}
