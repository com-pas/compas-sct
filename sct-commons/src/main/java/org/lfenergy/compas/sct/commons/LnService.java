// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;

public class LnService {

    public Stream<TAnyLN> getAnylns(TLDevice tlDevice) {
        return Stream.concat(Stream.of(tlDevice.getLN0()), tlDevice.getLN().stream());
    }

    /**
     * The Lnode status depends on the LN0 status.
     * If Ln stVAl = null => we take the LN0 status
     * If Ln stVAl = OFF => the status is OFF
     * If Ln stVAl = ON => we take the LN0 status
     *
     * @param tAnyLN the Lnode whose the status is required
     * @param ln0    the LN0
     * @return the Lnode Status
     */
    public ActiveStatus getLnStatus(TAnyLN tAnyLN, LN0 ln0) {
        Optional<ActiveStatus> ln0Status = getDaiModStval(ln0);
        return getDaiModStval(tAnyLN).filter(ActiveStatus.OFF::equals).orElseGet(() -> ln0Status.orElse(ActiveStatus.OFF));
    }

    public Optional<ActiveStatus> getDaiModStval(TAnyLN tAnyLN) {
        return tAnyLN
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
                .map(ActiveStatus::fromValue);
    }

    public Stream<TAnyLN> getActiveLns(TLDevice tlDevice) {
        LN0 ln0 = tlDevice.getLN0();
        Stream<TLN> tlnStream = tlDevice.getLN()
                .stream()
                .filter(tln -> ActiveStatus.ON.equals(getLnStatus(tln, ln0)));
        Stream<LN0> ln0Stream = Stream.of(ln0).filter(ln02 -> getDaiModStval(ln02).map(ActiveStatus.ON::equals).orElse(false));
        return Stream.concat(ln0Stream, tlnStream);
    }
}
