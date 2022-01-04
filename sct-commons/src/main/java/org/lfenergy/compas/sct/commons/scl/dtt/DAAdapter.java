// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TDA;

@Getter
public class DAAdapter extends AbstractDataAttributeAdapter<DOTypeAdapter, TDA> implements IDataTemplate, IDTTComparable<TDA> {
    protected DAAdapter(DOTypeAdapter parentAdapter, TDA currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDOOrDA().contains(currentElem);
    }
}
