// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TBDA;
import org.lfenergy.compas.scl2007b4.model.TDAType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

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
        assertEquals(daTypeAdapter.getBdaAdapters().get(0).getClass(),BDAAdapter.class);
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
    public void testContainsStructBdaWithDATypeId() {
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

        daTypeAdapter.checkStructuredData(daTypeName,0);
        assertEquals(TPredefinedBasicTypeEnum.ENUM.value(),daTypeName.getBType());
        assertEquals("RecCycModKind",daTypeName.getType());
        daTypeName.setStructNames(List.of("origin"));
        assertThrows(ScdException.class, () -> daTypeAdapter.checkStructuredData(daTypeName,0));

        DaTypeName daTypeName2 = new DaTypeName("d","check.ctlVal");
        assertThrows(ScdException.class, () -> daTypeAdapter.checkStructuredData(daTypeName2,0));
    }
}