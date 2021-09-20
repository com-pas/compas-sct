// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.junit.jupiter.api.Assertions.*;

public class DataTypeTemplateAdapterTest {

    @Test
    public void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        DataTypeTemplateAdapter tAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        assertTrue(tAdapter.amChildElementRef());

        assertThrows(IllegalArgumentException.class,
                () ->new DataTypeTemplateAdapter(sclRootAdapter,new TDataTypeTemplates()));
    }

}