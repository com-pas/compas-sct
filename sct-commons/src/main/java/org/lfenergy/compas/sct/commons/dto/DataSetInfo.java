// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.scl2007b4.model.TDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of the model object <em><b>Data Set</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link DataSetInfo#getName() <em>Name</em>}</li>
 *   <li>{@link DataSetInfo#getFcdaInfos()} <em>Refers to FCDA infos</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TDataSet
 */
@Getter
@NoArgsConstructor
public class DataSetInfo extends LNodeMetaDataEmbedder {
    //TODO this is a DTO object; it's meant to be used for carry information; he must be created be the one responsible for carying the info
    private String name;
    private List<FCDAInfo> fcdaInfos = new ArrayList<>();

    public DataSetInfo(TDataSet tDataSet) {
        super();
        this.name = tDataSet.getName();
        this.fcdaInfos = tDataSet.getFCDA()
                .stream()
                .map(fcda -> new FCDAInfo(name, fcda.getFc(), fcda.getLdInst(), fcda.getPrefix(), fcda.getLnClass().get(0), fcda.getLnInst(), new DoTypeName(fcda.getDoName()), new DaTypeName(fcda.getDaName()), fcda.getIx()))
                .toList();
    }

    /**
     * Check DataSet validity
     * @return validity state
     */
    public boolean isValid() {
        if (name.length() > 32 || fcdaInfos.isEmpty()) {
            return false;
        }
        return fcdaInfos.stream().allMatch(FCDAInfo::isValid);
    }
}