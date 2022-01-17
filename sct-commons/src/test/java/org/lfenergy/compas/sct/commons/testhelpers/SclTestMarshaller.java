// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.apache.commons.io.IOUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.MarshallerWrapper;

public class SclTestMarshaller {

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
