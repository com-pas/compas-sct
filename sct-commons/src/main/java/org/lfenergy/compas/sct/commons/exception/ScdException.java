// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.exception;

import org.lfenergy.compas.sct.commons.dto.SclReportItem;

/**
 * Thrown when SCD is inconsistent
 */
public class ScdException extends RuntimeException {

    private final String xPath;
    private final boolean isFatal;

    /**
     * Constructor
     *
     * @param message input (message to display)
     */
    public ScdException(String message) {
        super(message);
        this.xPath = "";
        this.isFatal = true;
    }

    /**
     * Constructor
     *
     * @param message input (message to display)
     */
    public ScdException(String xPath, String message, boolean isFatal) {
        super(message);
        this.xPath = xPath;
        this.isFatal = isFatal;
    }

    /**
     * Constructor
     *
     * @param message input (message to display)
     * @param cause   input (cause message to display)
     */
    public ScdException(String message, Throwable cause) {
        super(message, cause);
        this.xPath = "";
        this.isFatal = true;
    }

    /**
     * Constructor
     *
     * @param message input (message to display)
     * @param cause   input (cause message to display)
     */
    public ScdException(String xPath, String message, Throwable cause, boolean isFatal) {
        super(message, cause);
        this.xPath = xPath;
        this.isFatal = isFatal;
    }

    public SclReportItem toSclReportItem() {
        return new SclReportItem(xPath, getMessage(), isFatal);
    }
}
