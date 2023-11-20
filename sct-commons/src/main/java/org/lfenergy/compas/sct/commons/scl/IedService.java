// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TAccessPoint;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TServer;

import java.util.Optional;
import java.util.stream.Stream;

public class IedService {

    private static final String TEST_IED = "TEST";

    public Stream<TLDevice> getLDevices(TIED tied) {
        if (!tied.isSetAccessPoint()) {
            return Stream.empty();
        }
        return tied.getAccessPoint().stream()
                .filter(TAccessPoint::isSetServer)
                .map(TAccessPoint::getServer)
                .filter(TServer::isSetLDevice)
                .flatMap(tServer -> tServer.getLDevice().stream());
    }

    public Optional<TLDevice> findLDevice(TIED tied, String ldInst) {
        return getLDevices(tied)
                .filter(tlDevice -> ldInst.equals(tlDevice.getInst()))
                .findFirst();
    }

    public boolean isNotTestIed(TIED tied) {
        return !StringUtils.containsIgnoreCase(tied.getName(), TEST_IED);
    }

}
