// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LNodeTypeAdapterTest extends AbstractDTTLevel<DataTypeTemplateAdapter,TLNodeType> {
    @Override
    protected void completeInit() {
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        List<TLNodeType> tlNodeTypes = new ArrayList<>();
        tlNodeTypes.add(tlNodeType);
        sclElement = tlNodeType;
        Mockito.when(sclElementAdapter.getCurrentElem().getLNodeType()).thenReturn(tlNodeTypes);
    }

    @Test
    void testAmChildElementRef() {
        init();
        assertDoesNotThrow(
                () -> new LNodeTypeAdapter(sclElementAdapter,sclElement)
        );
    }

    @Test
    void testHasSameContentAs() {
        init();
        TLNodeType tlNodeType = createLNOdeType();
        TLNodeType tlNodeType1 = createLNOdeType();
        sclElement = tlNodeType;
        sclElementAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(
                () -> new LNodeTypeAdapter(sclElementAdapter,tlNodeType)
        );

        assertEquals(DTO.HOLDER_LN_CLASS,lNodeTypeAdapter.getLNClass());
        assertTrue(lNodeTypeAdapter.getDOTypeId("Op").isPresent());

        assertTrue(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));
        assertEquals(3,tlNodeType1.getDO().size());

        tlNodeType1.getDO().get(2).setAccessControl("AC");
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));

        tlNodeType1.getDO().get(2).setTransient(true);
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));

        tlNodeType1.getDO().get(2).setName("NAME");
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));

        tlNodeType1.getDO().get(2).setType("DO11");
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));

        TDO tdo = new TDO();
        tdo.setType("DO12");
        tdo.setName("OpPPP");
        tlNodeType1.getDO().add(tdo);
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));

        tlNodeType1.setIedType("ANOTHER_TYPE");
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));

        TPrivate aPrivate = new TPrivate();
        aPrivate.setType("TYPE");
        aPrivate.setSource("https://a.com");
        tlNodeType1.getPrivate().add(aPrivate);
        assertFalse(lNodeTypeAdapter.hasSameContentAs(tlNodeType1));
    }

    @Test
    void containsDOWithDOTypeId() {
        init();
        TLNodeType tlNodeType = createLNOdeType();
        TLNodeType tlNodeType1 = createLNOdeType();
        sclElement = tlNodeType;
        sclElementAdapter.getCurrentElem().getLNodeType().add(tlNodeType);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(
                () -> new LNodeTypeAdapter(sclElementAdapter,tlNodeType)
        );

        assertTrue(lNodeTypeAdapter.containsDOWithDOTypeId("DO1"));
        assertFalse(lNodeTypeAdapter.containsDOWithDOTypeId("DO11"));
    }


    private TLNodeType createLNOdeType(){
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("BDA1");
        tlNodeType.setIedType("IEDTYPE");
        tlNodeType.getLnClass().add(DTO.HOLDER_LN_CLASS);

        TDO tdo = new TDO();
        tdo.setType("DO1");
        tdo.setName("Op");

        TDO tdo1 = new TDO();
        tdo1.setType("DO2");
        tdo1.setName("Beh");

        TDO tdo2 = new TDO();
        tdo2.setType("DO3");
        tdo2.setName("StrVal");

        tlNodeType.getDO().addAll(List.of(tdo,tdo1,tdo2));
        return tlNodeType;
    }

    @Test
    void testGetResumedDTTs() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getLNodeTypeAdapterById("LN1").get());
        ResumedDataTemplate rootRDtt = new ResumedDataTemplate();
        rootRDtt.setDoName(new DoTypeName("Op"));
        ResumedDataTemplate filter = new ResumedDataTemplate();
        filter.setDoName(new DoTypeName("Op.res"));
        var rDtts = lNodeTypeAdapter.getResumedDTTs(filter);
        assertEquals(2,rDtts.size());

        filter.setDoName(new DoTypeName("Op.res"));
        filter.setDaName(new DaTypeName("d"));
        rDtts = lNodeTypeAdapter.getResumedDTTs(filter);
        assertEquals(1,rDtts.size());

        filter.setDoName(new DoTypeName("Op.res"));
        filter.setDaName(new DaTypeName("antRef"));
        assertTrue(lNodeTypeAdapter.getResumedDTTs(filter).isEmpty());
    }

    @Test
    void testCheck() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getLNodeTypeAdapterById("LN1").get());
        assertThrows(
                ScdException.class,
                () -> lNodeTypeAdapter.check(new DoTypeName(""),new DaTypeName(""))
        );

        assertThrows(
                ScdException.class,
                () -> lNodeTypeAdapter.check(new DoTypeName("do"),new DaTypeName(""))
        );

        assertThrows(
                ScdException.class,
                () -> lNodeTypeAdapter.check(new DoTypeName("do"),new DaTypeName("da"))
        );
        DoTypeName doTypeName = new DoTypeName("Op.res");
        DaTypeName daTypeName = new DaTypeName("d");

        assertDoesNotThrow(() -> lNodeTypeAdapter.check(doTypeName,daTypeName));
        doTypeName.setName("StrVal");
        doTypeName.getStructNames().clear();
        daTypeName.setName("origin");
        daTypeName.getStructNames().clear();
        daTypeName.setStructNames(List.of("origin","ctlVal"));
        assertDoesNotThrow(() -> lNodeTypeAdapter.check(doTypeName,daTypeName));

    }

    @Test
    void getResumedDTTByDaName() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(
                AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID
        );
        DaTypeName daTypeName = new DaTypeName("antRef","origin.ctlVal");
        LNodeTypeAdapter lNodeTypeAdapter = assertDoesNotThrow(() -> dttAdapter.getLNodeTypeAdapterById("LN1").get());
        List<ResumedDataTemplate> rDtts = assertDoesNotThrow(()-> lNodeTypeAdapter.getResumedDTTByDaName(daTypeName));
        assertEquals(2,rDtts.size());
    }
}