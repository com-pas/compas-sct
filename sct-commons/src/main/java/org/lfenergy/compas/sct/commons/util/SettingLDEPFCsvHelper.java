// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.CsvBindByPosition;
import lombok.ToString;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettings;

import java.io.Reader;
import java.util.List;


/**
 * This class is an implementation example for interface LDEPFSettings.
 * It relies on a CSV file.
 *
 * @see CsvUtils
 */
public class SettingLDEPFCsvHelper implements LDEPFSettings<SettingLDEPFCsvHelper.SettingLDEPF> {

    private final List<SettingLDEPF> settings ;

    /**
     * Constructor
     * @param reader input
     */
    public SettingLDEPFCsvHelper(Reader reader) {
        this.settings = CsvUtils.parseRows(reader, SettingLDEPF.class).stream().toList();
    }

    @Override
    public List<SettingLDEPF> getSettings() {
        return this.settings;
    }

    @ToString
    public static class SettingLDEPF {
        @CsvBindByPosition(position = 0)
        private String rteIedType;
        @CsvBindByPosition(position = 1)
        private String iedRedundancy;
        @CsvBindByPosition(position = 2)
        private String iedInstance;
        @CsvBindByPosition(position = 3)
        private String channelShortLabel;
        @CsvBindByPosition(position = 4)
        private String channelMREP;
        @CsvBindByPosition(position = 5)
        private String channelLevModQ;
        @CsvBindByPosition(position = 6)
        private String channelLevModLevMod;
        @CsvBindByPosition(position = 7)
        private String bapVariant;
        @CsvBindByPosition(position = 8)
        private String bapIgnoredValue;
        @CsvBindByPosition(position = 9)
        private String ldInst;
        @CsvBindByPosition(position = 10)
        private String lnPrefix;
        @CsvBindByPosition(position = 11)
        private String lnName;
        @CsvBindByPosition(position = 12)
        private String lnInst;
        @CsvBindByPosition(position = 13)
        private String doName;
        @CsvBindByPosition(position = 14)
        private String doInst;
        @CsvBindByPosition(position = 15)
        private String sdoName;
        @CsvBindByPosition(position = 16)
        private String daName;
        @CsvBindByPosition(position = 17)
        private String daType;
        @CsvBindByPosition(position = 18)
        private String dabType;
        @CsvBindByPosition(position = 19)
        private String bdaName;
        @CsvBindByPosition(position = 20)
        private String sbdaName;
        @CsvBindByPosition(position = 21)
        private String channelAnalogNum;
        @CsvBindByPosition(position = 22)
        private String channelDigitalNum;
        @CsvBindByPosition(position = 23)
        private String opt;
    }
}
