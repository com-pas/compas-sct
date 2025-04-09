// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DoService {

    public Stream<TDO> getDos(TLNodeType tlNodeType) {
        return tlNodeType.getDO().stream();
    }

    public Stream<TDO> getFilteredDos(TLNodeType tlNodeType, Predicate<TDO> tdoPredicate) {
        return getDos(tlNodeType).filter(tdoPredicate);
    }

    public Optional<TDO> findDo(TLNodeType tlNodeType, Predicate<TDO> tdoPredicate) {
        return getFilteredDos(tlNodeType, tdoPredicate).findFirst();
    }

    public Optional<TDO> findDo(TLNodeType tlNodeType, String doName) {
        return getFilteredDos(tlNodeType, tdo -> tdo.getName().equals(doName)).findFirst();
    }

}
