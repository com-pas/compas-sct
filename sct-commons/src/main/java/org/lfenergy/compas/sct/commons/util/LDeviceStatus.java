/*
 * // SPDX-FileCopyrightText: 2022 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.util;

/**
 * A representation of a specific object <em><b>TDAI</b></em> name that have attribute name STVal.
 */
public final class LDeviceStatus {
    public static final String ON = "on";
    public static final String OFF = "off";

    private LDeviceStatus() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
