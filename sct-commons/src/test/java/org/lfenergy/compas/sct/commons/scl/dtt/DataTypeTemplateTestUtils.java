// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.experimental.UtilityClass;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

@UtilityClass
public class DataTypeTemplateTestUtils {
    public static final String SCD_DTT = "/dtt-test-schema-conf/scd_dtt_import_test.xml";
    public static final String SCD_DTT_DIFF_CONTENT_SAME_ID = "/dtt-test-schema-conf/scd_dtt_import_sameid-diff-content-test.xml";
    public static final String SCD_DTT_DO_SDO_DA_BDA = "/dtt-test-schema-conf/scd_dtt_do_sdo_da_bda.xml";

    public static DataTypeTemplateAdapter initDttAdapterFromFile(String fileName) {
        SCL scd = SclTestMarshaller.getSCLFromFile(fileName);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return new DataTypeTemplateAdapter(
                sclRootAdapter,
                scd.getDataTypeTemplates()
        );
    }

    public static TDataTypeTemplates initDttFromFile(String fileName) {
        return SclTestMarshaller.getSCLFromFile(fileName).getDataTypeTemplates();
    }

}
