// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.app;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.app.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import static org.junit.jupiter.api.Assertions.*;

class SclAutomatisationServiceTest {

    @Test
    void createSCD() throws Exception {
        SCL ssd = SclTestMarshaller.getSCLFromFile("/ssd-create-scd/ssd.xml");
        assertThrows(ScdException.class,
                () ->  SclAutomatisationService.createSCD(ssd, "hVersion", "hRevision") );
    }
}