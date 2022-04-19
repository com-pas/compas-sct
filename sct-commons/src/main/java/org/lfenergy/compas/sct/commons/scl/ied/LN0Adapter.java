// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TPrivate;

public class LN0Adapter extends AbstractLNAdapter<LN0> {


    public LN0Adapter(LDeviceAdapter parentAdapter, LN0 ln0) {
        super(parentAdapter,ln0);
    }

    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getLN0();
    }

    @Override
    protected Class<LN0> getElementClassType() {
        return LN0.class;
    }

    public String getLNClass() {
        return TLLN0Enum.LLN_0.value();
    }

    @Override
    public String getLNInst() {
        return "";
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    @Override
    protected  boolean matchesDataAttributes(String dataAttribute){
        return  super.matchesDataAttributes(dataAttribute) ||
                currentElem.getSampledValueControl().stream().anyMatch(smp -> smp.getName().equals(dataAttribute)) ||
                currentElem.getGSEControl().stream().anyMatch(gse -> gse.getName().equals(dataAttribute));
    }
}
