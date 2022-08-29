// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

@Getter
public class DAAdapter extends AbstractDataAttributeAdapter<DOTypeAdapter, TDA> implements IDataTemplate, IDTTComparable<TDA> {
    /**
     * Constructor
     * @param parentAdapter input
     * @param currentElem input
     */
    protected DAAdapter(DOTypeAdapter parentAdapter, TDA currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDOOrDA().contains(currentElem);
    }

    /**
     * Updates DA Type Name
     * @param daTypeName DA Type Name to update
     * @throws ScdException
     */
    @Override
    public void check(DaTypeName daTypeName) throws ScdException {
        super.check(daTypeName);
        daTypeName.setFc(currentElem.getFc());
    }

}
