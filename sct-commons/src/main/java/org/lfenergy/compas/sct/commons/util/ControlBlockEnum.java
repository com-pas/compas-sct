// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.model.cbcom.TCBType;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum ControlBlockEnum {
    GSE(TGSEControl.class, "GSEControl"),
    SAMPLED_VALUE(TSampledValueControl.class, "SampledValueControl"),
    REPORT(TReportControl.class, "ReportControl"),
    LOG(TLogControl.class, "LogControl");

    private final Class<? extends TControl> controlBlockClass;
    private final String elementName;

    public static ControlBlockEnum from(TServiceType tServiceType) {
        Objects.requireNonNull(tServiceType);
        return switch (tServiceType) {
            case GOOSE -> GSE;
            case SMV -> SAMPLED_VALUE;
            case REPORT -> REPORT;
            default -> throw new IllegalArgumentException("Unsupported TServiceType " + tServiceType);
        };
    }

    public static ControlBlockEnum from(Class<? extends TControl> tControlClass) {
        return Arrays.stream(values())
            .filter(controlBlockEnum -> controlBlockEnum.controlBlockClass.isAssignableFrom(tControlClass))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported TControl class : " + tControlClass.getSimpleName()));
    }

    public static ControlBlockEnum from(TCBType tcbType) {
        return switch (tcbType){
            case GOOSE -> GSE;
            case SV -> SAMPLED_VALUE;
            default -> throw new IllegalArgumentException("Unsupported TCBType: " + tcbType);
        };
    }

}
