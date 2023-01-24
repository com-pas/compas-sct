// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * A representation of the model object <em><b>GooseControlBlock</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link GooseControlBlock#getId <em>appID</em>}</li>
 *   <li>{@link GooseControlBlock#getName <em>Name</em>}</li>
 *   <li>{@link GooseControlBlock#getDataSetRef <em>dataSetRef</em>}</li>
 *   <li>{@link GooseControlBlock#getDesc <em>Desc</em>}</li>
 *   <li>{@link GooseControlBlock#getConfRev <em>Refers To confRev</em>}</li>
 *   <li>{@link GooseControlBlock#getTargets <em>Refers To list of IEDName ot TClientLN</em>}</li>
 *   <li>{@link GooseControlBlock#getSecurityEnable <em>Refers To security Enable</em>}</li>
 *   <li>{@link GooseControlBlock#isFixedOffs <em>Refers To fixedOffs</em>}</li>
 *   <li>{@link GooseControlBlock#getProtocol <em>Refers To Protocol</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TGSEControl
 */
@Getter
@Setter
public class GooseControlBlock extends ControlBlock {
    private boolean fixedOffs = false;
    private TProtocol protocol;
    private TGSEControlTypeEnum type = TGSEControlTypeEnum.GOOSE;
    private TPredefinedTypeOfSecurityEnum securityEnable = TPredefinedTypeOfSecurityEnum.NONE;

    /**
     * Constructor
     * @param tgseControl input
     */
    public GooseControlBlock(TGSEControl tgseControl) {
        id = tgseControl.getAppID();
        name = tgseControl.getName();
        if(tgseControl.isSetConfRev()) {
            confRev = tgseControl.getConfRev();
        }
        desc = tgseControl.getDesc();
        dataSetRef = tgseControl.getDatSet();
        fixedOffs = tgseControl.isFixedOffs();
        targets = tgseControl.getIEDName().stream().map(ControlBlockTarget::from).collect(Collectors.toCollection(ArrayList::new));
        protocol = copyProtocol(tgseControl.getProtocol());
    }

    /**
     * Create GooseControlBlock with default values
     *
     * @param name       name of the GooseControlBlock
     * @param id         appId of the GooseControlBlock
     * @param dataSetRef datSet of the GooseControlBlock
     */
    public GooseControlBlock(String name, String id, String dataSetRef) {
        this.name = name;
        this.id = id;
        this.dataSetRef = dataSetRef;
    }

    @Override
    public ControlBlockEnum getControlBlockEnum() {
        return ControlBlockEnum.GSE;
    }

    @Override
    protected void validateSecurityEnabledValue(TServices tServices) throws ScdException {
        if (securityEnable == TPredefinedTypeOfSecurityEnum.NONE){
            return;
        }
        if(tServices == null ||
            tServices.getGSESettings() == null ||
            tServices.getGSESettings().getMcSecurity() == null){
            throw new ScdException("securityEnable is %s but Services/GSESettings/McSecurity is not defined".formatted(securityEnable));
        }

        TMcSecurity mcSecurity = tServices.getGSESettings().getMcSecurity();
        if (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE && !mcSecurity.isSignature()){
            throw new ScdException("securityEnable is %s but Services/GSESettings/McSecurity@signature is false".formatted(securityEnable));
        }
        if (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION &&  (!mcSecurity.isSignature() || !mcSecurity.isEncryption())){
            throw new ScdException("securityEnable is %s but Services/GSESettings/McSecurity@signature or @encryption is false".formatted(securityEnable));
        }
    }

    /**
     * Map the object to TGSEControl
     * @return new instance of TGSEControl
     */
    @Override
    public TGSEControl toTControl() {
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setAppID(id);
        tgseControl.setDatSet(dataSetRef);
        tgseControl.setConfRev(getConfRev());
        tgseControl.setName(name);
        tgseControl.setFixedOffs(fixedOffs);
        tgseControl.setSecurityEnable(securityEnable);
        tgseControl.setDesc(desc);
        tgseControl.setType(type);
        tgseControl.getIEDName().addAll(targets.stream().map(ControlBlockTarget::toIedName).toList());
        tgseControl.setProtocol(copyProtocol(protocol));
        return tgseControl;
    }

    /**
     * Convert instance to TGSEControl and add it to LN0.GSEControl.
     * GSEControl is only available on LN0 (not LN)
     * @param tAnyLN TLN0 element
     * @return the added TGSEControl
     * @throws IllegalArgumentException when tAnyLN is not of class TLNO
     */
    @Override
    public TGSEControl addToLN(TAnyLN tAnyLN) {
        if (tAnyLN instanceof TLN0 tln0){
            TGSEControl tgseControl = toTControl();
            tln0.getGSEControl().add(tgseControl);
            return tgseControl;
        }
        throw new IllegalArgumentException("GSEControlBlocks can only be added on LN0 element");
    }
}
