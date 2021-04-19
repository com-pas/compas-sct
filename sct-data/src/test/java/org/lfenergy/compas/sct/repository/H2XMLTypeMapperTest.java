package org.lfenergy.compas.sct.repository;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class H2XMLTypeMapperTest {
    private H2XMLTypeMapper h2XMLTypeMapper = new H2XMLTypeMapper();

    @Test
    void testReturnedClass(){
        assertEquals(byte[].class,h2XMLTypeMapper.returnedClass());
    }

    @Test
    void testEquals(){
        byte[] b1 = "hello".getBytes();
        byte[] b2 = "world".getBytes();
        assertTrue(h2XMLTypeMapper.equals(b1,b1));
        assertNotEquals(h2XMLTypeMapper.hashCode(b1),h2XMLTypeMapper.hashCode(b2));
        assertFalse(h2XMLTypeMapper.equals(b1,b2));
    }

    @Test
    void testDisassemble(){
        byte[] b = "hello".getBytes();

        assertNotNull(h2XMLTypeMapper.disassemble(b));
        assertTrue(Serializable.class.isInstance(h2XMLTypeMapper.disassemble(b)));

        // just for coverage ... assemble does nothing exceptional
        Serializable bSer = h2XMLTypeMapper.disassemble(b);
        h2XMLTypeMapper.assemble(bSer, new byte[]{});

    }



}