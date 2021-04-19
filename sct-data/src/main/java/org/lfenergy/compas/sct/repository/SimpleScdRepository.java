package org.lfenergy.compas.sct.repository;


import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.sct.exception.CompasDataAccessException;
import org.lfenergy.compas.sct.model.entity.SimpleScd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Repository
public class SimpleScdRepository extends AbstractScdSQLCrudRepository<SimpleScd, UUID> {

    public final ScdJpaRepository scdJpaRepository;

    @Autowired
    public SimpleScdRepository(ScdJpaRepository scdJpaRepository) {
        super(SimpleScd.class);
        this.scdJpaRepository = scdJpaRepository;
    }

    @Override
    public UUID getNextID() {
        return UUID.randomUUID();
    }

    @Override
    public SimpleScd save(SimpleScd simpleScd) throws CompasDataAccessException {
        return scdJpaRepository.save(simpleScd);
    }


    @Override
    public SimpleScd update(SimpleScd simpleScd) throws CompasDataAccessException {
        return scdJpaRepository.save(simpleScd);
    }

    @Override
    public Optional<SimpleScd> findById(UUID uuid) {
        return scdJpaRepository.findById(uuid);
    }

    @Override
    public boolean existsById(UUID uuid) {
        return scdJpaRepository.existsById(uuid);
    }

    @Override
    public boolean existsByHeaderId(UUID uuid) {
        return scdJpaRepository.existsByHeaderId(uuid);
    }

    @Override
    public long count() {
        return scdJpaRepository.count();
    }

    @Override
    public void deleteById(UUID uuid) {
        scdJpaRepository.deleteById(uuid);
    }
}
