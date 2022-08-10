// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A representation of the model object <em><b>LogicalNodeOptions</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link LogicalNodeOptions#withExtRef <em>withExtRef</em>}</li>
 *   <li>{@link LogicalNodeOptions#withResumedDtt <em>withResumedDtt</em>}</li>
 *   <li>{@link LogicalNodeOptions#withDatSet <em>withDatSet</em>}</li>
 *   <li>{@link LogicalNodeOptions#withCB <em>withCB</em>}</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class LogicalNodeOptions {
    private boolean withExtRef = false;
    private boolean withResumedDtt = false;
    private boolean withCB = false;
    private boolean withDatSet = false;

    /**
     * Constructor
     * @param withExtRef input
     * @param withResumedDtt input
     * @param withCB input
     * @param withDatSet input
     */
    public LogicalNodeOptions(boolean withExtRef, boolean withResumedDtt, boolean withCB, boolean withDatSet) {
        this.withExtRef = withExtRef;
        this.withResumedDtt = withResumedDtt;
        this.withCB = withCB;
        this.withDatSet = withDatSet;
    }
}
