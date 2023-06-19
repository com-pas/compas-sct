// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FcdaCandidatesTest {

    @Test
    void contains_should_return_true() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnClass("ANCR");
        dataAttributeRef.setDoName(new DoTypeName("DoName"));
        dataAttributeRef.setDaName(new DaTypeName("daNameST"));
        dataAttributeRef.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidates.contains(dataAttributeRef);
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void contains_should_return_false() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnClass("Non existent");
        dataAttributeRef.setDoName(new DoTypeName("Non existent"));
        dataAttributeRef.setDaName(new DaTypeName("Non existent"));
        dataAttributeRef.setFc(TFCEnum.ST);
        //When
        boolean result = fcdaCandidates.contains(dataAttributeRef);
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void contains_when_a_parameter_is_blank_should_throw_exception() {
        //Given
        FcdaCandidates fcdaCandidates = FcdaCandidates.SINGLETON;
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setLnClass(" ");
        dataAttributeRef.setDoName(new DoTypeName("Non existent"));
        dataAttributeRef.setDaName(new DaTypeName("Non existent"));
        dataAttributeRef.setFc(TFCEnum.ST);
        //When
        assertThatThrownBy(() -> fcdaCandidates.contains(dataAttributeRef))
            .isInstanceOf(IllegalArgumentException.class);
    }

}
