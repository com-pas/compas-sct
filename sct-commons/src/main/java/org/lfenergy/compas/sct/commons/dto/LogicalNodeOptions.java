// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class LogicalNodeOptions {
    private boolean withExtRef = false;
    //private boolean withResumedDtt = false;
    private boolean withCB = false;
    private boolean withDatSet = false;

    public LogicalNodeOptions(boolean withExtRef/*, boolean withResumedDtt*/, boolean withCB, boolean withDatSet) {
        this.withExtRef = withExtRef;
        //this.withResumedDtt = withResumedDtt;
        this.withCB = withCB;
        this.withDatSet = withDatSet;
    }
}
