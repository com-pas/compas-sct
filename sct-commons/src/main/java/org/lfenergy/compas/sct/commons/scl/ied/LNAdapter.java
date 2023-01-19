// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;


/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.LNAdapter LNAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link LNAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LNAdapter#getLNInst <em>Returns the value of the <b>inst </b>attribute</em>}</li>
 *      <li>{@link LNAdapter#getLNClass <em>Returns the value of the <b>lnClass </b>attribute</em>}</li>
 *      <li>{@link LNAdapter#getLnType <em>Returns the value of the <b>lnTYpe </b>attribute</em>}</li>
 *      <li>{@link LNAdapter#getLNodeName <em>Returns the logical node name <b>LNName = prefix + lnClass + lnInst</b></em>}</li>
 *
 *      <li>{@link LNAdapter#getExtRefs() <em>Returns the value of the <b>TExtRef </b>containment reference list</em>}</li>
 *      <li>{@link LNAdapter#getExtRefs(ExtRefSignalInfo) <em>Returns the value of the <b>TExtRef </b>containment reference list By <b>ExtRefSignalInfo <b></b></b></em>}</li>
 *      <li>{@link LNAdapter#isExtRefExist(ExtRefSignalInfo) <em>Returns the value of the <b>TExtRef </b>containment reference list By <b>ExtRefSignalInfo </b></em>}</li>
 *
 *      <li>{@link LNAdapter#getDAI <em>Returns the value of the <b>ResumedDataTemplate </b> containment reference By filter</em>}</li>
 *      <li>{@link LNAdapter#getDAIValues(ResumedDataTemplate) <em>Returns <b>DAI (sGroup, value) </b> containment reference list By <b>ResumedDataTemplate </b> filter</em>}</li>

 *      <li>{@link LNAdapter#getDataSetByName(String) <em>Returns the value of the <b>TDataSet </b>object reference By the value of the <b>name </b>attribute </em>}</li>
 *
 *      <li>{@link LNAdapter#getControlBlocks(List, TServiceType) <em>Returns the value of the <b>ControlBlock </b>containment reference list that match <b>datSet </b> value of given <b>TDataSet</b> </em>}</li>
 *      <li>{@link LNAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link LNAdapter#removeAllControlBlocksAndDatasets() <em>Remove all <b>ControlBlock</b></em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *      <li>{@link LNAdapter#matches(ObjectReference) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LNAdapter DataName</em>}</li>
 *      <li>{@link LNAdapter#matchesDataAttributes(String) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LNAdapter DataName Excluding DataName from DataTypeTemplat</em>}</li>
 *    </ul>
 * </ol>
 * <br/>
 *  <pre>
 *      <b>ObjectReference</b>: LDName/LNName.DataName[.DataName[…]].DataAttributeName[.DAComponentName[ ….]]
 *      <b>LDName</b> = "name" attribute of IEDName element + "inst" attribute of LDevice element
 *      <b>LNName</b> = "prefix" + "lnClass" + "lnInst"
 *  </pre>
 * @see org.lfenergy.compas.scl2007b4.model.TAnyLN
 * @see org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter
 */
public class LNAdapter extends AbstractLNAdapter<TLN>{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public LNAdapter(LDeviceAdapter parentAdapter, TLN currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Gets LNode class type
     * @return <em>TLN.class</em>
     */
    @Override
    protected Class<TLN> getElementClassType() {
        return TLN.class;
    }

    /**
     * Gets LNClass enum value of current LNode
     * @return LNClass value
     */
    @Override
    public String getLNClass() {
        if(currentElem.isSetLnClass()){
            return currentElem.getLnClass().get(0);
        }
        return null;
    }

    /**
     * Gets LNInst value of current LNode
     * @return LNInst value
     */
    @Override
    public String getLNInst() {
        return currentElem.getInst();
    }

    /**
     * Gets Prefix value of current LNode
     * @return Prefix value
     */
    @Override
    public String getPrefix() {
        return currentElem.getPrefix();
    }


    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        // the contains method compares object ref by default
        // as there's no equals method in TLN
        return parentAdapter.getCurrentElem().getLN().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("LN[%s and %s and %s]",
                Utils.xpathAttributeFilter("lnClass", currentElem.isSetLnClass() ? currentElem.getLnClass() : null),
                Utils.xpathAttributeFilter("inst", currentElem.isSetInst() ? currentElem.getInst() : null),
                Utils.xpathAttributeFilter("lnType", currentElem.isSetLnType() ? currentElem.getLnType() : null));
    }
}
