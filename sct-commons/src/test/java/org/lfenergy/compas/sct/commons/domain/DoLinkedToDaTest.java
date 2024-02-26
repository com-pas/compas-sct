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
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa();
        DataObject dataObject = new DataObject();
        dataObject.setDoName("doName");
        dataObject.setSdoNames(List.of("sdoName1"));
        dataObject.setCdc(TPredefinedCDCEnum.CST);
        doLinkedToDa.setDataObject(dataObject);
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setFc(TFCEnum.BL);
        doLinkedToDa.setDataAttribute(dataAttribute);
        // When
        DoLinkedToDa newDoLinkedToDa = DoLinkedToDa.copyFrom(doLinkedToDa);
        // Then
        assertAll("Copy From",
                () -> assertThat(newDoLinkedToDa.getDataObject().getCdc()).isEqualTo(doLinkedToDa.getDataObject().getCdc()),
                () -> assertThat(newDoLinkedToDa.getDataAttribute().getBType()).isEqualTo(doLinkedToDa.getDataAttribute().getBType()),
                () -> assertThat(newDoLinkedToDa.getDataAttribute().getType()).isEqualTo(doLinkedToDa.getDataAttribute().getType()),
                () -> assertThat(newDoLinkedToDa.getDataAttribute().getFc()).isEqualTo(doLinkedToDa.getDataAttribute().getFc()),
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
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa();
        doLinkedToDa.setDataObject(dataObject);
        assertThat(doLinkedToDa.getDoRef()).isEqualTo("doName.sdoName1");
    }

    @Test
    void test_getDaRef() {
        // Given
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("daName");
        dataAttribute.setBdaNames(List.of("bdaName1"));
        dataAttribute.setFc(TFCEnum.BL);
        // When Then
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa();
        doLinkedToDa.setDataAttribute(dataAttribute);
        assertThat(doLinkedToDa.getDaRef()).isEqualTo("daName.bdaName1");
    }

}