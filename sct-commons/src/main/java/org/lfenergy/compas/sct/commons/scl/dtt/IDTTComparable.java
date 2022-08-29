// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

public interface IDTTComparable<T> {

    /**
     * Compares if two elements has the content
     * @param sclElement element to compare with
     * @return <em>Boolean</em> value of comparison result
     */
    boolean hasSameContentAs(T sclElement);
}
