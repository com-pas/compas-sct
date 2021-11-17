// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TMcSecurity;
import org.lfenergy.compas.scl2007b4.model.TPredefinedTypeOfSecurityEnum;
import org.lfenergy.compas.scl2007b4.model.TProtocol;
import org.lfenergy.compas.scl2007b4.model.TSampledValueControl;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.scl2007b4.model.TServices;
import org.lfenergy.compas.scl2007b4.model.TSmpMod;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.Collections;


@Getter
@Setter
@NoArgsConstructor
public class SMVControlBlock extends ControlBlock<SMVControlBlock>{

    private TSampledValueControl.SmvOpts smvOpts;
    private TProtocol protocol;
    private boolean multicast = true;
    private long smpRate;
    private long nofASDU;
    private TSmpMod smpMod = TSmpMod.SMP_PER_PERIOD;


    public SMVControlBlock(TSampledValueControl tSampledValueControl) {
        super();
        this.id = tSampledValueControl.getSmvID();
        this.name = tSampledValueControl.getName();
        if(tSampledValueControl.getConfRev() != null) {
            this.confRev = tSampledValueControl.getConfRev();
        }
        this.desc = tSampledValueControl.getDesc();
        this.dataSetRef = tSampledValueControl.getDatSet();
        Collections.copy(iedNames, tSampledValueControl.getIEDName());
        smvOpts = tSampledValueControl.getSmvOpts();
        protocol = tSampledValueControl.getProtocol();
        multicast = tSampledValueControl.isMulticast();
        smpRate = tSampledValueControl.getSmpRate();
        nofASDU = tSampledValueControl.getNofASDU();
        smpMod = tSampledValueControl.getSmpMod();
        securityEnable = tSampledValueControl.getSecurityEnable();
    }

    @Override
    protected Class<SMVControlBlock> getClassType() {
        return SMVControlBlock.class;
    }

    @Override
    public TServiceType getServiceType() {
        return TServiceType.SMV;
    }

    @Override
    protected void validateSecurityEnabledValue(TServices tServices) throws ScdException {
        if(tServices == null ||
                tServices.getSMVSettings() == null ||
                tServices.getSMVSettings().getMcSecurity() == null){
            throw new ScdException("");
        }

        TMcSecurity mcSecurity = tServices.getSMVSettings().getMcSecurity();
        if (securityEnable != TPredefinedTypeOfSecurityEnum.NONE && (
                (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE && !mcSecurity.isSignature()) ||
                        (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION &&
                                (!mcSecurity.isSignature() || !mcSecurity.isEncryption())) ) ){
            throw new ScdException("");
        }
    }

    @Override
    public TSampledValueControl createControlBlock() {

        TSampledValueControl sampledValueControl = new TSampledValueControl();
        sampledValueControl.setSmvID(id);
        sampledValueControl.setDatSet(dataSetRef);
        sampledValueControl.setConfRev(getConfRev());
        sampledValueControl.setName(name);
        sampledValueControl.getIEDName().addAll(iedNames);

        if (smvOpts != null){
            sampledValueControl.setSmvOpts(smvOpts);
        }

        if(smpMod != null) {
            sampledValueControl.setSmpMod(smpMod);
        }
        sampledValueControl.setSmpRate(smpRate);
        sampledValueControl.setNofASDU(nofASDU);
        if(protocol != null) {
            sampledValueControl.setProtocol(protocol);
        }
        sampledValueControl.setMulticast(multicast);
        sampledValueControl.setSecurityEnable(securityEnable);
        sampledValueControl.setDesc(desc);

        return sampledValueControl;
    }
}
