// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class VoltageLevelServiceTest {

    private final VoltageLevelService voltageLevelService = new VoltageLevelService();

    @Test
    void getVoltageLevels_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-substation-import-ssd/ssd.xml");
        // When
        List<TVoltageLevel> tVoltageLevels =  voltageLevelService.getVoltageLevels(scd).toList();
        // Then
        assertThat(tVoltageLevels).hasSize(2);
    }

    @Test
    void findVoltageLevel_when_voltageLevelExist_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-substation-import-ssd/ssd.xml");
        // When Then
        assertThatCode(() -> voltageLevelService.findVoltageLevel(scd, tVoltageLevel1 -> "4".equals(tVoltageLevel1.getName())).orElseThrow())
                .doesNotThrowAnyException();
    }

    @Test
    void findVoltageLevel_when_voltageLevelNotExist_should_return_empty() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-substation-import-ssd/ssd.xml");
        // When Then
        assertThat(voltageLevelService.findVoltageLevel(scd, tVoltageLevel1 -> "5".equals(tVoltageLevel1.getName())))
                .isEmpty();
    }
}
