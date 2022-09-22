// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.examples;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.SclService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.Optional;
import java.util.UUID;

public class SctAppExample {

    public static void main( String[] args ) throws JAXBException {
        initSclWithSclService(Optional.empty(), "1.0", "1.0");
    }

    public static SclRootAdapter initSclWithSclService(Optional<UUID> hId, String hVersion, String hRevision) throws JAXBException {
        SclRootAdapter scl = SclService.initScl(hId, hVersion, hRevision);
        marshaller.marshal(scl.getCurrentElem(), System.out);
        return scl;
    }

    private static Marshaller marshaller;
    static{
        try {
            JAXBContext context = JAXBContext.newInstance(SCL.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
}
