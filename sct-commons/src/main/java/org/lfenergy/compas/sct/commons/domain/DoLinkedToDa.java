// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;


import lombok.*;


@Getter
@Setter
public class DoLinkedToDa {

    private DataObject dataObject;
    private DataAttribute dataAttribute;

    public static DoLinkedToDa copyFrom(DoLinkedToDa doLinkedToDa) {
        DoLinkedToDa newDoLinkedToDa = new DoLinkedToDa();
        newDoLinkedToDa.setDataObject(DataObject.copyFrom(doLinkedToDa.getDataObject()));
        newDoLinkedToDa.setDataAttribute(DataAttribute.copyFrom(doLinkedToDa.getDataAttribute()));
        return newDoLinkedToDa;
    }

    public String getDoRef() {
        return dataObject != null ? dataObject.toString() : "";
    }

    public String getDaRef() {
        return dataAttribute != null ? dataAttribute.toString() : "";
    }

}

