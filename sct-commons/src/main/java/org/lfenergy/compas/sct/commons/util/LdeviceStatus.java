// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;

import java.util.Arrays;

/**
 * Object describing Ldevice Status, prefere this to LdeviceStatus constants as we can get the list of constants here.
 */
public enum LdeviceStatus {
    ON("on"),
    OFF("off");

    @Getter
    private final String value;

    LdeviceStatus(String value) {
        this.value = value;
    }

    public static LdeviceStatus fromValue(String ldeviceStatus) {
        return Arrays.stream(LdeviceStatus.values())
                .filter(status -> status.getValue().equals(ldeviceStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The Ldevice status " + ldeviceStatus + " does not exist. It should be among " + Arrays.stream(LdeviceStatus.values()).map(LdeviceStatus::getValue).toList()));
    }
}
