// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

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
 *   <li>{@link ReportControlBlock#getIedNames <em>Refers To IedNames</em>}</li>
 *   <li>{@link ReportControlBlock#getSecurityEnable <em>Refers To securityEnable</em>}</li>
 *   <li>{@link ReportControlBlock#getTrgOps <em>Refers To trgOps</em>}</li>
 *   <li>{@link ReportControlBlock#getIntgPd <em>Refers To intgPd</em>}</li>
 *   <li>{@link ReportControlBlock#getRptEnabled <em>Refers To rptEnabled</em>}</li>
 *   <li>{@link ReportControlBlock#isBuffered <em>Refers To buffered</em>}</li>
 *   <li>{@link ReportControlBlock#getBufTime <em>Refers To bufTime</em>}</li>
 *   <li>{@link ReportControlBlock#isIndexed <em>Refers To indexed</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TReportControl
 */
@Getter
@Setter
@NoArgsConstructor
public class ReportControlBlock extends ControlBlock<ReportControlBlock> {

    private TReportControl.OptFields optFields;
    protected TTrgOps trgOps;
    protected long intgPd = 0;
    private TRptEnabled rptEnabled;
    private boolean buffered = false;
    private long bufTime = 0;
    private boolean indexed = true;


    /**
     * Constructor
     * @param reportControl input
     */
    public ReportControlBlock(TReportControl reportControl) {
        super();
        this.id = reportControl.getRptID();
        this.desc = reportControl.getDesc();
        this.name = reportControl.getName();
        this.confRev = reportControl.getConfRev();
        this.dataSetRef = reportControl.getDatSet();
        this.rptEnabled = reportControl.getRptEnabled();
        this.optFields = reportControl.getOptFields();
        this.trgOps = reportControl.getTrgOps();
        this.intgPd = reportControl.getIntgPd();
        this.buffered = reportControl.isBuffered();

        if(rptEnabled != null) {
            iedNames = rptEnabled.getClientLN()
                    .stream()
                    .map(ControlBlock::toIEDName)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Gets Class type value for ReportControlBlock
     * @return ReportControlBlock.class
     */
    @Override
    protected Class<ReportControlBlock> getClassType() {
        return ReportControlBlock.class;
    }

    /**
     * Get Service Type value for ReportControlBlock
     * @return Report
     */
    @Override
    public TServiceType getServiceType() {
        return TServiceType.REPORT;
    }

    /**
     * Gets ConfRev value for ReportControlBlock
     * @return 1L
     */
    @Override
    protected Long getConfRev() {
        return 1L;
    }

    /**
     * Validates Security Enabled parameter value (do nothing)
     * @param tServices Service object
     * @throws ScdException
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
    public TReportControl createControlBlock() {
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
        reportControl.setOptFields(optFields);
        reportControl.setTrgOps(trgOps);
        reportControl.setRptEnabled(rptEnabled);

        return reportControl;
    }

    /**
     * Validates Report Control Block
     * @throws ScdException
     */
    @Override
    public void validateCB() throws ScdException {
        super.validateCB();

        if(dataSetRef != null && dataSetRef.isBlank()){
            throw new ScdException("A required field is missing: datSet");
        }
    }
}
