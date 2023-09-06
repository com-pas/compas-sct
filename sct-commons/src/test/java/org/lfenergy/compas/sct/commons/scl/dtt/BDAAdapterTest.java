// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BDAAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenDATypeAndBDA_shouldThrowException() {
        //Given
        DATypeAdapter daTypeAdapter = mock(DATypeAdapter.class);
        when(daTypeAdapter.getCurrentElem()).thenReturn(new TDAType());
        TBDA tbda = new TBDA();
        //When Then
        assertThatCode(() -> new DATypeAdapter.BDAAdapter(daTypeAdapter, tbda))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenDATypeAndBDA_shouldNotThrowException() {
        //Given
        DATypeAdapter daTypeAdapter = mock(DATypeAdapter.class);
        TDAType daType = new TDAType();
        TBDA bda = new TBDA();
        daType.getBDA().add(bda);
        when(daTypeAdapter.getCurrentElem()).thenReturn(daType);
        //When Then
        assertThatCode(() -> new DATypeAdapter.BDAAdapter(daTypeAdapter, bda)).doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    void tesHasSameContentAs() {
        // Given
        DATypeAdapter daTypeAdapter = mock(DATypeAdapter.class);
        TDAType daType = new TDAType();
        TBDA tbda = createBDA();
        daType.getBDA().add(tbda);
        when(daTypeAdapter.getCurrentElem()).thenReturn(daType);
        //When Then
        DATypeAdapter.BDAAdapter bdaAdapter = assertDoesNotThrow(
                () -> new DATypeAdapter.BDAAdapter(daTypeAdapter, tbda));
        assertThat(bdaAdapter.getType()).isEqualTo("ENUM_BDA1");
        assertThat(bdaAdapter.getBType()).isEqualTo(TPredefinedBasicTypeEnum.ENUM);
        assertThat(bdaAdapter.getName()).isEqualTo("BDA1");
        TBDA tbda1 = createBDA();
        tbda1.getVal().get(0).setValue("VAL_CHANGED");
        //When Then
        assertThat(bdaAdapter.hasSameContentAs(tbda1)).isFalse();
        tbda1.getCount().add("1");
        //When Then
        assertThat(bdaAdapter.hasSameContentAs(tbda1)).isFalse();
        tbda1.setValImport(false);
        //When Then
        assertThat(bdaAdapter.hasSameContentAs(tbda1)).isFalse();
        tbda1.setValKind(TValKindEnum.SET);
        //When Then
        assertThat(bdaAdapter.hasSameContentAs(tbda1)).isFalse();
        tbda1.setSAddr("SADDR_BDA2");
        //When Then
        assertThat(bdaAdapter.hasSameContentAs(tbda1)).isFalse();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        //Given
        DATypeAdapter daTypeAdapter = mock(DATypeAdapter.class);
        TDAType daType = new TDAType();
        TBDA tbda = createBDA();
        daType.getBDA().add(tbda);
        when(daTypeAdapter.getCurrentElem()).thenReturn(daType);
        DATypeAdapter.BDAAdapter bdaAdapter = new DATypeAdapter.BDAAdapter(daTypeAdapter, tbda);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(bdaAdapter.getCurrentElem().getPrivate()).isEmpty();
        //When
        bdaAdapter.addPrivate(tPrivate);
        //Then
        assertThat(bdaAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        DATypeAdapter daTypeAdapter = mock(DATypeAdapter.class);
        TDAType daType = new TDAType();
        TBDA tbda = new TBDA();
        daType.getBDA().add(tbda);
        when(daTypeAdapter.getCurrentElem()).thenReturn(daType);
        DATypeAdapter.BDAAdapter bdaAdapter = new DATypeAdapter.BDAAdapter(daTypeAdapter, tbda);
        // When
        String result = bdaAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("BDA[not(@name)]");
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

}