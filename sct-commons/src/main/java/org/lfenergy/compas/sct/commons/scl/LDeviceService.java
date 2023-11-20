// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TLN;

import java.util.Optional;
import java.util.stream.Stream;

public class LDeviceService {

    public Stream<TExtRef> getExtRefs(TLDevice tlDevice) {
        if (!tlDevice.isSetLN0() || !tlDevice.getLN0().isSetInputs() || !tlDevice.getLN0().getInputs().isSetExtRef()) {
            return Stream.empty();
        } else {
            return tlDevice.getLN0().getInputs().getExtRef().stream();
        }
    }

    public Stream<TLN> getLns(TLDevice tlDevice) {
        if (!tlDevice.isSetLN()) {
            return Stream.empty();
        }
        return tlDevice.getLN().stream();
    }

    public Optional<TAnyLN> findAnyLn(TLDevice tlDevice, LnKey lnKey) {
        if (lnKey.isLn0()) {
            return Optional.ofNullable(tlDevice.getLN0());
        } else {
            return findLn(tlDevice, lnKey).map(TAnyLN.class::cast);
        }
    }

    public Optional<TLN> findLn(TLDevice tlDevice, LnKey lnKey) {
        return getLns(tlDevice)
                .filter(tln -> lnKey.equals(LnKey.from(tln)))
                .findFirst();
    }
}
