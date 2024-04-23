// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DataObject {

    private String doName;
    private TPredefinedCDCEnum cdc;
    private List<String> sdoNames = new ArrayList<>();

    public static DataObject copyFrom(DataObject dataObject) {
        DataObject dataObject1 = new DataObject();
        dataObject1.setDoName(dataObject.getDoName());
        dataObject1.setCdc(dataObject.getCdc());
        dataObject1.getSdoNames().addAll(dataObject.getSdoNames());
        return dataObject1;
    }

    @Override
    public String toString(){
        return doName + (getSdoNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", getSdoNames()));
    }

}
