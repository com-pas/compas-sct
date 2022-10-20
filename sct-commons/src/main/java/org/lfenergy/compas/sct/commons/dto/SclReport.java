/*
 * // SPDX-FileCopyrightText: 2022 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.dto;

import lombok.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Report of services which work on the SCD.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SclReport {

    /**
     * The SCD on which errors were encountered
     */
    private SclRootAdapter sclRootAdapter;

    /**
     * List of errors
     */
    private List<ErrorDescription> errorDescriptionList = new ArrayList<>();

    /**
     *
     * @return true the service succeeded, false otherwise
     */
    public boolean isSuccess() {
        return errorDescriptionList.isEmpty();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Builder
    public static class ErrorDescription{
        private String xpath;
        private String message;
    }
}
