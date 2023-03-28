// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.SettingLDEPFCsvHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;

class LDEPFSettingsTest {

    @Test
    void getLDEPFSettings_should_return_settings() {
        //Given
        String fileName = "LDEPF_Setting_file.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader reader = new InputStreamReader(inputStream);
        LDEPFSettings<SettingLDEPFCsvHelper.SettingLDEPF> ldepfSettings = new SettingLDEPFCsvHelper(reader);
        // When
        // Then
        assertThat(ldepfSettings.getSettings()).hasSize(161);
    }
}