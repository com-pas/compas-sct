// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;


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
 *   <li>{@link GooseControlBlock#getIedNames <em>Refers To Ied Names</em>}</li>
 *   <li>{@link GooseControlBlock#getSecurityEnable <em>Refers To security Enable</em>}</li>
 *   <li>{@link GooseControlBlock#isFixedOffs <em>Refers To fixedOffs</em>}</li>
 *   <li>{@link GooseControlBlock#getProtocol <em>Refers To Protocol</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TGSEControl
 */
@Getter
@Setter
@NoArgsConstructor
public class GooseControlBlock extends ControlBlock<GooseControlBlock> {
    private boolean fixedOffs = false;
    private TProtocol protocol;

    /**
     * Constructor
     * @param tgseControl input
     */
    public GooseControlBlock(TGSEControl tgseControl) {
        super();
        this.id = tgseControl.getAppID();
        this.name = tgseControl.getName();
        if(tgseControl.isSetConfRev()) {
            this.confRev = tgseControl.getConfRev();
        }
        this.desc = tgseControl.getDesc();
        this.dataSetRef = tgseControl.getDatSet();
        this.fixedOffs = tgseControl.isFixedOffs();
        this.iedNames = tgseControl.getIEDName();
        this.protocol = tgseControl.getProtocol();
    }

    /**
     * Gets classe type
     * @return classe type
     */
    @Override
    protected Class<GooseControlBlock> getClassType() {
        return GooseControlBlock.class;
    }

    /**
     * Gets ServiceType value
     * @return ServiceType enumeration value
     */
    @Override
    public TServiceType getServiceType() {
        return TServiceType.GOOSE;
    }

    /**
     * Checks Goose Control block security enable state from Service
     * @param tServices Service object
     * @throws ScdException
     */
    @Override
    protected void validateSecurityEnabledValue(TServices tServices) throws ScdException {
        if(tServices == null ||
            tServices.getGSESettings() == null ||
            tServices.getGSESettings().getMcSecurity() == null){
            throw new ScdException("");
        }

        TMcSecurity mcSecurity = tServices.getGSESettings().getMcSecurity();
        if (securityEnable != TPredefinedTypeOfSecurityEnum.NONE && (
                (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE && !mcSecurity.isSignature()) ||
                (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION &&
                        (!mcSecurity.isSignature() || !mcSecurity.isEncryption())) ) ){
            throw new ScdException("");
        }
    }

    /**
     * Creates ControlBlock
     * @return Goose Control Block object (GSEControl) value
     */
    @Override
    public TGSEControl createControlBlock() {
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setAppID(id);
        tgseControl.setDatSet(dataSetRef);
        tgseControl.setConfRev(getConfRev());
        tgseControl.setName(name);
        tgseControl.getIEDName().addAll(iedNames);
        tgseControl.setFixedOffs(fixedOffs);
        if(protocol != null) {
            tgseControl.setProtocol(protocol);
        }
        tgseControl.setSecurityEnable(securityEnable);
        tgseControl.setDesc(desc);
        return tgseControl;
    }
}
