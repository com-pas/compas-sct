// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class LNodeMetaDataEmbedder {

    protected LNodeMetaData metaData ;

    LNodeMetaDataEmbedder(){
        this.metaData = new LNodeMetaData();
    }


    public void setMetaData(LNodeMetaData metaData){
        this.metaData = metaData;
    }

    public String getHolderIEDName(){
        return metaData.getIedName();
    }

    public String getHolderLDInst(){
        return metaData.getLdInst();
    }
    public String getHolderLnClass(){
        return metaData.getLnClass();
    }
    public String getHolderLnInst(){
        return metaData.getLnInst();
    }
    public String getHolderLnPrefix(){
        return metaData.getLnPrefix();
    }

    public void setHolderIEDName(String iedName){
        metaData.setIedName(iedName);
    }

    public void setHolderLDInst(String ldInst){
        metaData.setLdInst(ldInst);
    }

    public void setHolderLnClass(String lnClass){
        metaData.setLnClass(lnClass);
    }

    public void setHolderLnInst(String lnInst){
        metaData.setLnInst(lnInst);
    }

    public void setHolderLnPrefix(String prefix){
        metaData.setLnPrefix(prefix);
    }
}
