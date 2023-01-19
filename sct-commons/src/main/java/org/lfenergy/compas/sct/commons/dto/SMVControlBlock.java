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
 *   <li>{@link SMVControlBlock#getTargets <em>Refers To IedNames</em>}</li>
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
public class SMVControlBlock extends ControlBlock{

    private TSampledValueControl.SmvOpts smvOpts = newDefaultSmvOpts();
    private TProtocol protocol;
    private boolean multicast = true;
    private Long smpRate = 4800L;
    private Long nofASDU = 2L;
    private TSmpMod smpMod = TSmpMod.SMP_PER_SEC;
    protected TPredefinedTypeOfSecurityEnum securityEnable = TPredefinedTypeOfSecurityEnum.NONE;

    /**
     * Create SMVControlBlock with default values
     *
     * @param name       name of the SMVControlBlock
     * @param id         smvId of the SMVControlBlock
     * @param dataSetRef datSet of the SMVControlBlock
     */
    public SMVControlBlock(String name, String id, String dataSetRef) {
        this.name = name;
        this.id = id;
        this.dataSetRef = dataSetRef;
    }

    /**
     * Create a new TSampledValueControl.SmvOpts with default values
     * @return new instance of TSampledValueControl.SmvOpts
     */
    private static TSampledValueControl.SmvOpts newDefaultSmvOpts() {
        TSampledValueControl.SmvOpts defaultSmvOpts = new TSampledValueControl.SmvOpts();
        defaultSmvOpts.setRefreshTime(false);
        defaultSmvOpts.setSampleSynchronized(true);
        defaultSmvOpts.setSampleRate(true);
        defaultSmvOpts.setDataSet(false);
        defaultSmvOpts.setSecurity(false);
        defaultSmvOpts.setTimestamp(false);
        defaultSmvOpts.setSynchSourceId(false);
        return defaultSmvOpts;
    }

    /**
     * Constructor
     * @param tSampledValueControl input
     */
    public SMVControlBlock(TSampledValueControl tSampledValueControl) {
        super();
        id = tSampledValueControl.getSmvID();
        name = tSampledValueControl.getName();
        if (tSampledValueControl.isSetConfRev()) {
            confRev = tSampledValueControl.getConfRev();
        }
        desc = tSampledValueControl.getDesc();
        dataSetRef = tSampledValueControl.getDatSet();
        multicast = tSampledValueControl.isMulticast();
        smpRate = tSampledValueControl.getSmpRate();
        nofASDU = tSampledValueControl.getNofASDU();
        smpMod = tSampledValueControl.getSmpMod();
        securityEnable = tSampledValueControl.getSecurityEnable();
        smvOpts = copySmvOpts(tSampledValueControl.getSmvOpts());
        protocol = copyProtocol(tSampledValueControl.getProtocol());
        targets = tSampledValueControl.getIEDName().stream().map(ControlBlockTarget::from).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ControlBlockEnum getControlBlockEnum() {
        return ControlBlockEnum.SAMPLED_VALUE;
    }

    @Override
    protected void validateSecurityEnabledValue(TServices tServices) throws ScdException {
        if (securityEnable == TPredefinedTypeOfSecurityEnum.NONE){
            return;
        }
        if(tServices == null ||
            tServices.getSMVSettings() == null ||
            tServices.getSMVSettings().getMcSecurity() == null){
            throw new ScdException("securityEnable is %s but Services/SMVSettings/McSecurity is not defined".formatted(securityEnable));
        }

        TMcSecurity mcSecurity = tServices.getSMVSettings().getMcSecurity();
        if (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE && !mcSecurity.isSignature()){
            throw new ScdException("securityEnable is %s but Services/SMVSettings/McSecurity@signature is false".formatted(securityEnable));
        }
        if (securityEnable == TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION &&  (!mcSecurity.isSignature() || !mcSecurity.isEncryption())){
            throw new ScdException("securityEnable is %s but Services/SMVSettings/McSecurity@signature or @encryption is false".formatted(securityEnable));
        }
    }

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
    public TSampledValueControl toTControl() {
        TSampledValueControl sampledValueControl = new TSampledValueControl();
        sampledValueControl.setSmvID(id);
        sampledValueControl.setDatSet(dataSetRef);
        sampledValueControl.setConfRev(getConfRev());
        sampledValueControl.setName(name);
        sampledValueControl.setSmpMod(smpMod);
        sampledValueControl.setSmpRate(smpRate);
        sampledValueControl.setNofASDU(nofASDU);
        sampledValueControl.setMulticast(multicast);
        sampledValueControl.setSecurityEnable(securityEnable);
        sampledValueControl.setDesc(desc);
        sampledValueControl.getIEDName().addAll(targets.stream().map(ControlBlockTarget::toIedName).toList());
        sampledValueControl.setSmvOpts(copySmvOpts(smvOpts));
        sampledValueControl.setProtocol(copyProtocol(protocol));
        return sampledValueControl;
    }

    @Override
    public TSampledValueControl addToLN(TAnyLN tAnyLN) {
        if (tAnyLN instanceof TLN0 tln0){
            TSampledValueControl tSampledValueControl = toTControl();
            tln0.getSampledValueControl().add(tSampledValueControl);
            return tSampledValueControl;
        }
        throw new IllegalArgumentException("SampledValueControlBlocks can only be added on LN0 element");
    }

    private static TSampledValueControl.SmvOpts copySmvOpts(TSampledValueControl.SmvOpts smvOpts) {
        if (smvOpts == null){
            return null;
        }
        TSampledValueControl.SmvOpts newSmvOpts = new TSampledValueControl.SmvOpts();
        newSmvOpts.setRefreshTime(smvOpts.isRefreshTime());
        newSmvOpts.setSampleSynchronized(smvOpts.isSampleSynchronized());
        newSmvOpts.setSampleRate(smvOpts.isSampleRate());
        newSmvOpts.setDataSet(smvOpts.isDataSet());
        newSmvOpts.setSecurity(smvOpts.isSecurity());
        newSmvOpts.setTimestamp(smvOpts.isTimestamp());
        newSmvOpts.setSynchSourceId(smvOpts.isSynchSourceId());
        return newSmvOpts;
    }
}
