// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDA;
import org.lfenergy.compas.scl2007b4.model.TDOType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DAAdapterTest {

    @Test
    void TestConstructor() {
        TDOType tdoType = new TDOType();
        tdoType.setId("ID");
        DOTypeAdapter doTypeAdapter = new DOTypeAdapter(null,tdoType);

        TDA tda = new TDA();
        tda.setType("Type");
        tda.setBType(TPredefinedBasicTypeEnum.ENUM);
        tda.setName("DA");
        tdoType.getSDOOrDA().add(tda);

        DAAdapter daAdapter = assertDoesNotThrow(() -> doTypeAdapter.getDAAdapterByName("DA").get());
        assertEquals("Type", daAdapter.getType());
        assertEquals(TPredefinedBasicTypeEnum.ENUM, daAdapter.getBType());
        assertEquals("DA", daAdapter.getName());
    }

    @Test
    void testHasSameContentAs() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        assertTrue(dttAdapter.getDATypeAdapters().size() > 1);

        DOTypeAdapter doTypeAdapter1 =  dttAdapter.getDOTypeAdapters().get(0);
        DOTypeAdapter doTypeAdapter2 =  dttAdapter.getDOTypeAdapters().get(1);
        DAAdapter daAdapter1 = assertDoesNotThrow(() -> doTypeAdapter1.getDAAdapterByName("dataNs").get());
        DAAdapter daAdapter2 = assertDoesNotThrow(() -> doTypeAdapter2.getDAAdapterByName("angRef").get());

        assertTrue(daAdapter1.hasSameContentAs(daAdapter1.getCurrentElem()));
        assertFalse(daAdapter1.hasSameContentAs(daAdapter2.getCurrentElem()));
    }

    @Test
    void testCheck() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);

        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDOTypeAdapterById("DO2").get());
        DAAdapter daAdapter = assertDoesNotThrow(() ->doTypeAdapter.getDAAdapterByName("angRef").get());
        assertEquals(TPredefinedBasicTypeEnum.ENUM, daAdapter.getBType());
        assertTrue(daAdapter.isTail());
        assertTrue(daAdapter.getDATypeAdapter().isEmpty());
        DaTypeName daTypeName = new DaTypeName("angRef");
        assertDoesNotThrow(() -> daAdapter.check(daTypeName));
        daTypeName.getDaiValues().put(0L,"Va");
        assertDoesNotThrow(() -> daAdapter.check(daTypeName));
        daTypeName.getDaiValues().put(0L,"unknown");
        assertThrows(ScdException.class, () -> daAdapter.check(daTypeName));
    }

    @Test
    void addPrivate() throws Exception {
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DOTypeAdapter doTypeAdapter =  dttAdapter.getDOTypeAdapters().get(0);
        DAAdapter daAdapter = assertDoesNotThrow(() -> doTypeAdapter.getDAAdapterByName("dataNs").get());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(daAdapter.getCurrentElem().getPrivate().isEmpty());
        daAdapter.addPrivate(tPrivate);
        assertEquals(1, daAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void elementXPath() throws Exception {
        // Given
        DataTypeTemplateAdapter dttAdapter = AbstractDTTLevel.initDttAdapterFromFile(AbstractDTTLevel.SCD_DTT);
        DOTypeAdapter doTypeAdapter =  dttAdapter.getDOTypeAdapters().get(0);
        DAAdapter daAdapter = assertDoesNotThrow(() -> doTypeAdapter.getDAAdapterByName("dataNs").get());
        // When
        String result = daAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DA[name=@name=\"dataNs\" and type=not(@type)]");
    }
}