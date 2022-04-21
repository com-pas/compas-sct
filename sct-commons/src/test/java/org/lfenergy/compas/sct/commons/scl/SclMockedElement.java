// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

public abstract class SclMockedElement <P extends SclElementAdapter, T> {
    protected P sclElementAdapter;
    protected T sclElement;

    public void init(){
        sclElementAdapter = (P) getMockedSclParentAdapter();
        completeInit();
    }
    protected abstract SclElementAdapter getMockedSclParentAdapter();
    protected abstract void completeInit() ;

}
