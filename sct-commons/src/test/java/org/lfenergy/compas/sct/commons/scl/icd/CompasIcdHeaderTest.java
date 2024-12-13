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

class CompasIcdHeaderTest {

    @Test
    void toTCompasICDHeader_shouldReturnCompasICDHeaderObject() {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());
        //When
        TCompasICDHeader tCompasICDHeader = compasIcdHeader.toTCompasICDHeader();
        //Then
        assertThat(tCompasICDHeader)
                .usingRecursiveComparison()
                .isEqualTo(createHeader());
    }

    @Test
    void getIcdSystemVersionUUID_shouldReturnExpectedValue() {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());
        //When
        String icdSystemVersionUUID = compasIcdHeader.getIcdSystemVersionUUID();
        //Then
        assertThat(icdSystemVersionUUID).isEqualTo("icdSystemVersionUUID");
    }

    @Test
    void toString_shouldReturnExpectedValue() {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());
        //When
        String icdHeaderString = compasIcdHeader.toString();
        //Then
        assertThat(icdHeaderString).isEqualTo("headerId = headerId, headerVersion = headerVersion, headerRevision = headerRevision and ICDSystemVersionUUID = icdSystemVersionUUID");
    }

    @ParameterizedTest
    @MethodSource("provideIcdHeaderForEquals")
    void testEquals(CompasIcdHeader compasIcdHeader2) {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());

        //When
        //Then
        assertThat(compasIcdHeader).isEqualTo(compasIcdHeader2);
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
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedIedSubstationinstance)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiediedSystemVersioninstance)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiediedName)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedbayLabel))
        );
    }

    @ParameterizedTest
    @MethodSource("provideIcdHeaderForNotEquals")
    void testEquals_should_not_equals(CompasIcdHeader compasIcdHeader2) {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());
        //When
        //Then
        assertThat(compasIcdHeader).isNotEqualTo(compasIcdHeader2);
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
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedicdSystemVersionUUID)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiediedType)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedvendorName)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiediedModel)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiediedRedundancy)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedhwRev)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedswRev)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedheaderId)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedheaderVersion)),
                Arguments.of(new CompasIcdHeader(tCompasICDHeaderModifiedheaderRevision))
        );
    }

    @Test
    void testHashCode() {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());
        CompasIcdHeader compasIcdHeader2 = new CompasIcdHeader(createHeader());
        //When
        //Then
        assertThat(compasIcdHeader).hasSameHashCodeAs(compasIcdHeader2);
    }

    @Test
    void testHashCode_should_not_equals() {
        //Given
        CompasIcdHeader compasIcdHeader = new CompasIcdHeader(createHeader());
        TCompasICDHeader header2 = createHeader();
        header2.setVendorName("patate");
        CompasIcdHeader compasIcdHeader2 = new CompasIcdHeader(header2);
        //When
        //Then
        assertThat(compasIcdHeader).doesNotHaveSameHashCodeAs(compasIcdHeader2);
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