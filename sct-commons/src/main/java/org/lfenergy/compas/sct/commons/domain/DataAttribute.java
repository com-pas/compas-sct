// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TVal;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DataAttribute {

    private String daName;
    private String type;
    private TPredefinedBasicTypeEnum bType;
    private TFCEnum fc;
    private boolean valImport;
    private List<String> bdaNames = new ArrayList<>();
    private List<DaVal> daiValues = new ArrayList<>();

    public static DataAttribute copyFrom(DataAttribute dataAttribute) {
        DataAttribute dataAttribute1 = new DataAttribute();
        dataAttribute1.setDaName(dataAttribute.getDaName());
        dataAttribute1.setFc(dataAttribute.getFc());
        dataAttribute1.setBType(dataAttribute.getBType());
        dataAttribute1.setType(dataAttribute.getType());
        dataAttribute1.getBdaNames().addAll(dataAttribute.getBdaNames());
        dataAttribute1.setValImport(dataAttribute.isValImport());
        return dataAttribute1;
    }

    public void addDaVal(List<TVal> vals) {
       vals.forEach(tVal -> daiValues.add(new DaVal(tVal.isSetSGroup() ? tVal.getSGroup() : null, tVal.getValue())));
    }

    @Override
    public String toString(){
        return daName + (getBdaNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", getBdaNames()));
    }

    /**
     * Check if DA Object is updatable
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

}
