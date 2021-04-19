// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.repository;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.sct.model.IScd;
import org.lfenergy.compas.sct.model.IScdCrudRepository;

/**
 * Abstract DAO for SQL-like DB
 * @param <T> Entity model type
 * @param <ID> Entity identifier type
 */
@Slf4j
public abstract class AbstractScdSQLCrudRepository<T extends IScd<ID>, ID> implements IScdCrudRepository<T,ID> {

    protected Class<T> genericType;

    public AbstractScdSQLCrudRepository(Class<T> genericType) {
        this.genericType = genericType;
    }

    /**
     * Get new unique identifier
     * @param value object for whom to determine an identifier
     * @return unique identifier
     */
    public ID getNextID(T value){
        ID id = value.getHeaderId();
        if(id == null || existsByHeaderId(id)){
            id = getNextID();
        }
        return id;
    }

    /**
     * Get new unique identifier
     * @return unique identifier
     */
    public abstract ID getNextID();
}
