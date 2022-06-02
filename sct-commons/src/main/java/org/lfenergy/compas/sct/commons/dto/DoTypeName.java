// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DoTypeName extends DataTypeName {
    public static final String VALIDATION_REGEX = "[A-Z][0-9A-Za-z]{0,11}(\\.[a-z][0-9A-Za-z]*(\\([0-9]+\\))?)?";
    private TPredefinedCDCEnum cdc;

    public DoTypeName(String doName) {
        super(doName);
    }
    public DoTypeName(String ppDoName, String sdoNames) {
        super(ppDoName, sdoNames);
    }

    public static DoTypeName from(DoTypeName dataName){
        DoTypeName doTypeName = new DoTypeName(dataName.toString());
        if(doTypeName.isDefined()) {
            doTypeName.setCdc(dataName.getCdc());
        }
        return doTypeName;
    }

    public void merge(DoTypeName doName) {
        if(!isDefined()) return;
        if(cdc == null)
            cdc = doName.getCdc();
    }
}
