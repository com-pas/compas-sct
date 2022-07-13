// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TFunction;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_ICDHEADER;

public class FunctionAdapter extends SclElementAdapter<BayAdapter, TFunction> {

    public FunctionAdapter(BayAdapter parentAdapter, TFunction currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getFunction().contains(currentElem);
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
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
                throw new ScdException(String.format("LNode is missing Private element of type %s.", COMPAS_ICDHEADER));
            }
            if (icdHeaders.size() == 1) {
                lNode.setIedName(icdHeaders.get(0).getIEDName());
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
            newLNode.setIedName(compasICDHeader.getIEDName());
            return newLNode;
        }).collect(Collectors.toList());
    }
}
