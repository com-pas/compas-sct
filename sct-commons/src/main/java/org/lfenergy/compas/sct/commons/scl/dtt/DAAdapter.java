// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

@Getter
public class DAAdapter extends AbstractDataAttributeAdapter<DOTypeAdapter, TDA> implements IDataTemplate, IDTTComparable<TDA> {
    protected DAAdapter(DOTypeAdapter parentAdapter, TDA currentElem) {
        super(parentAdapter, currentElem);
    }



    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDOOrDA().contains(currentElem);
    }

    @Override
    public void check(DaTypeName daTypeName) throws ScdException {
        super.check(daTypeName);
        daTypeName.setFc(currentElem.getFc());
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }
}
