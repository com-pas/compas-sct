// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;



@Getter
public abstract class SclElementAdapter<P extends SclElementAdapter, T> {
    protected P parentAdapter;
    protected T currentElem;

    protected SclElementAdapter(P parentAdapter) {
        this.parentAdapter = parentAdapter;
    }

    protected SclElementAdapter(P parentAdapter, T currentElem) {
        if(currentElem == null){
            throw new IllegalArgumentException("The SCL element to adapt must be defined");
        }
        this.parentAdapter = parentAdapter;
        this.customInit();
        setCurrentElem(currentElem);
    }


    protected boolean amRootElement(){
        return parentAdapter == null;
    }

    protected void customInit() {
        // do nothing
    }
    public final void setCurrentElem(T currentElem){
        this.currentElem = currentElem;
        if(!amRootElement() && !amChildElementRef()){
            throw new IllegalArgumentException("No relation between SCL parent element and child");
        }
    }

    protected abstract boolean amChildElementRef();

}

