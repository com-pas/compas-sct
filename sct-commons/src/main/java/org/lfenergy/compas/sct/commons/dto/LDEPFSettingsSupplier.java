// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.util.LDeviceStatus;
import org.lfenergy.compas.sct.commons.util.Utils;


/**
 * Represents a list of LDEPF settings
 *
 * This is a functional interface whose functional method is @getSettings
 */
@FunctionalInterface
public interface LDEPFSettingsSupplier {

    /**
     * This method provides list of LDEPF settings
     */
    List<LDEPFSetting> getSettings();

    @Builder
    record LDEPFSetting(String rteIedType, String iedRedundancy, BigInteger iedInstance,
                        String channelShortLabel, String channelMREP, String channelLevModQ,
                        String channelLevModLevMod, String bapVariant, String bapIgnoredValue,
                        String ldInst, String lnPrefix, String lnClass, String lnInst,
                        String doName, String doInst, String sdoName,
                        String daName, String daType,
                        String dabType, String bdaName, String sbdaName,
                        String channelAnalogNum, String channelDigitalNum, String opt) {
    }

    BiPredicate<LDEPFSetting, TExtRef> isAnalogMatchDesc = (setting, extRef) ->
            setting.channelAnalogNum() != null && setting.channelDigitalNum() == null
                    && extRef.getDesc().startsWith("DYN_LDEPF_ANALOG CHANNEL " + setting.channelAnalogNum()+"_1_AnalogueValue")
                    && extRef.getDesc().endsWith("_" + setting.daName() + "_1");
    BiPredicate<LDEPFSetting, TExtRef> isDigitalMatchDesc = (setting, extRef) ->
            setting.channelDigitalNum() != null && setting.channelAnalogNum() == null
                    && extRef.getDesc().startsWith("DYN_LDEPF_DIGITAL CHANNEL " + setting.channelDigitalNum()+"_1_BOOLEEN")
                    && extRef.getDesc().endsWith("_" + setting.daName() + "_1");


    BiPredicate<IEDAdapter, LDEPFSetting> isIcdHeaderMatch = (iedAdapter, setting) -> iedAdapter.getCompasICDHeader()
            .map(compasICDHeader -> compasICDHeader.getIEDType().value().equals(setting.rteIedType())
                            && compasICDHeader.getIEDredundancy().value().equals(setting.iedRedundancy())
                            && compasICDHeader.getIEDSystemVersioninstance().equals(setting.iedInstance()))
            .orElse(false);

    BiFunction<IEDAdapter, LDEPFSetting, Optional<LDeviceAdapter>> getActiveSourceLDevice = (iedAdapter, setting) ->
            iedAdapter.findLDeviceAdapterByLdInst(setting.ldInst())
                    .filter(lDeviceAdapter -> lDeviceAdapter.getLDeviceStatus()
                            .map(status -> status.equals(LDeviceStatus.ON))
                            .orElse(false))
                    .stream().findFirst();

    BiFunction<LDeviceAdapter, LDEPFSetting, Optional<AbstractLNAdapter<?>>> getActiveLNodeSource = (lDeviceAdapter, setting) ->
            lDeviceAdapter.getLNAdaptersIncludingLN0().stream()
                    .filter(lnAdapter -> lnAdapter.getLNClass().equals(setting.lnClass())
                            && lnAdapter.getLNInst().equals(setting.lnInst())
                            && Utils.equalsOrBothBlank(setting.lnPrefix(), lnAdapter.getPrefix()))
                    .findFirst()
                    .filter(lnAdapter -> lnAdapter.getDaiModStValValue()
                            .map(status -> status.equals(LDeviceStatus.ON))
                            .orElse(true));

    BiPredicate<AbstractLNAdapter<?>, LDEPFSetting> isValidDataTypeTemplate = (lnAdapter, setting) -> {
       String doName = setting.doInst() == null || setting.doInst().equals("0") ? setting.doName() : setting.doName()+setting.doInst();
       var lnType = lnAdapter.getLnType();
        return StringUtils.isEmpty(doName) || StringUtils.isEmpty(setting.daName()) ||
                lnAdapter.getDataTypeTemplateAdapter().getLNodeTypeAdapterById(lnType)
                .filter(lNodeTypeAdapter -> {
                    try {
                        lNodeTypeAdapter.check(new DoTypeName(doName), new DaTypeName(setting.daName()));
                    } catch (ScdException ex) {
                        return false;
                    }
                    return true;
                }).isPresent();
    };

    /**
     * Provides the matching setting for an ExtRef.
     * @param extRef The ExtRef object
     * @return the matching LDEPFSetting for an ExtRef
     */
    default Optional<LDEPFSetting> getLDEPFSettingMatchingExtRef(TExtRef extRef) {
        if(!extRef.isSetDesc()) return Optional.empty();
        return getSettings().stream()
                .filter(setting -> isAnalogMatchDesc.or(isDigitalMatchDesc).test(setting, extRef))
                .filter(setting -> extRef.isSetPLN() && Utils.lnClassEquals(extRef.getPLN(), setting.lnClass())
                        && extRef.isSetPDO() && extRef.getPDO().equals(setting.doName()))
                .findFirst();
    }

    /**
     * Provides the IED sources that belong to the LDEPF setting <br/>
     * Example of this setting include:
     * 1. COMPAS-Bay and COMPAS-ICDHeader verification.<br/>
     * 2. Active LDevice source object with the same DO/DA hierarchy as the LDEPF setting.
     * @param sclRootAdapter SCL
     * @param compasBay TCompasBay
     * @param setting LDEPFSetting
     * @return the IED sources matching the LDEPFSetting
     */
    default List<TIED> getIedSources(SclRootAdapter sclRootAdapter, TCompasBay compasBay, LDEPFSetting setting) {
        return sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> iedAdapter.getPrivateCompasBay().filter(bay -> bay.getUUID().equals(compasBay.getUUID())).isPresent())
                .filter(iedAdapter -> isIcdHeaderMatch.test(iedAdapter, setting))
                .filter(iedAdapter -> getActiveSourceLDevice.apply(iedAdapter, setting)
                        .filter(lDeviceAdapter -> getActiveLNodeSource.apply(lDeviceAdapter, setting)
                                .filter(lnAdapter -> isValidDataTypeTemplate.test(lnAdapter, setting)).isPresent())
                        .isPresent())
                .map(IEDAdapter::getCurrentElem).limit(2).toList();
    }

}
