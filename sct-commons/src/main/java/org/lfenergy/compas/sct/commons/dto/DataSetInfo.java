// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class DataSetInfo extends LNodeMetaDataEmbedder{
    private String name;
    private List<FCDAInfo> fcdaInfos = new ArrayList<>();

    public DataSetInfo(String name){
        super();
        this.name = name;
    }

    public static DataSetInfo from(TDataSet tDataSet) {
        DataSetInfo dataSetInfo = new DataSetInfo();
        dataSetInfo.name = tDataSet.getName();
        dataSetInfo.fcdaInfos.addAll(
                tDataSet.getFCDA().stream()
                        .map(tfcda -> new FCDAInfo(dataSetInfo.name, tfcda))
                        .collect(Collectors.toList())
        );
        return dataSetInfo;
    }

    public void addFCDAInfo(FCDAInfo fcdaInfo){
        fcdaInfos.add(fcdaInfo);
    }

    public List<FCDAInfo> getFCDAInfos(){
        return Collections.unmodifiableList(fcdaInfos);
    }

    public void setName(String name){
        this.name = name;
    }
}