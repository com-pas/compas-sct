// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.exception;


public class CompasDataAccessException extends Exception {

    public CompasDataAccessException(String message) {
        super(message);
    }

    public CompasDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
