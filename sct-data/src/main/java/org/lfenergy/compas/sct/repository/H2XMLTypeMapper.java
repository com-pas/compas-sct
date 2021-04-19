package org.lfenergy.compas.sct.repository;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class H2XMLTypeMapper extends SqlXmlTypeMapper {
    private final int[] SQL_TYPES = new int[] { Types.BINARY };

    public static final H2XMLTypeMapper INSTANCE = new H2XMLTypeMapper();

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES.clone() ;
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        return resultSet.getBytes(names[0]);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        preparedStatement.setBytes(index,(byte[]) value);
    }
}
