// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.*;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
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
@AllArgsConstructor
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
