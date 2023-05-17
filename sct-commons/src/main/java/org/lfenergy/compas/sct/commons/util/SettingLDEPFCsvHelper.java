// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TCompasBay;
import org.lfenergy.compas.scl2007b4.model.TCompasFlowKind;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettingData;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.Utils.*;


/**
 * This class is an implementation example for interface ILDEPFSettings.
 * It relies on a CSV file.
 *
 * @see CsvUtils
 */
@Getter
public class SettingLDEPFCsvHelper implements ILDEPFSettings {

    private final List<LDEPFSettingData> settings ;

    /**
     * Constructor
     *
     * @param reader input
     */
    public SettingLDEPFCsvHelper(Reader reader) {
        this.settings = CsvUtils.parseRows(reader, LDEPFSettingData.class);
    }

    @Override
    public Optional<LDEPFSettingData> getLDEPFSettingDataMatchExtRef(TExtRef extRef) {
        return this.settings.stream().filter(setting -> setting.isMatchExtRef(extRef)).findFirst();
    }

    @Override
    public List<TIED> getIedSources(SclRootAdapter sclRootAdapter, TCompasBay compasBay, LDEPFSettingData setting) {
        return sclRootAdapter.streamIEDAdapters()
                .filter(iedAdapter -> (setting.getBayScope().equals(TCompasFlowKind.BAY_EXTERNAL)
                    && iedAdapter.getPrivateCompasBay().stream().noneMatch(bay -> bay.getUUID().equals(compasBay.getUUID())))
                    || (setting.getBayScope().equals(TCompasFlowKind.BAY_INTERNAL)
                    && iedAdapter.getPrivateCompasBay().stream().anyMatch(bay -> bay.getUUID().equals(compasBay.getUUID()))))
                .filter(iedAdapter -> isIcdHeaderMatch(iedAdapter, setting))
                .filter(iedAdapter -> getActiveSourceLDevice(iedAdapter, setting)
                        .map(lDeviceAdapter -> getActiveLNodeSource(lDeviceAdapter, setting)
                                .map(lnAdapter -> isValidDataTypeTemplate(lnAdapter, setting))
                                .orElse(false))
                        .orElse(false))
                .map(IEDAdapter::getCurrentElem).limit(2).toList();
    }

}
