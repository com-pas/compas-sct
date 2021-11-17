// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
public class ResumedDataTemplate {

    private String prefix;
    private String lnType;
    private String lnClass;
    private String lnInst;
    private DoTypeName doName;
    private DaTypeName daName;
    private Map<Long,String> daiValues = new HashMap<>();
    private boolean valImport = false;

    public static ResumedDataTemplate copyFrom(ResumedDataTemplate dtt){
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.prefix = dtt.prefix;
        resumedDataTemplate.lnClass = dtt.lnClass;
        resumedDataTemplate.lnInst = dtt.lnInst;
        resumedDataTemplate.lnType = dtt.lnType;
        resumedDataTemplate.doName = new DoTypeName(dtt.getDoName().toString());
        resumedDataTemplate.doName.setCdc(dtt.getDoName().getCdc());
        resumedDataTemplate.daName = new DaTypeName(dtt.getDaName().toString());
        resumedDataTemplate.daName.setFc(dtt.getDaName().getFc());
        resumedDataTemplate.daName.setType(dtt.daName.getType());
        resumedDataTemplate.daName.setBType(dtt.daName.getBType());
        resumedDataTemplate.daiValues = dtt.getDaiValues().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        resumedDataTemplate.valImport = dtt.valImport;

        return resumedDataTemplate;
    }


    public boolean isUpdatable(){
        TFCEnum fc = daName.getFc();
        return valImport &&
            (fc == TFCEnum.CF ||
                fc == TFCEnum.DC ||
                fc == TFCEnum.SG ||
                fc == TFCEnum.SP ||
                fc == TFCEnum.ST ||
                fc == TFCEnum.SE
            );
    }

    public String getObjRef(String iedName, String ldInst){
        StringBuilder stringBuilder = new StringBuilder();
        //LDName
        stringBuilder.append(iedName)
                .append(ldInst)
                .append("/");
        if(TLLN0Enum.LLN_0.value().equals(lnClass)){
            stringBuilder.append(TLLN0Enum.LLN_0.value());
        } else {
            stringBuilder.append(prefix)
                    .append(lnClass)
                    .append(lnInst);
        }
        stringBuilder.append('.')
                .append(getDoRef())
                .append('.')
                .append(getDaRef());

        return stringBuilder.toString();
    }

    public String getDoRef(){
        return doName.toString();
    }

    public String getDaRef(){
        return daName.toString();
    }

    public List<String> getDaRefList(){
        ArrayList<String> daRefList = new ArrayList<>();
        daRefList.add(daName.getName());
        daRefList.addAll(daName.getStructNames());
        return daRefList;
    }

    public void setDoName(DoTypeName doName){
        this.doName = doName;
    }

    public void setDoName(String doName){
        this.doName = new DoTypeName(doName);
    }

    public void setDaName(DaTypeName daName){
        this.daName = daName;
    }

    public void setDaName(String daName){
        this.daName = new DaTypeName(daName);
    }
}
