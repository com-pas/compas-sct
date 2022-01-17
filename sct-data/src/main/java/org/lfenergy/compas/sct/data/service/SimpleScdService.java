// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.service;

import org.lfenergy.compas.sct.data.model.IScd;
import org.lfenergy.compas.sct.data.repository.CompasDataAccessException;
import org.lfenergy.compas.sct.data.repository.IScdCrudRepository;

import java.util.UUID;

public class SimpleScdService<T extends IScd<UUID>> {

    private IScdCrudRepository<T,UUID> scdCrudRepository;

    public SimpleScdService(IScdCrudRepository<T, UUID> scdCrudRepository) {
        this.scdCrudRepository = scdCrudRepository;
    }

    public T getElement(UUID uuid) throws CompasDataAccessException {
        return scdCrudRepository.findById(uuid)
                .orElseThrow( () -> new CompasDataAccessException("Unknown SCD Object"));
    }

    public T saveElement(T elem) {
        return scdCrudRepository.save(elem);
    }

    public T updateElement(T elem) {
        return scdCrudRepository.update(elem);
    }
}
