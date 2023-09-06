// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BayAdapterTest {

    private final VoltageLevelAdapter vLevelAdapter = mock(VoltageLevelAdapter.class);
    private BayAdapter bayAdapter;


    @BeforeEach
    public void init() {
        TSubstation tSubstation = new TSubstation();
        tSubstation.setName("SUBSTATION");
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.setName("VOLTAGE_LEVEL");
        TBay tBay = new TBay();
        tBay.setName("BAY");
        tBay.getFunction().add(new TFunction());

        tVoltageLevel.getBay().add(tBay);
        tSubstation.getVoltageLevel().add(tVoltageLevel);
        when(vLevelAdapter.getCurrentElem()).thenReturn(tVoltageLevel);
        bayAdapter = new BayAdapter(vLevelAdapter, tBay);
    }

    @Test
    void amChildElementRef_whenCalledWithExistingRelationBetweenVoltageLevelAndBay_shouldReturnTrue()  {
        // Given : init
        // When Then
        assertThat(bayAdapter.amChildElementRef()).isTrue();
    }

    @Test
    void amChildElementRef_when_no_Bay_given_should_return_false() {
        // When
        BayAdapter bayAdapter1 = new BayAdapter(vLevelAdapter);
        // Then
        assertThat(bayAdapter1.getParentAdapter()).isNotNull();
        assertThat(bayAdapter1.getCurrentElem()).isNull();
        assertThat(bayAdapter1.amChildElementRef()).isFalse();
    }


    @Test
    void constructor_whenCalledWithUnknown_shouldThrowException() {
        // When Then
        assertThatCode(() -> new BayAdapter(vLevelAdapter, "BAY_1"))
                .isInstanceOf(ScdException.class)
                .hasMessage("Unknown Bay name :BAY_1");
    }

    @Test
    void constructor_whenCalledKnownVoltageLevelName_shouldNotThrowException() {
        // Given
        TBay tBay = new TBay();
        tBay.setName("BAY_1");
        vLevelAdapter.getCurrentElem().getBay().add(tBay);
        // When Then
        assertThatCode(() -> new BayAdapter(vLevelAdapter, "BAY_1")).doesNotThrowAnyException();
    }

    @Test
    void setCurrentElement_whenCalledWithNoRelationBetweenVoltageLevelAndBay_shouldThrowException()  {
        // Given
        TBay tBay1 = new TBay();
        // When Then
        assertThatThrownBy(() -> bayAdapter.setCurrentElem(tBay1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(bayAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        bayAdapter.addPrivate(tPrivate);
        // Then
        assertThat(bayAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void elementXPath_should_succeed() {
        // Given : init
        // When
        String result = bayAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Bay[@name=\"BAY\"]");
    }

    @Test
    void elementXPath_when_name_is_missing_should_succeed() {
        // Given : init
        bayAdapter.getCurrentElem().setName(null);
        // When
        String result = bayAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Bay[not(@name)]");
    }

    @Test
    void streamFunctionAdapters_should_return_expected_list() {
        // Given : init
        // When
        Stream<FunctionAdapter> result = bayAdapter.streamFunctionAdapters();
        // Then
        assertThat(result).hasSize(1);
    }
}
