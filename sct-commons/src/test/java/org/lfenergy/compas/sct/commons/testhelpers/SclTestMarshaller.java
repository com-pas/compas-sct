// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.apache.commons.io.IOUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SclTestMarshaller {

    public static SCL getSCLFromFile(String filename) throws Exception {
        byte[] rawXml = IOUtils.resourceToByteArray(filename);
        return MarshallerWrapper.unmarshall(rawXml, SCL.class);
    }

    public static String assertIsMarshallable(SCL scl) {
        return assertDoesNotThrow(() -> MarshallerWrapper.marshall(scl));
    }

}
