// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Optional;

public class DOAdapter extends SclElementAdapter<LNodeTypeAdapter, TDO> implements IDataTemplate{
    protected DOAdapter(LNodeTypeAdapter parentAdapter, TDO currElement) {
        super(parentAdapter,currElement);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDO().contains(currentElem);
    }

    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter.getDataTypeTemplateAdapter();
    }

    public Optional<DOTypeAdapter> getDoTypeAdapter() {
        return getDataTypeTemplateAdapter().getDOTypeAdapterById(currentElem.getType());
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    public String getType() {
        return currentElem.getType();
    }
}
