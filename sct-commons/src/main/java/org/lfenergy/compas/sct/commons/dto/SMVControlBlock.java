// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.Collections;


/**
 * A representation of the model object <em><b>SMVControlBlock</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link SMVControlBlock#getId <em>smvID</em>}</li>
 *   <li>{@link SMVControlBlock#getName <em>Name</em>}</li>
 *   <li>{@link SMVControlBlock#getDataSetRef <em>dataSetRef</em>}</li>
 *   <li>{@link SMVControlBlock#getDesc <em>Desc</em>}</li>
 *   <li>{@link SMVControlBlock#getConfRev <em>Refers To confRev</em>}</li>
 *   <li>{@link SMVControlBlock#getIedNames <em>Refers To IedNames</em>}</li>
 *   <li>{@link SMVControlBlock#getSecurityEnable <em>Refers To securityEnable</em>}</li>
 *   <li>{@link SMVControlBlock#getSmvOpts <em>Refers To smvOpts</em>}</li>
 *   <li>{@link SMVControlBlock#getProtocol <em>Refers To protocol</em>}</li>
 *   <li>{@link SMVControlBlock#isMulticast <em>Refers To multicast</em>}</li>
 *   <li>{@link SMVControlBlock#getSmpRate <em>Refers To smpRate</em>}</li>
 *   <li>{@link SMVControlBlock#getNofASDU <em>Refers To nofASDU</em>}</li>
 *   <li>{@link SMVControlBlock#getSmpMod <em>Refers To smpMod</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TSampledValueControl
 */
@Getter
@Setter
@NoArgsConstructor
public class SMVControlBlock extends ControlBlock<SMVControlBlock>{

    private TSampledValueControl.SmvOpts smvOpts;
    private TProtocol protocol;
    private boolean multicast = true;
    private Long smpRate;
    private Long nofASDU;
    private TSmpMod smpMod = TSmpMod.SMP_PER_PERIOD;

    /**
     * Constructor
     * @param tSampledValueControl input
     */
    public SMVControlBlock(TSampledValueControl tSampledValueControl) {
        super();
        this.id = tSampledValueControl.getSmvID();
        this.name = tSampledValueControl.getName();
        this.confRev = tSampledValueControl.isSetConfRev() ? tSampledValueControl.getConfRev() : null;
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

    /**
     * Gets SMV Control Block Class Type value
     * @return SMVControlBlock.class
     */
    @Override
    protected Class<SMVControlBlock> getClassType() {
        return SMVControlBlock.class;
    }

    /**
     * Gets SMV Control Block Service Type value
     * @return SMV
     */
    @Override
    public TServiceType getServiceType() {
        return TServiceType.SMV;
    }

    /**
     * Validates Security Enabled parameter value
     * @param tServices Service object
     * @throws ScdException
     */
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

    /**
     * Validates SMV Control Block
     * @throws ScdException
     */
    @Override
    public void validateCB() throws ScdException {
        super.validateCB();

        if(dataSetRef != null && dataSetRef.isBlank()){
            throw new ScdException("A required field is missing: datSet");
        }
        if(smpRate == null){
            throw new ScdException("A required field is missing: smpRate");
        }

        if(nofASDU == null){
            throw new ScdException("A required field is missing: nofASDU ");
        }
    }

    /**
     * Creates SMV Control Block
     * @return TSampledValueControl object
     */
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
        if(smpRate != null) {
            sampledValueControl.setSmpRate(smpRate);
        }

        if(nofASDU != null) {
            sampledValueControl.setNofASDU(nofASDU);
        }
        if(protocol != null) {
            sampledValueControl.setProtocol(protocol);
        }
        sampledValueControl.setMulticast(multicast);
        sampledValueControl.setSecurityEnable(securityEnable);
        sampledValueControl.setDesc(desc);

        return sampledValueControl;
    }
}
