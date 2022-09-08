// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TEnumType;
import org.lfenergy.compas.scl2007b4.model.TEnumVal;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EnumTypeAdapterTest extends AbstractDTTLevel<DataTypeTemplateAdapter,TEnumType> {

    @Override
    protected void completeInit() {
        TEnumType tEnumType = new TEnumType();
        tEnumType.setId("ID");
        List<TEnumType> tEnumTypes = List.of(tEnumType);
        sclElement = tEnumType;
        Mockito.when(sclElementAdapter.getCurrentElem().getEnumType()).thenReturn(tEnumTypes);
    }

    @Test
    void testAmChildElement(){
        init();
        assertDoesNotThrow(
                () -> new EnumTypeAdapter(sclElementAdapter,sclElement)
        );
    }

    @Test
    void testHasSameContentAs(){
        init();
        EnumTypeAdapter enumTypeAdapter = assertDoesNotThrow(
                () -> new EnumTypeAdapter(sclElementAdapter,sclElement)
        );
        TEnumVal sameEnumVal = new TEnumVal();
        sameEnumVal.setValue("sameValue");
        sameEnumVal.setOrd(1);
        sameEnumVal.setDesc("A desc");
        enumTypeAdapter.getCurrentElem().getEnumVal().add(sameEnumVal);
        TEnumType anotherEnumType = new TEnumType();
        anotherEnumType.getEnumVal().add(sameEnumVal);

        assertTrue(enumTypeAdapter.hasSameContentAs(anotherEnumType));
        assertFalse(enumTypeAdapter.hasSameContentAs(new TEnumType())); // empty enumType

        TEnumVal notSameEnumVal = new TEnumVal();
        notSameEnumVal.setValue("notSameValue");
        notSameEnumVal.setOrd(1);
        notSameEnumVal.setDesc("Another desc");
        anotherEnumType = new TEnumType();
        anotherEnumType.getEnumVal().add(notSameEnumVal);
        assertFalse(enumTypeAdapter.hasSameContentAs(anotherEnumType));
    }

    @Test
    void addPrivate() {
        init();
        EnumTypeAdapter enumTypeAdapter = assertDoesNotThrow(
                () -> new EnumTypeAdapter(sclElementAdapter,sclElement)
        );

        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(enumTypeAdapter.getCurrentElem().getPrivate().isEmpty());
        enumTypeAdapter.addPrivate(tPrivate);
        assertEquals(1, enumTypeAdapter.getCurrentElem().getPrivate().size());
    }


    @Test
    void elementXPath() {
        // Given
        init();
        EnumTypeAdapter enumTypeAdapter = assertDoesNotThrow(
                () -> new EnumTypeAdapter(sclElementAdapter,sclElement)
        );
        // When
        String result = enumTypeAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("EnumType[@id=\"ID\"]");
    }
}