// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class PostgreSqlXmlDialect extends PostgreSQL95Dialect{

    public PostgreSqlXmlDialect() {
        super();
        registerColumnType(Types.SQLXML, "XML");
        registerHibernateType(Types.OTHER, "pg-uuid");
    }
}