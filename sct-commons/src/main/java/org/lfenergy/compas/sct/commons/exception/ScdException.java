// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.exception;

public class ScdException extends Exception {
    public ScdException(String message) {
        super(message);
    }

    public ScdException(String message, Throwable cause) {
        super(message, cause);
    }
}
