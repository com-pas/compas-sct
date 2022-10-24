// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FCDAInfoTest {

    @Test
    void testConstructor(){

        TFCDA tfcda = new TFCDA();
        tfcda.setDoName("doName");
        tfcda.setDaName("daName.bda1.bda2.bda3");
        tfcda.setLdInst("LDInst");
        tfcda.setFc(TFCEnum.CF);
        tfcda.getLnClass().add("LN_Class");
        tfcda.setLnInst("LNInst");
        tfcda.setPrefix("pre");
        tfcda.setIx(1L);

        FCDAInfo fcdaInfo = new FCDAInfo("dataSet",tfcda);
        assertEquals("daName",fcdaInfo.getDaName().getName());
        assertEquals("doName",fcdaInfo.getDoName().getName());
        assertEquals(tfcda.getDaName(),fcdaInfo.getDaName().toString());
        assertEquals(3,fcdaInfo.getDaName().getStructNames().size());

        FCDAInfo fcdaInfo1 = new FCDAInfo();
        fcdaInfo1.setIx(fcdaInfo.getIx());
        fcdaInfo1.setLdInst(fcdaInfo.getLdInst());
        fcdaInfo1.setLnInst(fcdaInfo.getLnInst());
        fcdaInfo1.setLnClass(fcdaInfo.getLnClass());
        fcdaInfo1.setDaName(fcdaInfo.getDaName());
        fcdaInfo1.setDoName(fcdaInfo.getDoName());
        fcdaInfo1.setFc(fcdaInfo.getFc());
        fcdaInfo1.setPrefix(fcdaInfo.getPrefix());
        assertEquals("daName",fcdaInfo1.getDaName().getName());
        assertEquals("doName",fcdaInfo1.getDoName().getName());
        assertEquals(tfcda.getDaName(),fcdaInfo1.getDaName().toString());

        TFCDA tfcda1 = fcdaInfo1.getFCDA();

        assertEquals(tfcda1.getFc(),fcdaInfo1.getFc());

        assertTrue(fcdaInfo1.isValid());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providerFCDAInfoObjects")
    void checkFCDACompatibilitiesForBinding(String testCase, FCDAInfo fcdaInfo, FCDAInfo expectedFcdaInfo) {
        assertThat(fcdaInfo.checkFCDACompatibilitiesForBinding(expectedFcdaInfo)).isFalse();
    }

    @Test
    void checkFCDACompatibilitiesForBinding_shouldReturnTrue_whensameContent() {
        //Given
        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst(DTO.createExtRefBindingInfo_Remote().getLdInst());
        tfcda.getLnClass().add(DTO.createExtRefBindingInfo_Remote().getLnClass());
        tfcda.setLnInst(DTO.createExtRefBindingInfo_Remote().getLnInst());
        tfcda.setDoName(DTO.createExtRefSignalInfo().getPDO());
        tfcda.setDaName(DTO.createExtRefSignalInfo().getPDA());

        FCDAInfo fcdaInfo = new FCDAInfo(tfcda);
        FCDAInfo expectedFcdaInfo = new FCDAInfo(tfcda);
        //When Then
        assertThat(fcdaInfo.checkFCDACompatibilitiesForBinding(expectedFcdaInfo)).isTrue();
    }

    private static Stream<Arguments> providerFCDAInfoObjects(){

        TFCDA tfcda = new TFCDA();
        tfcda.setLdInst(DTO.createExtRefBindingInfo_Remote().getLdInst());
        tfcda.getLnClass().add(DTO.createExtRefBindingInfo_Remote().getLnClass());
        tfcda.setLnInst(DTO.createExtRefBindingInfo_Remote().getLnInst());
        tfcda.setDoName(DTO.createExtRefSignalInfo().getPDO());
        tfcda.setDaName(DTO.createExtRefSignalInfo().getPDA());

        FCDAInfo fcdaInfo = new FCDAInfo(tfcda);

        FCDAInfo expectedFcdaInfo = new FCDAInfo(tfcda);

        FCDAInfo expectedLDInstNotMatch = new FCDAInfo(tfcda);
        expectedLDInstNotMatch.setLdInst("LDCMDDJ");

        FCDAInfo expectedLNClassNotMatch = new FCDAInfo(tfcda);
        expectedLNClassNotMatch.setLnClass("CSWI");

        FCDAInfo expectedLNInstNotMatch = new FCDAInfo(tfcda);
        expectedLNInstNotMatch.setLnInst("5");

        FCDAInfo expectedPrefixNotMatch = new FCDAInfo(tfcda);
        expectedPrefixNotMatch.setPrefix("Prefix");

        FCDAInfo expectedDONotMatch = new FCDAInfo(tfcda);
        expectedDONotMatch.setDoName(new DoTypeName("do.do1"));

        FCDAInfo expectedDANotMatch = new FCDAInfo(tfcda);
        expectedDANotMatch.setDaName(new DaTypeName("da.bda.bda1"));

        return Stream.of(
                Arguments.of("return false when LdInst not match ", fcdaInfo, expectedLDInstNotMatch),
                Arguments.of("return false when lnClass not match ", fcdaInfo, expectedLNClassNotMatch),
                Arguments.of("return false when lnInst not match ", fcdaInfo, expectedLNInstNotMatch),
                Arguments.of("return false when Prefix not match ", fcdaInfo, expectedPrefixNotMatch),
                Arguments.of("return false when DO not match ", fcdaInfo, expectedDONotMatch),
                Arguments.of("return false when DO not match ", fcdaInfo, expectedDANotMatch)

        );
    }
}