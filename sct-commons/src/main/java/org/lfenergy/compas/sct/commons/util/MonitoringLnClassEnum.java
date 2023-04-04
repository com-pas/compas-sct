/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.util;

public enum MonitoringLnClassEnum {
    LSVS("LSVS"),
    LGOS("LGOS");

    private final String value;

    MonitoringLnClassEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
