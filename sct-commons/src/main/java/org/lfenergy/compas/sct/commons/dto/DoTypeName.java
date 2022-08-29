// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

/**
 * A representation of the model object <em><b>DoTypeName</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link DoTypeName#getName <em>Name</em>}</li>
 *   <li>{@link DoTypeName#getStructNames <em>Refers To getStructNames</em>}</li>
 *   <li>{@link DoTypeName#getCdc <em>Refers To CDC</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDO
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DoTypeName extends DataTypeName {
    public static final String VALIDATION_REGEX = "[A-Z][0-9A-Za-z]{0,11}(\\.[a-z][0-9A-Za-z]*(\\([0-9]+\\))?)?";
    private TPredefinedCDCEnum cdc;

    /**
     * Constructor
     * @param doName input
     */
    public DoTypeName(String doName) {
        super(doName);
    }

    /**
     * Constructor
     * @param ppDoName input
     * @param sdoNames input
     */
    public DoTypeName(String ppDoName, String sdoNames) {
        super(ppDoName, sdoNames);
    }

    /**
     * Initializes DoTypeName
     * @param dataName input
     * @return DoTypeName object
     */
    public static DoTypeName from(DoTypeName dataName){
        DoTypeName doTypeName = new DoTypeName(dataName.toString());
        if(doTypeName.isDefined()) {
            doTypeName.setCdc(dataName.getCdc());
        }
        return doTypeName;
    }

    /**
     * Copies DO's content
     * @param doName DO object
     */
    public void merge(DoTypeName doName) {
        if(!isDefined()) return;
        if(cdc == null)
            cdc = doName.getCdc();
    }
}
