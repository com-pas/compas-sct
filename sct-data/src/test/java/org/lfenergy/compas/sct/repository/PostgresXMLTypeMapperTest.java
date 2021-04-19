package org.lfenergy.compas.sct.repository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

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