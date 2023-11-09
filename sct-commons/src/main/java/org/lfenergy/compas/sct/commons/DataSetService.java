// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TLN;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DataSetService {

    public Stream<TDataSet> getDataSets(TAnyLN tAnyLN) {
        return switch (tAnyLN) {
            case LN0 ln0 -> ln0.isSetDataSet() ? ln0.getDataSet().stream() : Stream.empty();
            case TLN tln -> tln.isSetDataSet() ? tln.getDataSet().stream() : Stream.empty();
            default -> throw new IllegalStateException("Unexpected value: " + tAnyLN);
        };
    }

    public Stream<TDataSet> getFilteredDataSets(TAnyLN tAnyLN, Predicate<TDataSet> dataSetPredicate) {
        return getDataSets(tAnyLN).filter(dataSetPredicate);
    }

    public Optional<TDataSet> findDataSet(TAnyLN tAnyLN, Predicate<TDataSet> dataSetPredicate) {
        return getFilteredDataSets(tAnyLN, dataSetPredicate).findFirst();
    }
}
