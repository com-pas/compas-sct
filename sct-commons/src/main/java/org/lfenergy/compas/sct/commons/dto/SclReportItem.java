// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

public record SclReportItem(String xpath, String message, boolean isError) {

    public static SclReportItem error(String xpath, String message) {
        return new SclReportItem(xpath, message, true);
    }

    public static SclReportItem warning(String xpath, String message) {
        return new SclReportItem(xpath, message, false);
    }

}
