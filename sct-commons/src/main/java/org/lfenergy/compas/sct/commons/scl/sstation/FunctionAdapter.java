// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TFunction;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_ICDHEADER;

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
 *      <li>{@link FunctionAdapter#distributeIcdHeaders(TLNode, List) <em>Add <b>TCompasICDHeader </b> Private in each <b>TLNode </b> reference object</em>}</li>
 *      <li>{@link FunctionAdapter#updateLNodeIedNames() <em>Update the value of <b>LNode@iedName </b> attribute corresponding to the value of <b>ICDHeader@IEDName </b> attribute </em> }</li>
 *      <li>{@link FunctionAdapter#setLNodeIedName(TLNode, String) <em>Update the value of <b>LNode@iedName </b> attribute</em>}</li>
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

    public FunctionAdapter(BayAdapter parentAdapter, TFunction currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getFunction().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("Function[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    public void updateLNodeIedNames() throws ScdException {
        if (!currentElem.isSetLNode()) {
            return;
        }
        // use ListIterator to add & remove items in place
        ListIterator<TLNode> lNodeIterator = currentElem.getLNode().listIterator();
        while (lNodeIterator.hasNext()) {
            TLNode lNode = lNodeIterator.next();
            List<TCompasICDHeader> icdHeaders = PrivateService.getCompasPrivates(lNode, TCompasICDHeader.class);
            if (icdHeaders.isEmpty()){
                throw new ScdException(getXPath() + " doesn't contain any Private/compas:ICDHeader");
            }
            if (icdHeaders.size() == 1) {
                setLNodeIedName(lNode, icdHeaders.get(0).getIEDName());
            } else {
                lNodeIterator.remove();
                PrivateService.removePrivates(lNode, COMPAS_ICDHEADER);
                List<TLNode> newLNodes = distributeIcdHeaders(lNode, icdHeaders);
                newLNodes.forEach(lNodeIterator::add);
            }
        }
    }

    private List<TLNode> distributeIcdHeaders(TLNode lNode, List<TCompasICDHeader> compasICDHeaders) {
        LNodeAdapter lNodeAdapter = new LNodeAdapter(null, lNode);
        return compasICDHeaders.stream().map(compasICDHeader -> {
            TPrivate icdHeaderPrivate = PrivateService.createPrivate(compasICDHeader);
            TLNode newLNode = lNodeAdapter.deepCopy();
            newLNode.getPrivate().add(icdHeaderPrivate);
            setLNodeIedName(newLNode, compasICDHeader.getIEDName());
            return newLNode;
        }).collect(Collectors.toList());
    }

    private void setLNodeIedName(TLNode lNode, String privateIedName) {
        if (StringUtils.isBlank(privateIedName)){
            LNodeAdapter lNodeAdapter = new LNodeAdapter(null, lNode);
            throw new ScdException(getXPath() + lNodeAdapter.getXPath() + "/Private/compas:ICDHeader/@IEDName is missing or is blank");
        }
        lNode.setIedName(privateIedName);
    }
}
