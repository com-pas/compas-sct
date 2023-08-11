// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubstationAdapterTest {

    private final SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);;
    private SubstationAdapter substationAdapter;

    @BeforeEach
    public void init() {
        SCL scl = new SCL();
        TSubstation tSubstation = new TSubstation();
        tSubstation.setName("SUBSTATION");
        scl.getSubstation().add(tSubstation);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        substationAdapter = new SubstationAdapter(sclRootAdapter, tSubstation);
    }

    @Test
    void amChildElementRef_whenCalledWithExistingRelationBetweenSCLAndSubstation_shouldReturnTrue()  {
        // Given : init
        // When Then
        assertThat(substationAdapter.amChildElementRef()).isTrue();
    }

    @Test
    void amChildElementRef_when_no_Substation_given_should_return_false() {
        // When
        SubstationAdapter substationAdapter1 = new SubstationAdapter(sclRootAdapter);
        // Then
        assertThat(substationAdapter1.getParentAdapter()).isNotNull();
        assertThat(substationAdapter1.getCurrentElem()).isNull();
        assertThat(substationAdapter1.amChildElementRef()).isFalse();
    }

    @Test
    void setCurrentElement_whenCalledWithNoRelationBetweenSCLAndSubstation_shouldThrowException()  {
        // Given
        TSubstation tSubstation1 = new TSubstation();
        // When Then
        assertThatThrownBy(() -> substationAdapter.setCurrentElem(tSubstation1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void getVoltageLevelAdapter_when_exist_should_return_not_empty_list_of_VoltageLevelAdapter() {
        // Given
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.setName("VOLTAGE_LEVEL1");
        substationAdapter.getCurrentElem().getVoltageLevel().add(tVoltageLevel);
        //When Then
        assertThat(substationAdapter.getVoltageLevelAdapter("VOLTAGE_LEVEL1")).isNotEmpty();
    }

    @Test
    void getVoltageLevelAdapter_when_not_exist_should_return_empty_list_of_VoltageLevelAdapter() {
        // Given : init
        //When Then
        assertThat(substationAdapter.getVoltageLevelAdapter("VOLTAGE_LEVEL1")).isEmpty();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(substationAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        substationAdapter.addPrivate(tPrivate);
        // Then
        assertThat(substationAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }


    @Test
    void getIedAndLDeviceNamesForLN0FromLNode_whenLNodeContainsLN0_shouldReturnListOf1Pair() {
        // Given
        SCL scl = SclTestMarshaller.getSCLFromFile("/scd-refresh-lnode/issue68_Test_Template.scd");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scl);
        substationAdapter = sclRootAdapter.getSubstationAdapter();
        // When
        List<Pair<String, String>> iedNameLdInstList = substationAdapter.getIedAndLDeviceNamesForLN0FromLNode();
        // Then
        assertThat(iedNameLdInstList)
                .hasSize(1)
                .containsExactly(Pair.of("IedName1", "LDSUIED"));
    }

}
