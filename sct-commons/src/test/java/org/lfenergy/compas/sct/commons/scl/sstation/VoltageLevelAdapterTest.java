// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VoltageLevelAdapterTest {

    private final SubstationAdapter substationAdapter = mock(SubstationAdapter.class);
    private VoltageLevelAdapter vLevelAdapter;

    @BeforeEach
    public void init() {
        TSubstation tSubstation = new TSubstation();
        tSubstation.setName("SUBSTATION");
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.setName("VOLTAGE_LEVEL");
        tSubstation.getVoltageLevel().add(tVoltageLevel);
        when(substationAdapter.getCurrentElem()).thenReturn(tSubstation);
        vLevelAdapter = new VoltageLevelAdapter(substationAdapter, tVoltageLevel);
    }

    @Test
    void amChildElementRef_whenCalledWithExistingRelationBetweenSubstationAndVoltageLevel_shouldReturnTrue()  {
        // Given : init
        // When Then
        assertThat(vLevelAdapter.amChildElementRef()).isTrue();
    }

    @Test
    void amChildElementRef_when_no_VoltageLevel_given_should_return_false() {
        // When
        VoltageLevelAdapter voltageLevelAdapter = new VoltageLevelAdapter(substationAdapter);
        // Then
        assertThat(voltageLevelAdapter.getParentAdapter()).isNotNull();
        assertThat(voltageLevelAdapter.getCurrentElem()).isNull();
        assertThat(voltageLevelAdapter.amChildElementRef()).isFalse();
    }

    @Test
    void setCurrentElement_whenCalledWithNoRelationBetweenSubstationAndVoltageLevel_shouldThrowException()  {
        // Given
        TVoltageLevel tVoltageLevel1 = new TVoltageLevel();
        // When Then
        assertThatThrownBy(() -> vLevelAdapter.setCurrentElem(tVoltageLevel1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }


    @Test
    void getBayAdapter_when_no_BayAdapter_exist_should_return_empty() {
        // Given
        TBay tBay = new TBay();
        tBay.setName("BAY");
        vLevelAdapter.getCurrentElem().getBay().add(tBay);
        // When Then
        assertThat(vLevelAdapter.getBayAdapter("BAY1")).isEmpty();
    }


    @Test
    void getBayAdapter_when_exist_should_return_expected_BayAdapter() {
        // Given
        TBay tBay = new TBay();
        tBay.setName("BAY");
        vLevelAdapter.getCurrentElem().getBay().add(tBay);
        // When Then
        assertThat(vLevelAdapter.getBayAdapter("BAY")).isPresent();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(vLevelAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        vLevelAdapter.addPrivate(tPrivate);
        // Then
        assertThat(vLevelAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }
}