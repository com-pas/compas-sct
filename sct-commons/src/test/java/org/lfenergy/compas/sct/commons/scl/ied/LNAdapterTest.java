// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDataSet;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.scl2007b4.model.TVal;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSourceInfo;
import org.lfenergy.compas.sct.commons.dto.ReportControlBlock;

import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.AbstractDTTLevel;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
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
        assertEquals(DTO.HOLDER_LN_CLASS,lnAdapter.getLNClass());
        assertFalse(lnAdapter.hasInputs());
        tln.setInputs(new TInputs());
        assertTrue(lnAdapter.hasInputs());
        assertFalse(lnAdapter.isLN0());

        assertEquals(DTO.HOLDER_LN_INST,lnAdapter.getLNInst());
        assertEquals(DTO.HOLDER_LN_PREFIX,lnAdapter.getPrefix());
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


    @Test
    void checkExtRefInfoCoherence() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapter("IED_NAME1"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INST11").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        assertThrows(IllegalArgumentException.class, () ->lnAdapter.checkExtRefInfoCoherence(new ExtRefInfo()));


        ExtRefInfo extRefInfo = DTO.createExtRefInfo();
        extRefInfo.setBindingInfo(null);
        extRefInfo.setSourceInfo(null);
        assertThrows(ScdException.class, () ->lnAdapter.checkExtRefInfoCoherence(extRefInfo));

        extRefInfo.getSignalInfo().setDesc(null);
        extRefInfo.getSignalInfo().setPLN(null);
        extRefInfo.getSignalInfo().setPServT(null);
        extRefInfo.getSignalInfo().setPDA("da11.bda111.bda112.bda113");
        extRefInfo.getSignalInfo().setPDO("Do11.sdo11");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR11");

        TExtRef extRef = assertDoesNotThrow(() ->lnAdapter.checkExtRefInfoCoherence(extRefInfo));
        assertEquals(extRefInfo.getSignalInfo().getPDO(),extRef.getPDO());

        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo();
        extRefInfo.setBindingInfo(bindingInfo);
        assertThrows(ScdException.class, () ->lnAdapter.checkExtRefInfoCoherence(extRefInfo));

        bindingInfo.setServiceType(null);
        bindingInfo.setIedName("IED_NAME1");
        bindingInfo.setLdInst("LD_INST12");
        bindingInfo.setLnInst("1");
        bindingInfo.setLnClass("ANCR");
        bindingInfo.setPrefix("PR");
        extRef = assertDoesNotThrow(() ->lnAdapter.checkExtRefInfoCoherence(extRefInfo));
        assertEquals(extRefInfo.getBindingInfo().getIedName(),extRef.getIedName());

        ExtRefSourceInfo sourceInfo = new ExtRefSourceInfo();
        sourceInfo.setSrcCBName("UNKNOWN_CB");
        extRefInfo.setSourceInfo(sourceInfo);
        assertThrows(ScdException.class, () ->lnAdapter.checkExtRefInfoCoherence(extRefInfo));

        sourceInfo.setSrcCBName("rpt1");
        assertDoesNotThrow(() ->lnAdapter.checkExtRefInfoCoherence(extRefInfo));
    }

    @Test
    void testUpdateExtRefSource() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-cb/scd_get_cbs_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapter("IED_NAME1"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INST11").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        ExtRefInfo extRefInfo = new ExtRefInfo();
        assertThrows(IllegalArgumentException.class, () ->lnAdapter.updateExtRefSource(extRefInfo));
        extRefInfo.setSignalInfo(new ExtRefSignalInfo());
        assertThrows(IllegalArgumentException.class, () ->lnAdapter.updateExtRefSource(extRefInfo));
        extRefInfo.setBindingInfo(new ExtRefBindingInfo());
        assertThrows(IllegalArgumentException.class, () ->lnAdapter.updateExtRefSource(extRefInfo));

        extRefInfo.getSignalInfo().setPDA("da11.bda111.bda112.bda113");
        extRefInfo.getSignalInfo().setPDO("Do11.sdo11");
        extRefInfo.getSignalInfo().setIntAddr("INT_ADDR11");
        extRefInfo.getBindingInfo().setIedName("IED_NAME1");
        extRefInfo.getBindingInfo().setLdInst("LD_INST12");
        extRefInfo.getBindingInfo().setLnInst("1");
        extRefInfo.getBindingInfo().setLnClass("ANCR");
        extRefInfo.getBindingInfo().setPrefix("PR");
        extRefInfo.setSourceInfo(new ExtRefSourceInfo());
        extRefInfo.getSourceInfo().setSrcCBName("rpt1");
        extRefInfo.getSourceInfo().setSrcLDInst(extRefInfo.getBindingInfo().getLdInst());
        extRefInfo.getSourceInfo().setSrcLNInst(extRefInfo.getBindingInfo().getLnInst());
        extRefInfo.getSourceInfo().setSrcLNClass(extRefInfo.getBindingInfo().getLnClass());
        extRefInfo.getSourceInfo().setSrcPrefix(extRefInfo.getBindingInfo().getPrefix());

        TExtRef extRef = assertDoesNotThrow( () ->lnAdapter.updateExtRefSource(extRefInfo));
        assertEquals(extRefInfo.getSourceInfo().getSrcCBName(), extRef.getSrcCBName());
        assertEquals(extRefInfo.getSourceInfo().getSrcLDInst(), extRef.getSrcLDInst());
        assertTrue(extRef.getLnClass().contains(extRefInfo.getSourceInfo().getSrcLNClass()) );

    }

    @Test
    void testAddDOI(){
        TLN tln = new TLN();
        tln.getLnClass().add(DTO.HOLDER_LN_CLASS);
        tln.setPrefix(DTO.HOLDER_LN_PREFIX);
        tln.setInst(DTO.HOLDER_LN_INST);
        LNAdapter lnAdapter = new LNAdapter(null,tln);

        DOIAdapter doiAdapter = lnAdapter.addDOI("Do");
        assertEquals("Do",doiAdapter.getCurrentElem().getName());
    }

    @Test
    void testUpdate() throws Exception {
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapter("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS1").get());
        AbstractLNAdapter<?> lnAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();

        assertThrows(ScdException.class, () -> lnAdapter.updateDAI(rDtt));
        rDtt.setDoName("Do.sdo1.d");
        assertThrows(ScdException.class, () -> lnAdapter.updateDAI(rDtt));
        rDtt.setDaName("antRef.bda1.bda2.bda3");
        TVal tVal = new TVal();
        tVal.setValue("newValue");
        rDtt.setDaiValues(List.of(tVal));
        assertDoesNotThrow(() -> lnAdapter.updateDAI(rDtt));
        MarshallerWrapper marshallerWrapper = AbstractDTTLevel.createWrapper();

        lDeviceAdapter = assertDoesNotThrow(() -> iAdapter.getLDeviceAdapterByLdInst("LD_INS2").get());
        AbstractLNAdapter<?> lnAdapter2 = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(TLLN0Enum.LLN_0.value())
                .build();
        rDtt.setValImport(true);
        rDtt.setFc(TFCEnum.SE);
        assertTrue(rDtt.isUpdatable());
        assertDoesNotThrow(() -> lnAdapter2.updateDAI(rDtt));

        System.out.println(marshallerWrapper.marshall(scd));

    }

    private LNAdapter initLNAdapter(TLN tln){
        LDeviceAdapter lDeviceAdapter = Mockito.mock(LDeviceAdapter.class);
        TLDevice tlDevice = Mockito.mock(TLDevice.class);
        Mockito.when(lDeviceAdapter.getCurrentElem()).thenReturn(tlDevice);
        tln.getLnClass().add(DTO.HOLDER_LN_CLASS);
        tln.setInst(DTO.HOLDER_LN_INST);
        tln.setLnType(DTO.LN_TYPE);
        tln.setPrefix(DTO.HOLDER_LN_PREFIX);
        Mockito.when(tlDevice.getLN()).thenReturn(List.of(tln));
        return assertDoesNotThrow( () -> new LNAdapter(lDeviceAdapter,tln));
    }
}