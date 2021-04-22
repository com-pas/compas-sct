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
public class ExtRefBindingInfo {

    private String iedName;
    private String ldInst;
    private String prefix;
    private String lnClass;
    private String lnInst;
    private String doName;
    private String daName;
    private TServiceType serviceType;

    public ExtRefBindingInfo(TExtRef tExtRef){
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
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o.getClass() != getClass()) return false;
        ExtRefBindingInfo that = (ExtRefBindingInfo) o;
        return Objects.equals(iedName, that.iedName) &&
                Objects.equals(ldInst, that.ldInst) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(lnClass, that.lnClass) &&
                Objects.equals(lnInst, that.lnInst) &&
                Objects.equals(doName, that.doName) &&
                Objects.equals(daName, that.daName) &&
                serviceType == that.serviceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iedName, ldInst, prefix, lnClass, lnInst, doName, daName, serviceType);
    }
}
