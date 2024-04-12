// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TVal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DaDomain implements Serializable {

    private String daName;
    private String type;
    private TPredefinedBasicTypeEnum bType;
    private TFCEnum fc;
    private boolean valImport;
    private Map<Long,String> daiValues;
    private List<String> bdaNames = new ArrayList<>();

    public void addDaiValues(List<TVal> vals) {
        if(vals.size() == 1){
            daiValues.put(0L,vals.getFirst().getValue());
        } else {
            vals.forEach(tVal -> daiValues.put(tVal.getSGroup(), tVal.getValue()));
        }
    }
}
