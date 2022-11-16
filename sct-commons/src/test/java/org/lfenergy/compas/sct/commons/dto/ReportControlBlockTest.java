// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReportControlBlockTest {

    private static final String ID = UUID.randomUUID().toString();
    private static final String DATASET_REF = "DATASET_REF";
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION";
    private static final String RPT_DESC = "RPT DESCRIPTION";
    private static final String RPT_TEXT = "RPT TEXT";

    @Test
    void testGetClassType() {
        ReportControlBlock reportControlBlock = new ReportControlBlock();
        assertEquals(ReportControlBlock.class, reportControlBlock.getClassType());
    }

    @Test
    void testGetServiceType() {
        ReportControlBlock reportControlBlock = new ReportControlBlock();
        assertEquals(TServiceType.REPORT, reportControlBlock.getServiceType());
    }

    @Test
    void testValidateCB() {
        ReportControlBlock reportControlBlock = create();
        assertDoesNotThrow(reportControlBlock::validateCB);

        reportControlBlock.setDataSetRef(null);
        assertDoesNotThrow(reportControlBlock::validateCB);

        assertFalse(reportControlBlock.getIedNames().isEmpty());
        reportControlBlock.getIedNames().get(0).setValue(null);
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.getIedNames().get(0).setValue("");
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.getIedNames().get(0).setLdInst("");
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.setDataSetRef("");
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.setName(null);
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.setName("");
        assertThrows(ScdException.class, reportControlBlock::validateCB);


        reportControlBlock.setId(null);
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.setId("");
        assertThrows(ScdException.class, reportControlBlock::validateCB);


    }

    @Test
    void testValidateDestination() throws ScdException {
        SclRootAdapter sclRootAdapter = Mockito.mock(SclRootAdapter.class);
        IEDAdapter iedAdapter = Mockito.mock(IEDAdapter.class);
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        LNAdapter lnAdapter = Mockito.mock(LNAdapter.class);

        Mockito.when(sclRootAdapter.getIEDAdapterByName(ArgumentMatchers.anyString())).thenReturn(iedAdapter);
        Mockito.when(iedAdapter.findLDeviceAdapterByLdInst(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(lDeviceAdapter));
        Mockito.when(
                lDeviceAdapter.getLNAdapter(
                        ArgumentMatchers.anyString(),ArgumentMatchers.anyString(),ArgumentMatchers.anyString()
                )
        ).thenReturn(lnAdapter);

        ReportControlBlock reportControlBlock = create();
        assertDoesNotThrow(() -> reportControlBlock.validateDestination(sclRootAdapter));

        assertFalse(reportControlBlock.getIedNames().isEmpty());
        reportControlBlock.getIedNames().get(0).getLnClass().clear();
        assertDoesNotThrow(() -> reportControlBlock.validateDestination(sclRootAdapter));

        Mockito.when(iedAdapter.findLDeviceAdapterByLdInst(ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        assertThrows(ScdException.class, () -> reportControlBlock.validateDestination(sclRootAdapter));
    }


    @Test
    void testGetControlBlockServiceSetting(){
        ReportControlBlock reportControlBlock = create();
        assertEquals(TServiceSettingsNoDynEnum.FIX, reportControlBlock.getControlBlockServiceSetting(null));

        TServices tServices = Mockito.mock(TServices.class);
        TReportSettings reportSettings = Mockito.mock(TReportSettings.class);
        Mockito.when(tServices.getReportSettings()).thenReturn(reportSettings);
        Mockito.when(reportSettings.getCbName()).thenReturn(TServiceSettingsNoDynEnum.CONF);
        assertEquals(TServiceSettingsNoDynEnum.CONF,reportControlBlock.getControlBlockServiceSetting(tServices));

        Mockito.when(tServices.getReportSettings()).thenReturn(null);
        assertEquals(TServiceSettingsNoDynEnum.FIX,reportControlBlock.getControlBlockServiceSetting(tServices));
    }

    @Test
    void testValidateSecurityEnabledValue() {
        ReportControlBlock reportControlBlock = new ReportControlBlock();
        IEDAdapter iedAdapter = Mockito.mock(IEDAdapter.class);
        Mockito.when(iedAdapter.getServices()).thenReturn(new TServices());
        assertDoesNotThrow(() -> reportControlBlock.validateSecurityEnabledValue(iedAdapter));
    }

    @Test
    void testCreateControlBlock() {
        TReportControl tReportControl = create().createControlBlock();
        assertAll("CREATE CB",
                () -> assertEquals(ID,tReportControl.getRptID()),
                () -> assertEquals(DATASET_REF,tReportControl.getDatSet()),
                () -> assertEquals(NAME,tReportControl.getName()),
                () -> assertEquals(1,tReportControl.getConfRev()),
                () -> assertEquals(DESC,tReportControl.getDesc())
        );

        ReportControlBlock reportControlBlock = new ReportControlBlock(tReportControl);
        assertAll("INIT : " + reportControlBlock,
                () -> assertEquals(ID,reportControlBlock.getId()),
                () -> assertEquals(DATASET_REF,reportControlBlock.getDataSetRef()),
                () -> assertEquals(NAME,reportControlBlock.getName()),
                () -> assertEquals(DESC,reportControlBlock.getDesc()),
                () -> assertNotNull(reportControlBlock.getOptFields()),
                () -> assertNotNull(reportControlBlock.getRptEnabled()),
                () -> assertEquals(TPredefinedTypeOfSecurityEnum.NONE,reportControlBlock.getSecurityEnable())
        );
    }

    @Test
    void testCast(){
        Object cb  = new ReportControlBlock();
        ReportControlBlock rpt = new ReportControlBlock();
        assertEquals(ReportControlBlock.class, rpt.cast(cb).getClass());
        assertThrows(UnsupportedOperationException.class, () -> rpt.cast(new SMVControlBlock()).getClass());
    }

    @Test
    void testTClientLN2IEDName(){
        TClientLN clientLN = Mockito.mock(TClientLN.class);
        Mockito.when(clientLN.getIedName()).thenReturn(DTO.HOLDER_IED_NAME);
        Mockito.when(clientLN.getApRef()).thenReturn("AP_REF");
        Mockito.when(clientLN.getLdInst()).thenReturn(DTO.HOLDER_LD_INST);
        Mockito.when(clientLN.getLnInst()).thenReturn(DTO.HOLDER_LN_INST);
        Mockito.when(clientLN.getPrefix()).thenReturn(DTO.HOLDER_LN_PREFIX);
        Mockito.when(clientLN.getLnClass()).thenReturn(List.of(DTO.HOLDER_LN_CLASS));

        TControlWithIEDName.IEDName iedName = ReportControlBlock.toIEDName(clientLN);
        assertEquals(clientLN.getApRef(),iedName.getApRef());
    }


    private ReportControlBlock create(){
        ReportControlBlock reportControlBlock = new ReportControlBlock();
        reportControlBlock.setId(ID);
        reportControlBlock.setDataSetRef(DATASET_REF);
        reportControlBlock.setConfRev(1L);
        reportControlBlock.setName(NAME);
        reportControlBlock.setBuffered(true);
        reportControlBlock.setIndexed(true);
        reportControlBlock.setIntgPd(1000);

        reportControlBlock.setOptFields(createOptFields());
        reportControlBlock.setRptEnabled(createTRptEnabled());
        reportControlBlock.setDesc(DESC);

        TControlWithIEDName.IEDName iedName = new TControlWithIEDName.IEDName();
        iedName.setLdInst(DTO.HOLDER_LD_INST);
        iedName.setLnInst(DTO.HOLDER_LN_INST);
        iedName.setPrefix(DTO.HOLDER_LN_PREFIX);
        iedName.setApRef("AP_REF");
        iedName.getLnClass().add(DTO.HOLDER_LN_CLASS);
        iedName.setValue(DTO.HOLDER_IED_NAME);
        reportControlBlock.getIedNames().add(iedName);
        return reportControlBlock;
    }

    private TRptEnabled createTRptEnabled() {
        TRptEnabled rptEnabled = new TRptEnabled();

        rptEnabled.setDesc(RPT_DESC);
        rptEnabled.setMax(2L);
        TText tText = new TText();
        tText.setSource(RPT_TEXT);
        rptEnabled.setText(tText);

        return rptEnabled;
    }

    private TReportControl.OptFields createOptFields(){
        TReportControl.OptFields optFields = new TReportControl.OptFields();
        optFields.setBufOvfl(true);
        optFields.setBufOvfl(true);
        optFields.setDataRef(true);
        return optFields;
    }
}
