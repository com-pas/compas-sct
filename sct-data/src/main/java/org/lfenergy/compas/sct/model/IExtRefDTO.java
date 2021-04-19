// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model;


import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TServiceType;

public interface IExtRefDTO {

    String getDesc();
    String getPDO();
    String getPLN();
    String getPDA();
    String getIntAddr();
    String getLdInst();
    String getIedName();
    String getPrefix();
    String getLnClass();
    String getLnInst();
    String getDoName();
    String getDaName();
    TServiceType getServiceType();
    String getSrcLDInst();
    String getSrcPrefix();
    String getSrcLNClass();
    String getSrcLNInst();
    String getSrcCBName();
    TServiceType getPServT();

    void setDesc(String desc);
    void setPDO(String pdo);
    void setPLN(String pln);
    void setPDA(String pda);
    void setIntAddr(String intAddr);
    void setLdInst(String ldInst);
    void setIedName(String iedName);
    void setPrefix(String prefix);
    void setLnClass(String lnClass);
    void setLnInst(String lnInst);
    void setDoName(String doName);
    void setDaName(String daName);
    void setServiceType(TServiceType serviceType);
    void setSrcLDInst(String srcLDInst);
    void setSrcPrefix(String srcPrefix);
    void setSrcLNClass( String srcLNClass);
    void setSrcLNInst(String srcLNCInst);
    void setSrcCBName(String srcCBName);
    void setPServT(TServiceType pServT);

    default boolean isIdentical(IExtRefDTO other){

        if(this.getDesc() != null && !getDesc().equals(other.getDesc())) return false;
        if(this.getPDO() != null && !getPDO().equals(other.getPDO())) return false;
        if(this.getPDA() != null && !getPDA().equals(other.getPDA())) return false;
        if(this.getPLN() != null && !getPLN().equals(other.getPLN())) return false;
        if(this.getIedName() != null && !getIedName().equals(other.getIedName())) return false;
        if(this.getIntAddr() != null && !getIntAddr().equals(other.getIntAddr())) return false;
        if(this.getLdInst() != null && !getLdInst().equals(other.getLdInst())) return false;
        if(this.getPrefix() != null && !getPrefix().equals(other.getPrefix())) return false;
        if(this.getLnClass() != null && !getLnClass().equals(other.getLnClass())) return false;
        if(this.getLnInst() != null && !getLnInst().equals(other.getLnInst())) return false;
        if(this.getDoName() != null && !getDoName().equals(other.getDoName())) return false;
        if(this.getDaName() != null && !getDaName().equals(other.getDaName())) return false;
        if(this.getSrcLNClass() != null && !getSrcLNClass().equals(other.getSrcLNClass())) return false;
        if(this.getSrcLNInst() != null && !getSrcLNInst().equals(other.getSrcLNInst())) return false;
        if(this.getSrcCBName() != null && !getSrcCBName().equals(other.getSrcCBName())) return false;
        if(this.getSrcLDInst() != null && !getSrcLDInst().equals(other.getSrcLDInst())) return false;
        if(this.getPServT() != null && !getPServT().equals(other.getPServT())) return false;
        if(this.getServiceType() != null && !getServiceType().equals(other.getServiceType())) return false;

        return true;
    }

    default boolean isIdentical(TExtRef other){

        if(this.getDesc() != null && !getDesc().equals(other.getDesc())) return false;
        if(this.getPDO() != null && !getPDO().equals(other.getPDO())) return false;
        if(this.getPDA() != null && !getPDA().equals(other.getPDA())) return false;
        if(this.getPLN() != null && !other.getPLN().contains(getPLN())) return false;
        if(this.getIedName() != null && !getIedName().equals(other.getIedName())) return false;
        if(this.getIntAddr() != null && !getIntAddr().equals(other.getIntAddr())) return false;
        if(this.getLdInst() != null && !getLdInst().equals(other.getLdInst())) return false;
        if(this.getPrefix() != null && !getPrefix().equals(other.getPrefix())) return false;
        if(this.getLnClass() != null && !other.getLnClass().contains(getLnClass())) return false;
        if(this.getLnInst() != null && !getLnInst().equals(other.getLnInst())) return false;
        if(this.getDoName() != null && !getDoName().equals(other.getDoName())) return false;
        if(this.getDaName() != null && !getDaName().equals(other.getDaName())) return false;
        if(this.getSrcLNClass() != null && !other.getSrcLNClass().contains(getSrcLNClass())) return false;
        if(this.getSrcLNInst() != null && !getSrcLNInst().equals(other.getSrcLNInst())) return false;
        if(this.getSrcCBName() != null && !getSrcCBName().equals(other.getSrcCBName())) return false;
        if(this.getSrcLDInst() != null && !getSrcLDInst().equals(other.getSrcLDInst())) return false;
        if(this.getPServT() != null && !getPServT().equals(other.getPServT())) return false;
        if(this.getServiceType() != null && !getServiceType().equals(other.getServiceType())) return false;

        return true;
    }
}