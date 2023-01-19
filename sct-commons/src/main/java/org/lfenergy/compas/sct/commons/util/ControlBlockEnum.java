// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.*;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum ControlBlockEnum {
    GSE(TGSEControl.class),
    SAMPLED_VALUE(TSampledValueControl.class),
    REPORT(TReportControl.class),
    LOG(TLogControl.class);

    private final Class<? extends TControl> controlBlockClass;

    ControlBlockEnum(Class<? extends TControl> controlBlockClass) {
        this.controlBlockClass = controlBlockClass;
    }

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

}
