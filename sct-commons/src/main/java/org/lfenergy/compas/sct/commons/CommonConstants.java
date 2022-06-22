// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

public class CommonConstants {
    CommonConstants() {
        throw new UnsupportedOperationException("CommonConstants class");
    }

    public static final String XML_DEFAULT_NS_PREFIX = "scl";
    public static final String XML_DEFAULT_NS_URI = "http://www.iec.ch/61850/2003/SCL";
    public static final String XML_DEFAULT_XSD_PATH = "classpath:schema/SCL.xsd";

    public static final String COMPAS_SCL_FILE_TYPE = "COMPAS-SclFileType";
    public static final String SCL_FILE_TYPE = "SclFileType";
    public static final String COMPAS_ICDHEADER = "COMPAS-ICDHeader";
    public static final String ICD_SYSTEM_VERSION_UUID = "ICDSystemVersionUUID";
    public static final String IED_NAME = "IEDName";
    public static final String HEADER_ID = "headerId";
    public static final String HEADER_VERSION = "headerVersion";
    public static final String HEADER_REVISION = "headerRevision";
    public static final String IED_TYPE = "IEDType";
    public static final String VENDOR_NAME = "VendorName";
    public static final String IED_REDUNDANCY = "IEDredundancy";
    public static final String IED_MODEL = "IEDmodel";
    public static final String HW_REV = "hwRev";
    public static final String SW_REV = "swRev";

}
