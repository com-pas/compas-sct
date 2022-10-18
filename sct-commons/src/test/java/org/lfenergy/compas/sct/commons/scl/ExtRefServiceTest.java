// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.opentest4j.AssertionFailedError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ExtRefServiceTest {

    @Test
    void updateAllExtRefIedNames_should_succeed() throws Exception {
        // Given : An ExtRef with a matching compas:Flow
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_success.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then : The ExtRef iedName should be updated from "IED_NAME1" to "matched FlowId1"
        assertThat(sclReport).isNotNull();
        assertThat(sclReport.isSuccess()).isTrue();
        List<TExtRef> extRefs = sclReport.getSclRootAdapter()
            .getIEDAdapterByName("IED_NAME1")
            .getLDeviceAdapterByLdInst("LD_INST11").orElseThrow(() -> new AssertionFailedError("LD_INST11 not found"))
            .getLN0Adapter()
            .getExtRefs();
        assertThat(extRefs)
            .hasSize(1)
            .first().hasFieldOrPropertyWithValue("iedName", "matched FlowId1");
    }

    @Test
    void updateAllExtRefIedNames_should_report_errors() throws Exception {
        // Given : see comments in SCD file
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-iedname/scd_set_extref_iedname_with_errors.xml");
        // When
        SclReport sclReport = ExtRefService.updateAllExtRefIedNames(scd);
        // Then : the sclReport should report all errors described in the comments of the SCD file
        assertThat(sclReport).isNotNull();
        assertThat(sclReport.isSuccess()).isFalse();
        assertThat(sclReport.getErrorDescriptionList()).containsExactly(
            SclReport.ErrorDescription.builder()
                .xpath("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                    "/LN0/Inputs/ExtRef[@desc=\"No matching compas:Flow\"]")
                .message("The signal ExtRef has no matching compas:Flow Private").build(),
            SclReport.ErrorDescription.builder()
                .xpath("/SCL/IED[@name=\"IED_NAME1\"]/AccessPoint/Server/LDevice[@inst=\"LD_INST11\"]" +
                    "/LN0/Inputs/ExtRef[@desc=\"Matching two compas:Flow\"]")
                .message("The signal ExtRef has more than one matching compas:Flow Private").build());
    }

}
