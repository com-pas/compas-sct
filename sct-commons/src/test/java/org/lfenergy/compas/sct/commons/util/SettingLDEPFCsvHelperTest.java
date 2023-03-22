// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


class SettingLDEPFCsvHelperTest {

    @Test
    void readCsvFile_should_return_settings() {
        // Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        // When
        // Then
        SettingLDEPFCsvHelper settingLDEPFCsvHelper = new SettingLDEPFCsvHelper(reader);
        assertThat(settingLDEPFCsvHelper.getSettings()).hasSize(161);
    }
}