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

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SclReport {

    private SclRootAdapter sclRootAdapter;

    private List<ErrorDescription> errorDescriptionList = new ArrayList<>();

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
