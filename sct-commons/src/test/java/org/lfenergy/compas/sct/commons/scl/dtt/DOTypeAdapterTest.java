// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.scl2007b4.model.TDOType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DOTypeAdapterTest extends AbstractDTTLevel<DataTypeTemplateAdapter, TDOType> {

    @Override
    protected void completeInit() {
        TDOType tdoType = new TDOType();
        tdoType.setId("ID");
        List<TDOType> tdoTypes = List.of(tdoType);
        sclElement = tdoType;
        Mockito.when(sclElementAdapter.getCurrentElem().getDOType()).thenReturn(tdoTypes);
    }

    @Test
    public void testAmChildElementRef() {
        init();
        assertDoesNotThrow(
                () -> new DOTypeAdapter(sclElementAdapter, sclElement)
        );
    }

    @Test
    public void testContainsDAWithEnumTypeId() {
        init();
        TDA tda = new TDA();
        tda.setType("ID_BDA");
        tda.setBType(TPredefinedBasicTypeEnum.ENUM);
        tda.setType("enumTypeId");
        sclElement.getSDOOrDA().add(tda);

        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(
                () -> new DOTypeAdapter(sclElementAdapter, sclElement)
        );
        assertTrue(doTypeAdapter.containsDAWithEnumTypeId("enumTypeId"));
        assertFalse(doTypeAdapter.containsDAWithEnumTypeId("no_enumTypeId"));
    }

    @Test
    public void testContainsDAStructWithDATypeId() {
        init();
        TDA tda = new TDA();
        tda.setType("ID_BDA");
        tda.setBType(TPredefinedBasicTypeEnum.STRUCT);
        tda.setType("daTypeId");
        sclElement.getSDOOrDA().add(tda);

        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(
                () -> new DOTypeAdapter(sclElementAdapter, sclElement)
        );
        assertTrue(doTypeAdapter.containsDAStructWithDATypeId("daTypeId"));
        assertFalse(doTypeAdapter.containsDAStructWithDATypeId("no_daTypeId"));
    }

    @Test
    void hasSameContentAs() throws Exception {
        DataTypeTemplateAdapter rcvDttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        assertTrue(rcvDttAdapter.getDATypeAdapters().size() > 1);

        DOTypeAdapter rcvDOTypeAdapter =  rcvDttAdapter.getDOTypeAdapters().get(0);
        assertTrue(rcvDOTypeAdapter.hasSameContentAs(rcvDOTypeAdapter.getCurrentElem()));
        assertFalse(rcvDOTypeAdapter.hasSameContentAs(rcvDttAdapter.getDOTypeAdapters().get(1).getCurrentElem()));
    }

    @Test
    void testCheckStructuredData() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT_DIFF_CONTENT_SAME_ID);
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDOTypeAdapterById("DO2").get());
        DoTypeName doTypeName = new DoTypeName("Op","origin");

        doTypeAdapter.checkStructuredData(doTypeName,0);
        assertEquals(TPredefinedCDCEnum.WYE,doTypeName.getCdc());

    }
}