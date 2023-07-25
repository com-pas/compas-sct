// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.ied.DataSetAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.CommonConstants;
import org.lfenergy.compas.sct.commons.util.LdeviceStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newFcda;

class HmiServiceTest {

    @Test
    void createAllIhmReportControlBlocks_with_fc_ST_should_create_dataset_and_controlblock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        TFCDA fcda = newFcda("LdInst11", "ANCR", "1", null, "DoName1", null, TFCEnum.ST);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        // Check DataSet is created
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataSetAdapter dataSet = findDataSet(sclRootAdapter, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(fcda);
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);
        TReportControl reportControl = findControlBlock(sclRootAdapter, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
        assertThat(reportControl).extracting(TReportControl::getRptID, TControl::getDatSet, TReportControl::getConfRev, TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed,
                        TControlWithTriggerOpt::getIntgPd)
                .containsExactly("IedName1LdInst11/LLN0.CB_LDINST11_DQPO", "DS_LDINST11_DQPO", 1L, true, 0L, true, 60000L);
        assertThat(reportControl.getTrgOps())
                .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isDupd, TTrgOps::isPeriod, TTrgOps::isGi)
                .containsExactly(true, true, false, true, true);
        assertThat(reportControl.getRptEnabled().getMax()).isEqualTo(1);
        assertThat(reportControl.getRptEnabled().isSetClientLN()).isFalse();
    }

    @Test
    void createAllIhmReportControlBlocks_with_fc_MX_should_create_dataset_and_controlblock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        TFCDA fcda = newFcda("LdInst11", "PVOC", "1", null, "DoName2", null, TFCEnum.MX);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        // Check DataSet is created
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataSetAdapter dataSet = findDataSet(sclRootAdapter, "IedName1", "LdInst11", "DS_LDINST11_CYPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(fcda);
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);

        TReportControl reportControl = findControlBlock(sclRootAdapter, "IedName1", "LdInst11", "CB_LDINST11_CYPO", TReportControl.class);
        assertThat(reportControl).extracting(TReportControl::getRptID, TControl::getDatSet, TReportControl::getConfRev, TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed,
                        TControlWithTriggerOpt::getIntgPd)
                .containsExactly("IedName1LdInst11/LLN0.CB_LDINST11_CYPO", "DS_LDINST11_CYPO", 1L, true, 0L, true, 2000L);
        assertThat(reportControl.getTrgOps())
                .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isDupd, TTrgOps::isPeriod, TTrgOps::isGi)
                .containsExactly(false, false, false, true, true);
        assertThat(reportControl.getRptEnabled().getMax()).isEqualTo(1);
        assertThat(reportControl.getRptEnabled().isSetClientLN()).isFalse();
    }

    @Test
    void createAllIhmReportControlBlocks_with_FCDA_on_ln0_should_create_dataset_and_controlblock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        TFCDA fcda = newFcda("LdInst11", "LLN0", null, null, "DoName0", null, TFCEnum.ST);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        // Check DataSet is created
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataSetAdapter dataSet = findDataSet(sclRootAdapter, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(fcda);
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);
        TReportControl reportControl = findControlBlock(sclRootAdapter, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
        assertThat(reportControl).extracting(TReportControl::getRptID, TControl::getDatSet, TReportControl::getConfRev, TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed,
                        TControlWithTriggerOpt::getIntgPd)
                .containsExactly("IedName1LdInst11/LLN0.CB_LDINST11_DQPO", "DS_LDINST11_DQPO", 1L, true, 0L, true, 60000L);
        assertThat(reportControl.getTrgOps())
                .extracting(TTrgOps::isDchg, TTrgOps::isQchg, TTrgOps::isDupd, TTrgOps::isPeriod, TTrgOps::isGi)
                .containsExactly(true, true, false, true, true);
        assertThat(reportControl.getRptEnabled().getMax()).isEqualTo(1);
        assertThat(reportControl.getRptEnabled().isSetClientLN()).isFalse();
    }

    @Test
    void createAllIhmReportControlBlocks_when_lDevice_ON_but_LN_Mod_StVal_missing_should_create_dataset_and_controlblock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LNAdapter ln = findLn(sclRootAdapter, "IedName1", "LdInst11", "ANCR", "1", null);
        ln.getCurrentElem().unsetDOI();
        TFCDA fcda = newFcda("LdInst11", "ANCR", "1", null, "DoName1", null, TFCEnum.ST);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        // Check DataSet is created
        DataSetAdapter dataSet = findDataSet(sclRootAdapter, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(fcda);
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);
        TReportControl reportControl = findControlBlock(sclRootAdapter, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
        assertThat(reportControl).extracting(TReportControl::getRptID, TControl::getDatSet, TReportControl::getConfRev, TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed,
                        TControlWithTriggerOpt::getIntgPd)
                .containsExactly("IedName1LdInst11/LLN0.CB_LDINST11_DQPO", "DS_LDINST11_DQPO", 1L, true, 0L, true, 60000L);
    }

    @Test
    void createAllIhmReportControlBlocks_when_lDevice_ON_but_LN_Mod_StVal_OFF_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LNAdapter ln = findLn(sclRootAdapter, "IedName1", "LdInst11", "ANCR", "1", null);
        ln.getDOIAdapterByName(CommonConstants.MOD_DO_NAME).getDataAdapterByName(CommonConstants.STVAL_DA_NAME).setVal("off");
        assertThat(ln.getDaiModStValValue()).hasValue("off");
        TFCDA fcda = newFcda("LdInst11", "ANCR", "1", null, "DoName1", null, TFCEnum.ST);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        assertThat(streamAllDataSets(sclRootAdapter)).isEmpty();
        assertThat(streamAllControlBlocks(sclRootAdapter, TReportControl.class)).isEmpty();
    }

    @Test
    void createAllIhmReportControlBlocks_when_lDevice_OFF_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IedName1", "LdInst11");
        ln0.getDOIAdapterByName(CommonConstants.MOD_DO_NAME).getDataAdapterByName(CommonConstants.STVAL_DA_NAME).setVal("off");
        assertThat(findLDevice(sclRootAdapter, "IedName1", "LdInst11").getLDeviceStatus()).hasValue(LdeviceStatus.OFF.getValue());
        TFCDA fcda = newFcda("LdInst11", "ANCR", "1", null, "DoName1", null, TFCEnum.ST);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        assertThat(streamAllDataSets(sclRootAdapter)).isEmpty();
        assertThat(streamAllControlBlocks(sclRootAdapter, TReportControl.class)).isEmpty();
    }

    @Test
    void createAllIhmReportControlBlocks_when_LDevice_has_no_status_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IedName1", "LdInst11");
        ln0.getDOIAdapterByName(CommonConstants.MOD_DO_NAME).getDataAdapterByName(CommonConstants.STVAL_DA_NAME).getCurrentElem().unsetVal();
        assertThat(findLDevice(sclRootAdapter, "IedName1", "LdInst11").getLDeviceStatus()).isEmpty();
        TFCDA fcda = newFcda("LdInst11", "ANCR", "1", null, "DoName1", null, TFCEnum.ST);
        // When
        HmiService.createAllHmiReportControlBlocks(scd, List.of(fcda));
        // Then
        assertThat(streamAllDataSets(sclRootAdapter)).isEmpty();
        assertThat(streamAllControlBlocks(sclRootAdapter, TReportControl.class)).isEmpty();
    }

}
