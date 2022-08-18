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

    /**
     * Constructor
     * @param dataName imput DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) names
     */
    public DataTypeName(String dataName){
        if(dataName == null) return;
        String[] tokens = dataName.split("\\.");
        name = tokens[0];
        if(tokens.length > 1){
            structNames.addAll(List.of(tokens).subList(1, tokens.length));
        }
    }

    /**
     * Creates DataTypeName object from DataName by constructor
     * @param name string containing DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name
     * @param names string containing DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) names
     */
    public DataTypeName(String name, String names){
        if(name == null) return;
        this.name = name;
        if (StringUtils.isNotBlank(names)){
            structNames.addAll(List.of(names.split("\\.")));
        }
    }

    /**
     * Initializes DataTypeName from DataTypeName
     * @param dataName input
     * @return DataTypeName object
     */
    public static DataTypeName from(DataTypeName dataName){
        return new DataTypeName(dataName.toString());
    }

    /**
     * Checks if DataTypeName is well defined
     * @return boolean definition state of DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name
     */
    public boolean isDefined(){
        return !StringUtils.isBlank(name);
    }

    /**
     * Converts to formatted String the DataTypeName
     * @return string of name and structNames comma separated
     */
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

    /**
     * Adds list of DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) names to existing DataTypeName
     * @param structName list of string DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name's
     */
    public void addStructName(String structName) {
        structNames.add(structName);
    }

    /**
     * Gets last name from DataTypeName (last DA(da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name
     * from list of DA(da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) names)
     * @return string DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name
     */
    @JsonIgnore
    public String getLast(){
        int sz = structNames.size();
        return sz == 0 ? name : structNames.get(sz -1);
    }

    /**
     * Adds DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name to DataTypeName
     * @param name DA (da.bda1[.bda2...bda_n])/DO (do.sdo1[.sdo2 ...sdo_n]) name
     */
    public void addName(String name) {
        if(isDefined()){
            structNames.add(name);
        } else {
            this.name = name;
        }
    }
}
