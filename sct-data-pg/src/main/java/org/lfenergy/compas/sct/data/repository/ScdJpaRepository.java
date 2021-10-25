// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import org.lfenergy.compas.sct.data.model.SimpleScd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface ScdJpaRepository extends JpaRepository<SimpleScd, UUID> {

    boolean existsByHeaderId(UUID id);
}
