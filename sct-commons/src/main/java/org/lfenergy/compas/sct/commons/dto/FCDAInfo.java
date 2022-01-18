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
    private DataTypeName doName; //doName.[...sdoNames]
    private DataTypeName daName; //daName.[...bdaNames]
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
        doName = new DataTypeName(tfcda.getDoName());
        daName = new DataTypeName(tfcda.getDaName());
        ix = tfcda.getIx();
    }


    @JsonIgnore
    public TFCDA getFCDA(){
        TFCDA tfcda = new TFCDA();
        tfcda.setFc(fc);
        tfcda.setLdInst(ldInst);
        tfcda.getLnClass().add(lnClass);
        if(!StringUtils.isBlank(lnInst)){
            tfcda.setLnInst(lnInst);
        }
        tfcda.setDoName(doName.toString());
        if(daName != null){
            tfcda.setDaName(daName.toString());
        }
        if(ix != null){
            tfcda.setIx(ix);
        }

        return tfcda;
    }

    public boolean isValid() {
        return doName != null && !StringUtils.isBlank(doName.toString());
    }
}