// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class DataTypeName {
    protected String name = ""; // dataName or DataAttributeName

    private List<String> structNames = new ArrayList<>(); // [.DataName[…]] or [.DAComponentName[ ….]]

    public DataTypeName(String dataName){
        if(dataName == null) return;

        String[] tokens = dataName.split("\\.");
        name = tokens[0];
        if(tokens.length > 1){
            int idx = dataName.indexOf(".");
            tokens = dataName.substring(idx + 1).split("\\.");
            structNames = Stream.of(tokens).collect(Collectors.toList());
        }
    }

    public static DataTypeName from(DataTypeName dataName){
        return new DataTypeName(dataName.toString());
    }

    public boolean isDefined(){
        return !StringUtils.isBlank(name);
    }

    public DataTypeName(String name, @NonNull String names){
        this.name = name;
        String[] tokens = names.split("\\.");
        structNames = Stream.of(tokens).collect(Collectors.toList());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o==null || o.getClass() != getClass()) return false;
        DataTypeName that = (DataTypeName) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(structNames.toArray(new String[0]),
                        that.structNames.toArray(new String[0]));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, structNames);
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
