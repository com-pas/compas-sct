package org.lfenergy.compas.sct.repository;

import org.lfenergy.compas.sct.exception.CompasDataAccessException;
import org.lfenergy.compas.sct.model.IScdCrudRepository;

import java.util.Optional;

public class ScdBaseXRepository<T,ID> implements IScdCrudRepository<T,ID> {
    private static final String NOT_IMPLEMENTED_YET = "Not implemented yet!";
    @Override
    public <S extends T> S save(S s) throws CompasDataAccessException {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED_YET");
    }

    @Override
    public <S extends T> S update(S s) throws CompasDataAccessException {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED_YET");
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ID id) {
        return false;
    }

    @Override
    public boolean existsByHeaderId(ID id) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(ID id) throws CompasDataAccessException {
        throw new UnsupportedOperationException("NOT_IMPLEMENTED_YET");
    }
}
