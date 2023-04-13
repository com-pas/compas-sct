// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettingsSupplier;

import java.io.Reader;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;


/**
 * This class is an implementation example for interface LDEPFSettings.
 * It relies on a CSV file.
 *
 * @see CsvUtils
 */
public class SettingLDEPFCsvHelper implements LDEPFSettingsSupplier {

    private final List<LDEPFSetting> settings ;

    /**
     * Constructor
     *
     * @param reader input
     */
    public SettingLDEPFCsvHelper(Reader reader) {
        this.settings = CsvUtils.parseRows(reader, LDEPFSettingBind.class).stream().map(lDEPFSettingsFunction).toList();
    }

    @Override
    public List<LDEPFSetting> getSettings() {
        return this.settings;
    }

    Function<LDEPFSettingBind, LDEPFSetting> lDEPFSettingsFunction = (LDEPFSettingBind setting) -> LDEPFSetting.builder()
            .rteIedType(setting.getRteIedType())
            .iedRedundancy(setting.getIedRedundancy())
            .iedInstance(setting.getIedInstance())
            .channelShortLabel(setting.getChannelShortLabel())
            .channelMREP(setting.getChannelMREP())
            .channelLevModQ(setting.getChannelLevModQ())
            .channelLevModLevMod(setting.getChannelLevModLevMod())
            .bapVariant(setting.getBapVariant())
            .bapIgnoredValue(setting.getBapIgnoredValue())
            .ldInst(setting.getLdInst())
            .lnPrefix(setting.getLnPrefix())
            .lnClass(setting.getLnClass())
            .lnInst(setting.getLnInst())
            .doName(setting.getDoName())
            .doInst(setting.getDoInst())
            .sdoName(setting.getSdoName())
            .daName(setting.getDaName())
            .daType(setting.getDaType())
            .dabType(setting.getDabType())
            .bdaName(setting.getBdaName())
            .sbdaName(setting.getSbdaName())
            .channelAnalogNum(setting.getChannelAnalogNum())
            .channelDigitalNum(setting.getChannelDigitalNum())
            .opt(setting.getOpt())
            .build();

    @Getter
    public static class LDEPFSettingBind {
        @CsvBindByPosition(position = 0)
        private String rteIedType;
        @CsvBindByPosition(position = 1)
        private String iedRedundancy;
        @CsvBindByPosition(position = 2)
        private BigInteger iedInstance;
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
        private String lnClass;
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
