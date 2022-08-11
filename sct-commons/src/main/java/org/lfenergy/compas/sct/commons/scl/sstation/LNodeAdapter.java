// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TFunction;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.scl2007b4.model.TText;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

public class LNodeAdapter extends SclElementAdapter<FunctionAdapter, TLNode> {

    public LNodeAdapter(FunctionAdapter parentAdapter, TLNode currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        TFunction parentElem = parentAdapter.getCurrentElem();
        return parentElem.isSetLNode() && parentElem.getLNode().contains(this.currentElem);
    }

    public TLNode deepCopy() {
        TLNode newLNode = new TLNode();
        newLNode.setDesc(currentElem.getDesc());
        newLNode.setIedName(currentElem.getIedName());
        newLNode.setLdInst(currentElem.getLdInst());
        newLNode.setLnInst(currentElem.getLnInst());
        newLNode.setLnType(currentElem.getLnType());
        newLNode.setPrefix(currentElem.getPrefix());
        if (currentElem.isSetText()) {
            TText newText = new TText();
            newText.setSource(currentElem.getText().getSource());
            newText.getContent().addAll(currentElem.getText().getContent());
            newText.getOtherAttributes().putAll(currentElem.getText().getOtherAttributes());
            newLNode.setText(newText);
        }
        if (currentElem.isSetPrivate()) {
            newLNode.getPrivate().addAll(currentElem.getPrivate());
        }
        if (currentElem.isSetLnClass()) {
            newLNode.getLnClass().addAll(currentElem.getLnClass());
        }
        if (currentElem.isSetAny()) {
            newLNode.getAny().addAll(currentElem.getAny());
        }
        newLNode.getOtherAttributes().putAll(currentElem.getOtherAttributes());
        return newLNode;
    }

    @Override
    protected String elementXPath() {
        return String.format("LNode[%s and %s and %s and %s and %s]",
            Utils.xpathAttributeFilter("iedName", currentElem.isSetIedName() ? currentElem.getIedName() : null),
            Utils.xpathAttributeFilter("ldInst", currentElem.isSetLdInst() ? currentElem.getLdInst() : null),
            Utils.xpathAttributeFilter("Prefix", currentElem.isSetPrefix() ? currentElem.getPrefix() : null),
            Utils.xpathAttributeFilter("lnClass", currentElem.isSetLnClass() ? currentElem.getLnClass() : null),
            Utils.xpathAttributeFilter("lnInst", currentElem.isSetLnInst() ? currentElem.getLnInst() : null)
        );
    }

}
