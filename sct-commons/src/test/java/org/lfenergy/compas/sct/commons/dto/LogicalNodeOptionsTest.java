// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class LogicalNodeOptionsTest {

    @Test
    @Tag("issue-321")
    void constructor_whenCalled_shouldFillValues() {
        // When
        LogicalNodeOptions logicalNodeOptions = new LogicalNodeOptions();
        // Then
        assertThat(logicalNodeOptions.isWithExtRef()).isFalse();
        assertThat(logicalNodeOptions.isWithCB()).isFalse();
        assertThat(logicalNodeOptions.isWithDatSet()).isFalse();
        assertThat(logicalNodeOptions.isWithDataAttributeRef()).isFalse();

        logicalNodeOptions.setWithCB(true);
        logicalNodeOptions.setWithDatSet(false);
        logicalNodeOptions.setWithExtRef(false);
        logicalNodeOptions.setWithDataAttributeRef(false);
        // Then
        assertThat(logicalNodeOptions.isWithExtRef()).isFalse();
        assertThat(logicalNodeOptions.isWithCB()).isTrue();
        assertThat(logicalNodeOptions.isWithDatSet()).isFalse();
        assertThat(logicalNodeOptions.isWithDataAttributeRef()).isFalse();

        // When
        logicalNodeOptions = new LogicalNodeOptions(true,false, true,false);
        // Then
        assertThat(logicalNodeOptions.isWithExtRef()).isTrue();
        assertThat(logicalNodeOptions.isWithCB()).isTrue();
        assertThat(logicalNodeOptions.isWithDatSet()).isFalse();
        assertThat(logicalNodeOptions.isWithDataAttributeRef()).isFalse();
    }
}