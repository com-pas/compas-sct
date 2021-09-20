// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public abstract class SclElementAdapter<P extends SclElementAdapter, T> {
    protected P parentAdapter;
    protected T currentElem;

    public SclElementAdapter(P parentAdapter) {
        this.parentAdapter = parentAdapter;
    }

    public SclElementAdapter(P parentAdapter, T currentElem) {
        this.parentAdapter = parentAdapter;
        setCurrentElem(currentElem);
    }

    public final void setCurrentElem(T currentElem){
        this.currentElem = currentElem;
        Assert.isTrue(amChildElementRef(),"No relation between SCL parent element and child");
    }

    protected abstract boolean amChildElementRef();
}

