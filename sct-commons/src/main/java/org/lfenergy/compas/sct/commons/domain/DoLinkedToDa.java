// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;


import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;


public record DoLinkedToDa(DataObject dataObject, DataAttribute dataAttribute) {

    public DoLinkedToDa deepCopy() {
        return new DoLinkedToDa(
                dataObject().deepCopy(),
                dataAttribute().deepCopy());
    }

    public String getDoRef() {
        return dataObject != null ? dataObject.toString() : "";
    }

    public String getDaRef() {
        return dataAttribute != null ? dataAttribute.toString() : "";
    }

    /**
     * Checks if DA/DO is updatable
     *
     * @return true if updatable, false otherwise
     */
    public boolean isUpdatable() {
        return isDOModDAstVal() || dataAttribute.isUpdatable();
    }

    /**
     * Checks if DO is Mod and DA is stVal
     *
     * @return true if DO is "Mod" and DA is "stVal", false otherwise
     */
    private boolean isDOModDAstVal() {
        return dataObject.getDoName().equals(MOD_DO_NAME) && dataAttribute.getDaName().equals(STVAL_DA_NAME);
    }

    /**
     * Create DoLinkedToDaFilter from this
     * @return new DoLinkedToDaFilter instance with same data Object and data Attribute Ref
     */
    public DoLinkedToDaFilter toFilter() {
        return DoLinkedToDaFilter.from(getDoRef(), getDaRef());
    }
}
