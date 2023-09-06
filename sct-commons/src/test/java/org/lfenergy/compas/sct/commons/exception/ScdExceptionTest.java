// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ScdExceptionTest {
    private static final String MSG = "MSG";
    private static final String ANOTHER_MSG = "ANOTHER_MSG";
    private static final String CAUSE_MSG = "CAUSE_MSG";

    @Test
    void constructor_whenCalledWithMessage_shouldFillValues() {
        // When
        ScdException exception = new ScdException(MSG);
        // Then
        assertThat(exception.getMessage()).isEqualTo(MSG);
    }

    @Test
    void constructor_whenCalledWithMessageAndThrowable_shouldFillValues() {
        // When
        ScdException exception = new ScdException(ANOTHER_MSG, new RuntimeException(CAUSE_MSG));
        // Then
        assertThat(exception.getMessage()).isEqualTo(ANOTHER_MSG);
        assertThat(exception.getCause().getClass()).isEqualTo(RuntimeException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo(CAUSE_MSG);
    }


}