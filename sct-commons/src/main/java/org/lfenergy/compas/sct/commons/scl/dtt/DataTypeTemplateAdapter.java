// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;


public class DataTypeTemplateAdapter extends SclElementAdapter<SclRootAdapter, TDataTypeTemplates> {

    public DataTypeTemplateAdapter(SclRootAdapter parentAdapter, TDataTypeTemplates dataTypeTemplate) {
        super(parentAdapter,dataTypeTemplate);
    }

    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getDataTypeTemplates();
    }
}
