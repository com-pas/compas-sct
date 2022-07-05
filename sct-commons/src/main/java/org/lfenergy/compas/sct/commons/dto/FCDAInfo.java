// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

@Getter
@Setter
@NoArgsConstructor
public class FCDAInfo {

    private String dataSet;

    private TFCEnum fc;
    private String ldInst;
    private String prefix;
    private String lnClass;
    private String lnInst;
    private DoTypeName doName; //doName.[...sdoNames]
    private DaTypeName daName; //daName.[...bdaNames]
    private Long ix;

    public FCDAInfo(String dataSet, TFCDA tfcda) {
        this.dataSet = dataSet;
        fc = tfcda.getFc();
        ldInst = tfcda.getLdInst();
        prefix = tfcda.getPrefix();
        if (!tfcda.getLnClass().isEmpty()) {
            this.lnClass = tfcda.getLnClass().get(0);
        }
        lnInst = tfcda.getLnInst();
        doName = new DoTypeName(tfcda.getDoName());
        daName = new DaTypeName(tfcda.getDaName());
        ix = tfcda.isSetIx() ? tfcda.getIx() : null;
    }

    @JsonIgnore
    public TFCDA getFCDA(){
        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst(ldInst);
        tfcda.setFc(fc);
        if(!StringUtils.isBlank(lnClass)){
            tfcda.getLnClass().add(lnClass);
            if(!StringUtils.isBlank(lnInst)){
                tfcda.setLnInst(lnInst);
            }
            if(!StringUtils.isBlank(prefix)){
                tfcda.setPrefix(prefix);
            }
        }

        if(doName != null && doName.isDefined()){
            tfcda.setDaName(doName.toString());
        }

        if(daName != null && daName.isDefined()){
            tfcda.setDaName(daName.toString());
        }

        if(ix != null){
            tfcda.setIx(ix);
        }
        return tfcda;
    }

    public boolean isValid() {
        return doName != null && doName.isDefined();
    }
}
