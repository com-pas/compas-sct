// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.service;

import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
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

    public SclRootAdapter initScl(UUID hId, String hVersion, String hRevision) throws ScdException {
        if(scdCrudRepository.existsByHeaderId(hId)){
            throw new ScdException(String.format("SCL file with header ID [%s] exists already", hId.toString()));
        }
        return new SclRootAdapter(hId.toString(),hVersion,hRevision);
    }
}
