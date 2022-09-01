// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;

import java.util.List;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter LN0Adapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link LN0Adapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LN0Adapter#getLNInst <em>Returns the value of the <b>inst </b>attribute</em>}</li>
 *      <li>{@link LN0Adapter#getLNClass <em>Returns the value of the <b>lnClass </b>attribute</em>}</li>
 *      <li>{@link LN0Adapter#getLnType <em>Returns the value of the <b>lnTYpe </b>attribute</em>}</li>
 *      <li>{@link LN0Adapter#getLNodeName <em>Returns the logical node name <b>LNName = prefix + lnClass + lnInst</b></em>}</li>
 *
 *      <li>{@link LN0Adapter#getExtRefs() <em>Returns the value of the <b>TExtRef </b>containment reference list</em>}</li>
 *      <li>{@link LN0Adapter#getExtRefs(ExtRefSignalInfo) <em>Returns the value of the <b>TExtRef </b>containment reference list By <b>ExtRefSignalInfo <b></b></b></em>}</li>
 *      <li>{@link LN0Adapter#getExtRefsBySignalInfo(ExtRefSignalInfo) <em>Returns the value of the <b>TExtRef </b>containment reference list By <b>ExtRefSignalInfo </b></em>}</li>
 *
 *      <li>{@link LN0Adapter#getDAI <em>Returns the value of the <b>ResumedDataTemplate </b> containment reference By filter</em>}</li>
 *      <li>{@link LN0Adapter#getDAIValues(ResumedDataTemplate) <em>Returns <b>DAI (sGroup, value) </b> containment reference list By <b>ResumedDataTemplate </b> filter</em>}</li>

 *      <li>{@link LN0Adapter#getDataSet(ExtRefInfo)  <em>Returns the value of the <b>TDataSet </b>containment reference list By <b>ExtRefInfo</b> </em>}</li>
 *      <li>{@link LN0Adapter#getDataSetByRef(String) <em>Returns the value of the <b>TDataSet </b>object reference By the value of the <b>name </b>attribute </em>}</li>
 *
 *      <li>{@link LN0Adapter#getControlSetByExtRefInfo(ExtRefInfo) <em>Returns the value of the <b>ControlBlock </b>containment reference list By <b>ExtRefInfo</b> </em>}</li>
 *      <li>{@link LN0Adapter#getControlBlocks(List, TServiceType) <em>Returns the value of the <b>ControlBlock </b>containment reference list that match <b>datSet </b> value of given <b>TDataSet</b> </em>}</li>
 *      <li>{@link LN0Adapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link LN0Adapter#removeAllControlBlocksAndDatasets() <em>Remove all <b>ControlBlock</b></em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *      <li>{@link LN0Adapter#matches(ObjectReference) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LN0Adapter DataName</em>}</li>
 *      <li>{@link LN0Adapter#matchesDataAttributes(String) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LNAdapter DataName Excluding DataName from DataTypeTemplat</em>}</li>
 *    </ul>
 * </ol>
 * <br/>
 *  <pre>
 *      <b>ObjectReference</b>: LDName/LNName.DataName[.DataName[…]].DataAttributeName[.DAComponentName[ ….]]
 *      <b>LDName</b> = "name" attribute of IEDName element + "inst" attribute of LDevice element
 *      <b>LNName</b> = "prefix" + "lnClass" + "lnInst"
 *  </pre>
 * @see org.lfenergy.compas.scl2007b4.model.TLN0
 * @see org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter
 */
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
    protected  boolean matchesDataAttributes(String dataAttribute){
        return  super.matchesDataAttributes(dataAttribute) ||
                currentElem.getSampledValueControl().stream().anyMatch(smp -> smp.getName().equals(dataAttribute)) ||
                currentElem.getGSEControl().stream().anyMatch(gse -> gse.getName().equals(dataAttribute));
    }

    @Override
    public void removeAllControlBlocksAndDatasets() {
        super.removeAllControlBlocksAndDatasets();
        currentElem.unsetGSEControl();
        currentElem.unsetSampledValueControl();
    }
}
