// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Objects;


@Getter
@Setter
public class DataTypeName {
    protected String name = "";
    protected String validationPattern = "";
    private List<String> structNames = new ArrayList<>();

    public DataTypeName(String dataName){
        if(dataName == null) return;

        String[] tokens = dataName.split("\\.");
        name = tokens[0];
        if(tokens.length > 1){
            int idx = dataName.indexOf(".");
            tokens = dataName.substring(idx + 1).split("\\.");
            structNames = Arrays.asList(Arrays.copyOf(tokens,tokens.length));
        }
    }

    public DataTypeName(String name, @NonNull String names){
        this.name = name;
        String[] tokens = names.split("\\.");
        structNames = Arrays.asList(Arrays.copyOf(tokens,tokens.length));
    }

    public void setStructNames(List<String> ss){
        structNames = List.copyOf(ss);
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
}
