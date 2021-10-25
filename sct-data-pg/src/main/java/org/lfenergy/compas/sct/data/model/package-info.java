// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

@TypeDef(
        name = "xmltype",
        defaultForType = byte[].class,
        typeClass = PostgresXMLTypeMapper.class
)
package org.lfenergy.compas.sct.data.model;

import org.hibernate.annotations.TypeDef;
import org.lfenergy.compas.sct.data.repository.PostgresXMLTypeMapper;
