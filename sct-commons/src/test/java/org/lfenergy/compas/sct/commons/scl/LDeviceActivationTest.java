// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.TCompasLDeviceStatus;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LDeviceActivationTest {

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "UNTESTED"})
    void checkLDeviceActivationStatus_shouldReturnNoError_when_LDeviceStatusACTIVE_Or_UNTESTED_And_Contains_ON_And_LDeviceReferencedINLNode(String lDeviceStatus) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of(Pair.of("iedName1", "ldInst1"));
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.fromValue(lDeviceStatus), Set.of("on"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isTrue();
        assertThat(lDeviceActivation.getErrorMessage()).isNull();
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("on");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "UNTESTED"})
    void checkLDeviceActivationStatus_shouldReturnError_when_LDeviceStatusACTIVE_Or_UNTESTED_And_Contains_ON_And_NotLDeviceReferencedINLNode(String lDeviceStatus) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of();
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst2", TCompasLDeviceStatus.valueOf(lDeviceStatus), Set.of("on"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isFalse();
        assertThat(lDeviceActivation.getErrorMessage()).isEqualTo("The LDevice cannot be set to 'off' but has not been selected into SSD.");
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("on");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "UNTESTED"})
    void checkLDeviceActivationStatus_shouldReturnError_when_LDeviceStatusACTIVE_Or_UNTESTED_And_Contains_OFF_And_LDeviceReferencedINLNode(String lDeviceStatus) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of(Pair.of("iedName1", "ldInst1"));
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("off");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.valueOf(lDeviceStatus), Set.of("off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isFalse();
        assertThat(lDeviceActivation.getErrorMessage()).isEqualTo(
                "The LDevice cannot be set to 'on' but has been selected into SSD.");
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "UNTESTED"})
    void checkLDeviceActivationStatus_shouldReturnNoError_when_LDeviceStatusACTIVE_Or_UNTESTED_And_Contains_OFF_And_NotLDeviceReferencedINLNode(String lDeviceStatus) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of();
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.valueOf(lDeviceStatus), Set.of("off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isTrue();
        assertThat(lDeviceActivation.getErrorMessage()).isNull();
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "UNTESTED"})
    void checkLDeviceActivationStatus_shouldReturnNoError_when_LDeviceStatusACTIVE_Or_UNTESTED_And_Contains_ON_And_OFF_And_LDeviceReferencedINLNode(String lDeviceStatus) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of(Pair.of("iedName1", "ldInst1"));
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("off");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.valueOf(lDeviceStatus), Set.of("on", "off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isTrue();
        assertThat(lDeviceActivation.getErrorMessage()).isNull();
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("on");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "UNTESTED"})
    void checkLDeviceActivationStatus_shouldReturnNoError_when_LDeviceStatusACTIVE_Or_UNTESTED_And_Contains_ON_And_OFF_And_NotLDeviceReferencedINLNode(String lDeviceStatus) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of();
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("off");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst2", TCompasLDeviceStatus.valueOf(lDeviceStatus), Set.of("on", "off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isTrue();
        assertThat(lDeviceActivation.getErrorMessage()).isNull();
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

    @ParameterizedTest
    @CsvSource(value = {"ACTIVE; iedName1; ldInst1","INACTIVE; iedName1; ldInst1", "UNTESTED; iedName1; ldInst1", "ACTIVE;;","INACTIVE;;", "UNTESTED;;"}, delimiter = ';')
    void checkLDeviceActivationStatus_shouldReturnError_when_Contains_Not_ON_Nor_OFF(String lDeviceStatus, String iedName, String ldInst) {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of(Pair.of(iedName, ldInst));
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("off");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.valueOf(lDeviceStatus), Set.of());
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isFalse();
        assertThat(lDeviceActivation.getErrorMessage()).isEqualTo(
                "The LDevice cannot be activated or desactivated because its BehaviourKind Enum contains NOT 'on' AND NOT 'off'.");
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

    @Test
    void checkLDeviceActivationStatus_shouldReturnError_when_LDeviceStatusINACTIVE_And_Contains_ON_And_LDeviceReferencedINLNode() {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of(Pair.of("iedName1", "ldInst1"));
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.INACTIVE, Set.of("on"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isFalse();
        assertThat(lDeviceActivation.getErrorMessage()).isEqualTo("The LDevice is not qualified into STD but has been selected into SSD.");
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("on");
    }

    @Test
    void checkLDeviceActivationStatus_shouldReturnNoError_when_LDeviceStatusINACTIVE_And_Contains_ON_And_OFF_And_NotLDeviceReferencedINLNode() {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of();
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.INACTIVE, Set.of("on", "off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isTrue();
        assertThat(lDeviceActivation.getErrorMessage()).isNull();
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

    @Test
    void checkLDeviceActivationStatus_shouldReturnError_when_LDeviceStatusINACTIVE_And_Contains_ON_And_NotLDeviceReferencedINLNode() {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of();
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.INACTIVE, Set.of("on"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isFalse();
        assertThat(lDeviceActivation.getErrorMessage()).isEqualTo("The LDevice cannot be set to 'off' but has not been selected into SSD.");
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("on");
    }

    @Test
    void checkLDeviceActivationStatus_shouldReturnNoError_when_LDeviceStatusINACTIVE_And_Contains_OFF_And_NotLDeviceReferencedINLNode() {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of();
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("on");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.INACTIVE, Set.of("off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isTrue();
        assertThat(lDeviceActivation.getErrorMessage()).isNull();
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

    @Test
    void checkLDeviceActivationStatus_shouldReturnError_when_LDeviceStatusINACTIVE_And_Contains_ON_And_OFF_And_LDeviceReferencedINLNode() {
        // Given
        List<Pair<String, String>> iedNameLdInstList = List.of(Pair.of("iedName1", "ldInst1"));
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLdInstList);
        lDeviceActivation.setNewVal("off");
        // When
        lDeviceActivation.checkLDeviceActivationStatus("iedName1", "ldInst1", TCompasLDeviceStatus.INACTIVE, Set.of("on", "off"));
        // Then
        assertThat(lDeviceActivation.isUpdatable()).isFalse();
        assertThat(lDeviceActivation.getErrorMessage()).isEqualTo(
                "The LDevice is not qualified into STD but has been selected into SSD.");
        assertThat(lDeviceActivation.getNewVal()).isEqualTo("off");
    }

}