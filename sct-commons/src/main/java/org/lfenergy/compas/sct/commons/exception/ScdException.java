// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.exception;

/**
 * Thrown when SCD is inconsistent
 */
public class ScdException extends RuntimeException {
    /**
     * Constructor
     * @param message input (message to display)
     */
    public ScdException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param message input (message to display)
     * @param cause input (cause message to display)
     */
    public ScdException(String message, Throwable cause) {
        super(message, cause);
    }
}
