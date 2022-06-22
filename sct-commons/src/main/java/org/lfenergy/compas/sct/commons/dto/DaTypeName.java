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

    public DaTypeName(String daName) {
        super(daName);
    }

    public DaTypeName(String name, String names) {
        super(name, names);
    }

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

    public boolean isValImport(){
        return valImport;
    }

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

    public void addDaiValues(List<TVal> vals) {
        if(vals.size() == 1){
            daiValues.put(0L,vals.get(0).getValue());
        } else {
            vals.forEach(tVal -> daiValues.put(tVal.getSGroup(), tVal.getValue()));
        }
    }

    public void addDaiValue(TVal val) {
        if(val.getSGroup() == null){
            daiValues.put(0L,val.getValue());
        } else {
            daiValues.put(val.getSGroup(), val.getValue());
        }
    }

    public void addDaiValue(Long sg, String val) {
        if(sg == null){
            daiValues.put(0L,val);
        } else {
            daiValues.put(sg, val);
        }
    }

    public void merge(DaTypeName daName) {
        if(!isDefined()) return;
        fc = daName.fc;
        bType = daName.bType;
        type = daName.type;
        valImport = daName.valImport;
        daiValues.putAll(daiValues);
    }
}
