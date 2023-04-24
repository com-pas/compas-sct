// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.LDEPFSettingData;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.List;
import java.util.Optional;


/**
 * This interface showcases the LDEPF parameters for the LDEPF LDevice.
 */
public interface ILDEPFSettings {

     /**
     * Provides the matching setting for an ExtRef.
     * @param extRef The ExtRef object
     * @return the matching LDEPFSettingDTO for an ExtRef
     */
    Optional<LDEPFSettingData> getLDEPFSettingDataMatchExtRef(TExtRef extRef);

    /**
     * Provides valid IED sources with LDEPF configuration.<br/>
     * Example of LDEPF configuration include:<br/>
     * 1. COMPAS-Bay verification that should be closed to the provided Flow Kind<br/>
     * 2. COMPAS-ICDHeader verification that should match the provided parameters, see {@link Utils#isIcdHeaderMatch}<br/>
     * 3. Active LDevice source object that should match the provided parameters, see {@link Utils#getActiveSourceLDevice}<br/>
     * 4. Active LNode source object that should match the provided parameters, see {@link Utils#getActiveLNodeSource}<br/>
     * 5. Valid DataTypeTemplate Object hierarchy that should match the DO/DA/BDA parameters, see {@link Utils#isValidDataTypeTemplate}<br/>
     * @param sclRootAdapter SCL
     * @param compasBay TCompasBay
     * @param setting LDEPFSetting
     * @return the IED sources matching the LDEPFSetting
     */
    List<TIED> getIedSources(SclRootAdapter sclRootAdapter, TCompasBay compasBay, LDEPFSettingData setting);

}
