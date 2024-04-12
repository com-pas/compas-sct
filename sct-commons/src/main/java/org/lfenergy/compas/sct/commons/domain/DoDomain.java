// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DoDomain implements Serializable {

    private String doName;
    private TPredefinedCDCEnum cdc;
    private List<String> sdoNames = new ArrayList<>();
}
