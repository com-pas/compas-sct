// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.examples;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.SclEditorService;
import org.lfenergy.compas.sct.commons.api.SclEditor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.UUID;

public class SctAppExample {

    private static final SclEditor sclEditor = new SclEditorService();

    public static void main( String[] args ) throws JAXBException {
        initSclWithSclService(UUID.randomUUID(), "1.0", "1.0");
    }

    public static SCL initSclWithSclService(UUID hId, String hVersion, String hRevision) throws JAXBException {
        SCL scl = sclEditor.initScl(hId, hVersion, hRevision);
        marshaller.marshal(scl, System.out);
        return scl;
    }

    private static final Marshaller marshaller;
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
