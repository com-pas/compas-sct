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
    private String doName;
    private TPredefinedCDCEnum cdc;
    private List<String> sdoNames = new ArrayList<>();
    private String daName;
    private TFCEnum fc;
    private List<String> bdaNames = new ArrayList<>();
    private Map<Long,String> daiValues = new HashMap<>();
    private boolean valImport = false;
    private String type;
    private String bType;


    public static ResumedDataTemplate copyFrom(ResumedDataTemplate dtt){
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.prefix = dtt.prefix;
        resumedDataTemplate.lnClass = dtt.lnClass;
        resumedDataTemplate.lnInst = dtt.lnInst;
        resumedDataTemplate.lnType = dtt.lnType;
        resumedDataTemplate.doName = dtt.doName;
        resumedDataTemplate.cdc = dtt.cdc;
        String[] dd = Arrays.copyOf(dtt.sdoNames.toArray(new String[0]),dtt.sdoNames.size());
        resumedDataTemplate.sdoNames.addAll(Arrays.asList(dd));
        resumedDataTemplate.daName = dtt.daName;
        resumedDataTemplate.fc = dtt.fc;
        dd = Arrays.copyOf(dtt.bdaNames.toArray(new String[0]),dtt.bdaNames.size());
        resumedDataTemplate.bdaNames.addAll(Arrays.asList(dd));
        resumedDataTemplate.daiValues = dtt.getDaiValues().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        resumedDataTemplate.type = dtt.type;
        resumedDataTemplate.bType = dtt.bType;
        resumedDataTemplate.valImport = dtt.valImport;

        return resumedDataTemplate;
    }


    public boolean isUpdatable(){
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
                .append("/")
                .append(prefix)
                .append(lnClass);
        if(!TLLN0Enum.LLN_0.value().equals(lnClass)){
            stringBuilder.append(lnInst);
        }
        stringBuilder.append('.')
                .append(getDoRef())
                .append('.')
                .append(getDaRef());

        return stringBuilder.toString();
    }

    public String getDoRef(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(doName);
        for(String sdo : sdoNames){
            stringBuilder.append('.');
            stringBuilder.append(sdo);
        }
        return stringBuilder.toString();
    }

    public String getDaRef(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(daName);
        for(String bda : bdaNames){
            stringBuilder.append('.');
            stringBuilder.append(bda);
        }
        return stringBuilder.toString();
    }

    public List<String> getDaRefList(){
        ArrayList<String> daRefList = new ArrayList<>();
        daRefList.add(daName);
        for(String bda : bdaNames){
            daRefList.add(bda);
        }
        return daRefList;
    }

    public void setDoName(String doName){

        List<String> sp = Arrays.asList(doName.split("\\."));
        if(!sp.isEmpty()) {
            this.doName = sp.get(0);
            if (sp.size() > 1) {
                sdoNames.addAll(sp.subList(1, sp.size()));
            }
        }
    }

    public void setDaName(String doName){

        List<String> sp = Arrays.asList(doName.split("\\."));
        if(!sp.isEmpty()) {
            this.daName = sp.get(0);
            if (sp.size() > 1) {
                bdaNames.addAll(sp.subList(1, sp.size()));
            }
        }
    }
}
