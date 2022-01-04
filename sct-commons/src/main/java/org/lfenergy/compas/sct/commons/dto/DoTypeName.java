// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class DoTypeName extends DataTypeName {
    public static final String VALIDATION_REGEX = "[A-Z][0-9A-Za-z]{0,11}(\\.[a-z][0-9A-Za-z]*(\\([0-9]+\\))?)?";
    private TPredefinedCDCEnum cdc;

    public DoTypeName(String doName) {
        super(doName);
        validationPattern = VALIDATION_REGEX;
    }

    public DoTypeName(String ppDoName, String sdoNames) {
        super(ppDoName, sdoNames);
        validationPattern = VALIDATION_REGEX;
    }

    public static DoTypeName from(DoTypeName dataName){
        DoTypeName doTypeName = new DoTypeName(dataName.toString());
        if(doTypeName.isDefined()) {
            doTypeName.setCdc(dataName.getCdc());
        }
        return doTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() !=getClass()) return false;
        if (!super.equals(o)) return false;
        DoTypeName that = (DoTypeName) o;
        return cdc == that.cdc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cdc);
    }

    public void merge(DoTypeName doName) {
        if(!isDefined()) return;
        if(cdc == null)
            cdc = doName.getCdc();
    }
}
