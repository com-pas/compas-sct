// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogicalNodeOptionsTest {

    @Test
    void testConstructor(){
        LogicalNodeOptions logicalNodeOptions = new LogicalNodeOptions();
        assertFalse(logicalNodeOptions.isWithExtRef());
        assertFalse(logicalNodeOptions.isWithCB());
        assertFalse(logicalNodeOptions.isWithDatSet());
        assertFalse(logicalNodeOptions.isWithDataAttributeRef());

        logicalNodeOptions.setWithCB(true);
        logicalNodeOptions.setWithDatSet(false);
        logicalNodeOptions.setWithExtRef(false);
        logicalNodeOptions.setWithDataAttributeRef(false);

        assertFalse(logicalNodeOptions.isWithExtRef());
        assertTrue(logicalNodeOptions.isWithCB());
        assertFalse(logicalNodeOptions.isWithDatSet());
        assertFalse(logicalNodeOptions.isWithDataAttributeRef());

        logicalNodeOptions = new LogicalNodeOptions(true,false,
                true,false);
        assertTrue(logicalNodeOptions.isWithExtRef());
        assertTrue(logicalNodeOptions.isWithCB());
        assertFalse(logicalNodeOptions.isWithDatSet());
        assertFalse(logicalNodeOptions.isWithDataAttributeRef());
    }
}