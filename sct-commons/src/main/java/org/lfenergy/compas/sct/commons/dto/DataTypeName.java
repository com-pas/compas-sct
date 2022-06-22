// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode
public class DataTypeName {
    protected String name = ""; // dataName or DataAttributeName

    private List<String> structNames = new ArrayList<>(); // [.DataName[…]] or [.DAComponentName[ ….]]

    public DataTypeName(String dataName){
        if(dataName == null) return;
        String[] tokens = dataName.split("\\.");
        name = tokens[0];
        if(tokens.length > 1){
            structNames.addAll(List.of(tokens).subList(1, tokens.length));
        }
    }

    public DataTypeName(String name, String names){
        if(name == null) return;
        this.name = name;
        if (StringUtils.isNotBlank(names)){
            structNames.addAll(List.of(names.split("\\.")));
        }
    }

    public static DataTypeName from(DataTypeName dataName){
        return new DataTypeName(dataName.toString());
    }

    public boolean isDefined(){
        return !StringUtils.isBlank(name);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        for(String sName : structNames){
            stringBuilder.append('.');
            stringBuilder.append(sName);
        }
        return stringBuilder.toString();
    }

    public void addStructName(String structName) {
        structNames.add(structName);
    }

    @JsonIgnore
    public String getLast(){
        int sz = structNames.size();
        return sz == 0 ? name : structNames.get(sz -1);
    }

    public void addName(String name) {
        if(isDefined()){
            structNames.add(name);
        } else {
            this.name = name;
        }
    }
}
