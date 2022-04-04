// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.junit.jupiter.api.Assertions.*;

class SclAutomatisationServiceTest {

    @Test
    void createSCD() {
        assertThrows(ScdException.class,
                () ->  SclAutomatisationService.createSCD(null, "hVersion", "hRevision") );
    }
}