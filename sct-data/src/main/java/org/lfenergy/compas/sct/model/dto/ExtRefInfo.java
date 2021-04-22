// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TExtRef;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ExtRefInfo {
    private ExtRefSignalInfo signalInfo;
    private ExtRefBindingInfo bindingInfo;
    private ExtRefSourceInfo sourceInfo;

    public ExtRefInfo(TExtRef tExtRef) {
        bindingInfo = new ExtRefBindingInfo(tExtRef);
        sourceInfo = new ExtRefSourceInfo(tExtRef);
        signalInfo = new ExtRefSignalInfo(tExtRef);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        if (this == o) return true;
        ExtRefInfo that = (ExtRefInfo) o;
        return Objects.equals(signalInfo, that.signalInfo) &&
                Objects.equals(bindingInfo, that.bindingInfo) &&
                Objects.equals(sourceInfo, that.sourceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signalInfo, bindingInfo, sourceInfo);
    }
}
