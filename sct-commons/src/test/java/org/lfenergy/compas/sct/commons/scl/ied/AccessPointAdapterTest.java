/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ServicesConfigEnum;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessPointAdapterTest {

    @Test
    void amChildElementRef_whenCalledWithExistingRelationBetweenIEDAndAccessPoint_shouldReturnTrue() {
        //Given
        IEDAdapter iedAdapter = mock(IEDAdapter.class);
        TIED tied = new TIED();
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName("AP_NAME");
        tied.getAccessPoint().add(tAccessPoint);
        when(iedAdapter.getCurrentElem()).thenReturn(tied);

        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, tAccessPoint);
        // When Then
        assertThat(accessPointAdapter.amChildElementRef()).isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = {"AP_NAME;AccessPoint[@name=\"AP_NAME\"]", ";AccessPoint[not(@name)]"}
            , delimiter = ';')
    void elementXPath_should_return_expected_xpath_value(String apName, String message) {
        // Given
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName(apName);
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(null, tAccessPoint);
        // When
        String elementXPathResult = accessPointAdapter.elementXPath();
        // Then
        assertThat(elementXPathResult).isEqualTo(message);
    }

    @Test
    void getXPath_should_return_expected_xpath_value() {
        // Given
        TIED tied = new TIED();
        tied.setName("IED_NAME");
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName("AP_NAME");
        tied.getAccessPoint().add(tAccessPoint);
        IEDAdapter iedAdapter = new IEDAdapter(null, tied);
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, tAccessPoint);
        // When
        String elementXPathResult = accessPointAdapter.getXPath();
        // Then
        assertThat(elementXPathResult).isEqualTo("/IED[@name=\"IED_NAME\"]/AccessPoint[@name=\"AP_NAME\"]");
    }

    @Test
    void checkFCDALimitations_should_succeed_no_error_message() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkFCDALimitations();
        //Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void checkFCDALimitations_should_fail_with_one_error_messages() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfDataSet().setMaxAttributes(2L);
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkFCDALimitations();
        //Then
        assertThat(sclReportItems).hasSize(1)
                .extracting(SclReportItem::message)
                .containsExactlyInAnyOrder("There are too much FCDA for the DataSet dataset6 for the LDevice LD_INST21 in IED IED_NAME: 3 > 2 max");
    }
    @Test
    void checkFCDALimitations_should_fail_with_four_error_messages() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfDataSet().setMaxAttributes(1L);
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkFCDALimitations();
        //Then
        assertThat(sclReportItems).hasSize(4)
                .extracting(SclReportItem::message)
                .containsExactlyInAnyOrder("There are too much FCDA for the DataSet dataset3 for the LDevice LD_INST21 in IED IED_NAME: 2 > 1 max",
                        "There are too much FCDA for the DataSet dataset6 for the LDevice LD_INST21 in IED IED_NAME: 3 > 1 max",
                        "There are too much FCDA for the DataSet dataset6 for the LDevice LD_INST22 in IED IED_NAME: 2 > 1 max",
                        "There are too much FCDA for the DataSet dataset5 for the LDevice LD_INST22 in IED IED_NAME: 2 > 1 max");
    }


    @Test
    void checkControlsLimitation_should_fail_for_dataset_with_one_error_messages() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfDataSet().setMax(5L);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.DATASET);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::message).isEqualTo("There are too much DataSets for the IED IED_NAME: 6 > 5 max");
    }

    @Test
    void checkControlsLimitation_should_fail_for_smv_with_one_error_messages() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getSMVsc().setMax(2L);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.SMV);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::message).isEqualTo("There are too much SMV Control Blocks for the IED IED_NAME: 3 > 2 max");
    }

    @Test
    void checkControlsLimitation_should_fail_for_goose_with_one_error_messages() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getGOOSE().setMax(2L);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.GSE);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::message).isEqualTo("There are too much GOOSE Control Blocks for the IED IED_NAME: 3 > 2 max");
    }

    @Test
    void checkControlsLimitation_should_fail_for_report_with_one_error_messages() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfReportControl().setMax(0L);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.REPORT);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::message).isEqualTo("There are too much Report Control Blocks for the IED IED_NAME: 1 > 0 max");
    }

    public static AccessPointAdapter provideAPForCheckLimitationForIED() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_ied_controls_dataset.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        return new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
    }

    @Test
    void checkControlsLimitation_when_ConfReportControl_is_missing_should_succeed() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().setConfReportControl(null);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.REPORT);
        //Then
        assertThat(sclReportItem).isEmpty();
    }

    @Test
    void checkControlsLimitation_when_GOOSE_is_missing_should_succeed() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().setGOOSE(null);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.GSE);
        //Then
        assertThat(sclReportItem).isEmpty();
    }

    @Test
    void checkControlsLimitation_when_ConfDataSet_is_missing_should_succeed() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().setConfDataSet(null);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.DATASET);
        //Then
        assertThat(sclReportItem).isEmpty();
    }

    @Test
    void checkControlsLimitation_when_Services_is_missing_should_succeed() {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().setServices(null);
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.SMV);
        //Then
        assertThat(sclReportItem).isEmpty();
    }

    @Test
    void checkLimitationForBoundIEDFCDAs_should_success_no_error() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxAttributes(11L);
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkLimitationForBoundIedFcdas(tExtRefs);

        //Then
        assertThat(sclReportItem).isEmpty();
    }

    @Test
    void checkLimitationForBoundIEDFCDAs_should_fail_one_error_message() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        accessPointAdapter.getCurrentElem().getServices().getClientServices().setMaxAttributes(4L);
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();

        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkLimitationForBoundIedFcdas(tExtRefs);

        //Then
        assertThat(sclReportItem).isPresent()
                .get()
                .extracting(SclReportItem::message)
                .isEqualTo("The Client IED IED_NAME1 subscribes to too much FCDA: 9 > 4 max");
    }

    @Test
    void checkLimitationForBoundIEDControls_should_fail_three_error_messages() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkLimitationForBoundIEDControls(tExtRefs);

        //Then
        assertThat(sclReportItems).hasSize(3)
                .extracting(SclReportItem::message)
                .containsExactlyInAnyOrder("The Client IED IED_NAME1 subscribes to too much GOOSE Control Blocks: 3 > 2 max",
                        "The Client IED IED_NAME1 subscribes to too much SMV Control Blocks: 2 > 1 max",
                        "The Client IED IED_NAME1 subscribes to too much Report Control Blocks: 1 > 0 max");
    }

    @Test
    void checkLimitationForBoundIEDControls_should_succeed_no_error_message() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxAttributes(11L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxGOOSE(5L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxReports(2L);
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxSMV(2L);
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkLimitationForBoundIEDControls(tExtRefs);

        //Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void getAllCoherentExtRefForAnalyze_succeed() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_bound_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        //When
         AccessPointAdapter.ExtRefAnalyzeRecord extRefAnalyzeRecord = accessPointAdapter.getAllCoherentExtRefForAnalyze();
        //Then
        assertThat(extRefAnalyzeRecord)
                .extracting(AccessPointAdapter.ExtRefAnalyzeRecord::sclReportItems)
                .asList().isEmpty();
    }

    @Test
    void getAllCoherentExtRefForAnalyze_fail_with_one_error() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_coherent_extRefs.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        //When
        AccessPointAdapter.ExtRefAnalyzeRecord extRefAnalyzeRecord = accessPointAdapter.getAllCoherentExtRefForAnalyze();
        //Then
        assertThat(extRefAnalyzeRecord)
                .extracting(AccessPointAdapter.ExtRefAnalyzeRecord::sclReportItems)
                .asList().hasSize(1);
    }
}

