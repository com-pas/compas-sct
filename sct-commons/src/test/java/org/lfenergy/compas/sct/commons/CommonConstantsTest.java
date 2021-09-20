// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CommonConstantsTest {
    @Test
    public void testShouldReturnNOKWhenConstructClassCauseForbidden() {
        assertThrows(UnsupportedOperationException.class, () -> new CommonConstants());
    }

}