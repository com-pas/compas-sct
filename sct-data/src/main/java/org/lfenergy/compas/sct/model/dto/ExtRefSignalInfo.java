// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TServiceType;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ExtRefSignalInfo {
    private String desc;
    private String pLN;
    private String pDO;
    private String pDA;
    private String intAddr;
    private TServiceType pServT;

    public ExtRefSignalInfo(TExtRef tExtRef){
        desc = tExtRef.getDesc();
        if(!tExtRef.getPLN().isEmpty()) {
            pLN = tExtRef.getPLN().get(0);
        }
        pDO = tExtRef.getPDO();
        pDA = tExtRef.getPDA();
        intAddr = tExtRef.getIntAddr();
        if(tExtRef.getPServT() != null) {
            pServT = tExtRef.getPServT();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o.getClass() != this.getClass()) return false;
        ExtRefSignalInfo that = (ExtRefSignalInfo) o;
        return Objects.equals(desc, that.desc) &&
                Objects.equals(pLN, that.pLN) &&
                Objects.equals(pDO, that.pDO) &&
                Objects.equals(pDA, that.pDA) &&
                Objects.equals(intAddr, that.intAddr) &&
                pServT == that.pServT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, pLN, pDO, pDA, intAddr, pServT);
    }

    public boolean isWrappedIn(TExtRef tExtRef) {
        if((desc != null && tExtRef.getDesc() == null) ||
                (desc == null && tExtRef.getDesc() != null)  ||
                (desc != null && tExtRef.getDesc() != null && !desc.equals(tExtRef.getDesc()))){
            return false;
        }
        if(!tExtRef.getPLN().isEmpty() && !tExtRef.getPLN().contains(pLN)){
            return false;
        }

        if((pDO == null && tExtRef.getPDO() != null) ||
                (pDO != null && tExtRef.getPDO() == null) ||
                (pDO != null && !pDO.equals(tExtRef.getPDO()))){
            return false;
        }
        if( (pDA == null && tExtRef.getPDA() != null) ||
                (pDA != null && tExtRef.getPDO() == null) ||
                (pDA != null && !pDA.equals(tExtRef.getPDA()))){
            return false;
        }

        if((intAddr != null && tExtRef.getIntAddr() == null) ||
                (intAddr == null && tExtRef.getIntAddr() != null) ||
                (intAddr != null && !intAddr.equals(tExtRef.getIntAddr()))){
            return false;
        }

        if((pServT != null && tExtRef.getPServT() == null) ||
                (pServT == null && tExtRef.getPServT() != null) ||
                (pServT != null && !pServT.equals(tExtRef.getPServT()))){
            return false;
        }
        return true;
    }
}
