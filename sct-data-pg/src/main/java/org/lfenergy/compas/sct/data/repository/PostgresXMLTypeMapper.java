// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;


@Slf4j
public class PostgresXMLTypeMapper extends SqlXmlTypeMapper {
    private final int[] SQL_TYPES = new int[] { Types.SQLXML };

    public static final PostgresXMLTypeMapper INSTANCE = new PostgresXMLTypeMapper();

    public PostgresXMLTypeMapper() {
        super();
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES.clone();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor ssci, Object o) throws HibernateException, SQLException {
        byte[] rawXml = null;
        SQLXML sqlxml = null;
        try {
            sqlxml = rs.getSQLXML(names[0]);
            if(sqlxml != null) {
                rawXml = sqlxml.getString().trim().getBytes();
            }
        } finally {
            if (null != sqlxml) {
                sqlxml.free();
            }
        }
        return rawXml;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor ssci) throws HibernateException, SQLException {

        SQLXML xmlType = st.getConnection().createSQLXML();
        if (value != null && value.getClass() == byte[].class) {
            xmlType.setString(new String((byte[]) value));
            st.setObject(index, xmlType);
        } else {
            log.debug("Binding null to parameter {} ",index);
            st.setNull(index,Types.SQLXML);
        }
    }
}
