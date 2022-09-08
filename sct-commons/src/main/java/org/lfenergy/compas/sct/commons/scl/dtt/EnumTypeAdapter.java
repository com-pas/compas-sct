// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;


import org.lfenergy.compas.scl2007b4.model.TEnumType;
import org.lfenergy.compas.scl2007b4.model.TEnumVal;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Objects;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TEnumType EnumType}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link EnumTypeAdapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link EnumTypeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link EnumTypeAdapter#hasSameContentAs <em>Compare Two TEnumType</em>}</li>
 *       <li>{@link EnumTypeAdapter#hasValue <em>Check whether TEnumType contain given value</em>}</li>
 *    </ul>
 * </ol>
 */
public class EnumTypeAdapter extends AbstractDataTypeAdapter<TEnumType>{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public EnumTypeAdapter(DataTypeTemplateAdapter parentAdapter, TEnumType currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getEnumType().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("EnumType[%s]",
                Utils.xpathAttributeFilter("id", currentElem.isSetId() ? currentElem.getId() : null));
    }

    /**
     * Compares current EnumType and given EnumType
     * @param tEnumType EnumType to compare with
     * @return <em>Boolean</em> value of comparison result
     */
    public boolean hasSameContentAs(TEnumType tEnumType) {

        if(!DataTypeTemplateAdapter.hasSamePrivates(currentElem, tEnumType)) {
            return false;
        }

        List<TEnumVal> rcvEnumValList = currentElem.getEnumVal();
        List<TEnumVal> prdEnumValList = tEnumType.getEnumVal();
        if(rcvEnumValList.size() != prdEnumValList.size()) {
            return false;
        }
        for(TEnumVal tEnumVal : tEnumType.getEnumVal()){
            boolean hasSameVal = currentElem.getEnumVal().stream()
                    .anyMatch(rcvVal -> rcvVal.getValue().equals(tEnumVal.getValue()) &&
                            Objects.equals(rcvVal.getOrd(), tEnumVal.getOrd()));
            if(!hasSameVal) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if current EnumType has specified value
     * @param val value to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean hasValue(String val) {
        return currentElem.getEnumVal().stream().anyMatch(tEnumVal -> tEnumVal.getValue().equals(val));
    }

    /**
     * Gets linked DataTypeTemplateAdapter as parent
     * @return <em>DataTypeTemplateAdapter</em> object
     */
    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter;
    }

}
