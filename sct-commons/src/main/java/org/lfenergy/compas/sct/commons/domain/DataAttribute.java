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
import java.util.Objects;

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

    public DataAttribute deepCopy() {
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName(getDaName());
        dataAttribute.setType(getType());
        dataAttribute.setBType(getBType());
        dataAttribute.setFc(getFc());
        dataAttribute.setValImport(isValImport());
        dataAttribute.getBdaNames().addAll(getBdaNames());
        dataAttribute.getDaiValues().addAll(getDaiValues());
        return dataAttribute;
    }

    public void addDaVal(List<TVal> vals) {
        vals.forEach(tVal -> {
            Long settingGroup = tVal.isSetSGroup() ? tVal.getSGroup() : null;
            daiValues.removeIf(daVal -> Objects.equals(daVal.settingGroup(), settingGroup));
            daiValues.add(new DaVal(settingGroup, tVal.getValue()));
        });
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
