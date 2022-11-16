// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public final class SclReportItem {
    private final String xpath;
    private final String message;
    private final boolean isFatal;

    public static SclReportItem fatal(String xpath, String message) {
        return new SclReportItem(xpath, message, true);
    }

    public static SclReportItem warning(String xpath, String message) {
        return new SclReportItem(xpath, message, false);
    }

}
