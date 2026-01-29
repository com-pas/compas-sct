// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DAITrackerTest {

    @Test
    // Test should be modified to reflect each test case and remove no concerned test and assertions.
    @Tag("issue-321")
    void testInit(){
        // Given
        LN0Adapter ln0Adapter = new LN0Adapter(null,new LN0());
        DoTypeName doTypeName = new DoTypeName();
        DaTypeName daTypeName = new DaTypeName();
        DAITracker daiTracker = new DAITracker(ln0Adapter, doTypeName, daTypeName);
        assertThat(daiTracker.getIndexDoType()).isEqualTo(-2);
        assertThat(daiTracker.getIndexDaType()).isEqualTo(-2);
        assertThat(daiTracker.getBdaiOrDaiAdapter()).isNull();
        assertThat(daiTracker.getDoiOrSdoiAdapter()).isNull();

        assertThat(daiTracker.getLnAdapter()).isNotNull();
        assertThat(daiTracker.getDaTypeName()).isNotNull();
        assertThat(daiTracker.getDoTypeName()).isNotNull();
        //When Then
        assertThatThrownBy(() -> new DAITracker(null, doTypeName, daTypeName))
                .isInstanceOf(NullPointerException.class);
        //When Then
        assertThatThrownBy(() -> new DAITracker(ln0Adapter,null, daTypeName))
                .isInstanceOf(NullPointerException.class);
        //When Then
        assertThatThrownBy(() -> new DAITracker(ln0Adapter, doTypeName,null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @Tag("issue-321")
    void testSearch() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").orElseThrow());

        // Given
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        DAITracker daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);

        // When
        DAITracker.MatchResult matchResult = daiTracker.search();
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.FULL_MATCH);

        // When Then
        lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS2").isPresent() ? iAdapter.findLDeviceAdapterByLdInst("LD_INS2").get() : null);

        // Given
        lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        // When
        matchResult = daiTracker.search();
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.FAILED);

        // Given
        lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass("ANCR")
                .withLnInst("1")
                .build();
        doTypeName = new DoTypeName("StrVal");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        // When
        matchResult = daiTracker.search();
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.PARTIAL_MATCH);

        // When Then
        lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LD_INS3").isPresent() ? iAdapter.findLDeviceAdapterByLdInst("LD_INS3").get() : null);

        // Given
        lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        doTypeName = new DoTypeName("Do.sdo1.d");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        // When
        matchResult = daiTracker.search();
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.PARTIAL_MATCH);

        // Given
        doTypeName = new DoTypeName("Do");
        daTypeName = new DaTypeName("da3");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        // When
        matchResult = daiTracker.search();
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.PARTIAL_MATCH);

        // Given
        doTypeName = new DoTypeName("Do");
        daTypeName = new DaTypeName("da2");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        // When
        matchResult = daiTracker.search();
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.FULL_MATCH);
    }

    @Test
    @Tag("issue-321")
    // Useless test
    void testMatchResult() {
        // Given
        DAITracker.MatchResult matchResult = DAITracker.MatchResult.FAILED;
        // Then
        assertThat(matchResult).hasToString("FAILED");

        // Given
        matchResult = DAITracker.MatchResult.fromValue("FULL_MATCH");
        // Then
        assertThat(matchResult).isEqualTo(DAITracker.MatchResult.FULL_MATCH);
        assertThat(DAITracker.MatchResult.fromValue("DUMMY")).isNull();
    }

    @Test
    @Tag("issue-321")
    void testValidateBoundedDAI() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("ied-test-schema-conf/scd_with_dai_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        // When Then
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        // When Then
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.findLDeviceAdapterByLdInst("LDSUIED").isPresent() ? iAdapter.findLDeviceAdapterByLdInst("LDSUIED").get() : null);
        // Given
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        DoTypeName doTypeName = new DoTypeName("StrVal1");
        DaTypeName daTypeName = new DaTypeName("setMag.f");

        DAITracker daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        // When Then
        assertThatCode(daiTracker::validateBoundedDAI).doesNotThrowAnyException();

        doTypeName.setCdc(TPredefinedCDCEnum.ING);
        // When Then
        assertThatThrownBy(daiTracker::validateBoundedDAI)
                .isInstanceOf(ScdException.class);

        daTypeName.addDaiValue(0L,"80.78");
        // When Then
        assertThatCode(daiTracker::validateBoundedDAI).doesNotThrowAnyException();

        daTypeName.addDaiValue(0L,"45.9");
        // When Then
        assertThatThrownBy(daiTracker::validateBoundedDAI)
                .isInstanceOf(ScdException.class);

        daTypeName.addDaiValue(0L,"1000");
        // When Then
        assertThatThrownBy( daiTracker::validateBoundedDAI)
                .isInstanceOf(ScdException.class);
    }

    @Test
    @Tag("issue-321")
    void testGetDaiNumericValue() {
        // Given
        DaTypeName daTypeName = new DaTypeName("setMag.f");
        DoTypeName doTypeName = new DoTypeName("StrVal1");
        DAITracker daiTracker = new DAITracker(new LN0Adapter(null, new LN0()),doTypeName,daTypeName);

        // When Then
        double val = assertDoesNotThrow( ()-> daiTracker.getDaiNumericValue(daTypeName,13.0));
        assertThat(val).isEqualTo(13.0);

        // Given
        daTypeName.addDaiValue(0L,"45.9");
        // When Then
        assertThatThrownBy(()-> daiTracker.getDaiNumericValue(daTypeName,13.0))
                .isInstanceOf(NumberFormatException.class);

        // Given
        daTypeName.setBType(TPredefinedBasicTypeEnum.DBPOS);
        // When Then
        assertThatThrownBy(()-> daiTracker.getDaiNumericValue(daTypeName,13.0))
                .isInstanceOf(NumberFormatException.class);

        // Given
        daTypeName.setBType(TPredefinedBasicTypeEnum.FLOAT_32);
        // When Then
        assertThatCode(()-> daiTracker.getDaiNumericValue(daTypeName,13.0)).doesNotThrowAnyException();
    }

}
