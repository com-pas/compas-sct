// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDaFilter;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface LnEditor {

    Optional<ActiveStatus> getDaiModStValValue(TAnyLN tAnyLN);

    Optional<TDAI> getDaiModStVal(TAnyLN tAnyLN);

    Optional<TDAI> getDOAndDAInstances(TAnyLN tAnyLN, DoLinkedToDaFilter doLinkedToDaFilter);

    void updateOrCreateDOAndDAInstances(TAnyLN tAnyLN, DoLinkedToDa doLinkedToDa);

    DoLinkedToDa getDoLinkedToDaCompletedFromDAI(TIED tied, String ldInst, TAnyLN anyLN, DoLinkedToDa doLinkedToDa);

    Stream<TAnyLN> getAnylns(TLDevice tlDevice);

    Optional<TLN> findLn(TLDevice tlDevice, Predicate<TLN> lnPredicate);

    boolean matchesLn(TAnyLN tAnyLN, String lnClass, String lnInst, String lnPrefix);
}
