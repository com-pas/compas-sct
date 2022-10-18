// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;

import java.util.Objects;
/**
 * A representation of the model object <em><b>ExtRef</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ExtRefInfo#getHolderIEDName() <em>Ied Name</em>}</li>
 *   <li>{@link ExtRefInfo#getHolderLDInst() <em>Ld Inst</em>}</li>
 *   <li>{@link ExtRefInfo#getHolderLnClass() <em>Ln Class</em>}</li>
 *   <li>{@link ExtRefInfo#getHolderLnInst() <em>Ln Inst</em>}</li>
 *   <li>{@link ExtRefInfo#getHolderLnPrefix() <em>Prefix</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo <em>Refers To SignalInfo</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo <em>Refers To BindingInfo</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo <em>Refers To SourceInfo</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TExtRef
 */
@Getter
@Setter
@NoArgsConstructor
public class ExtRefInfo extends LNodeMetaDataEmbedder{

    private ExtRefSignalInfo signalInfo;
    private ExtRefBindingInfo bindingInfo;
    private ExtRefSourceInfo sourceInfo;

    /**
     * Constructor
     * @param tExtRef input
     */
    public ExtRefInfo(TExtRef tExtRef) {
        super();
        bindingInfo = new ExtRefBindingInfo(tExtRef);
        sourceInfo = new ExtRefSourceInfo(tExtRef);
        signalInfo = new ExtRefSignalInfo(tExtRef);
    }

    /**
     * Initializes ExtRefInfo
     * @param tExtRef input
     * @param iedName input
     * @param ldInst input
     * @param lnClass input
     * @param lnInst input
     * @param prefix input
     * @return ExtRefInfo object
     */
    public static ExtRefInfo from(TExtRef tExtRef, String iedName, String ldInst,
                                  String lnClass, String lnInst, String prefix){
        ExtRefInfo extRefInfo = new ExtRefInfo(tExtRef);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLnInst(lnInst);
        extRefInfo.setHolderLnClass(lnClass);
        extRefInfo.setHolderLnPrefix(prefix);

        return extRefInfo;
    }

    /**
     * Check match between FCDA and ExtRef information (for binding)
     * @param tfcda FCDA data object
     * @return match state
     */
    //TODO  this method should be checked, return if parameter tested are not present in FCDA even if two object are different
    public boolean matchFCDA(@NonNull TFCDA tfcda){
        boolean returnValue = true;
        if(AbstractLNAdapter.isFCDANull(tfcda)) {
            returnValue = false;
        }

        if(tfcda.getLdInst() != null &&
                (bindingInfo == null || !tfcda.getLdInst().equals(bindingInfo.getLdInst()))){
            returnValue = false;
        }
        if (!tfcda.getLnClass().isEmpty() &&
                ( bindingInfo == null || !tfcda.getLnClass().contains(bindingInfo.getLnClass())) ){
            returnValue = false;
        }

        boolean isLN0 = tfcda.getLnClass().contains(TLLN0Enum.LLN_0.value());
        if (!isLN0 && tfcda.getLnInst() != null &&
                (bindingInfo == null || !tfcda.getLnInst().equals(bindingInfo.getLnInst()))) {
            returnValue = false;
        }
        if (!isLN0 && !StringUtils.isBlank(tfcda.getPrefix()) &&
                (bindingInfo == null || !tfcda.getPrefix().equals(bindingInfo.getPrefix()))) {
            returnValue = false;
        }

        if(!StringUtils.isBlank(tfcda.getDoName()) &&
                (signalInfo == null || !Objects.equals(signalInfo.getPDO(),tfcda.getDoName())) ){
            returnValue = false;
        }

        if(!StringUtils.isBlank(tfcda.getDaName()) &&
                (signalInfo == null || !Objects.equals(signalInfo.getPDA(),tfcda.getDaName())) ){
            returnValue = false;
        }
        return returnValue;
    }
    /**
     * Check matching between FCDA and ExtRef information (for external binding)
     * Check is done for parameter lDInst(mandatory), lNClass(mandatory), lNInst, prefix doName as pDO(mandatory) and daName as pDA
     * present in ExtRef and FCDA
     * @param tfcda FCDA data to check compatibilities with ExtRef
     * @return true if ExtRef matches FCDA for parameters ahead false otherwise
     */
    public boolean checkMatchingFCDA(@NonNull TFCDA tfcda){
        if(bindingInfo == null  || signalInfo == null) return false;
        FCDAInfo fcdaInfo = new FCDAInfo(tfcda);
        FCDAInfo fcdaOfBinding = FCDAInfo.builder()
                .ldInst(bindingInfo.getLdInst())
                .lnClass(bindingInfo.getLnClass())
                .lnInst(bindingInfo.getLnInst())
                .prefix(bindingInfo.getPrefix())
                .doName(new DoTypeName(signalInfo.getPDO()))
                .daName(new DaTypeName(signalInfo.getPDA()))
                .build();
        return fcdaInfo.checkFCDACompatibilitiesForBinding(fcdaOfBinding);
    }

}
