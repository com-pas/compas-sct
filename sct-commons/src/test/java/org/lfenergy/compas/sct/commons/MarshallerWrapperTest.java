// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.core.commons.exception.CompasException;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;


import static org.junit.jupiter.api.Assertions.*;

class MarshallerWrapperTest {
    private static final String SCL_CONTENT = "<SCL xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns=\"http://www.iec.ch/61850/2003/SCL\" version=\"2007\" revision=\"B\" release=\"4\">\n" +
            "    <Header id=\"HeaderID\" version=\"version\" revision=\"Revision\"" +
            "            toolID=\"toolID\" nameStructure=\"IEDName\"/>\n" +
            "</SCL>";

    MarshallerWrapper marshallerWrapper;

    @BeforeEach
    void setUp(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SCL.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbMarshaller.setProperty("jaxb.formatted.output",true);
            marshallerWrapper = new MarshallerWrapper(jaxbUnmarshaller,jaxbMarshaller);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testMarshallShouldReturnOK() {
        SCL scd = Mockito.mock(SCL.class);
        Mockito.when(scd.getHeader()).thenReturn(new THeader());
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        String scl = marshallerWrapper.marshall(sclRootAdapter.getCurrentElem());
        System.out.println(scl);
    }

    @Test
    void testMarshallShouldReturnNOK() throws JAXBException {

        Marshaller marshaller = Mockito.mock(Marshaller.class);
        SCL scd = Mockito.mock(SCL.class);
        Mockito.when(scd.getHeader()).thenReturn(new THeader());

        Unmarshaller unmarshaller = Mockito.mock(Unmarshaller.class);
        MarshallerWrapper marshallerWrapper = new MarshallerWrapper(unmarshaller,marshaller);
        Mockito.doThrow(JAXBException.class).when(marshaller).marshal(ArgumentMatchers.any(SCL.class),
                ArgumentMatchers.any(Result.class));

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        assertThrows( CompasException.class, () -> marshallerWrapper.marshall(sclRootAdapter.getCurrentElem()));
    }

    @Test
    void testUnmarshallShouldReturnOK() {
        SCL scl = marshallerWrapper.unmarshall(SCL_CONTENT.getBytes(StandardCharsets.UTF_8),SCL.class);
        assertEquals( 4, scl.getRelease());
    }

    @Test
    void testUnmarshallShouldReturnNOK() throws JAXBException {
        SCL scl = marshallerWrapper.unmarshall(SCL_CONTENT.getBytes(StandardCharsets.UTF_8),SCL.class);
        assertThrows(CompasException.class, () -> marshallerWrapper.unmarshall(SCL_CONTENT.getBytes(StandardCharsets.UTF_8),
                THeader.class));



        Marshaller marshaller = Mockito.mock(Marshaller.class);
        Unmarshaller unmarshaller = Mockito.mock(Unmarshaller.class);
        MarshallerWrapper marshallerWrapper = new MarshallerWrapper(unmarshaller,marshaller);
        Mockito.when(unmarshaller.unmarshal(ArgumentMatchers.any(StreamSource.class))).thenThrow(JAXBException.class);

        assertThrows(CompasException.class,
                () -> marshallerWrapper.unmarshall(new ByteArrayInputStream("<SCL/>".getBytes()),SCL.class));
    }
}