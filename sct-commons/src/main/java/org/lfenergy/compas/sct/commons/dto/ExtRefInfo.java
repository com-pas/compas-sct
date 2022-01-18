// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;

import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
public class ExtRefInfo extends LNodeMetaDataEmbedder{

    private ExtRefSignalInfo signalInfo;
    private ExtRefBindingInfo bindingInfo;
    private ExtRefSourceInfo sourceInfo;

    public ExtRefInfo(TExtRef tExtRef) {
        super();
        bindingInfo = new ExtRefBindingInfo(tExtRef);
        sourceInfo = new ExtRefSourceInfo(tExtRef);
        signalInfo = new ExtRefSignalInfo(tExtRef);
    }

    public static ExtRefInfo from(TExtRef tExtRef, String iedName, String ldInst,
                                  String lnClass, String lnInst, String prefix){
        ExtRefInfo extRefInfo = new ExtRefInfo(tExtRef);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLnInst(lnInst);
        extRefInfo.setHolderLnClass(lnClass);
        extRefInfo.setHolderLnPrefix(prefix);

        return extRefInfo;
    }

    public boolean matchFCDA(@NonNull TFCDA tfcda){
        if(AbstractLNAdapter.isNull(tfcda)) {
            return false;
        }

        if(tfcda.getLdInst() != null &&
                (bindingInfo == null || !tfcda.getLdInst().equals(bindingInfo.getLdInst()))){
            return false;
        }
        if (!tfcda.getLnClass().isEmpty() &&
                ( bindingInfo == null || !tfcda.getLnClass().contains(bindingInfo.getLnClass())) ){
            return false;
        }

        boolean isLN0 = tfcda.getLnClass().contains(TLLN0Enum.LLN_0.value());
        if (!isLN0 && tfcda.getLnInst() != null &&
                (bindingInfo == null || !tfcda.getLnInst().equals(bindingInfo.getLnInst()))) {
            return false;
        }
        if (!isLN0 && !StringUtils.isBlank(tfcda.getPrefix()) &&
                (bindingInfo == null || !tfcda.getPrefix().equals(bindingInfo.getPrefix()))) {
            return false;
        }

        if(!StringUtils.isBlank(tfcda.getDoName()) &&
                (signalInfo == null || !Objects.equals(signalInfo.getPDO(),tfcda.getDoName())) ){
            return false;
        }

        if(!StringUtils.isBlank(tfcda.getDaName()) &&
                (signalInfo == null || !Objects.equals(signalInfo.getPDA(),tfcda.getDaName())) ){
            return false;
        }
        return true;
    }
}
