// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TBaseElement;
import org.lfenergy.compas.scl2007b4.model.TPrivate;


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

    public void addPrivate(TPrivate tPrivate){
        if (currentElem instanceof TBaseElement){
            ((TBaseElement) currentElem).getPrivate().add(tPrivate);
        } else {
            throw new UnsupportedOperationException("Not implemented for class " + this.getClass().getName());
        }
    }

    public String getXPath(){
        String parentXpath = (parentAdapter != null) ? parentAdapter.getXPath() : "";
        return parentXpath + "/" + elementXPath();
    }

    protected String elementXPath(){
        return String.format("undefined(%s)", currentElem.getClass().getSimpleName());
    }

}
