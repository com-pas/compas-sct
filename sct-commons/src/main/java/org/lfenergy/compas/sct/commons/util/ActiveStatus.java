// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;

import java.util.Arrays;

/**
 * Object describing Status, prefere this to LdeviceStatus constants as we can get the list of constants here.
 */
public enum ActiveStatus {
    ON("on"),
    OFF("off");

    @Getter
    private final String value;

    ActiveStatus(String value) {
        this.value = value;
    }

    public static ActiveStatus fromValue(String activeStatus) {
        return Arrays.stream(ActiveStatus.values())
                .filter(status -> status.getValue().equals(activeStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The status " + activeStatus + " does not exist. It should be among " + Arrays.stream(ActiveStatus.values()).map(ActiveStatus::getValue).toList()));
    }
}
