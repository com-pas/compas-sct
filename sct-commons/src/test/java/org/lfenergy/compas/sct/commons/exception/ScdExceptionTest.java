// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScdExceptionTest {
    private static final String MSG = "MSG";
    private static final String ANOTHER_MSG = "ANOTHER_MSG";
    private static final String CAUSE_MSG = "CAUSE_MSG";

    @Test
    void testInit() {
        ScdException exception = new ScdException(MSG);
        assertEquals(MSG, exception.getMessage());
        exception = new ScdException(ANOTHER_MSG, new RuntimeException(CAUSE_MSG));
        assertEquals(ANOTHER_MSG, exception.getMessage());
        assertEquals(RuntimeException.class, exception.getCause().getClass());
        assertEquals(CAUSE_MSG, exception.getCause().getMessage());
    }

}