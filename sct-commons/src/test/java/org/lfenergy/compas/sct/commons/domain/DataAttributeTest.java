// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataAttributeTest {

    @Test
    void isUpdatable_should_return_true_whenValImportIsTrue() {
        // Given
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setValImport(true);
        dataAttribute.setFc(TFCEnum.SE);
        // When Then
        assertThat(dataAttribute.isUpdatable()).isTrue();
    }

    @Test
    void isUpdatable_should_return_false_whenValImportIsFalse() {
        // Given
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setValImport(false);
        dataAttribute.setFc(TFCEnum.SE);
        // When Then
        assertThat(dataAttribute.isUpdatable()).isFalse();
    }


    @Test
    void isUpdatable_should_return_true_whenFcIsNotAppropriate() {
        // Given
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setValImport(true);
        dataAttribute.setFc(TFCEnum.MX);
        // When Then
        assertThat(dataAttribute.isUpdatable()).isFalse();
    }


    @ParameterizedTest
    @CsvSource(value = {"MX:true:false", "CF:true:true", "CF:false:false",
            "DC:false:false", "DC:false:false",
            "SG:false:false", "SG:false:false",
            "SP:false:false", "SP:false:false",
            "ST:false:false", "ST:false:false",
            "SE:false:false", "SE:false:false"}, delimiter = ':')
    void isUpdatable_should_return_ExpectedValue(String fcVal, boolean valImport, boolean expected) {
        // Given
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setValImport(valImport);
        dataAttribute.setFc(TFCEnum.valueOf(fcVal));
        // When Then
        assertThat(dataAttribute.isUpdatable()).isEqualTo(expected);

    }

}