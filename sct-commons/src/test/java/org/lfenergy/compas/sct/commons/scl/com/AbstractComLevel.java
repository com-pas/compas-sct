// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import org.lfenergy.compas.sct.commons.scl.SclMockedElement;
import org.mockito.Mockito;

public abstract class AbstractComLevel<P extends SclElementAdapter, T> extends SclMockedElement<P,T>{


    public SclElementAdapter getMockedSclParentAdapter(){
        CommunicationAdapter dataTypeTemplateAdapter = Mockito.mock(CommunicationAdapter.class);
        TCommunication tCommunication = Mockito.mock(TCommunication.class);
        Mockito.when(dataTypeTemplateAdapter.getCurrentElem()).thenReturn(tCommunication);

        return dataTypeTemplateAdapter;
    }
}
