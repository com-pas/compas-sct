// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.sct.exception.CompasDataAccessException;
import org.lfenergy.compas.sct.model.IScd;
import org.lfenergy.compas.sct.repository.AbstractScdSQLCrudRepository;
import org.lfenergy.compas.sct.service.scl.SclManager;

import java.util.UUID;

@Setter
@Getter
@Slf4j
public abstract class AbstractSqlScdService <T extends IScd<UUID>, R extends AbstractScdSQLCrudRepository<T,UUID>>
        implements ISCDService<T> {

    protected  R repository;

    public AbstractSqlScdService(R repository){
        this.repository = repository;
    }

    @Override
    public T findScd(UUID id) throws CompasDataAccessException {
        return repository.findById(id)
                .orElseThrow(() ->  new CompasDataAccessException("Unknown SCD with id '" + id + "'"));
    }

    public SCL initiateScl(String hVersion, String hRevision){
        UUID hId = UUID.randomUUID();
        return SclManager.initialize(hId.toString(),hVersion,hRevision);
    }

    public T addElement(T scd) throws CompasDataAccessException {
        return repository.save(scd);
    }

    public T updateElement(T scd) throws CompasDataAccessException{
        return addElement(scd);
    }

    public T addHistoryItem(UUID id, String who, String what, String why) throws ScdException, CompasDataAccessException {
        T scdObj = findScd(id);
        return addHistoryItem(scdObj,who,what, why);
    }
}
