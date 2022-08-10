// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TExtRef;

import java.util.Objects;

/**
 * A representation of the model object <em><b>ExtRef Source Information</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ExtRefSourceInfo#getSrcLDInst <em>Src LD Inst</em>}</li>
 *   <li>{@link ExtRefSourceInfo#getSrcLNClass <em>Src LN Class</em>}</li>
 *   <li>{@link ExtRefSourceInfo#getSrcLNInst <em>Src LN Inst</em>}</li>
 *   <li>{@link ExtRefSourceInfo#getSrcPrefix <em>Src Prefix</em>}</li>
 *   <li>{@link ExtRefSourceInfo#getSrcCBName <em>Src CB Name</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TExtRef
 */
@Setter
@Getter
@NoArgsConstructor
public class ExtRefSourceInfo {
    private String srcLDInst;
    private String srcPrefix = "";
    private String srcLNClass;
    private String srcLNInst = "";
    private String srcCBName;

    /**
     * Constructor
     * @param tExtRef input
     */
    public ExtRefSourceInfo(TExtRef tExtRef){
        srcLDInst = tExtRef.getSrcLDInst();
        srcPrefix = tExtRef.getSrcPrefix();
        srcLNInst = tExtRef.getSrcLNInst();
        if(!tExtRef.getSrcLNClass().isEmpty()){
            srcLNClass = tExtRef.getSrcLNClass().get(0);
        }
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

    /**
     * Checks ExtRefSourceInfo nullability
     * @return nullability state
     */
    public boolean isNull(){
        return srcCBName == null &&
                srcLDInst == null &&
                srcLNClass == null &&
                srcLNInst == null &&
                srcPrefix == null;
    }

    /**
     * Checks ExtRefSourceInfo validity
     * @return validity state
     */
    public boolean isValid() {
        return !StringUtils.isBlank(srcCBName);
    }

}
