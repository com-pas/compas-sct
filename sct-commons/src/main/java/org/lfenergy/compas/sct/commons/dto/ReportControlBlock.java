// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TReportControl;
import org.lfenergy.compas.scl2007b4.model.TRptEnabled;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.scl2007b4.model.TServices;
import org.lfenergy.compas.scl2007b4.model.TTrgOps;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.stream.Collectors;


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
                    .map(clientLN -> toIEDName(clientLN))
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected Class<ReportControlBlock> getClassType() {
        return ReportControlBlock.class;
    }

    @Override
    public TServiceType getServiceType() {
        return TServiceType.REPORT;
    }

    @Override
    protected Long getConfRev() {
        return 1L;
    }

    @Override
    protected void validateSecurityEnabledValue(TServices tServices) throws ScdException {
        //doNothing
    }

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
}
