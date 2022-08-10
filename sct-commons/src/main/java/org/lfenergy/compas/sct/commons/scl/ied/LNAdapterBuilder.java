// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.sct.commons.exception.ScdException;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.LNAdapterBuilder LNAdapterBuilder}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link LNAdapterBuilder#build() <em>Returns Adapter of Given <b>TAnyLN </b> attributes </em>}</li>
 * </ul>
 * @see org.lfenergy.compas.scl2007b4.model.TAnyLN
 */
public class LNAdapterBuilder {
    private String lnInst = "";
    private String prefix = "";
    private String lnClass = TLLN0Enum.LLN_0.value();
    private LDeviceAdapter lDeviceAdapter;

    public LNAdapterBuilder withLnClass(String lnClass){
        this.lnClass = lnClass;
        return this;
    }

    public LNAdapterBuilder withLnInst(String lnInst){
        this.lnInst = lnInst;
        return this;
    }

    public LNAdapterBuilder withLnPrefix(String prefix){
        this.prefix = prefix;
        return this;
    }

    public LNAdapterBuilder withLDeviceAdapter(LDeviceAdapter lDeviceAdapter){
        this.lDeviceAdapter = lDeviceAdapter;
        return this;
    }

    public AbstractLNAdapter<?> build() throws ScdException {
        if(lDeviceAdapter == null){
            throw new IllegalArgumentException("Cannot build LNode adapter without LDevice");
        }
        if(TLLN0Enum.LLN_0.value().equals(lnClass)){
            return lDeviceAdapter.getLN0Adapter();
        }

        if(lnClass == null || lnInst == null){ // none LLN0 must an lnInst!
            throw new IllegalArgumentException("Missing lnClass and/or lnInst");
        }
        return lDeviceAdapter.getLNAdapter(lnClass, lnInst, prefix);
    }
}
