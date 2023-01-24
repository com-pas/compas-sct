// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReportControlBlockTest {

    private static final String ID = UUID.randomUUID().toString();
    private static final String DATASET_REF = "DATASET_REF";
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION";

    @Test
    void constructor_should_fill_default_values() {
        // Given : NAME, DATASET_REF, ID constants
        // When
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        // Then
        assertThat(reportControlBlock)
            .extracting(ControlBlock::getName, ControlBlock::getDataSetRef, ControlBlock::getId, ControlBlock::getConfRev,
                ReportControlBlock::isBuffered, ReportControlBlock::getBufTime, ReportControlBlock::isIndexed, ReportControlBlock::getIntgPd)
            .containsExactly(NAME, DATASET_REF, ID, 1L, true, 0L, true, 60000L);

        assertThat(reportControlBlock.getTrgOps())
            .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isPeriod, TTrgOps::isGi)
            .containsOnly(true);
        assertThat(reportControlBlock.getTrgOps().isDupd()).isFalse();

        assertThat(reportControlBlock.getOptFields())
            .extracting(TReportControl.OptFields::isSeqNum, TReportControl.OptFields::isTimeStamp, TReportControl.OptFields::isDataSet, TReportControl.OptFields::isReasonCode,
                TReportControl.OptFields::isDataRef, TReportControl.OptFields::isEntryID, TReportControl.OptFields::isConfigRef)
            .containsOnly(false);
        assertThat(reportControlBlock.getOptFields().isBufOvfl()).isTrue();

    }

    @Test
    void constructor_should_copy_all_data_from_TReportControl() {
        // Given
        /*
        optFields
            rptEnabled
            trgOps

         */
        TReportControl tReportControl = createTReportControl();
        // When
        ReportControlBlock reportControlBlock = new ReportControlBlock(tReportControl);
        // Then
        assertThat(reportControlBlock)
            .extracting(ControlBlock::getName, ControlBlock::getDataSetRef, ControlBlock::getId, ControlBlock::getConfRev,
                ReportControlBlock::isBuffered, ReportControlBlock::getBufTime, ReportControlBlock::isIndexed, ReportControlBlock::getIntgPd)
            .containsExactly(NAME, DATASET_REF, ID, 5L, false, 1L, false, 1L);

        assertThat(reportControlBlock.getOptFields())
            .extracting(TReportControl.OptFields::isSeqNum, TReportControl.OptFields::isTimeStamp, TReportControl.OptFields::isDataSet, TReportControl.OptFields::isReasonCode,
                TReportControl.OptFields::isDataRef, TReportControl.OptFields::isEntryID, TReportControl.OptFields::isConfigRef, TReportControl.OptFields::isBufOvfl)
            .containsOnly(false);

        assertThat(reportControlBlock.getTrgOps())
            .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isPeriod, TTrgOps::isGi, TTrgOps::isDupd)
            .containsOnly(false);
    }

    @Test
    void getServiceType_should_return_REPORT() {
        // Given
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        // When
        TServiceType serviceType = reportControlBlock.getServiceType();
        // Then
        assertThat(serviceType).isEqualTo(TServiceType.REPORT);
    }

    @Test
    void getControlBlockEnum_should_return_REPORT() {
        // Given
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        // When
        ControlBlockEnum controlBlockEnum = reportControlBlock.getControlBlockEnum();
        // Then
        assertThat(controlBlockEnum).isEqualTo(ControlBlockEnum.REPORT);
    }

    @Test
    void toTControl_should_return_TReportControl() {
        // Given
        ReportControlBlock reportControlBlock = createReportControlBlock();

        // When
        TReportControl tReportControl = reportControlBlock.toTControl();
        // Then
        assertThat(tReportControl)
            .extracting(TControl::getName, TControl::getDatSet, TReportControl::getRptID, TReportControl::getConfRev, TUnNaming::getDesc,
                TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed, TControlWithTriggerOpt::getIntgPd)
            .containsExactly(NAME, DATASET_REF, ID, 5L, DESC, false, 1L, false, 1L);
        assertThat(tReportControl.getOptFields())
            .extracting(TReportControl.OptFields::isSeqNum, TReportControl.OptFields::isTimeStamp, TReportControl.OptFields::isDataSet, TReportControl.OptFields::isReasonCode,
                TReportControl.OptFields::isDataRef, TReportControl.OptFields::isEntryID, TReportControl.OptFields::isConfigRef, TReportControl.OptFields::isBufOvfl)
            .containsOnly(false);

        assertThat(tReportControl.getTrgOps())
            .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isPeriod, TTrgOps::isGi, TTrgOps::isDupd)
            .containsOnly(false);
    }

    @Test
    void addToLN_should_add_ControlBlock_to_given_LN0() {
        // Given
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        TLN0 ln0 = new TLN0();
        // When
        TReportControl tReportControl = reportControlBlock.addToLN(ln0);
        // Then
        assertThat(ln0.getReportControl()).hasSize(1)
            .first()
            .isSameAs(tReportControl);
        assertThat(tReportControl).extracting(TControl::getName, TControl::getDatSet, TReportControl::getRptID)
            .containsExactly(NAME, DATASET_REF, ID);
    }

    @Test
    void addToLN_should_add_ControlBlock_to_given_LN() {
        // Given
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        TLN ln = new TLN();
        // When
        TReportControl tReportControl = reportControlBlock.addToLN(ln);
        // Then
        assertThat(ln.getReportControl()).hasSize(1)
            .first()
            .isSameAs(tReportControl);
        assertThat(tReportControl).extracting(TControl::getName, TControl::getDatSet, TReportControl::getRptID)
            .containsExactly(NAME, DATASET_REF, ID);
    }

    @Test
    void testGetServiceType() {
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        assertEquals(TServiceType.REPORT, reportControlBlock.getServiceType());
    }

    @Test
    void testValidateCB() {
        ReportControlBlock reportControlBlock = createReportControlBlock();
        assertDoesNotThrow(reportControlBlock::validateCB);

        reportControlBlock.setDataSetRef(null);
        assertDoesNotThrow(reportControlBlock::validateCB);

        assertFalse(reportControlBlock.getTargets().isEmpty());
        reportControlBlock.getTargets().set(0,
            new ControlBlockTarget("AP_REF", null, DTO.HOLDER_LD_INST, DTO.HOLDER_LN_INST, DTO.HOLDER_LN_CLASS, DTO.HOLDER_LN_PREFIX));
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.getTargets().set(0,
            new ControlBlockTarget("AP_REF", "", DTO.HOLDER_LD_INST, DTO.HOLDER_LN_INST, DTO.HOLDER_LN_CLASS, DTO.HOLDER_LN_PREFIX));
        assertThrows(ScdException.class, reportControlBlock::validateCB);

        reportControlBlock.getTargets().set(0,
            new ControlBlockTarget("AP_REF", DTO.HOLDER_LD_INST, "", DTO.HOLDER_LN_INST, DTO.HOLDER_LN_CLASS, DTO.HOLDER_LN_PREFIX));
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
    void testGetControlBlockServiceSetting(){
        ReportControlBlock reportControlBlock = createReportControlBlock();
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
    void validateSecurityEnabledValue_should_do_nothing() {
        // Given
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        // When & Then
        Assertions.assertThatCode(() -> reportControlBlock.validateSecurityEnabledValue(new TServices()))
            .doesNotThrowAnyException();
    }

    private static ReportControlBlock createReportControlBlock() {
        ReportControlBlock reportControlBlock = new ReportControlBlock(NAME, ID, DATASET_REF);
        reportControlBlock.setConfRev(5L);
        reportControlBlock.setDesc(DESC);
        reportControlBlock.setBuffered(false);
        reportControlBlock.setBufTime(1L);
        reportControlBlock.setIndexed(false);
        reportControlBlock.setIntgPd(1L);
        TReportControl.OptFields optFields = new TReportControl.OptFields();
        optFields.setBufOvfl(false);
        reportControlBlock.setOptFields(optFields);
        TTrgOps trgOps = new TTrgOps();
        trgOps.setGi(false);
        reportControlBlock.setTrgOps(trgOps);
        reportControlBlock.setRptEnabledMax(10L);
        reportControlBlock.getTargets().add(new ControlBlockTarget("apRef", "iedName", "ldInst", "lnInst", "lnClass", "prefix", "desc"));
        return reportControlBlock;
    }

    private static TReportControl createTReportControl() {
        TReportControl tReportControl = new TReportControl();
        tReportControl.setName(NAME);
        tReportControl.setDatSet(DATASET_REF);
        tReportControl.setRptID(ID);
        tReportControl.setConfRev(5L);
        tReportControl.setDesc(DESC);
        tReportControl.setBuffered(false);
        tReportControl.setBufTime(1L);
        tReportControl.setIndexed(false);
        tReportControl.setIntgPd(1L);
        TReportControl.OptFields optFields = new TReportControl.OptFields();
        optFields.setBufOvfl(false);
        tReportControl.setOptFields(optFields);
        TTrgOps trgOps = new TTrgOps();
        trgOps.setGi(false);
        tReportControl.setTrgOps(trgOps);
        tReportControl.setRptEnabled(new TRptEnabled());
        tReportControl.getRptEnabled().setMax(10L);
        return tReportControl;
    }
}
