// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.junit.jupiter.api.Assertions.*;

public class CommunicationAdapterTest {

    @Test
    public void testAmChildElementRef() throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setCommunication(new TCommunication());
        CommunicationAdapter cAdapter = sclRootAdapter.getCommunicationAdapter();
        assertTrue(cAdapter.amChildElementRef());

        CommunicationAdapter finalCAdapter = new CommunicationAdapter(sclRootAdapter);
        assertThrows(IllegalArgumentException.class,
                () -> finalCAdapter.setCurrentElem(new TCommunication()));
    }
}