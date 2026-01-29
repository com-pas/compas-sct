// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.lfenergy.compas.sct.commons.model.da_comm.DACOMM;

import java.io.InputStream;
import java.util.Objects;

public class DaComTestMarshallerHelper {

    public static DACOMM getDACOMMFromResource(String resource) {
        InputStream inputStream = Objects.requireNonNull(DaComTestMarshallerHelper.class.getClassLoader().getResourceAsStream(resource));
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DACOMM.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (DACOMM) unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
