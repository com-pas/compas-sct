// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.model.cb_po.FCDAs;
import org.lfenergy.compas.sct.commons.model.cb_po.PO;
import org.lfenergy.compas.sct.commons.model.cb_po.TFCDAFilter;
import org.lfenergy.compas.sct.commons.model.cb_po.Tfc;
import org.lfenergy.compas.sct.commons.scl.ied.DataSetAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;
import org.lfenergy.compas.sct.commons.util.CommonConstants;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.*;

@ExtendWith(MockitoExtension.class)
class HmiServiceTest {

    private static final String DQC_REPORT_TYPE = "DQC";
    private static final String CYC_REPORT_TYPE = "CYC";
    @InjectMocks
    HmiService hmiService;

    private final PO po = new PO();

    @BeforeEach
    void setUp() {
        FCDAs fcdAs = new FCDAs();
        po.setFCDAs(fcdAs);
    }

    @Test
    void createAllIhmReportControlBlocks_with_fc_ST_should_create_dataset_and_controlblock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "ANCR", "1", null, "DoName1", Tfc.ST, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        // Check DataSet is created
        DataSetAdapter dataSet = findDataSet(scd, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(toFCDA(tfcdaFilter));
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);
        TReportControl reportControl = findControlBlock(scd, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
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
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "PVOC", "1", null, "DoName2", Tfc.MX, CYC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        // Check DataSet is created
        DataSetAdapter dataSet = findDataSet(scd, "IedName1", "LdInst11", "DS_LDINST11_CYPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(toFCDA(tfcdaFilter));
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);

        TReportControl reportControl = findControlBlock(scd, "IedName1", "LdInst11", "CB_LDINST11_CYPO", TReportControl.class);
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
    void createAllIhmReportControlBlocks_with_fc_MX_and_DQC_REPORT_TYPE_should_create_dataset_and_controlblock_with_suffix_DQPO() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "PVOC", "1", null, "DoName2", Tfc.MX, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        // Check DataSet is created
        DataSetAdapter dataSet = findDataSet(scd, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(toFCDA(tfcdaFilter));
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);

        TReportControl reportControl = findControlBlock(scd, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
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
    void createAllIhmReportControlBlocks_with_FCDA_on_ln0_should_create_dataset_and_controlblock() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "LLN0", null, null, "DoName0", Tfc.ST, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        // Check DataSet is created
        DataSetAdapter dataSet = findDataSet(scd, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(toFCDA(tfcdaFilter));
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);
        TReportControl reportControl = findControlBlock(scd, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
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
        LNAdapter ln = findLn(scd, "IedName1", "LdInst11", "ANCR", "1", null);
        ln.getCurrentElem().unsetDOI();
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "ANCR", "1", null, "DoName1", Tfc.ST, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        // Check DataSet is created
        DataSetAdapter dataSet = findDataSet(scd, "IedName1", "LdInst11", "DS_LDINST11_DQPO");
        assertThat(dataSet.getCurrentElem().getFCDA()).hasSize(1).first()
                .usingRecursiveComparison().isEqualTo(toFCDA(tfcdaFilter));
        // Check ControlBlock is created
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        assertThat(ln0.getTControlsByType(TReportControl.class)).hasSize(1);
        TReportControl reportControl = findControlBlock(scd, "IedName1", "LdInst11", "CB_LDINST11_DQPO", TReportControl.class);
        assertThat(reportControl).extracting(TReportControl::getRptID, TControl::getDatSet, TReportControl::getConfRev, TReportControl::isBuffered, TReportControl::getBufTime, TReportControl::isIndexed,
                        TControlWithTriggerOpt::getIntgPd)
                .containsExactly("IedName1LdInst11/LLN0.CB_LDINST11_DQPO", "DS_LDINST11_DQPO", 1L, true, 0L, true, 60000L);
    }

    @Test
    void createAllIhmReportControlBlocks_when_lDevice_ON_but_LN_Mod_StVal_OFF_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        LNAdapter ln = findLn(scd, "IedName1", "LdInst11", "ANCR", "1", null);
        ln.getDOIAdapterByName(CommonConstants.MOD_DO_NAME).getDataAdapterByName(CommonConstants.STVAL_DA_NAME).setVal("off");
        assertThat(ln.getDaiModStValValue()).hasValue("off");
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "ANCR", "1", null, "DoName1", Tfc.ST, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        assertThat(streamAllDataSets(scd)).isEmpty();
        assertThat(streamAllControlBlocks(scd, TReportControl.class)).isEmpty();
    }

    @Test
    void createAllIhmReportControlBlocks_when_lDevice_OFF_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        ln0.getDOIAdapterByName(CommonConstants.MOD_DO_NAME).getDataAdapterByName(CommonConstants.STVAL_DA_NAME).setVal("off");
        assertThat(findLDevice(scd, "IedName1", "LdInst11").getLDeviceStatus()).hasValue(ActiveStatus.OFF.getValue());
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "ANCR", "1", null, "DoName1", Tfc.ST, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        assertThat(streamAllDataSets(scd)).isEmpty();
        assertThat(streamAllControlBlocks(scd, TReportControl.class)).isEmpty();
    }

    @Test
    void createAllIhmReportControlBlocks_when_LDevice_has_no_status_should_not_create_dataset() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-hmi-create-report-cb/scd_create_dataset_and_controlblocks_for_hmi.xml");
        LN0Adapter ln0 = findLn0(scd, "IedName1", "LdInst11");
        ln0.getDOIAdapterByName(CommonConstants.MOD_DO_NAME).getDataAdapterByName(CommonConstants.STVAL_DA_NAME).getCurrentElem().unsetVal();
        assertThat(findLDevice(scd, "IedName1", "LdInst11").getLDeviceStatus()).isEmpty();
        TFCDAFilter tfcdaFilter = createFCDAFilter("LdInst11", "ANCR", "1", null, "DoName1", Tfc.ST, DQC_REPORT_TYPE);
        po.getFCDAs().getFCDA().add(tfcdaFilter);
        // When
        hmiService.createAllHmiReportControlBlocks(scd, po);
        // Then
        assertThat(streamAllDataSets(scd)).isEmpty();
        assertThat(streamAllControlBlocks(scd, TReportControl.class)).isEmpty();
    }

    private static TFCDAFilter createFCDAFilter(String ldInst, String lnClass, String lnInst, String prefix, String doName, Tfc tfc, String reportType) {
        TFCDAFilter tfcdaFilter = new TFCDAFilter();
        tfcdaFilter.setLdInst(ldInst);
        tfcdaFilter.setLnClass(lnClass);
        tfcdaFilter.setPrefix(prefix);
        tfcdaFilter.setDoName(doName);
        tfcdaFilter.setLnInst(lnInst);
        tfcdaFilter.setFc(tfc);
        tfcdaFilter.setReportType(reportType);
        return tfcdaFilter;
    }

    private static TFCDA toFCDA(TFCDAFilter tfcdaFilter) {
        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst(tfcdaFilter.getLdInst());
        tfcda.getLnClass().add(tfcdaFilter.getLnClass());
        tfcda.setPrefix(tfcdaFilter.getPrefix());
        tfcda.setLnInst(tfcdaFilter.getLnInst());
        tfcda.setDoName(tfcdaFilter.getDoName());
        tfcda.setFc(TFCEnum.fromValue(tfcdaFilter.getFc().value()));
        return tfcda;
    }

}
