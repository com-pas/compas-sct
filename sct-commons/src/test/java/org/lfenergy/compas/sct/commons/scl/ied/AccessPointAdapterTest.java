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
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ServicesConfigEnum;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPointAdapterTest {

    @Test
    void amChildElementRef() {
        //Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TIED tied = new TIED();
        tied.setName(DTO.HOLDER_IED_NAME);
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName("AP_NAME");
        tAccessPoint.setServices(new TServices());
        tied.getAccessPoint().add(tAccessPoint);
        sclRootAdapter.getCurrentElem().getIED().add(tied);
        IEDAdapter iAdapter = sclRootAdapter.getIEDAdapterByName(DTO.HOLDER_IED_NAME);
        //When
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iAdapter, iAdapter.getCurrentElem().getAccessPoint().get(0));
        //Then
        assertThat(accessPointAdapter.amChildElementRef()).isTrue();
        assertThat(accessPointAdapter.getCurrentElem().getServices()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"AP_NAME;AccessPoint[@name=\"AP_NAME\"]", ";AccessPoint[not(@name)]"}
            , delimiter = ';')
    void elementXPath(String apName, String message) {
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
    void getXPath() {
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
    void checkFCDALimitations_should_succed_no_error_message() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkFCDALimitations();
        //Then
        assertThat(sclReportItems).isEmpty();
    }

    @Test
    void checkFCDALimitations_should_fail_with_one_error_messages() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfDataSet().setMaxAttributes(2L);
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkFCDALimitations();
        //Then
        assertThat(sclReportItems).hasSize(1)
                .extracting(SclReportItem::getMessage)
                .containsExactlyInAnyOrder("There are too much FCDA for the DataSet DATASET6 for the LDevice LD_INST21 in IED IED_NAME");
    }
    @Test
    void checkFCDALimitations_should_fail_with_four_error_messages() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfDataSet().setMaxAttributes(1L);
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkFCDALimitations();
        //Then
        assertThat(sclReportItems).hasSize(4)
                .extracting(SclReportItem::getMessage)
                .containsExactlyInAnyOrder("There are too much FCDA for the DataSet DATASET3 for the LDevice LD_INST21 in IED IED_NAME",
                        "There are too much FCDA for the DataSet DATASET6 for the LDevice LD_INST21 in IED IED_NAME",
                        "There are too much FCDA for the DataSet DATASET6 for the LDevice LD_INST22 in IED IED_NAME",
                        "There are too much FCDA for the DataSet DATASET5 for the LDevice LD_INST22 in IED IED_NAME");
    }


    @Test
    void checkControlsLimitation_should_fail_for_dataset_with_one_error_messages() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfDataSet().setMax(5L);
        String message = "Too much DataSet for";
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.DATASET,message);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::getMessage).isEqualTo(message +" IED_NAME");
    }

    @Test
    void checkControlsLimitation_should_fail_for_smv_with_one_error_messages() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getSMVsc().setMax(2L);
        String message = "Too much SMV Control for";
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.SMV,message);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::getMessage).isEqualTo(message +" IED_NAME");
    }

    @Test
    void checkControlsLimitation_should_fail_for_goose_with_one_error_messages() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getGOOSE().setMax(2L);
        String message = "Too much Goose Control for";
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.GSE,message);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::getMessage).isEqualTo(message +" IED_NAME");
    }

    @Test
    void checkControlsLimitation_should_fail_for_report_with_one_error_messages() throws Exception {
        //Given
        AccessPointAdapter accessPointAdapter = provideAPForCheckLimitationForIED();
        accessPointAdapter.getCurrentElem().getServices().getConfReportControl().setMax(0L);
        String message = "Too much Report Control for";
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkControlsLimitation(ServicesConfigEnum.REPORT,message);
        //Then
        assertThat(sclReportItem).isPresent()
                .get().extracting(SclReportItem::getMessage).isEqualTo(message +" IED_NAME");
    }

    public static AccessPointAdapter provideAPForCheckLimitationForIED() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_ied_controls_dataset.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME");
        return new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
    }

    @Test
    void checkLimitationForBindedIEDFCDAs_should_success_no_error() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        iedAdapter.getCurrentElem().getAccessPoint().get(0).getServices().getClientServices().setMaxAttributes(11L);
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        String message = "Too much FCDA";
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();
        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkLimitationForBoundIEDFCDAs(tExtRefs, message);

        //Then
        assertThat(sclReportItem).isEmpty();
    }

    @Test
    void checkLimitationForBindedIEDFCDAs_should_fail_one_error_message() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        accessPointAdapter.getCurrentElem().getServices().getClientServices().setMaxAttributes(4L);
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();
        String message = "Too much FCDA for IED_NAME1";

        //When
        Optional<SclReportItem> sclReportItem = accessPointAdapter.checkLimitationForBoundIEDFCDAs(tExtRefs, message);

        //Then
        assertThat(sclReportItem).isPresent()
                .get()
                .extracting(SclReportItem::getMessage)
                .isEqualTo(message);
    }

    @Test
    void checkLimitationForBindedIEDControls_should_fail_three_error_messages() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName("IED_NAME1");
        AccessPointAdapter accessPointAdapter = new AccessPointAdapter(iedAdapter, iedAdapter.getCurrentElem().getAccessPoint().get(0));
        List<TExtRef> tExtRefs = accessPointAdapter.getAllCoherentExtRefForAnalyze().tExtRefs();
        //When
        List<SclReportItem> sclReportItems = accessPointAdapter.checkLimitationForBoundIEDControls(tExtRefs);

        //Then
        assertThat(sclReportItems).hasSize(3)
                .extracting(SclReportItem::getMessage)
                .containsExactlyInAnyOrder("The Client IED IED_NAME1 subscribes to too much GOOSE Control Blocks.",
                        "The Client IED IED_NAME1 subscribes to too much SMV Control Blocks.",
                        "The Client IED IED_NAME1 subscribes to too much REPORT Control Blocks.");
    }

    @Test
    void checkLimitationForBindedIEDControls_should_succed_no_error_message() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
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
    void getAllCoherentExtRefForAnalyze_succed() throws Exception {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/limitation_cb_dataset_fcda/scd_check_limitation_binded_ied_controls_fcda.xml");
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
    void getAllCoherentExtRefForAnalyze_fail_with_one_error() throws Exception {
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