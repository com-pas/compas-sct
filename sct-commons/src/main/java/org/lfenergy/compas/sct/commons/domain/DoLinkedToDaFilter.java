// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DoLinkedToDaFilter {

    private String doName;
    private String daName;
    private List<String> sdoNames = new ArrayList<>();
    private List<String> bdaNames = new ArrayList<>();

    public String getDoRef() {
        return doName + (getSdoNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", getSdoNames()));
    }

    public String getDaRef() {
        return daName + (getBdaNames().isEmpty() ? StringUtils.EMPTY : "." + String.join(".", getBdaNames()));
    }

}
