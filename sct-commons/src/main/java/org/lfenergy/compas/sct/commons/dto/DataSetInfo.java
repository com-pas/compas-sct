// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TAnyLN;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A representation of the model object <em><b>Data Set</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link DataSetInfo#getName() <em>Name</em>}</li>
 *   <li>{@link DataSetInfo#getFCDAInfos() <em>Refers to FCDA infos</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDataSet
 */
@Getter
@NoArgsConstructor
public class DataSetInfo extends LNodeMetaDataEmbedder{
    private String name;
    private List<FCDAInfo> fcdaInfos = new ArrayList<>();

    /**
     * Constructor
     * @param name input
     */
    public DataSetInfo(String name){
        super();
        this.name = name;
    }

    /**
     * Convert DataSet object to DataSetInfo object
     * @param tDataSet object
     * @return DataSetInfo object value
     */
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

    /**
     * Get Set of DataSet from LnAdapter
     * @param lnAdapter object LnAdapter
     * @return Set of DataSetInfo
     */
    public static Set<DataSetInfo> getDataSets(AbstractLNAdapter<? extends TAnyLN> lnAdapter){
        return lnAdapter.getDataSetMatchingExtRefInfo(null)
                .stream().map(DataSetInfo::from).collect(Collectors.toSet());
    }

    /**
     * Add FCDA to FCDA list
     * @param fcdaInfo object FCDAInfo containing FCDA datas
     */
    public void addFCDAInfo(FCDAInfo fcdaInfo){
        fcdaInfos.add(fcdaInfo);
    }

    /**
     * Get FCDA list from DtaSetInfo
     * @return FCDA list
     */
    public List<FCDAInfo> getFCDAInfos(){
        return Collections.unmodifiableList(fcdaInfos);
    }

    /**
     * Set DataSet name
     * @param name string DataSet name
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Check DataSet validity
     * @return validity state
     */
    public boolean isValid(){
        if(name.length() > 32 || fcdaInfos.isEmpty()){
            return false;
        }
        return fcdaInfos.stream().allMatch(FCDAInfo::isValid);
    }
}