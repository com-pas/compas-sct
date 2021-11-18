// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

class PostgresXMLTypeMapperTest {
    private PostgresXMLTypeMapper xmlTypeMapper = new PostgresXMLTypeMapper();

    @Test
    void testReturnedClass(){
        assertEquals(byte[].class,xmlTypeMapper.returnedClass());
    }

    @Test
    void testIsMutable(){
        assertTrue(xmlTypeMapper.isMutable());
    }

    @Test
    void testDeepCopy(){
        byte[] b1 = "hello".getBytes();
       assertArrayEquals(b1, (byte[]) xmlTypeMapper.deepCopy(b1));
    }

    @Test void testReplace(){
        byte[] b1 = "hello".getBytes();
        byte[] b2 = "world".getBytes();
        assertArrayEquals(b2, (byte[]) xmlTypeMapper.replace(b1,b2,null));
    }

    @Test
    void testEquals(){
        byte[] b1 = "hello".getBytes();
        byte[] b2 = "world".getBytes();
        assertTrue(xmlTypeMapper.equals(b1,b1));
        assertNotEquals(xmlTypeMapper.hashCode(b1),xmlTypeMapper.hashCode(b2));
        assertFalse(xmlTypeMapper.equals(b1,b2));
    }

    @Test
    void testDisassemble(){
        byte[] b = "hello".getBytes();

        assertNotNull(xmlTypeMapper.disassemble(b));
        assertTrue(Serializable.class.isInstance(xmlTypeMapper.disassemble(b)));

        // just for coverage ... assemble does nothing exceptional
        Serializable bSer = xmlTypeMapper.disassemble(b);
        xmlTypeMapper.assemble(bSer, new byte[]{});

    }

    @Test
    void testSqlTypes() {
        assertArrayEquals(new int[]{Types.SQLXML}, xmlTypeMapper.sqlTypes());
    }

    @Test
    void testNullSafeSet() throws SQLException {
        PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        Connection mockConnection = Mockito.mock(Connection.class);
        SQLXML mockXmlType = Mockito.mock(SQLXML.class);

        Mockito.when(mockPreparedStatement.getConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.createSQLXML()).thenReturn(mockXmlType);
        byte[] b = "<SCL/>".getBytes();

        xmlTypeMapper.nullSafeSet(mockPreparedStatement,b,1,null);

        xmlTypeMapper.nullSafeSet(mockPreparedStatement,null,1,null);
    }

    @Test
    void testNullSafeGet() throws SQLException {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        SQLXML mockXmlType = Mockito.mock(SQLXML.class);

        Mockito.when(mockResultSet.getSQLXML(ArgumentMatchers.anyString())).thenReturn(mockXmlType);
        Mockito.when(mockXmlType.getString()).thenReturn("<SCL/>");
        byte[] expected = "<SCL/>".getBytes();
        byte[] res = (byte[]) xmlTypeMapper.nullSafeGet(mockResultSet,new String[]{"RAW_XML"},null,null);

        assertArrayEquals(res,expected);

    }
}