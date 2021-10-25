// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;


import org.lfenergy.compas.sct.data.model.SimpleScd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public class SimpleScdRepository implements IScdCrudRepository<SimpleScd, UUID> {

    public final ScdJpaRepository scdJpaRepository;

    @Autowired
    public SimpleScdRepository(ScdJpaRepository scdJpaRepository) {
        this.scdJpaRepository = scdJpaRepository;
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
