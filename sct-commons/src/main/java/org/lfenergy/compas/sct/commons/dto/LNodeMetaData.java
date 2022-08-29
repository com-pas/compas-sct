// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;

/**
 * A representation of common attributes that defines <em><b>LDName, LNName</b></em>.
 * <ul>
 *   <li>{@link LNodeMetaData#getIedName <em>Ied Name</em>}</li>
 *   <li>{@link LNodeMetaData#getLdInst <em>Ld Inst</em>}</li>
 *   <li>{@link LNodeMetaData#getLnClass <em>Ln Class</em>}</li>
 *   <li>{@link LNodeMetaData#getLnInst <em>Ln Inst</em>}</li>
 *   <li>{@link LNodeMetaData#getLnPrefix <em>Prefix</em>}</li>
 * </ul>
 *  @see org.lfenergy.compas.sct.commons.scl.ObjectReference
 */
@Getter
@Setter
@NoArgsConstructor
public class LNodeMetaData {
    private String iedName;
    private String ldInst;
    private String lnClass;
    private String lnInst;
    private String lnPrefix;

    /**
     * Initializes LNode meta data's'
     * @param tAbstractLNAdapter input
     * @return LNodeMetaData object
     */
    public static LNodeMetaData from(@NonNull AbstractLNAdapter<?> tAbstractLNAdapter) {
        LNodeMetaData metaData = new LNodeMetaData();
        metaData.lnClass = tAbstractLNAdapter.getLNClass();
        metaData.lnInst = tAbstractLNAdapter.getLNInst();
        metaData.lnPrefix = tAbstractLNAdapter.getPrefix();

        LDeviceAdapter lDeviceAdapter = tAbstractLNAdapter.getParentAdapter();
        if(lDeviceAdapter != null){
            metaData.ldInst = lDeviceAdapter.getInst();
            if(lDeviceAdapter.getParentAdapter() != null){
                metaData.iedName = lDeviceAdapter.getParentAdapter().getName();
            }
        }
        return metaData;
    }
}
