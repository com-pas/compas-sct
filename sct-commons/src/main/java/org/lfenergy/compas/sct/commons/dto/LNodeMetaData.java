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
