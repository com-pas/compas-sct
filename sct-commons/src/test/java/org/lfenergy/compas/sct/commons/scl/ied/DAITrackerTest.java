// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.LN0;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import static org.junit.jupiter.api.Assertions.*;

class DAITrackerTest {

    @Test
    void testInit(){
        LN0Adapter ln0Adapter = new LN0Adapter(null,new LN0());
        DAITracker daiTracker = new DAITracker(ln0Adapter,new DoTypeName(),new DaTypeName());
        assertEquals(-2, daiTracker.getIndexDoType());
        assertEquals(-2, daiTracker.getIndexDaType());
        assertEquals(null, daiTracker.getBdaiOrDaiAdapter());
        assertEquals(null, daiTracker.getDoiOrSdoiAdapter());

        assertNotNull(daiTracker.getLnAdapter());
        assertNotNull(daiTracker.getDaTypeName());
        assertNotNull(daiTracker.getDoTypeName());

        assertThrows(
                NullPointerException.class,
                () -> new DAITracker(null,new DoTypeName(),new DaTypeName())
        );

        assertThrows(
                NullPointerException.class,
                () -> new DAITracker(new LN0Adapter(null,new LN0()),null,new DaTypeName())
        );
        assertThrows(
                NullPointerException.class,
                () -> new DAITracker(new LN0Adapter(null,new LN0()),new DoTypeName(),null)
        );
    }

    @Test
    void testSearch() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS1").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");

        DAITracker daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);

        DAITracker.MatchResult matchResult = daiTracker.Search();
        assertEquals(DAITracker.MatchResult.FULL_MATCH,matchResult);

        lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        matchResult = daiTracker.Search();
        assertEquals(DAITracker.MatchResult.FAILED,matchResult);

        lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass("ANCR")
                .withLnInst("1")
                .build();

        doTypeName = new DoTypeName("StrVal");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        matchResult = daiTracker.Search();
        assertEquals(DAITracker.MatchResult.PARTIAL_MATCH,matchResult);

        lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS3").get());
        lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        doTypeName = new DoTypeName("Do.sdo1.d");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        matchResult = daiTracker.Search();
        assertEquals(DAITracker.MatchResult.PARTIAL_MATCH,matchResult);

        doTypeName = new DoTypeName("Do");
        daTypeName = new DaTypeName("da3");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        matchResult = daiTracker.Search();
        assertEquals(DAITracker.MatchResult.PARTIAL_MATCH,matchResult);



        doTypeName = new DoTypeName("Do");
        daTypeName = new DaTypeName("da2");
        daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        matchResult = daiTracker.Search();
        assertEquals(DAITracker.MatchResult.FULL_MATCH,matchResult);
    }

    @Test
    void testMatchResult(){
        DAITracker.MatchResult matchResult = DAITracker.MatchResult.FAILED;
        assertEquals("FAILED",matchResult.toString());

        matchResult = DAITracker.MatchResult.fromValue("FULL_MATCH");
        assertEquals(DAITracker.MatchResult.FULL_MATCH, matchResult);

        assertNull(DAITracker.MatchResult.fromValue("DUMMY"));
    }

    @Test
    void testValidateBoundedDAI() throws Exception {

        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/scd_with_dai_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LDSUIED").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        DoTypeName doTypeName = new DoTypeName("StrVal1");
        DaTypeName daTypeName = new DaTypeName("setMag.f");

        DAITracker daiTracker = new DAITracker(lnAdapter,doTypeName,daTypeName);
        assertDoesNotThrow(() -> daiTracker.validateBoundedDAI());

        doTypeName.setCdc(TPredefinedCDCEnum.ING);
        assertThrows(ScdException.class, () -> daiTracker.validateBoundedDAI());

        daTypeName.addDaiValue(0L,"80.78");
        assertDoesNotThrow(() -> daiTracker.validateBoundedDAI());

        daTypeName.addDaiValue(0L,"45.9");
        assertThrows(ScdException.class, () -> daiTracker.validateBoundedDAI());

        daTypeName.addDaiValue(0L,"1000");
        assertThrows(ScdException.class, () -> daiTracker.validateBoundedDAI());
    }

    @Test
    void testGetDaiNumericValue() {
        DaTypeName daTypeName = new DaTypeName("setMag.f");
        DoTypeName doTypeName = new DoTypeName("StrVal1");
        DAITracker daiTracker = new DAITracker(new LN0Adapter(null, new LN0()),doTypeName,daTypeName);

        double val = assertDoesNotThrow( ()->daiTracker.getDaiNumericValue(daTypeName,13.0));
        assertEquals(13.0,val);

        daTypeName.addDaiValue(0L,"45.9");
        assertThrows(NumberFormatException.class, ()->daiTracker.getDaiNumericValue(daTypeName,13.0));

        daTypeName.setBType(TPredefinedBasicTypeEnum.DBPOS);
        assertThrows(NumberFormatException.class, ()->daiTracker.getDaiNumericValue(daTypeName,13.0));

        daTypeName.setBType(TPredefinedBasicTypeEnum.FLOAT_32);
        assertDoesNotThrow(()->daiTracker.getDaiNumericValue(daTypeName,13.0));
    }
}