// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ExtRefSourceInfoTest {

    @Test
    @Tag("issue-321")
    void testConstruction(){
        // Given
        ExtRefSourceInfo sourceInfo = DTO.createExtRefSourceInfo();
        ExtRefSourceInfo bindingInfo_bis = DTO.createExtRefSourceInfo();
        // When
        ExtRefSourceInfo bindingInfo_ter = new ExtRefSourceInfo(DTO.createExtRef());
        // When
        ExtRefSourceInfo bindingInfo_qt = new ExtRefSourceInfo();

        // Then
        assertThat(bindingInfo_ter).isEqualTo(sourceInfo).hasSameHashCodeAs(sourceInfo);
        assertThat(bindingInfo_bis).isEqualTo(sourceInfo);
        assertThat(sourceInfo).isNotNull();
        assertThat(bindingInfo_qt).isNotEqualTo(sourceInfo);
        assertThat(new ExtRefSourceInfo()).isNotEqualTo(sourceInfo);
    }
}