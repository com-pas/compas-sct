// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompasDataAccessExceptionTest {

    @Test
    void constructor_whenCalled_shouldFillValues(){
        // When
        CompasDataAccessException compasDataAccessException = new CompasDataAccessException("msg",new RuntimeException());
        // Then
        assertThat(compasDataAccessException.getLocalizedMessage()).isEqualTo("msg");
        assertThat(compasDataAccessException.getCause()).isNotNull();
        assertThat(compasDataAccessException.getCause().getClass()).isEqualTo(RuntimeException.class);
    }

}