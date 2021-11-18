// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

public class CompasDataAccessException extends RuntimeException {

    public CompasDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompasDataAccessException(String message) {
        super(message);
    }
}
