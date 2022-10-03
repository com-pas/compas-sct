// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TFunction;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

/**
 * A representation of the model object
 * <em><b>{@link FunctionAdapter FunctionAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link FunctionAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *      <li>{@link FunctionAdapter#elementXPath <em>Returns the <b>XPATH </b> for this object</em>}</li>
 *    </ul>
 *  </ol>
 *   <p>
 *       <b>XPATH Example :</b>
 *       <pre>
 *           Function[@name="functionName"]
 *       </pre>
 *   </p>
 *  @see org.lfenergy.compas.scl2007b4.model.TLNode
 *  @see org.lfenergy.compas.scl2007b4.model.TCompasICDHeader
 *  @see <a href="https://github.com/com-pas/compas-sct/issues/124" target="_blank">Issue !124 (update LNode iedName)</a>
 */
public class FunctionAdapter extends SclElementAdapter<BayAdapter, TFunction> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public FunctionAdapter(BayAdapter parentAdapter, TFunction currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getFunction().contains(currentElem);
    }

    /**
     * Returns XPath path to current Function
     * @return path to current Function
     */
    @Override
    protected String elementXPath() {
        return String.format("Function[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

}
