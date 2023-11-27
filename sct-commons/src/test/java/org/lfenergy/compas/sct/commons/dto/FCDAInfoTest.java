// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import static org.assertj.core.api.Assertions.assertThat;

class FCDAInfoTest {

    @Test
    @Tag("issue-321")
    void constructor_whenCalled_shouldFillValues(){
        // Given
        String dataSetName = "dataSet";
        String ldInst = "LDInst";
        String prefix = "pre";
        String lnClass = "LN_Class";
        String lnInst = "LNInst";
        DoTypeName doName = new DoTypeName("doName");
        DaTypeName daName = new DaTypeName("daName.bda1.bda2.bda3");
        long ix = 1L;

        // When
        FCDAInfo fcdaInfo = new FCDAInfo(dataSetName, TFCEnum.CF, ldInst, prefix, lnClass, lnInst, doName, daName, ix);
        // Then
        assertThat(fcdaInfo.getDaName().getName()).isEqualTo("daName");
        assertThat(fcdaInfo.getDoName().getName()).isEqualTo("doName");
        assertThat(fcdaInfo.getDaName()).hasToString("daName.bda1.bda2.bda3");
        assertThat(fcdaInfo.getDaName().getStructNames()).hasSize(3);
        assertThat(fcdaInfo.getFc()).isEqualTo(TFCEnum.CF);
        assertThat(fcdaInfo.isValid()).isTrue();
    }

}