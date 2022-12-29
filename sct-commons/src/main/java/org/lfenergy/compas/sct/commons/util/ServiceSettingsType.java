// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.lfenergy.compas.scl2007b4.model.TServiceType;

public enum ServiceSettingsType {
    GSE,
    SMV,
    REPORT,
    LOG;

    public static ServiceSettingsType fromTServiceType(TServiceType tServiceType){
        if (tServiceType == null) {
            return null;
        }
        return switch (tServiceType) {
            case GOOSE -> GSE;
            case SMV -> SMV;
            case REPORT -> REPORT;
            default -> null;
        };
    }
}
