// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TBDA;
import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DATypeAdapterTest extends AbstractDTTLevel<DataTypeTemplateAdapter,TDAType> {

    @Override
    protected void completeInit() {
        TDAType tdaType = new TDAType();
        tdaType.setId("ID");
        List<TDAType> tdaTypes = new ArrayList<>();
        tdaTypes.add(tdaType);
        sclElement = tdaType;
        Mockito.when(sclElementAdapter.getCurrentElem().getDAType()).thenReturn(tdaTypes);
    }

    @Test
    void testAmChildElementRef() {
        init();
        assertDoesNotThrow(
                () -> new DATypeAdapter(sclElementAdapter, sclElement)
        );
    }

    @Test
    void testGetBdaAdapters() {
        init();
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        sclElement.getBDA().add(tbda);

        DATypeAdapter daTypeAdapter = assertDoesNotThrow(
                () -> new DATypeAdapter(sclElementAdapter, sclElement)
        );
        assertFalse(daTypeAdapter.getBdaAdapters().isEmpty());
        assertEquals(daTypeAdapter.getBdaAdapters().get(0).getClass(), DATypeAdapter.BDAAdapter.class);
    }

    @Test
    void testContainsBDAWithEnumTypeID() {
        init();
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        tbda.setBType(TPredefinedBasicTypeEnum.ENUM);
        tbda.setType("enumTypeId");
        sclElement.getBDA().add(tbda);

        DATypeAdapter daTypeAdapter = assertDoesNotThrow(
                () -> new DATypeAdapter(sclElementAdapter, sclElement)
        );
        assertTrue(daTypeAdapter.containsBDAWithEnumTypeID("enumTypeId"));
        assertFalse(daTypeAdapter.containsBDAWithEnumTypeID("no_enumTypeId"));
    }

    @Test
    void testContainsStructBdaWithDATypeId() {
        init();
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        tbda.setBType(TPredefinedBasicTypeEnum.STRUCT);
        tbda.setType("daTypeId");
        sclElement.getBDA().add(tbda);

        DATypeAdapter daTypeAdapter = assertDoesNotThrow(
                () -> new DATypeAdapter(sclElementAdapter, sclElement)
        );
        assertTrue(daTypeAdapter.containsStructBdaWithDATypeId("daTypeId"));
        assertFalse(daTypeAdapter.containsStructBdaWithDATypeId("no_daTypeId"));
    }

    @Test
    void hasSameContentAs() throws Exception {

        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        assertTrue(rcvDttAdapter.getDATypeAdapters().size() > 1);

        DATypeAdapter rcvDATypeAdapter =  rcvDttAdapter.getDATypeAdapters().get(0);
        assertTrue(rcvDATypeAdapter.hasSameContentAs(rcvDATypeAdapter.getCurrentElem()));
        assertFalse(rcvDATypeAdapter.hasSameContentAs(rcvDttAdapter.getDATypeAdapters().get(1).getCurrentElem()));
    }

    @Test
    void testCheckStructuredData() throws Exception {

        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        DaTypeName daTypeName = new DaTypeName("origin","origin.ctlVal");

        daTypeAdapter.check(daTypeName);
        assertEquals(TPredefinedBasicTypeEnum.ENUM,daTypeName.getBType());
        assertEquals("RecCycModKind",daTypeName.getType());
        DaTypeName daTypeName1 = new DaTypeName("origin","origin");
        assertThrows(ScdException.class, () -> daTypeAdapter.check(daTypeName1));

        DaTypeName daTypeName2 = new DaTypeName("d","check.ctlVal");
        assertThrows(ScdException.class, () -> daTypeAdapter.check(daTypeName2));
    }

    @Test
    void testGetResumedRTTs() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        ResumedDataTemplate rootRDtt = new ResumedDataTemplate();
        rootRDtt.getDaName().setName("origin");
        rootRDtt.getDoName().setName("StrVal");
        List<ResumedDataTemplate> rDtts = daTypeAdapter.getResumedDTTs(rootRDtt, new ResumedDataTemplate());
        assertEquals(2,rDtts.size());

    }

    @Test
    void getResumedDTTByDaName() throws Exception {

        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(
                AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID
        );
        DaTypeName daTypeName = new DaTypeName("antRef","origin.ctlVal");
        DoTypeName doTypeName = new DoTypeName("Op.origin");
        ResumedDataTemplate rDtt = new ResumedDataTemplate();
        rDtt.setDoName(doTypeName);
        rDtt.getDaName().setName("antRef");
        assertTrue(rDtt.getBdaNames().isEmpty());
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        assertDoesNotThrow(() -> daTypeAdapter.getResumedDTTByDaName(daTypeName,0,rDtt).get());
    }

    @Test
    void addPrivate() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertFalse(daTypeAdapter.getCurrentElem().getPrivate().isEmpty());
        daTypeAdapter.addPrivate(tPrivate);
        assertEquals(2, daTypeAdapter.getCurrentElem().getPrivate().size());
    }
    @Test
    void elementXPath() throws Exception {
        // Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDATypeAdapterById("DA1").get());
        // When
        String result = daTypeAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DAType[@id=\"DA1\"]");
    }

}
