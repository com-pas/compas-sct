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

class FcdaCandidatesHelperTest {

    @Test
    void contains_should_return_true() {
        //Given
        FcdaCandidatesHelper fcdaCandidatesHelper = FcdaCandidatesHelper.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass("ANCR");
        resumedDataTemplate.setDoName(new DoTypeName("DoName"));
        resumedDataTemplate.setDaName(new DaTypeName("daNameST"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidatesHelper.isFcdaCandidate(resumedDataTemplate);
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void contains_should_return_false() {
        //Given
        FcdaCandidatesHelper fcdaCandidatesHelper = FcdaCandidatesHelper.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass("Non existent");
        resumedDataTemplate.setDoName(new DoTypeName("Non existent"));
        resumedDataTemplate.setDaName(new DaTypeName("Non existent"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidatesHelper.isFcdaCandidate(resumedDataTemplate);
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void contains_when_a_parameter_is_blank_should_throw_exception() {
        //Given
        FcdaCandidatesHelper fcdaCandidatesHelper = FcdaCandidatesHelper.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass(" ");
        resumedDataTemplate.setDoName(new DoTypeName("Non existent"));
        resumedDataTemplate.setDaName(new DaTypeName("Non existent"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        assertThatThrownBy(() -> fcdaCandidatesHelper.isFcdaCandidate(resumedDataTemplate))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void contains_should_ignore_comments() {
        //Given
        FcdaCandidatesHelper fcdaCandidatesHelper = FcdaCandidatesHelper.SINGLETON;
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate();
        resumedDataTemplate.setLnClass("#COM");
        resumedDataTemplate.setDoName(new DoTypeName("Comment"));
        resumedDataTemplate.setDaName(new DaTypeName("comment"));
        resumedDataTemplate.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidatesHelper.isFcdaCandidate(resumedDataTemplate);
        //Then
        assertThat(result).isFalse();
    }
}
