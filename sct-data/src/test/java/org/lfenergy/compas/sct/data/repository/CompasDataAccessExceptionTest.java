// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompasDataAccessExceptionTest {

    @Test
    void testConstructor(){
        CompasDataAccessException ex = new CompasDataAccessException("msg",new RuntimeException());
        assertEquals("msg",ex.getLocalizedMessage());
        assertNotNull(ex.getCause());
        assertEquals(RuntimeException.class,ex.getCause().getClass());

    }

}