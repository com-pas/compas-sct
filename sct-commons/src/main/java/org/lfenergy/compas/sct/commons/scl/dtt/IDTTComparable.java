// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

public interface IDTTComparable<T> {
    boolean hasSameContentAs(T sclElement);
}
