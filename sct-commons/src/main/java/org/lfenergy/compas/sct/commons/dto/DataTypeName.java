// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of the model object <em><b>Data Type Name</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link DataTypeName#getName <em>Name</em>}</li>
 *   <li>{@link DataTypeName#getStructNames <em>Refers to middle name part of a structured data name</em>}
 *      <p>
 *          <b>Example of instantiated subdata</b>
 *      </p>
 *      <pre>
 *          {@code
 *              <DOI name=”X”>
 *                  <SDI name=”X”>
 *                      <SDI name=”X”>
 *                          <DAI name=”X” />
 *                      </SDI>
 *                  </SDI>
 *              </DOI>
 *          }
 *   </li>
 * </ul>
 *
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class DataTypeName {
    protected static final String DELIMITER = ".";

    protected String name = ""; // dataName or DataAttributeName

    private List<String> structNames = new ArrayList<>(); // [.DataName[…]] or [.DAComponentName[ ….]]

    /**
     * Create DataTypeName object from DataName by constructor
     * @param dataName string containing DA/DO names
     */
    public DataTypeName(String dataName){
        if(dataName == null) return;
        String[] tokens = dataName.split("\\.");
        this.name = tokens[0];
        if(tokens.length > 1){
            structNames.addAll(List.of(tokens).subList(1, tokens.length));
        }
    }

    /**
     * Create DataTypeName object from DataName by constructor
     * @param name string containing DA/DO name
     * @param names string containing DA/DO names
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
     * Check if DataTypeName is well defined
     * @return boolean definition state of DA/DO name
     */
    public boolean isDefined(){
        return !StringUtils.isBlank(name);
    }

    /**
     * Convert to formatted String the DataTypeName
     * @return string of name and structNames comma separated
     */
    @Override
    public String toString(){
        return name
            + (getStructNames().isEmpty() ? StringUtils.EMPTY : DELIMITER + String.join(DELIMITER, getStructNames()));
    }

    /**
     * Add list of DA/DO names to existing DataTypeName
     * @param structName list of string DA/DO name's
     */
    public void addStructName(String structName) {
        structNames.add(structName);
    }

    /**
     * Get last name from DataTypeName (last DA/DO name from list of DA/DO names)
     * @return string DA/DO name
     */
    @JsonIgnore
    public String getLast(){
        int sz = structNames.size();
        return sz == 0 ? name : structNames.get(sz -1);
    }

    /**
     * Add DA/DO name to DataTypeName
     * @param name DA/DO name
     */
    public void addName(String name) {
        if(isDefined()){
            structNames.add(name);
        } else {
            this.name = name;
        }
    }
}
