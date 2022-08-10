// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TFunction;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.scl2007b4.model.TText;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

/**
 * A representation of the model object
 * <em><b>{@link LNodeAdapter LNodeAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LNodeAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *      <li>{@link LNodeAdapter#elementXPath <em>Returns the <b>XPATH </b> for this object</em>}</li>
 *      <li>{@link LNodeAdapter#deepCopy <em>Copy the <b>LNode </b> object</em>}</li>
 *    </ul>
 *  </ol>
 *  <p>
 *    <b>XPATH Example :</b>
 *    <pre>
 *      LNode[@iedName="iedName1" and @ldInst="ldInst1" and @Prefix="prefix1" and @lnClass="lnClass1" and @lnInst="lnInst1"]
 *    </pre>
 *  </p>
 *  @see org.lfenergy.compas.scl2007b4.model.TCompasICDHeader
 *  @see <a href="https://github.com/com-pas/compas-sct/issues/124" target="_blank">Issue !124 (update LNode iedName)</a>
 */
public class LNodeAdapter extends SclElementAdapter<FunctionAdapter, TLNode> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public LNodeAdapter(FunctionAdapter parentAdapter, TLNode currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        TFunction parentElem = parentAdapter.getCurrentElem();
        return parentElem.isSetLNode() && parentElem.getLNode().contains(this.currentElem);
    }

    /**
     * Copies current Substation LNode to new one
     * @return copie of current Substation LNode
     */
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

    /**
     * Returns XPath path to current Substation LNode
     * @return path to current Substation LNode
     */
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
