// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import org.apache.commons.io.IOUtils;
import org.lfenergy.compas.sct.commons.model.da_comm.DACOMM;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DaComTestMarshallerHelper {

    public static DACOMM getDACOMMFromFile(String filename) {
        byte[] rawXml;
        try {
            rawXml = IOUtils.resourceToByteArray(filename);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new DaComParamTestMarshaller.Builder().build().unmarshall(rawXml);
    }

}
