// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.dtt.IDTTComparable DTTComparable}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link IDTTComparable#hasSameContentAs <em>Compare Two SCL element</em>}</li>
 * </ul>
 *
 */
public interface IDTTComparable<T> {

    /**
     * Compares if two elements has the content
     * @param sclElement element to compare with
     * @return <em>Boolean</em> value of comparison result
     */
    boolean hasSameContentAs(T sclElement);
}
