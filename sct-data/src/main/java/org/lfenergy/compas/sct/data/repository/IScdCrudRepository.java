// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;


import org.lfenergy.compas.sct.data.model.IScd;

import java.util.Optional;

public interface IScdCrudRepository<T extends IScd,ID> {
    T save(T s) throws CompasDataAccessException;
    T update(T s) throws CompasDataAccessException;
    Optional<T> findById(ID id);
    boolean existsById(ID id) ;
    boolean existsByHeaderId(ID id) ;
    long count() ;
    void deleteById(ID id) throws CompasDataAccessException;
}
