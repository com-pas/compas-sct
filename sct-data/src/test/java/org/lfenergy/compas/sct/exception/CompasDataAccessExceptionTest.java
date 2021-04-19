package org.lfenergy.compas.sct.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CompasDataAccessExceptionTest {

    @Test
    void testConstructor(){
        CompasDataAccessException compasDAOException = new CompasDataAccessException("Exception message");

        assertEquals("Exception message", compasDAOException.getMessage());
        compasDAOException = new CompasDataAccessException("Exception message",
                new RuntimeException("Please check me"));

        assertEquals(compasDAOException.getCause().getClass(),RuntimeException.class);
    }

}