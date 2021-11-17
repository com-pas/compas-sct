// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TGSEControl;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TSampledValueControl;
import org.lfenergy.compas.sct.commons.dto.GooseControlBlock;
import org.lfenergy.compas.sct.commons.dto.SMVControlBlock;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public void addControlBlock(GooseControlBlock controlBlock) {
        currentElem.getGSEControl().add(controlBlock.createControlBlock());
    }

    public void addControlBlock(SMVControlBlock controlBlock) {
        currentElem.getSampledValueControl().add(controlBlock.createControlBlock());
    }

    /**
     * Get all GSE control block that references a given Data Set
     *
     * @param dataSetRef Data set reference
     * @return list of GSE control block
     */
    public List<TGSEControl> lookUpGseControlBlocks(String dataSetRef) {
        return currentElem.getGSEControl()
                .stream()
                .filter(tgseControl -> Objects.equals(dataSetRef,tgseControl.getDatSet()))
                .collect(Collectors.toList());
    }

    /**
     * Get all SMV control block that references a given Data Set
     *
     * @param dataSetRef Data set reference
     * @return list of SMV control block
     */
    public List<TSampledValueControl> lookUpSMVControlBlocks(String dataSetRef) {

        return currentElem.getSampledValueControl()
                .stream()
                .filter(tSampledValueControl -> dataSetRef.equals(tSampledValueControl.getDatSet()))
                .collect(Collectors.toList());
    }
}
