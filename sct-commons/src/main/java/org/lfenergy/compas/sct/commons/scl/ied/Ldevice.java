// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.util.LdeviceStatus;

import java.util.Collection;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;

/**
 * Object representing '<LDevice inst="XXX" ldName="XXX" desc="XXX">' , but with utility methods to manipulate the business
 * Do not include "currentElements" and so on inside this class
 */
@Getter
public class Ldevice {

    //Object is supposed to grow by the needs we have
    private final String inst;
    private final String ldName;
    private final String description;
    private final Optional<LdeviceStatus> ldeviceStatus;

    public Ldevice(TLDevice tlDevice) {
        inst = tlDevice.getInst();
        ldName = tlDevice.getLdName();
        description = tlDevice.getDesc();
        ldeviceStatus = tlDevice.getLN0()
                .getDOI()
                .stream()
                .filter(tdoi -> MOD_DO_NAME.equals(tdoi.getName()))
                .findFirst()
                .flatMap(tdoi -> tdoi.getSDIOrDAI()
                        .stream()
                        .filter(dai -> dai.getClass().equals(TDAI.class))
                        .map(TDAI.class::cast)
                        .filter(tdai -> STVAL_DA_NAME.equals(tdai.getName()))
                        .map(TDAI::getVal)
                        .flatMap(Collection::stream)
                        .findFirst()
                        .map(TVal::getValue))
                .map(LdeviceStatus::fromValue);
    }
}
