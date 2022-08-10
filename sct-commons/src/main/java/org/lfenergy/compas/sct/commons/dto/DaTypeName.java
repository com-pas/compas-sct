// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TVal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A representation of the model object <em><b>DA</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link DaTypeName#getName <em>Name</em>}</li>
 *   <li>{@link DaTypeName#getStructNames <em>Refers To StructNames</em>}</li>
 *   <li>{@link org.lfenergy.compas.scl2007b4.model.TFCEnum <em> Refers To TFCEnum </em>}</li>
 *   <li>{@link DaTypeName#getType <em>type</em>}</li>
 *   <li>{@link org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum <em>Refers To TPredefinedBasicTypeEnum</em>}</li>
 *   <li>{@link DaTypeName#isValImport <em>Refers To valImport</em>}</li>
 *   <li>{@link DaTypeName#getDaiValues <em>Refers To DAI Values</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDA
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DaTypeName extends DataTypeName{
    public static final String VALIDATION_REGEX
            = "[a-zA-Z][a-zA-Z0-9]*(\\([0-9]+\\))?(\\.[a-zA-Z][a-zA-Z0-9]*(\\([0-9]+\\))?)*";
    private TFCEnum fc;
    private String type;
    private TPredefinedBasicTypeEnum bType;
    private boolean valImport;
    private Map<Long,String> daiValues = new HashMap<>();

    /**
     * Constructor
     * @param daName input
     */
    public DaTypeName(String daName) {
        super(daName);
    }

    /**
     * Constructor
     * @param name input
     * @param names input
     */
    public DaTypeName(String name, String names) {
        super(name, names);
    }

    /**
     * Initializes DaTypeName
     * @param dataName input
     * @return DaTypeName object
     */
    public static DaTypeName from(DaTypeName dataName){
        DaTypeName daTypeName = new DaTypeName(dataName.toString());
        if(dataName.isDefined()) {
            daTypeName.setFc(dataName.getFc());
            daTypeName.setType(dataName.getType());
            daTypeName.setBType(dataName.getBType());
            daTypeName.daiValues = dataName.getDaiValues().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            daTypeName.valImport = dataName.valImport;
        }
        return daTypeName;
    }

    /**
     * Check valImport state
     * @return boolean value of valImport
     */
    public boolean isValImport(){
        return valImport;
    }

    /**
     * Check if DA is updatable
     * @return boolean value of DA state
     */
    public boolean isUpdatable(){
        return isValImport() &&
                (fc == TFCEnum.CF ||
                        fc == TFCEnum.DC ||
                        fc == TFCEnum.SG ||
                        fc == TFCEnum.SP ||
                        fc == TFCEnum.ST ||
                        fc == TFCEnum.SE
                );
    }

    /**
     * Add DAI values to list of DAI values
     * @param vals list of DAI values
     */
    public void addDaiValues(List<TVal> vals) {
        if(vals.size() == 1){
            daiValues.put(0L,vals.get(0).getValue());
        } else {
            vals.forEach(tVal -> daiValues.put(tVal.getSGroup(), tVal.getValue()));
        }
    }

    /**
     * Add DAI value to Map of DAI values
     * @param val DAI value
     */
    public void addDaiValue(TVal val) {
        if(!val.isSetSGroup()){
            daiValues.put(0L,val.getValue());
        } else {
            daiValues.put(val.getSGroup(), val.getValue());
        }
    }

    /**
     *  Add DAI value to Map of DAI values
     * @param sg Setting group value
     * @param val value
     */
    public void addDaiValue(Long sg, String val) {
        if(sg == null){
            daiValues.put(0L,val);
        } else {
            daiValues.put(sg, val);
        }
    }

    /**
     * Copy DA's contain
     * @param daName DA object
     */
    public void merge(DaTypeName daName) {
        if(!isDefined()) return;
        fc = daName.fc;
        bType = daName.bType;
        type = daName.type;
        valImport = daName.valImport;
        daiValues.putAll(daiValues);
    }
}
