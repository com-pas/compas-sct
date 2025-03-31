// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SubNetworkService {

    public Stream<TSubNetwork> getSubNetworks(SCL scl) {
        if (!scl.isSetCommunication()) {
            return Stream.empty();
        }
        if (!scl.getCommunication().isSetSubNetwork()) {
            return Stream.empty();
        }
        return scl.getCommunication().getSubNetwork().stream();
    }

    public Stream<TSubNetwork> getSubNetworks(TCommunication tCommunication) {
        if (!tCommunication.isSetSubNetwork()) {
            return Stream.empty();
        }
        return tCommunication.getSubNetwork().stream();
    }

    public Stream<TSubNetwork> getFilteredSubNetworks(SCL tlNodeType, Predicate<TSubNetwork> tdoPredicate) {
        return getSubNetworks(tlNodeType).filter(tdoPredicate);
    }

    public Optional<TSubNetwork> findSubNetwork(SCL tlNodeType, Predicate<TSubNetwork> tdoPredicate) {
        return getFilteredSubNetworks(tlNodeType, tdoPredicate).findFirst();
    }

}
