// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ReportControlBlock;

import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.SclTestMarshaller;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LNAdapterTest {

    @Test
    void testAmChildElementRef() {
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);

        assertEquals(TLN.class,lnAdapter.getElementClassType());
        assertEquals(DTO.LN_TYPE,lnAdapter.getLnType());
        assertEquals(DTO.LN_CLASS,lnAdapter.getLNClass());
        assertFalse(lnAdapter.hasInputs());
        tln.setInputs(new TInputs());
        assertTrue(lnAdapter.hasInputs());
        assertFalse(lnAdapter.isLN0());

        assertEquals(DTO.LN_INST,lnAdapter.getLNInst());
        assertEquals(DTO.PREFIX,lnAdapter.getPrefix());
        assertTrue(lnAdapter.getCurrentElem().getReportControl().isEmpty());

        lnAdapter.addControlBlock(new ReportControlBlock());
        assertFalse(lnAdapter.getCurrentElem().getReportControl().isEmpty());

        assertThrows(IllegalArgumentException.class,  () -> new LNAdapter(lnAdapter.getParentAdapter(),new TLN()));
    }

    @Test
    void testGetExtRefs(){
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);

        assertTrue(lnAdapter.getExtRefs(null).isEmpty());
        TInputs tInputs = new TInputs();
        TExtRef extRef = DTO.createExtRef();
        ExtRefSignalInfo extRefSignalInfo = new ExtRefSignalInfo(extRef);
        tInputs.getExtRef().add(DTO.createExtRef());
        tln.setInputs(tInputs);
        assertEquals(1,lnAdapter.getExtRefs(null).size());
        assertEquals(1,lnAdapter.getExtRefs(extRefSignalInfo).size());
    }

    @Test
    void testFindDataSetByRef(){
        TLN tln = new TLN();
        LNAdapter lnAdapter = initLNAdapter(tln);

        assertTrue(lnAdapter.findDataSetByRef(DTO.CB_DATASET_REF).isEmpty());
        TDataSet tDataSet = new TDataSet();
        tDataSet.setName(DTO.CB_DATASET_REF);
        tln.getDataSet().add(tDataSet);

        assertTrue(lnAdapter.findDataSetByRef(DTO.CB_DATASET_REF).isPresent());

    }

    @Test
    void testUpdateExtRefBinders() {
        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        TExtRef extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());

        assertNull(extRef.getIedName());

        LNAdapter lnAdapter = initLNAdapter(new TLN());
        extRefInfo.getBindingInfo().setServiceType(null);
        lnAdapter.updateExtRefBindingInfo(extRef,extRefInfo);
        assertEquals(extRefInfo.getBindingInfo().getServiceType(),extRefInfo.getSignalInfo().getPServT());
        assertEquals(extRefInfo.getBindingInfo().getIedName(),extRef.getIedName());
        assertEquals(extRefInfo.getSourceInfo().getSrcLDInst(), extRef.getSrcLDInst());

        extRefInfo.setBindingInfo(null);
        extRefInfo.setSourceInfo(null);
        extRef = ExtRefSignalInfo.initExtRef(extRefInfo.getSignalInfo());
        assertNull(extRef.getIedName());
        lnAdapter.updateExtRefBindingInfo(extRef,extRefInfo);
        assertNull(extRef.getIedName());
        assertNull(extRef.getSrcLDInst());
    }

    @Test
    void testUpdateExtRefBindingInfo() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapter("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        LNAdapter lnAdapter = (LNAdapter) AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass("ANCR")
                .withLnInst("1")
                .build();

        ExtRefInfo info = DTO.createExtRefInfo();
        assertThrows(ScdException.class, () -> lnAdapter.updateExtRefBinders(Set.of(info)));

        info.getSignalInfo().setPDO("StrVal.sdo2");
        info.getSignalInfo().setPDA("antRef.bda1.bda2.bda3");
        info.getSignalInfo().setIntAddr("INT_ADDR2");
        info.getSignalInfo().setDesc(null);
        info.getSignalInfo().setPServT(null);
        assertDoesNotThrow(() -> lnAdapter.updateExtRefBinders(Set.of(info)));
        List<TExtRef> tExtRefs = lnAdapter.getExtRefs(null);
        assertEquals(1,tExtRefs.size());
        TExtRef extRef = tExtRefs.get(0);

        assertEquals(info.getBindingInfo().getIedName(),extRef.getIedName());

    }

    private LNAdapter initLNAdapter(TLN tln){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        tln.getLnClass().add(DTO.LN_CLASS);
        tln.setInst(DTO.LN_INST);
        tln.setLnType(DTO.LN_TYPE);
        tln.setPrefix(DTO.PREFIX);
        Mockito.when(tlDevice.getLN()).thenReturn(List.of(tln));
        return assertDoesNotThrow( () -> new LNAdapter(lDeviceAdapter,tln));
    }
}