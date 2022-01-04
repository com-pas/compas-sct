// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;
import org.lfenergy.compas.scl2007b4.model.TVal;

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
    private DoTypeName doName = new DoTypeName("");
    private DaTypeName daName = new DaTypeName("");

    public static ResumedDataTemplate copyFrom(ResumedDataTemplate dtt){
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.prefix = dtt.prefix;
        resumedDataTemplate.lnClass = dtt.lnClass;
        resumedDataTemplate.lnInst = dtt.lnInst;
        resumedDataTemplate.lnType = dtt.lnType;
        resumedDataTemplate.doName = DoTypeName.from(dtt.getDoName());
        resumedDataTemplate.daName = DaTypeName.from(dtt.getDaName());

        return resumedDataTemplate;
    }

    public boolean isUpdatable(){
       return daName.isDefined() ? daName.isUpdatable() : false;
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
        return isDoNameDefined() ? doName.toString() : "";
    }

    public String getDaRef(){
        return isDaNameDefined() ? daName.toString() : "";
    }

    public List<String> getDaRefList(){
        ArrayList<String> daRefList = new ArrayList<>();
        if(isDaNameDefined()) {
            daRefList.add(daName.getName());
            daRefList.addAll(daName.getStructNames());
        }
        return daRefList;
    }

    public TFCEnum getFc(){
        return daName.isDefined() ? daName.getFc() : null;
    }

    public void setFc(TFCEnum fc){
        if(isDaNameDefined()){
            daName.setFc(fc);
        } else {
            throw new IllegalArgumentException("Cannot set functional constrain for undefined DA");
        }
    }

    public TPredefinedCDCEnum getCdc(){
        return daName.isDefined() ? doName.getCdc() : null;
    }

    public void setCdc(TPredefinedCDCEnum cdc){
        if(isDoNameDefined()){
            doName.setCdc(cdc);
        } else {
            throw new IllegalArgumentException("Cannot set CDC for undefined DOType");
        }
    }

    public List<String> getSdoNames(){
        if(!isDoNameDefined()) return new ArrayList<>();
        return List.of(doName.getStructNames().toArray(new String[0]));
    }

    public List<String> getBdaNames(){
        if(!isDaNameDefined()) return new ArrayList<>();
        return List.of(daName.getStructNames().toArray(new String[0]));
    }

    public <T extends DataTypeName> void addStructName(String structName, Class<T> cls){
        if(cls.equals(DaTypeName.class) && isDaNameDefined()) {
            daName.addStructName(structName);
        } else if(cls.equals(DoTypeName.class) && isDoNameDefined()) {
            doName.addStructName(structName);
        }  else {
            throw new IllegalArgumentException("Cannot add Struct name for undefined data type");
        }
    }

    public boolean isDoNameDefined() {
        return doName != null && doName.isDefined();
    }

    public boolean isDaNameDefined() {
        return daName != null && daName.isDefined();
    }

    public TPredefinedBasicTypeEnum getBType(){
        return daName != null ? daName.getBType() : null;
    }

    public void setType(String type){
        if(isDaNameDefined()){
            daName.setType(type);
        } else {
            throw new IllegalArgumentException("Cannot define type for undefined BDA");
        }
    }

    public String getType(){
        return daName != null ? daName.getType() : null;
    }

    public void setBType(String bType){
        if(isDaNameDefined()){
            daName.setBType(TPredefinedBasicTypeEnum.fromValue(bType));
        } else {
            throw new IllegalArgumentException("Cannot define Basic type for undefined DA or BDA");
        }
    }

    public void setDoName(DoTypeName doName){
        this.doName = DoTypeName.from(doName);
    }
    public void setDoName(String doName){
        this.doName = new DoTypeName(doName);
    }

    public void setDaName(DaTypeName daName){
        this.daName = DaTypeName.from(daName);
    }
    public void setDaName(String daName){
        this.daName = new DaTypeName(daName);
    }

    public void setDaiValues(List<TVal> values) {
        if(isDaNameDefined()){
            daName.setDaiValues(values);
        }
    }

    public void setValImport(boolean valImport) {
        if(isDaNameDefined()){
            daName.setValImport(valImport);
        }
    }

    public boolean isValImport(){
        return daName.isValImport();
    }
}
