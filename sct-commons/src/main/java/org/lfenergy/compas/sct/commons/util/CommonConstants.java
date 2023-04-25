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
    public static final String STVAL_DA_NAME = "stVal";
    public static final String ATTRIBUTE_VALUE_SEPARATOR = "_";
    public static final String CONTROLBLOCK_NAME_PREFIX = "CB" + ATTRIBUTE_VALUE_SEPARATOR;
    public static final String DATASET_NAME_PREFIX = "DS" + ATTRIBUTE_VALUE_SEPARATOR;

    public static final String LDEVICE_LDEPF = "LDEPF";
    public static final String IED_TEST_NAME = "IEDTEST";

    /**
     * Private Controlller, should not be instanced
     */
    private CommonConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
