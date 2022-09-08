// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;


/**
 * A representation of constants used in application
 */
public final class CommonConstants {

    public static final String ICD_SYSTEM_VERSION_UUID = "ICDSystemVersionUUID";
    public static final String IED_NAME = "IEDName";
    public static final String HEADER_ID = "headerId";
    public static final String HEADER_VERSION = "headerVersion";
    public static final String HEADER_REVISION = "headerRevision";
    public static final String BEHAVIOUR_DO_NAME = "Beh";
    public static final String MOD_DO_NAME = "Mod";
    public static final String STVAL = "stVal";

    /**
     * Private Controlller, should not be instanced
     */
    private CommonConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
