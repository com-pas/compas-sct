// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ExtRefInfoTest {

    @Test
    @Tag("issue-321")
    void constructor_whenCalled_shouldFillValues(){
        // When
        ExtRefInfo extRefInfo = createExtRef();

        // Then
        assertThat(extRefInfo.getSignalInfo().getDesc()).isEqualTo(DTO.DESC);
        assertThat(extRefInfo.getSignalInfo().getPDA()).isEqualTo(DTO.P_DA);
        assertThat(extRefInfo.getSignalInfo().getPDO()).isEqualTo(DTO.P_DO);
        assertThat(extRefInfo.getSignalInfo().getPLN()).contains(DTO.P_LN);
        assertThat(extRefInfo.getSignalInfo().getIntAddr()).isEqualTo(DTO.INT_ADDR);
        assertThat(extRefInfo.getSignalInfo().getPServT()).isEqualTo(TServiceType.fromValue(DTO.P_SERV_T));
        assertThat(extRefInfo.getBindingInfo()).isNotNull();
        assertThat(extRefInfo.getSourceInfo()).isNotNull();

        // Given
        TExtRef extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());
        // When
        ExtRefInfo extRefInfo1 = new ExtRefInfo(extRef);
        // Then
        assertThat(extRefInfo1.getBindingInfo()).isNotNull();
        assertThat(extRefInfo1.getSourceInfo()).isNotNull();
    }

    private ExtRefInfo createExtRef(){
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setSignalInfo(DTO.createExtRefSignalInfo());
        extRefInfo.setBindingInfo(DTO.createExtRefBindingInfo_Remote());
        extRefInfo.setSourceInfo(DTO.createExtRefSourceInfo());

        return extRefInfo;
    }

    @Test
    void checkMatchingFCDA_whenContentMatch_should_return_True() {
        //Given
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setBindingInfo(DTO.createExtRefBindingInfo_Remote());
        extRefInfo.setSignalInfo(DTO.createExtRefSignalInfo());
        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst(DTO.createExtRefBindingInfo_Remote().getLdInst());
        tfcda.getLnClass().add(DTO.createExtRefBindingInfo_Remote().getLnClass());
        tfcda.setLnInst(DTO.createExtRefBindingInfo_Remote().getLnInst());
        tfcda.setPrefix(DTO.createExtRefBindingInfo_Remote().getPrefix());
        tfcda.setDoName(DTO.createExtRefSignalInfo().getPDO());
        tfcda.setDaName(DTO.createExtRefSignalInfo().getPDA());

        //When Then
        assertThat(extRefInfo.checkMatchingFCDA(tfcda)).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideIncompleteExtRefInfo")
    void checkMatchingFCDA_whenCalled_should_return_false(String testCase, TFCDA fcda, ExtRefBindingInfo bindingInfo, ExtRefSignalInfo signalInfo) {
        //Given
        ExtRefInfo extRefInfo = new ExtRefInfo();
        extRefInfo.setBindingInfo(bindingInfo);
        extRefInfo.setSignalInfo(signalInfo);
        //When Then
        assertThat(extRefInfo.checkMatchingFCDA(fcda)).isFalse();
    }


    private static Stream<Arguments> provideIncompleteExtRefInfo() {

        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();

        ExtRefBindingInfo extRefBindingInfoLDInstNotMatch = DTO.createExtRefBindingInfo_Remote();
        extRefBindingInfoLDInstNotMatch.setLdInst("ldInst");

        ExtRefBindingInfo extRefBindingInfoLNClassNotMatch = DTO.createExtRefBindingInfo_Remote();
        extRefBindingInfoLNClassNotMatch.setLnClass("lnClass");

        ExtRefBindingInfo xtRefBindingInfoLNInstNotMatch = DTO.createExtRefBindingInfo_Remote();
        xtRefBindingInfoLNInstNotMatch.setLnInst("5");

        ExtRefBindingInfo ExtRefBindingInfoPrefixNotMatch = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfoPrefixNotMatch.setPrefix("Pref");

        ExtRefSignalInfo extRefSignalInfoDOnotMatch = DTO.createExtRefSignalInfo();
        extRefSignalInfoDOnotMatch.setPDO("do");

        ExtRefSignalInfo extRefSignalInfoDAnotMatch = DTO.createExtRefSignalInfo();
        extRefSignalInfoDAnotMatch.setPDA("da");

        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst(DTO.createExtRefBindingInfo_Remote().getLdInst());
        tfcda.getLnClass().add(DTO.createExtRefBindingInfo_Remote().getLnClass());
        tfcda.setLnInst(DTO.createExtRefBindingInfo_Remote().getLnInst());
        tfcda.setPrefix(DTO.createExtRefBindingInfo_Remote().getPrefix());
        tfcda.setDoName(DTO.createExtRefSignalInfo().getPDO());
        tfcda.setDaName(DTO.createExtRefSignalInfo().getPDA());

        return Stream.of(
                Arguments.of("return false when binding and signal are null ", tfcda, null, null),
                Arguments.of("return false when only signal is null ", tfcda, bindingInfo, null),
                Arguments.of("return false when only binding is null ", tfcda, null, signalInfo),
                Arguments.of("return false when ldInst not match ", tfcda, extRefBindingInfoLDInstNotMatch, signalInfo),
                Arguments.of("return false when lnClass not match ", tfcda, extRefBindingInfoLNClassNotMatch, signalInfo),
                Arguments.of("return false when lNInst not match ", tfcda, xtRefBindingInfoLNInstNotMatch, signalInfo),
                Arguments.of("return false when prefix not match ", tfcda, ExtRefBindingInfoPrefixNotMatch, signalInfo),
                Arguments.of("return false when pDO not match ",tfcda, bindingInfo, extRefSignalInfoDOnotMatch),
                Arguments.of("return false when pDA not match ",tfcda, bindingInfo, extRefSignalInfoDAnotMatch)
        );
    }
}