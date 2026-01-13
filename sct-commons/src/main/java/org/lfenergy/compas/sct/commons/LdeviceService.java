// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.RequiredArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TAccessPoint;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TServer;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LdeviceService {

    private final LnService lnService;

    public Stream<TLDevice> getLdevices(TIED tied) {
        if (!tied.isSetAccessPoint()) {
            return Stream.empty();
        }
        return tied.getAccessPoint()
                .stream()
                .map(TAccessPoint::getServer)
                .filter(Objects::nonNull)
                .filter(TServer::isSetLDevice)
                .flatMap(tServer -> tServer.getLDevice().stream());
    }

    public Stream<TLDevice> getFilteredLdevices(TIED tied, Predicate<TLDevice> ldevicePredicate) {
        return getLdevices(tied).filter(ldevicePredicate);
    }

    public Optional<TLDevice> findLdevice(TIED tied, Predicate<TLDevice> ldevicePredicate) {
        return getFilteredLdevices(tied, ldevicePredicate).findFirst();
    }

    public Optional<TLDevice> findLdevice(TIED tied, String ldInst) {
        return findLdevice(tied, tlDevice -> tlDevice.getInst().equals(ldInst));
    }

    public Optional<ActiveStatus> getLdeviceStatus(TLDevice tlDevice) {
        return lnService.getPrivateCompasLNodeStatus(tlDevice.getLN0());
    }

    public boolean isActive(TLDevice tlDevice) {
        return getLdeviceStatus(tlDevice).filter(ActiveStatus.ON::equals).isPresent();
    }
}
