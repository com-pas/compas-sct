// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TServiceType;
import org.lfenergy.compas.sct.model.IExtRefDTO;
import org.springframework.lang.NonNull;


@Setter
@Getter
@NoArgsConstructor
public class ExtRefDTO implements IExtRefDTO {
    private String iedName;
    private String ldInst;
    private String prefix;
    private String lnClass;
    private String lnInst;
    private String doName;
    private String daName;
    private TServiceType serviceType;
    private String srcLDInst;
    private String srcPrefix;
    private String srcLNClass;
    private String srcLNInst;
    private String srcCBName;
    private String desc;
    private String pLN;
    private String pDO;
    private String pDA;
    private String intAddr;
    private TServiceType pServT;

    public ExtRefDTO(@NonNull TExtRef tExtRef) {
        iedName = tExtRef.getIedName();
        ldInst = tExtRef.getLdInst();
        prefix = tExtRef.getPrefix();
        if(!tExtRef.getLnClass().isEmpty()) {
            this.lnClass = tExtRef.getLnClass().get(0);
        }
        lnInst = tExtRef.getLnInst();
        doName = tExtRef.getDoName();
        daName = tExtRef.getDaName();
        if(tExtRef.getServiceType() != null) {
            serviceType = tExtRef.getServiceType();
        }
        srcLDInst = tExtRef.getSrcLDInst();
        srcPrefix = tExtRef.getSrcPrefix();
        srcLNInst = tExtRef.getSrcLNInst();
        srcCBName = tExtRef.getSrcCBName();
        desc = tExtRef.getDesc();
        if(!tExtRef.getPLN().isEmpty()) {
            pLN = tExtRef.getPLN().get(0);
        }
        if(!tExtRef.getSrcLNClass().isEmpty()){
            srcLNClass = tExtRef.getSrcLNClass().get(0);
        }
        pDO = tExtRef.getPDO();
        pDA = tExtRef.getPDA();
        intAddr = tExtRef.getIntAddr();
        if(tExtRef.getPServT() != null) {
            pServT = tExtRef.getPServT();
        }
    }
}

