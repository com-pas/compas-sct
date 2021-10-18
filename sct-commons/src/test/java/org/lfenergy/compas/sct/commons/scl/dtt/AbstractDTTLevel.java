// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.apache.commons.io.IOUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.sct.commons.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.mockito.Mockito;


public abstract class AbstractDTTLevel<P extends SclElementAdapter, T> {
    public static final String SCD_DTT = "/dtt-test-schema-conf/scd_dtt_import_test.xml";
    public static final String SCD_DTT_DIFF_CONTENT_SAME_ID = "/dtt-test-schema-conf/scd_dtt_import_sameid-diff-content-test.xml";

    protected P sclElementAdapter;
    protected T sclElement;

    public void init(){
       sclElementAdapter = (P) getMockedSclParentAdapter();
       completeInit();
    }

    protected static DataTypeTemplateAdapter initDttAdapterFromFile(String fileName) throws Exception {
        SCL scd = AbstractDTTLevel.getSCLFromFile(fileName);
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        return new DataTypeTemplateAdapter(
                sclRootAdapter,
                scd.getDataTypeTemplates()
        );
    }

    protected SclElementAdapter getMockedSclParentAdapter(){
        DataTypeTemplateAdapter dataTypeTemplateAdapter = Mockito.mock(DataTypeTemplateAdapter.class);
        TDataTypeTemplates tDataTypeTemplates = Mockito.mock(TDataTypeTemplates.class);
        Mockito.when(dataTypeTemplateAdapter.getCurrentElem()).thenReturn(tDataTypeTemplates);

        return dataTypeTemplateAdapter;
    }
    protected abstract void completeInit() ;

    public static SCL getSCLFromFile(String filename) throws Exception {
        MarshallerWrapper marshallerWrapper = createWrapper();
        byte[] rawXml = IOUtils.resourceToByteArray(filename);
        return marshallerWrapper.unmarshall(rawXml,SCL.class);
    }

    public static MarshallerWrapper createWrapper() {
        return MarshallerWrapper.builder()
                .withProperties("classpath:scl_schema.yml")
                .build();
    }
}
