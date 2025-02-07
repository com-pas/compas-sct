// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DataObject {

    private String doName;
    private TPredefinedCDCEnum cdc;
    private List<String> sdoNames = new ArrayList<>();

    public DataObject(String doName, TPredefinedCDCEnum cdc, List<String> sdoNames) {
        this.doName = doName;
        this.cdc = cdc;
        this.sdoNames.addAll(sdoNames);
    }

    public DataObject deepCopy() {
        return new DataObject(getDoName(), getCdc(), getSdoNames());
    }

    @Override
    public String toString(){
        return doName + (getSdoNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", getSdoNames()));
    }

}
