// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.domain;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DoLinkedToDaTest {

    @Test
    void test_copyFrom() {
        // Given
        DataObject dataObject = new DataObject();
        dataObject.setDoName("doName");
        dataObject.setSdoNames(List.of("sdoName1"));
        dataObject.setCdc(TPredefinedCDCEnum.CST);
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setFc(TFCEnum.BL);
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When
        DoLinkedToDa newDoLinkedToDa = DoLinkedToDa.copyFrom(doLinkedToDa);
        // Then
        assertAll("Copy From",
                () -> assertThat(newDoLinkedToDa.dataObject().getCdc()).isEqualTo(doLinkedToDa.dataObject().getCdc()),
                () -> assertThat(newDoLinkedToDa.dataAttribute().getBType()).isEqualTo(doLinkedToDa.dataAttribute().getBType()),
                () -> assertThat(newDoLinkedToDa.dataAttribute().getType()).isEqualTo(doLinkedToDa.dataAttribute().getType()),
                () -> assertThat(newDoLinkedToDa.dataAttribute().getFc()).isEqualTo(doLinkedToDa.dataAttribute().getFc()),
                () -> assertThat(newDoLinkedToDa.getDoRef()).isEqualTo(doLinkedToDa.getDoRef()),
                () -> assertThat(newDoLinkedToDa.getDaRef()).isEqualTo(doLinkedToDa.getDaRef()));
    }

    @Test
    void test_getDoRef() {
        // Given
        DataObject dataObject = new DataObject();
        dataObject.setDoName("doName");
        dataObject.setSdoNames(List.of("sdoName1"));
        dataObject.setCdc(TPredefinedCDCEnum.CST);
        // When Then
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, new DataAttribute());
        assertThat(doLinkedToDa.getDoRef()).isEqualTo("doName.sdoName1");
    }

    @Test
    void test_getDaRef() {
        // Given
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setFc(TFCEnum.BL);
        // When
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(new DataObject(), dataAttribute);

        // Then
        assertThat(doLinkedToDa.getDaRef()).isEqualTo("daName.bdaName1");
    }

    @Test
    void isUpdatable_should_return_true_whenIsDOModDAstVal() {
        // Given
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Mod");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("stVal");

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When Then
        assertThat(doLinkedToDa.isUpdatable()).isTrue();
    }

    @Test
    void isUpdatable_should_return_true_whenValImportIsTrue() {
        // Given
        DataObject dataObject = new DataObject();
        dataObject.setDoName("doName");
        dataObject.setSdoNames(List.of("sdoName1"));
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setValImport(true);
        dataAttribute.setFc(TFCEnum.SE);

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When Then
        assertThat(doLinkedToDa.isUpdatable()).isTrue();
    }

    @Test
    void isUpdatable_should_return_false_whenValImportIsFalse() {
        // Given
        DataObject dataObject = new DataObject();
        dataObject.setDoName("doName");
        dataObject.setSdoNames(List.of("sdoName1"));
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setValImport(false);
        dataAttribute.setFc(TFCEnum.SE);

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        // When Then
        assertThat(doLinkedToDa.isUpdatable()).isFalse();
    }

}
