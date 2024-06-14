// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record DoLinkedToDaFilter(String doName, List<String> sdoNames, String daName, List<String> bdaNames) {

    public DoLinkedToDaFilter(String doName, List<String> sdoNames, String daName, List<String> bdaNames) {
        this.doName = StringUtils.isBlank(doName) ? null : doName;
        this.sdoNames = sdoNames == null ? Collections.emptyList() : List.copyOf(sdoNames);
        this.daName = StringUtils.isBlank(daName) ? null : daName;
        this.bdaNames = bdaNames == null ? Collections.emptyList() : List.copyOf(bdaNames);
    }
    public DoLinkedToDaFilter(){
        this(null,null,null,null);
    }

    public static DoLinkedToDaFilter from(String doNames, String daNames) {
        String doName = null;
        List<String> sdoNames = null;
        String daName = null;
        List<String> bdaNames = null;
        if (StringUtils.isNotBlank(doNames)){
            doName = doNames.split("\\.")[0];
            sdoNames = Arrays.stream(doNames.split("\\.")).skip(1).toList();
        }
        if (StringUtils.isNotBlank(daNames)){
            daName = daNames.split("\\.")[0];
            bdaNames = Arrays.stream(daNames.split("\\.")).skip(1).toList();
        }
        return new DoLinkedToDaFilter(doName, sdoNames, daName, bdaNames);
    }

public String getDoRef() {
    return doName + (sdoNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", sdoNames()));
}

public String getDaRef() {
    return daName + (bdaNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", bdaNames()));
}

}
