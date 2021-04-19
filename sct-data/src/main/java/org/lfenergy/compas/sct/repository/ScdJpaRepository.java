// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.repository;

import org.lfenergy.compas.sct.model.entity.SimpleScd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface ScdJpaRepository extends JpaRepository<SimpleScd, UUID> {

    boolean existsByHeaderId(UUID id);
}
