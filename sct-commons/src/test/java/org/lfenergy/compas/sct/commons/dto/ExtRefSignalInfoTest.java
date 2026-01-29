// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

import static org.assertj.core.api.Assertions.assertThat;

class ExtRefSignalInfoTest {

    @Test
    void constructor_whenCalled_shouldFillValues() {
        // When
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        // Then
        assertThat(signalInfo.getDesc()).isEqualTo(DTO.DESC);
        assertThat(signalInfo.getPDA()).isEqualTo(DTO.P_DA);
        assertThat(signalInfo.getPDO()).isEqualTo(DTO.P_DO);
        assertThat(signalInfo.getPLN()).isEqualTo(DTO.P_LN);
        assertThat(signalInfo.getIntAddr()).isEqualTo(DTO.INT_ADDR);
        assertThat(signalInfo.getPServT()).isEqualTo(TServiceType.fromValue(DTO.P_SERV_T));
    }

    @Test
    void initExtRef_whenCalledWithExtRefSignalInfo_shouldFillValues(){
        // When
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        TExtRef extRef = ExtRefSignalInfo.initExtRef(signalInfo);
        // Then
        assertThat(signalInfo.getDesc()).isEqualTo(DTO.DESC);
        assertThat(signalInfo.getPDA()).isEqualTo(DTO.P_DA);
        assertThat(signalInfo.getPDO()).isEqualTo(DTO.P_DO);
        assertThat(extRef.getPLN()).asInstanceOf(InstanceOfAssertFactories.LIST).contains(DTO.P_LN);
        assertThat(signalInfo.getIntAddr()).isEqualTo(DTO.INT_ADDR);
        assertThat(signalInfo.getPServT()).isEqualTo(TServiceType.fromValue(DTO.P_SERV_T));
    }

    @Test
    @Tag("issue-321")
    void testIsWrappedIn(){
        // Given
        TExtRef tExtRef = DTO.createExtRef();
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo(tExtRef);
        ExtRefSignalInfo signalInfo1 = new ExtRefSignalInfo();
        // When Then
        assertThat(signalInfo.isWrappedIn(tExtRef)).isTrue();
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isFalse();

        signalInfo1.setDesc(signalInfo.getDesc());
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isFalse();

        signalInfo1.setPDA(signalInfo.getPDA());
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isFalse();

        signalInfo1.setPDO(signalInfo.getPDO());
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isFalse();

        signalInfo1.setIntAddr(signalInfo.getIntAddr());
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isFalse();

        signalInfo1.setPLN(signalInfo.getPLN());
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isFalse();

        signalInfo1.setPServT(signalInfo.getPServT());
        // When Then
        assertThat(signalInfo1.isWrappedIn(tExtRef)).isTrue();
    }

    @Test
    @Tag("issue-321")
    void testIsValid(){
        // Given
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();
        assertThat(signalInfo.isValid()).isTrue();
        signalInfo.setIntAddr("");
        // When
        assertThat(signalInfo.isValid()).isFalse();
        signalInfo.setPDO("");
        // When
        assertThat(signalInfo.isValid()).isFalse();

    }

}
