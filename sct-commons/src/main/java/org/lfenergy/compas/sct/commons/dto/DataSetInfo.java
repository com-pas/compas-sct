// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
public class DataSetInfo {
    private String name;
    private List<FCDAInfo> fcdaInfos = new ArrayList<>();

    public DataSetInfo(String name){
        this.name = name;
    }

    public void addFCDAInfo(FCDAInfo fcdaInfo){
        fcdaInfos.add(fcdaInfo);
    }

    public List<FCDAInfo> getFcdaInfos(){
        return Collections.unmodifiableList(fcdaInfos);
    }

    public void setName(String name){
        this.name = name;
    }
}