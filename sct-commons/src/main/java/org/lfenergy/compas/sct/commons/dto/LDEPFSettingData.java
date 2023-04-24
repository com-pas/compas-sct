// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TCompasFlowKind;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.math.BigInteger;

/**
 * A representation of settings made for LDEPF LDevice
 *
 * @see <a href="https://github.com/com-pas/compas-sct/issues/256" target="_blank">Issue !256</a>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LDEPFSettingData {

    @CsvBindByPosition(position = 0)
    private TCompasFlowKind bayScope;
    @CsvBindByPosition(position = 1)
    private String iedType;
    @CsvBindByPosition(position = 2)
    private String iedRedundancy;
    @CsvBindByPosition(position = 3)
    private BigInteger iedInstance;
    @CsvBindByPosition(position = 4)
    private String channelShortLabel;
    @CsvBindByPosition(position = 5)
    private String channelMREP;
    @CsvBindByPosition(position = 6)
    private String channelLevModQ;
    @CsvBindByPosition(position = 7)
    private String channelLevMod;
    @CsvBindByPosition(position = 8)
    private String bapVariant;
    @CsvBindByPosition(position = 9)
    private String bapIgnoredValue;
    @CsvBindByPosition(position = 10)
    private String ldInst;
    @CsvBindByPosition(position = 11)
    private String lnPrefix;
    @CsvBindByPosition(position = 12)
    private String lnClass;
    @CsvBindByPosition(position = 13)
    private String lnInst;
    @CsvBindByPosition(position = 14)
    private String doName;
    @CsvBindByPosition(position = 15)
    private String doInst;
    @CsvBindByPosition(position = 16)
    private String sdoName;
    @CsvBindByPosition(position = 17)
    private String daName;
    @CsvBindByPosition(position = 18)
    private String daType;
    @CsvBindByPosition(position = 19)
    private String dabType;
    @CsvBindByPosition(position = 20)
    private String bdaName;
    @CsvBindByPosition(position = 21)
    private String sbdaName;
    @CsvBindByPosition(position = 22)
    private Integer channelAnalogNum;
    @CsvBindByPosition(position = 23)
    private Integer channelDigitalNum;
    @CsvBindByPosition(position = 24)
    private String opt;

    /**
     * verify if an Extref matches the Analog type or not.
     */
    private Boolean isAnalogTypeMatchDesc(TExtRef extRef) {
        return getChannelAnalogNum() != null && getChannelDigitalNum() == null
                && extRef.getDesc().startsWith("DYN_LDEPF_ANALOG CHANNEL " + getChannelAnalogNum()+"_1_AnalogueValue")
                && extRef.getDesc().endsWith("_" + getDaName() + "_1");
    }

    /**
     * verify if an Extref matches the Digital type or not.
     */
    private Boolean isDigitalTypeMatchDesc(TExtRef extRef) {
        return getChannelDigitalNum() != null && getChannelAnalogNum() == null
                && extRef.getDesc().startsWith("DYN_LDEPF_DIGITAL CHANNEL " + getChannelDigitalNum()+"_1_BOOLEEN")
                && extRef.getDesc().endsWith("_" + getDaName() + "_1");
    }

    /**
     * verify if an Extref matches the LDEPFSettingData or not.
     */
    public Boolean isMatchExtRef(TExtRef extRef) {
        return extRef.isSetDesc() && (isAnalogTypeMatchDesc(extRef) || isDigitalTypeMatchDesc(extRef))
                && extRef.isSetPLN() && Utils.lnClassEquals(extRef.getPLN(), getLnClass())
                && extRef.isSetPDO() && extRef.getPDO().equals(getDoName());
    }

}
