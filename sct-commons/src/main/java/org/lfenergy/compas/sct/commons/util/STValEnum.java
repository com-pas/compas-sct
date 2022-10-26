/*
 * // SPDX-FileCopyrightText: 2022 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.util;

/**
 * A representation of a specific object <em><b>TDAI</b></em> name that have attribute name STVal.
 *
 * @see org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum
 */
public enum STValEnum {
    ON("on"),
    OFF("off");
    public final String value;
    STValEnum(String value) {
        this.value = value;
    }
}