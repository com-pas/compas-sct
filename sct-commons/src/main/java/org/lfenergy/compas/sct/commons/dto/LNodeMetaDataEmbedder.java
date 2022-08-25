// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.AllArgsConstructor;

/**
 * A representation of the model object <em><b>LNodeMetaDataEmbedder</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link LNodeMetaDataEmbedder#metaData <em>Refers to {@link org.lfenergy.compas.sct.commons.dto.LNodeMetaData LNodeMetaData}</em>}</li>
 * </ul>
 */
@AllArgsConstructor
public class LNodeMetaDataEmbedder {

    protected LNodeMetaData metaData ;

    /**
     * Default constructor
     */
    LNodeMetaDataEmbedder(){
        this.metaData = new LNodeMetaData();
    }

    /**
     * Sets Meta Data's information
     * @param metaData input
     */
    public void setMetaData(LNodeMetaData metaData){
        this.metaData = metaData;
    }

    /**
     * Gets IED name's vale
     * @return string IED name
     */
    public String getHolderIEDName(){
        return metaData.getIedName();
    }

    /**
     * Gets LDevice Inst's value
     * @return String LDevice Inst
     */
    public String getHolderLDInst(){
        return metaData.getLdInst();
    }

    /**
     * Gets LNode Class value
     * @return String LNode Class
     */
    public String getHolderLnClass(){
        return metaData.getLnClass();
    }

    /**
     * Gets LNode Inst's value
     * @return String LNode Inst's value
     */
    public String getHolderLnInst(){
        return metaData.getLnInst();
    }

    /**
     * Gets LNode Prefix's value
     * @return String LNode Prefix's value
     */
    public String getHolderLnPrefix(){
        return metaData.getLnPrefix();
    }

    /**
     * Sets IED name's value
     * @param iedName input
     */
    public void setHolderIEDName(String iedName){
        metaData.setIedName(iedName);
    }

    /**
     * Sets LDevice Inst's value
     * @param ldInst input
     */
    public void setHolderLDInst(String ldInst){
        metaData.setLdInst(ldInst);
    }

    /**
     * Sets LNode Class's value
     * @param lnClass input
     */
    public void setHolderLnClass(String lnClass){
        metaData.setLnClass(lnClass);
    }

    /**
     * Sets LNode Inst's value
     * @param lnInst input
     */
    public void setHolderLnInst(String lnInst){
        metaData.setLnInst(lnInst);
    }

    /**
     * Sets LNode Prefix's value
     * @param prefix input
     */
    public void setHolderLnPrefix(String prefix){
        metaData.setLnPrefix(prefix);
    }
}
