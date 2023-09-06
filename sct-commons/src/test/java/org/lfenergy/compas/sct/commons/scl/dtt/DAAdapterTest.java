// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.SCD_DTT;
import static org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateTestUtils.initDttAdapterFromFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DAAdapterTest {

    @Test
    @Tag("issue-321")
    void testHasSameContentAs() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        assertThat(dttAdapter.getDATypeAdapters()).hasSizeGreaterThan(1);
        DOTypeAdapter doTypeAdapter1 =  dttAdapter.getDOTypeAdapters().get(0);
        DOTypeAdapter doTypeAdapter2 =  dttAdapter.getDOTypeAdapters().get(1);
        //When Then
        DAAdapter daAdapter1 = assertDoesNotThrow(() -> doTypeAdapter1.getDAAdapterByName("dataNs").get());
        //When Then
        DAAdapter daAdapter2 = assertDoesNotThrow(() -> doTypeAdapter2.getDAAdapterByName("angRef").get());
        //When Then
        assertThat(daAdapter1.hasSameContentAs(daAdapter1.getCurrentElem())).isTrue();
        //When Then
        assertThat(daAdapter1.hasSameContentAs(daAdapter2.getCurrentElem())).isFalse();
    }

    @Test
    @Tag("issue-321")
    void testCheck() {
        //Given
        DataTypeTemplateAdapter dttAdapter = initDttAdapterFromFile(SCD_DTT);
        DOTypeAdapter doTypeAdapter = assertDoesNotThrow(() ->dttAdapter.getDOTypeAdapterById("DO2").get());
        DAAdapter daAdapter = assertDoesNotThrow(() ->doTypeAdapter.getDAAdapterByName("angRef").get());
        assertThat(daAdapter.getBType()).isEqualTo(TPredefinedBasicTypeEnum.ENUM);
        assertThat(daAdapter.isTail()).isTrue();
        assertThat(daAdapter.getDATypeAdapter()).isEmpty();
        DaTypeName daTypeName = new DaTypeName("angRef");
        //When Then
        assertThatCode(() -> daAdapter.check(daTypeName)).doesNotThrowAnyException();
        //Given
        daTypeName.getDaiValues().put(0L,"Va");
        //When Then
        assertThatCode(() -> daAdapter.check(daTypeName)).doesNotThrowAnyException();
        //Given
        daTypeName.getDaiValues().put(0L,"unknown");
        //When Then
        assertThatThrownBy(() -> daAdapter.check(daTypeName))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        //Given
        DOTypeAdapter daTypeAdapter = mock(DOTypeAdapter.class);
        TDOType tdoType = new TDOType();
        TDA tda = new TDA();
        tdoType.getSDOOrDA().add(tda);
        when(daTypeAdapter.getCurrentElem()).thenReturn(tdoType);
        DAAdapter daAdapter = new DAAdapter(daTypeAdapter, tda);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(tda.getPrivate()).isEmpty();
        //When
        daAdapter.addPrivate(tPrivate);
        //Then
        assertThat(tda.getPrivate()).isNotEmpty();
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        DOTypeAdapter daTypeAdapter = mock(DOTypeAdapter.class);
        TDOType tdoType = new TDOType();
        TDA tda = new TDA();
        tda.setName("dataNs");
        tdoType.getSDOOrDA().add(tda);
        when(daTypeAdapter.getCurrentElem()).thenReturn(tdoType);
        DAAdapter daAdapter = new DAAdapter(daTypeAdapter, tda);
        // When
        String result = daAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DA[name=@name=\"dataNs\" and type=not(@type)]");
    }

}