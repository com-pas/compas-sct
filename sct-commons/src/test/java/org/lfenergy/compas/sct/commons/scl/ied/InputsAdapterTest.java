// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findInputs;

class InputsAdapterTest {

    @Test
    void constructor_should_succeed() {
        // Given
        TInputs tInputs = new TInputs();
        LN0 ln0 = new LN0();
        ln0.setInputs(tInputs);
        LN0Adapter ln0Adapter = new LN0Adapter(null, ln0);
        // When && Then
        assertThatNoException().isThrownBy(() -> new InputsAdapter(ln0Adapter, tInputs));
    }

    @Test
    void elementXPath_should_succeed() {
        // Given
        TInputs tInputs = new TInputs();
        InputsAdapter inputsAdapter = new InputsAdapter(null, tInputs);
        // When
        String result = inputsAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Inputs");
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_report_Target_Ied_missing_Private_compasBay_errors() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_ied_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).containsExactly(
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]",
                "IED is missing Private/compas:Bay@UUID attribute")
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_report_Source_Ied_missing_Private_compasBay_errors() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_ied_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME3", "LD_INST31");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).containsExactly(
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME3\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST31\"]/LN0/Inputs/ExtRef[@desc=\"Source IED is " +
                    "missing compas:Bay @UUID\"]",
                "Source IED is missing Private/compas:Bay@UUID attribute")
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_report_ExtRef_attribute_missing() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_extref_errors.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).containsExactlyInAnyOrder(
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]/LN0/Inputs/" +
                    "ExtRef[@desc=\"ExtRef is missing ServiceType attribute\"]",
                "The signal ExtRef is missing ServiceType attribute"),
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]/LN0/Inputs/" +
                    "ExtRef[@desc=\"ExtRef is ServiceType Poll\"]",
                "The signal ExtRef ServiceType attribute is unexpected : POLL"),
            SclReportItem.fatal("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]/LN0/Inputs/" +
                    "ExtRef[@desc=\"ExtRef is ServiceType Report with malformed desc attribute\"]",
                "ExtRef.serviceType=Report but ExtRef.desc attribute is malformed")
        );
    }

    @Test
    void updateAllSourceDataSetsAndControlBlocks_should_succeed() throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).isEmpty();
    }


}
