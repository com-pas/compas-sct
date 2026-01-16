// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

public record DaVal(Long settingGroup, String val) {
    public DaVal(String val) {
        this(null, val);
    }
}
