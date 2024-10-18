// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConnectedAPService {

    public Stream<TConnectedAP> getConnectedAP(TSubNetwork tSubNetwork) {
        if (!tSubNetwork.isSetConnectedAP()) {
            return Stream.empty();
        }
        return tSubNetwork.getConnectedAP().stream();
    }

    public Stream<TConnectedAP> getFilteredConnectedAP(TSubNetwork tSubNetwork, Predicate<TConnectedAP> tConnectedAPPredicate) {
        return getConnectedAP(tSubNetwork).filter(tConnectedAPPredicate);
    }

    public Optional<TConnectedAP> findConnectedAP(TSubNetwork tSubNetwork, Predicate<TConnectedAP> tConnectedAPPredicate) {
        return getFilteredConnectedAP(tSubNetwork, tConnectedAPPredicate).findFirst();
    }

}
