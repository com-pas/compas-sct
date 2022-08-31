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

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     */
    protected SclElementAdapter(P parentAdapter) {
        this.parentAdapter = parentAdapter;
    }

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    protected SclElementAdapter(P parentAdapter, T currentElem) {
        if(currentElem == null){
            throw new IllegalArgumentException("The SCL element to adapt must be defined");
        }
        this.parentAdapter = parentAdapter;
        this.customInit();
        setCurrentElem(currentElem);
    }

    /**
     * Check if node is root in SCL
     * @return true if root node of SCL and false if not
     */
    protected boolean amRootElement(){
        return parentAdapter == null;
    }

    protected void customInit() {
        // do nothing
    }

    /**
     * Sets current element
     * @param currentElem new value of current element
     */
    public final void setCurrentElem(T currentElem){
        this.currentElem = currentElem;
        if(!amRootElement() && !amChildElementRef()){
            throw new IllegalArgumentException("No relation between SCL parent element and child");
        }
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    protected abstract boolean amChildElementRef();

    /**
     * Adds Private to current element
     * @param tPrivate Private to add
     */
    public void addPrivate(TPrivate tPrivate){
        if (currentElem instanceof TBaseElement){
            ((TBaseElement) currentElem).getPrivate().add(tPrivate);
        } else {
            throw new UnsupportedOperationException("Not implemented for class " + this.getClass().getName());
        }
    }

    /**
     * Gets XPath path to current element from parent element
     * @return path to current element
     */
    public String getXPath(){
        String parentXpath = (parentAdapter != null) ? parentAdapter.getXPath() : "";
        return parentXpath + "/" + elementXPath();
    }

    /**
     * Returns XPath path to current element
     * @return message as <em>undefined</em>
     */
    protected String elementXPath(){
        return String.format("undefined(%s)", currentElem.getClass().getSimpleName());
    }

}
