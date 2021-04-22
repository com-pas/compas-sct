// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TExtRef;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
public class ExtRefSourceInfo {
    private String srcLDInst;
    private String srcPrefix;
    private String srcLNClass;
    private String srcLNInst;
    private String srcCBName;

    public ExtRefSourceInfo(TExtRef tExtRef){
        srcLDInst = tExtRef.getSrcLDInst();
        srcPrefix = tExtRef.getSrcPrefix();
        srcLNInst = tExtRef.getSrcLNInst();
        srcCBName = tExtRef.getSrcCBName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        if (this == o) return true;
        ExtRefSourceInfo that = (ExtRefSourceInfo) o;
        return Objects.equals(srcLDInst, that.srcLDInst) &&
                Objects.equals(srcPrefix, that.srcPrefix) &&
                Objects.equals(srcLNClass, that.srcLNClass) &&
                Objects.equals(srcLNInst, that.srcLNInst) &&
                Objects.equals(srcCBName, that.srcCBName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcLDInst, srcPrefix, srcLNClass, srcLNInst, srcCBName);
    }
}
