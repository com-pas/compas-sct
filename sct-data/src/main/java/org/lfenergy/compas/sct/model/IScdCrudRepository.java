package org.lfenergy.compas.sct.model;

import org.lfenergy.compas.sct.exception.CompasDataAccessException;

import java.util.Optional;

public interface IScdCrudRepository<T, ID> {
    <S extends T> S save(S s) throws CompasDataAccessException;
    <S extends T> S update(S s) throws CompasDataAccessException;
    Optional<T> findById(ID id);
    boolean existsById(ID id) ;
    boolean existsByHeaderId(ID id) ;
    long count() ;
    void deleteById(ID id) throws CompasDataAccessException;
}
