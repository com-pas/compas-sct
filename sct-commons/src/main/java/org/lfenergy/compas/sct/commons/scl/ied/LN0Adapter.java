// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;

public class LN0Adapter extends AbstractLNAdapter<LN0> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param ln0 Current reference
     */
    public LN0Adapter(LDeviceAdapter parentAdapter, LN0 ln0) {
        super(parentAdapter,ln0);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getLN0();
    }

    /**
     * Gets current LN0 class type
     * @return <em>LN0.class</em>
     */
    @Override
    protected Class<LN0> getElementClassType() {
        return LN0.class;
    }

    /**
     * Gets LNClass enum value of current LNO
     * @return LNClass value
     */
    public String getLNClass() {
        return TLLN0Enum.LLN_0.value();
    }

    /**
     * Gets LNInst value of current LN0
     * @return <em>""</em>
     */
    @Override
    public String getLNInst() {
        return "";
    }

    /**
     * Gets Prefix value of current LN0
     * @return <em>""</em>
     */
    @Override
    public String getPrefix() {
        return "";
    }

    /** Checks if given attibrute corresponds to DataSet or ReportControl or SMVControl or GSEControl in current LN0
     * @param dataAttribute attribute to check
     * @return <em>Boolean</em> value of check result
     */
    @Override
    protected  boolean matchesDataAttributes(String dataAttribute){
        return  super.matchesDataAttributes(dataAttribute) ||
                currentElem.getSampledValueControl().stream().anyMatch(smp -> smp.getName().equals(dataAttribute)) ||
                currentElem.getGSEControl().stream().anyMatch(gse -> gse.getName().equals(dataAttribute));
    }

    /**
     * Remove all SMVControl and GSEControl of current LN0
     */
    @Override
    public void removeAllControlBlocksAndDatasets() {
        super.removeAllControlBlocksAndDatasets();
        currentElem.unsetGSEControl();
        currentElem.unsetSampledValueControl();
    }
}
