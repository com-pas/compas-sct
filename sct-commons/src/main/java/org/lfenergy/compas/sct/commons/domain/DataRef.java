// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DataRef is the reference of data.
 * At least doName and daName must be filled.
 * It is used to find a DoLinkedToDa.
 *
 * @param doName
 * @param sdoNames
 * @param daName
 * @param bdaNames
 */
public record DataRef(String doName, List<String> sdoNames, String daName, List<String> bdaNames) {

    public DataRef(String doName, List<String> sdoNames, String daName, List<String> bdaNames) {
        if (StringUtils.isBlank(doName)) {
            throw new IllegalArgumentException("doName cannot be blank");
        }
        if (StringUtils.isBlank(daName)) {
            throw new IllegalArgumentException("daName cannot be blank");
        }
        this.doName = doName;
        this.sdoNames = sdoNames == null ? Collections.emptyList() : List.copyOf(sdoNames);
        this.daName = daName;
        this.bdaNames = bdaNames == null ? Collections.emptyList() : List.copyOf(bdaNames);
    }

    public static DataRef from(String doNames, String daNames) {
        if (StringUtils.isBlank(doNames)) {
            throw new IllegalArgumentException("doNames cannot be blank");
        }
        if (StringUtils.isBlank(daNames)) {
            throw new IllegalArgumentException("daNames cannot be blank");
        }
        String doName = doNames.split("\\.")[0];
        List<String> sdoNames = Arrays.stream(doNames.split("\\.")).skip(1).toList();
        String daName = daNames.split("\\.")[0];
        List<String> bdaNames = Arrays.stream(daNames.split("\\.")).skip(1).toList();
        return new DataRef(doName, sdoNames, daName, bdaNames);
    }

    public String getDoRef() {
        return doName + (sdoNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", sdoNames()));
    }

    public String getDaRef() {
        return daName + (bdaNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", bdaNames()));
    }

    @Override
    public String toString() {
        return getDoRef() + "." + getDaRef();
    }
}
