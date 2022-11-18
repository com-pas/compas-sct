// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FcdaCandidatesTest {

    @Test
    void contains_should_return_true() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass("ANCR");
        resumedDataTemplate.setDoName(new DoTypeName("DoName"));
        resumedDataTemplate.setDaName(new DaTypeName("daNameST"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidates.contains(resumedDataTemplate);
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void contains_should_return_false() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass("Non existent");
        resumedDataTemplate.setDoName(new DoTypeName("Non existent"));
        resumedDataTemplate.setDaName(new DaTypeName("Non existent"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidates.contains(resumedDataTemplate);
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void contains_when_a_parameter_is_blank_should_throw_exception() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass(" ");
        resumedDataTemplate.setDoName(new DoTypeName("Non existent"));
        resumedDataTemplate.setDaName(new DaTypeName("Non existent"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        assertThatThrownBy(() -> fcdaCandidates.contains(resumedDataTemplate))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void contains_should_ignore_first_lines() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        //When
        boolean result = fcdaCandidates.contains("FCDA.lnClass", "FCDA.doName", "FCDA.daName", "FCDA.fc");
        //Then
        assertThat(result).isFalse();
    }
}
