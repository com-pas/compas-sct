// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

@Getter
public enum ServicesConfigEnum {
    GSE("GOOSE Control Block"),
    SMV("SMV Control Block"),
    REPORT("Report Control Block"),
    DATASET("DataSet"),
    FCDA("FCDA");

    private final String displayName;

    ServicesConfigEnum(String displayName) {
        this.displayName = displayName;
    }

    public static ServicesConfigEnum from(TServiceType tServiceType){
        return switch (tServiceType){
            case GOOSE -> GSE;
            case SMV -> SMV;
            case REPORT -> REPORT;
            default -> throw new IllegalArgumentException("Unsupported TServiceType " + tServiceType);
        };
    }
}
