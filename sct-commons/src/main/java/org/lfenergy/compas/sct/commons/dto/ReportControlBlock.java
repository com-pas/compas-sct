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
 * A representation of the model object <em><b>ReportControlBlock</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ReportControlBlock#getId <em>rptID</em>}</li>
 *   <li>{@link ReportControlBlock#getName <em>Name</em>}</li>
 *   <li>{@link ReportControlBlock#getDataSetRef <em>dataSetRef</em>}</li>
 *   <li>{@link ReportControlBlock#getDesc <em>Desc</em>}</li>
 *   <li>{@link ReportControlBlock#getConfRev <em>Refers To confRev</em>}</li>
 *   <li>{@link ReportControlBlock#getTargets <em>Refers To IedNames</em>}</li>
 *   <li>{@link ReportControlBlock#getTrgOps <em>Refers To trgOps</em>}</li>
 *   <li>{@link ReportControlBlock#getIntgPd <em>Refers To intgPd</em>}</li>
 *   <li>{@link ReportControlBlock#getRptEnabledMax <em>Refers To rptEnabled.max</em>}</li>
 *   <li>{@link ReportControlBlock#getRptEnabledDesc <em>Refers To rptEnabled.desc</em>}</li>
 *   <li>{@link ReportControlBlock#isBuffered <em>Refers To buffered</em>}</li>
 *   <li>{@link ReportControlBlock#getBufTime <em>Refers To bufTime</em>}</li>
 *   <li>{@link ReportControlBlock#isIndexed <em>Refers To indexed</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TReportControl
 */
@Getter
@Setter
public class ReportControlBlock extends ControlBlock {

    private TReportControl.OptFields optFields = newDefaultOptFields();
    protected TTrgOps trgOps = newDefaultTrgOps();
    protected long intgPd = 60000L;
    private Long rptEnabledMax = 1L;
    private String rptEnabledDesc;
    private boolean buffered = true;
    private long bufTime = 0;
    private boolean indexed = true;

    /**
     * Create ReportControlBlock with default values
     *
     * @param name       name of the ReportControlBlock
     * @param id         rptId of the ReportControlBlock
     * @param dataSetRef datSet of the ReportControlBlock
     */
    public ReportControlBlock(String name, String id, String dataSetRef) {
        confRev = 1L;
        this.name = name;
        this.dataSetRef = dataSetRef;
        this.id = id;
    }

    /**
     * Create a new TTrgOps with default values
     * @return new instance of TTrgOps
     */
    private static TTrgOps newDefaultTrgOps() {
        TTrgOps newDefaultTrgOps = new TTrgOps();
        newDefaultTrgOps.setDchg(true);
        newDefaultTrgOps.setQchg(true);
        newDefaultTrgOps.setDupd(false);
        newDefaultTrgOps.setPeriod(true);
        newDefaultTrgOps.setGi(true);
        return newDefaultTrgOps;
    }

    /**
     * Create a new TReportControl.OptFields with default values
     * @return new instance of TReportControl.OptFields
     */
    private static TReportControl.OptFields newDefaultOptFields() {
        TReportControl.OptFields newDefaultOptFields = new TReportControl.OptFields();
        newDefaultOptFields.setSeqNum(false);
        newDefaultOptFields.setTimeStamp(false);
        newDefaultOptFields.setDataSet(false);
        newDefaultOptFields.setReasonCode(false);
        newDefaultOptFields.setDataRef(false);
        newDefaultOptFields.setEntryID(false);
        newDefaultOptFields.setConfigRef(false);
        newDefaultOptFields.setBufOvfl(true);
        return newDefaultOptFields;
    }

    /**
     * Constructor
     * @param reportControl input
     */
    public ReportControlBlock(TReportControl reportControl) {
        super();
        id = reportControl.getRptID();
        desc = reportControl.getDesc();
        name = reportControl.getName();
        if(reportControl.isSetConfRev()) {
            confRev = reportControl.getConfRev();
        }
        dataSetRef = reportControl.getDatSet();
        indexed = reportControl.isIndexed();
        intgPd = reportControl.getIntgPd();
        buffered = reportControl.isBuffered();
        bufTime = reportControl.getBufTime();
        optFields = copyOptFields(reportControl.getOptFields());
        trgOps = copyTTrgOps(reportControl.getTrgOps());
        if (reportControl.isSetRptEnabled()){
            rptEnabledMax = reportControl.getRptEnabled().getMax();
            rptEnabledDesc = reportControl.getRptEnabled().getDesc();
            targets = reportControl.getRptEnabled().getClientLN().stream().map(ControlBlockTarget::from).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    @Override
    public ControlBlockEnum getControlBlockEnum() {
        return ControlBlockEnum.REPORT;
    }

    /**
     * Implementation is required by superclass but ReportControl blocks has no SecurityEnabled attributes.
     * This implementation does nothing.
     * @param tServices Service object
     * @throws ScdException never (this is just to comply with superclass)
     */
    @Override
    protected void validateSecurityEnabledValue(TServices tServices) throws ScdException {
        //doNothing
    }

    /**
     * Creates Report Control Block
     * @return TReportControl object
     */
    @Override
    public TReportControl toTControl() {
        TReportControl reportControl = new TReportControl();
        reportControl.setRptID(id);
        reportControl.setName(name);
        reportControl.setDesc(desc);
        reportControl.setConfRev(getConfRev());
        reportControl.setBuffered(buffered);
        reportControl.setBufTime(bufTime);
        reportControl.setIndexed(indexed);
        reportControl.setDatSet(dataSetRef);
        reportControl.setDesc(desc);
        reportControl.setIntgPd(intgPd);
        reportControl.setOptFields(copyOptFields(optFields));
        reportControl.setTrgOps(copyTTrgOps(trgOps));
        if (rptEnabledMax != null || rptEnabledDesc != null || !targets.isEmpty()) {
            TRptEnabled tRptEnabled = new TRptEnabled();
            tRptEnabled.setMax(rptEnabledMax);
            tRptEnabled.setDesc(rptEnabledDesc);
            tRptEnabled.getClientLN().addAll(targets.stream().map(ControlBlockTarget::toTClientLn).toList());
            reportControl.setRptEnabled(tRptEnabled);
        }
        return reportControl;
    }

    /**
     * Validates Report Control Block
     * @throws ScdException when required fields are missing
     */
    @Override
    public void validateCB() throws ScdException {
        super.validateCB();

        if(dataSetRef != null && dataSetRef.isBlank()){
            throw new ScdException("A required field is missing: datSet");
        }
    }

    @Override
    public TReportControl addToLN(TAnyLN tAnyLN) {
        TReportControl tReportControl = toTControl();
        tAnyLN.getReportControl().add(tReportControl);
        return tReportControl;
    }

    private static TReportControl.OptFields copyOptFields(TReportControl.OptFields optFields) {
        if (optFields == null){
            return null;
        }
        TReportControl.OptFields newOptFields = new TReportControl.OptFields();
        newOptFields.setSeqNum(optFields.isSeqNum());
        newOptFields.setTimeStamp(optFields.isTimeStamp());
        newOptFields.setDataSet(optFields.isDataSet());
        newOptFields.setReasonCode(optFields.isReasonCode());
        newOptFields.setDataRef(optFields.isDataRef());
        newOptFields.setEntryID(optFields.isEntryID());
        newOptFields.setConfigRef(optFields.isConfigRef());
        newOptFields.setBufOvfl(optFields.isBufOvfl());
        return newOptFields;
    }

    private static TTrgOps copyTTrgOps(TTrgOps tTrgOps) {
        if (tTrgOps == null){
            return null;
        }
        TTrgOps newTTrgOps = new TTrgOps();
        newTTrgOps.setDchg(tTrgOps.isDchg());
        newTTrgOps.setQchg(tTrgOps.isQchg());
        newTTrgOps.setDupd(tTrgOps.isDupd());
        newTTrgOps.setPeriod(tTrgOps.isPeriod());
        newTTrgOps.setGi(tTrgOps.isGi());
        return newTTrgOps;
    }

}
