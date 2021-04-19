

@TypeDef(
        name = "xmltype",
        defaultForType = byte[].class,
        typeClass = PostgresXMLTypeMapper.class
)
package org.lfenergy.compas.sct.model.entity;

import org.hibernate.annotations.TypeDef;
import org.lfenergy.compas.sct.repository.PostgresXMLTypeMapper;