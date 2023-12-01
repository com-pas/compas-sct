// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.icd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TCompasIEDRedundancy;
import org.lfenergy.compas.scl2007b4.model.TCompasIEDType;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IcdHeaderTest {

    @Test
    void toTCompasICDHeader_shouldReturnCompasICDHeaderObject() {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());
        //When
        TCompasICDHeader tCompasICDHeader = icdHeader.toTCompasICDHeader();
        //Then
        assertThat(tCompasICDHeader)
                .usingRecursiveComparison()
                .isEqualTo(createHeader());
    }

    @Test
    void getIcdSystemVersionUUID_shouldReturnExpectedValue() {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());
        //When
        String icdSystemVersionUUID = icdHeader.getIcdSystemVersionUUID();
        //Then
        assertThat(icdSystemVersionUUID).isEqualTo("icdSystemVersionUUID");
    }

    @Test
    void toString_shouldReturnExpectedValue() {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());
        //When
        String icdHeaderString = icdHeader.toString();
        //Then
        assertThat(icdHeaderString).isEqualTo("headerId = headerId, headerVersion = headerVersion, headerRevision = headerRevision and ICDSystemVersionUUID = icdSystemVersionUUID");
    }

    @ParameterizedTest
    @MethodSource("provideIcdHeaderForEquals")
    void testEquals(IcdHeader icdHeader2) {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());

        //When
        //Then
        assertThat(icdHeader).isEqualTo(icdHeader2);
    }

    private static Stream<Arguments> provideIcdHeaderForEquals() {
        TCompasICDHeader tCompasICDHeaderModifiedIedSubstationinstance = createHeader();
        tCompasICDHeaderModifiedIedSubstationinstance.setIEDSubstationinstance(BigInteger.TEN);
        TCompasICDHeader tCompasICDHeaderModifiediedSystemVersioninstance = createHeader();
        tCompasICDHeaderModifiediedSystemVersioninstance.setIEDSystemVersioninstance(BigInteger.TEN);
        TCompasICDHeader tCompasICDHeaderModifiediedName = createHeader();
        tCompasICDHeaderModifiediedName.setIEDName("patate");
        TCompasICDHeader tCompasICDHeaderModifiedbayLabel = createHeader();
        tCompasICDHeaderModifiedbayLabel.setBayLabel("patate");
        return Stream.of(
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedIedSubstationinstance)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiediedSystemVersioninstance)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiediedName)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedbayLabel))
        );
    }

    @ParameterizedTest
    @MethodSource("provideIcdHeaderForNotEquals")
    void testEquals_should_not_equals(IcdHeader icdHeader2) {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());
        //When
        //Then
        assertThat(icdHeader).isNotEqualTo(icdHeader2);
    }

    private static Stream<Arguments> provideIcdHeaderForNotEquals() {
        TCompasICDHeader tCompasICDHeaderModifiedicdSystemVersionUUID = createHeader();
        tCompasICDHeaderModifiedicdSystemVersionUUID.setICDSystemVersionUUID("patate");
        TCompasICDHeader tCompasICDHeaderModifiediedType = createHeader();
        tCompasICDHeaderModifiediedType.setIEDType(TCompasIEDType.AUT);
        TCompasICDHeader tCompasICDHeaderModifiedvendorName = createHeader();
        tCompasICDHeaderModifiedvendorName.setVendorName("patate");
        TCompasICDHeader tCompasICDHeaderModifiediedModel = createHeader();
        tCompasICDHeaderModifiediedModel.setIEDmodel("patate");
        TCompasICDHeader tCompasICDHeaderModifiediedRedundancy = createHeader();
        tCompasICDHeaderModifiediedRedundancy.setIEDredundancy(TCompasIEDRedundancy.B);
        TCompasICDHeader tCompasICDHeaderModifiedhwRev = createHeader();
        tCompasICDHeaderModifiedhwRev.setHwRev("patate");
        TCompasICDHeader tCompasICDHeaderModifiedswRev = createHeader();
        tCompasICDHeaderModifiedswRev.setSwRev("patate");
        TCompasICDHeader tCompasICDHeaderModifiedheaderId = createHeader();
        tCompasICDHeaderModifiedheaderId.setHeaderId("patate");
        TCompasICDHeader tCompasICDHeaderModifiedheaderVersion = createHeader();
        tCompasICDHeaderModifiedheaderVersion.setHeaderVersion("patate");
        TCompasICDHeader tCompasICDHeaderModifiedheaderRevision = createHeader();
        tCompasICDHeaderModifiedheaderRevision.setHeaderRevision("patate");
        return Stream.of(
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedicdSystemVersionUUID)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiediedType)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedvendorName)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiediedModel)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiediedRedundancy)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedhwRev)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedswRev)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedheaderId)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedheaderVersion)),
                Arguments.of(new IcdHeader(tCompasICDHeaderModifiedheaderRevision))
        );
    }

    @Test
    void testHashCode() {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());
        IcdHeader icdHeader2 = new IcdHeader(createHeader());
        //When
        //Then
        assertThat(icdHeader).hasSameHashCodeAs(icdHeader2);
    }

    @Test
    void testHashCode_should_not_equals() {
        //Given
        IcdHeader icdHeader = new IcdHeader(createHeader());
        TCompasICDHeader header2 = createHeader();
        header2.setVendorName("patate");
        IcdHeader icdHeader2 = new IcdHeader(header2);
        //When
        //Then
        assertThat(icdHeader).doesNotHaveSameHashCodeAs(icdHeader2);
    }

    private static TCompasICDHeader createHeader() {
        TCompasICDHeader tCompasICDHeader = new TCompasICDHeader();
        tCompasICDHeader.setICDSystemVersionUUID("icdSystemVersionUUID");
        tCompasICDHeader.setIEDType(TCompasIEDType.SCU);
        tCompasICDHeader.setIEDSubstationinstance(BigInteger.ONE);
        tCompasICDHeader.setIEDSystemVersioninstance(BigInteger.ONE);
        tCompasICDHeader.setIEDName("iedName");
        tCompasICDHeader.setVendorName("vendorName");
        tCompasICDHeader.setIEDmodel("iedModel");
        tCompasICDHeader.setIEDredundancy(TCompasIEDRedundancy.A);
        tCompasICDHeader.setBayLabel("bayLabel");
        tCompasICDHeader.setHwRev("hwRev");
        tCompasICDHeader.setSwRev("swRev");
        tCompasICDHeader.setHeaderId("headerId");
        tCompasICDHeader.setHeaderVersion("headerVersion");
        tCompasICDHeader.setHeaderRevision("headerRevision");
        return tCompasICDHeader;
    }
}