// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.apache.commons.io.IOUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SclTestMarshaller {

    public static SCL getSCLFromFile(String filename) {
        byte[] rawXml;
        try {
            rawXml = IOUtils.resourceToByteArray(filename);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return MarshallerWrapper.unmarshall(rawXml, SCL.class);
    }

    public static String assertIsMarshallable(SCL scl) {
        return assertDoesNotThrow(() -> MarshallerWrapper.marshall(scl));
    }

}
