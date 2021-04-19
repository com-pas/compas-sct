// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

@TypeDef(
        name = "xmltype",
        defaultForType = byte[].class,
        typeClass = H2XMLTypeMapper.class
)
package org.lfenergy.compas.sct.model.entity;
import org.hibernate.annotations.TypeDef;
import org.lfenergy.compas.sct.repository.H2XMLTypeMapper;

