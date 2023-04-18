// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.testhelpers.FCDARecord;

import static org.assertj.core.api.Assertions.assertThat;

class FCDARecordTest {

    @Test
    void from_TFCDA_should_create_instance_with_same_attributes() {
        //Given
        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst("ldInst");
        tfcda.getLnClass().add("lnClass");
        tfcda.setLnInst("lnInst");
        tfcda.setPrefix("prefix");
        tfcda.setDoName("DoName.sdo");
        tfcda.setDaName("daName.bda");
        tfcda.setFc(TFCEnum.ST);
        //When
        FCDARecord fcdaRecord = FCDARecord.toFCDARecord(tfcda);
        //Then
        assertThat(fcdaRecord).isEqualTo(
                new FCDARecord("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "daName.bda", TFCEnum.ST));
    }

}
