// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import java.util.List;

/**
 * Represents a list of LDEPF settings
 * This is a functional interface whose functional method is getLDEPFSettings
 * the type of the input to the operation
 *
 * @param <T> the type of the result of the LDEPFSettings
 *
 * @see org.lfenergy.compas.sct.commons.util.SettingLDEPFCsvHelper
 */
@FunctionalInterface
public interface LDEPFSettings<T> {

    /**
     * This method provides list of LDEPF settings
     */
    List<T> getSettings();
}
