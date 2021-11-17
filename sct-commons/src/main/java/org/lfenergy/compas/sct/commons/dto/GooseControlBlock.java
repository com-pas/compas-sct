// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TGSEControl;
import org.lfenergy.compas.scl2007b4.model.TMcSecurity;
import org.lfenergy.compas.scl2007b4.model.TPredefinedTypeOfSecurityEnum;
import org.lfenergy.compas.scl2007b4.model.TProtocol;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.scl2007b4.model.TServices;
import org.lfenergy.compas.sct.commons.exception.ScdException;


@Getter
@Setter
@NoArgsConstructor
public class GooseControlBlock extends ControlBlock<GooseControlBlock> {
    private boolean fixedOffs = false;
    private TProtocol protocol;

    public GooseControlBlock(TGSEControl tgseControl) {
        super();
        this.id = tgseControl.getAppID();
        this.name = tgseControl.getName();
        if(tgseControl.getConfRev() != null) {
            this.confRev = tgseControl.getConfRev();
        }
        this.desc = tgseControl.getDesc();
        this.dataSetRef = tgseControl.getDatSet();
        this.fixedOffs = tgseControl.isFixedOffs();
        this.iedNames = tgseControl.getIEDName();
        this.protocol = tgseControl.getProtocol();
    }

    @Override
    protected Class<GooseControlBlock> getClassType() {
        return GooseControlBlock.class;
    }

    @Override
    public TServiceType getServiceType() {
        return TServiceType.GOOSE;
    }

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
