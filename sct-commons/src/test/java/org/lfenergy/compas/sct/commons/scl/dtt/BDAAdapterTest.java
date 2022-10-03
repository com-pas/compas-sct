// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BDAAdapterTest extends AbstractDTTLevel<DATypeAdapter,TBDA> {

    @Test
    void testAmChildElementRef()  {
        init();
        assertDoesNotThrow(
                () -> new DATypeAdapter.BDAAdapter(sclElementAdapter,sclElement)
        );
    }

    @Override
    protected SclElementAdapter getMockedSclParentAdapter(){
        DataTypeTemplateAdapter dataTypeTemplateAdapter = (DataTypeTemplateAdapter) super.getMockedSclParentAdapter();
        TDAType tdaType = new TDAType();
        tdaType.setId("ID");
        List<TDAType> tdaTypes = List.of(tdaType);
        Mockito.when(dataTypeTemplateAdapter.getCurrentElem().getDAType()).thenReturn(tdaTypes);
        DATypeAdapter daTypeAdapter = assertDoesNotThrow(
                () -> new DATypeAdapter(dataTypeTemplateAdapter,tdaType)
        );
        return daTypeAdapter;
    }

    @Test
    void tesHasSameContentAs() {
        init();
        TBDA tbda = createBDA();
        sclElementAdapter.getCurrentElem().getBDA().add(tbda);
        DATypeAdapter.BDAAdapter bdaAdapter = assertDoesNotThrow(
                () -> new DATypeAdapter.BDAAdapter(sclElementAdapter,tbda)
        );
        assertEquals("ENUM_BDA1", bdaAdapter.getType());
        assertEquals(TPredefinedBasicTypeEnum.ENUM, bdaAdapter.getBType());
        assertEquals("BDA1", bdaAdapter.getName());

        TBDA tbda1 = createBDA();
        tbda1.getVal().get(0).setValue("VAL_CHANGED");
        assertFalse(bdaAdapter.hasSameContentAs(tbda1));

        tbda1.getCount().add("1");
        assertFalse(bdaAdapter.hasSameContentAs(tbda1));

        tbda1.setValImport(false);
        assertFalse(bdaAdapter.hasSameContentAs(tbda1));

        tbda1.setValKind(TValKindEnum.SET);
        assertFalse(bdaAdapter.hasSameContentAs(tbda1));

        tbda1.setSAddr("SADDR_BDA2");
        assertFalse(bdaAdapter.hasSameContentAs(tbda1));
    }

    private TBDA createBDA(){
        TBDA tbda = new TBDA();
        tbda.setName("BDA1");
        tbda.setBType(TPredefinedBasicTypeEnum.ENUM);
        tbda.setType("ENUM_BDA1");
        tbda.setSAddr("SADDR_BDA1");
        tbda.setValKind(TValKindEnum.CONF);
        tbda.setValImport(true);
        TVal tVal = new TVal();
        tVal.setValue("VAL1");
        TVal tVal1 = new TVal();
        tVal1.setValue("VAL1");

        tbda.getVal().addAll(List.of(tVal,tVal1));

        return tbda;
    }

    @Override
    protected void completeInit() {
        TBDA tbda = new TBDA();
        tbda.setType("ID_BDA");
        sclElement = tbda;
        sclElementAdapter.getCurrentElem().getBDA().add(tbda);
    }

    @Test
    void addPrivate() {
        init();
        DATypeAdapter.BDAAdapter bdaAdapter = new DATypeAdapter.BDAAdapter(sclElementAdapter,sclElement);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(bdaAdapter.getCurrentElem().getPrivate().isEmpty());
        bdaAdapter.addPrivate(tPrivate);
        assertEquals(1, bdaAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void elementXPath() {
        // Given
        init();
        DATypeAdapter.BDAAdapter bdaAdapter = new DATypeAdapter.BDAAdapter(sclElementAdapter,sclElement);
        // When
        String result = bdaAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("BDA[not(@name)]");
    }

}