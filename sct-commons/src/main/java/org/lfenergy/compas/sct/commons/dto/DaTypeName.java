// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.util.Objects;

@Getter
@Setter
public class DaTypeName extends DataTypeName{
    public static final String VALIDATION_REGEX
            = "[a-zA-Z][a-zA-Z0-9]*(\\([0-9]+\\))?(\\.[a-zA-Z][a-zA-Z0-9]*(\\([0-9]+\\))?)*";
    private TFCEnum fc;
    private String type;
    private String bType;

    public DaTypeName(String dataName) {
        super(dataName);
        validationPattern = VALIDATION_REGEX;
    }

    public DaTypeName(String name, String names) {
        super(name, names);
        validationPattern = VALIDATION_REGEX;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        if (!super.equals(o)) return false;
        DaTypeName that = (DaTypeName) o;
        return fc == that.fc &&
                Objects.equals(bType, that.bType) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fc, bType, type);
    }
}
