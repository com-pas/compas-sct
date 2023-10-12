/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class FcdaForDataSetsCreation {

    @CsvBindByPosition(position = 0)
    private String lnClass;
    @CsvBindByPosition(position = 1)
    private String doName;
    @CsvBindByPosition(position = 2)
    private String daName;
    @CsvBindByPosition(position = 3)
    private String fc;
}
