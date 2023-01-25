// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.lfenergy.compas.core.commons.exception.CompasErrorCode;
import org.lfenergy.compas.core.commons.exception.CompasException;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public class MarshallerWrapper {
    private static MarshallerWrapper singleton;

    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    public static String marshall(final Object obj) {
        try {
            StringWriter sw = new StringWriter();
            Result result = new StreamResult(sw);
            getInstance().marshaller.marshal(obj, result);

            return sw.toString();
        } catch (JAXBException exp) {
            String message = String.format("Error marshalling the Class: %s", exp);
            log.error(message, exp);
            throw new CompasException(CompasErrorCode.MARSHAL_ERROR_CODE, message);
        }
    }

    public static <T> T unmarshall(final byte[] xml, Class<T> cls) {
        ByteArrayInputStream input = new ByteArrayInputStream(xml);
        return unmarshall(input, cls);
    }

    public static <T> T unmarshall(final InputStream xml, Class<T> cls) {
        try {
            Object result = getInstance().unmarshaller.unmarshal(new StreamSource(xml));
            if (!result.getClass().isAssignableFrom(cls)) {
                throw new CompasException(CompasErrorCode.UNMARSHAL_ERROR_CODE,
                    "Error unmarshalling to the Class. Invalid class");
            }
            return cls.cast(result);
        } catch (JAXBException exp) {
            String message = String.format("Error unmarshalling to the Class: %s", exp.getLocalizedMessage());
            log.error(message, exp);
            throw new CompasException(CompasErrorCode.UNMARSHAL_ERROR_CODE, message, exp);
        }
    }

    private static MarshallerWrapper getInstance() {
        if (singleton == null) {
            try {
                singleton = createMarshallerWrapper();
            } catch (JAXBException | SAXException | ParserConfigurationException exp) {
                var message = "Error creating JAXB Marshaller and Unmarshaller.";
                log.error(message, exp);
                throw new CompasException(CompasErrorCode.CREATION_ERROR_CODE, message, exp);
            }
        }
        return singleton;
    }

    private static MarshallerWrapper createMarshallerWrapper() throws JAXBException, SAXException, ParserConfigurationException {
        // Create marshaller and unmarshaller
        JAXBContext jaxbContext = JAXBContext.newInstance("org.lfenergy.compas.scl2007b4.model");
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        // Setup schema validation
        Schema schema = loadSchema();
        jaxbMarshaller.setSchema(schema);
        jaxbUnmarshaller.setSchema(schema);

        return new MarshallerWrapper(jaxbUnmarshaller, jaxbMarshaller);
    }

    private static Schema loadSchema() throws SAXException, ParserConfigurationException {
        XMLReader xmlReader = SAXParserFactory.newDefaultNSInstance().newSAXParser().getXMLReader();
        Source[] schemaSources = Stream.of("/xsd/SCL2007B4/SCL.xsd", "/xsd/SCL_CoMPAS.xsd")
            .map(xsdPath -> toSchemaSource(xsdPath, xmlReader))
            .toArray(Source[]::new);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schemaFactory.newSchema(schemaSources);
    }

    private static Source toSchemaSource(String path, XMLReader xmlReader) {
        URL url;
        try {
            url = IOUtils.resourceToURL(path);
        } catch (IOException e) {
            var message = "Error loading XML schema : " + path;
            log.error(message, e);
            throw new CompasException(CompasErrorCode.CREATION_ERROR_CODE, message);
        }
        InputSource inputSource = new InputSource(url.toString());
        return new SAXSource(xmlReader, inputSource);
    }

    public static void assertValidateXmlSchema(SCL scl){
        Assertions.assertThatNoException().isThrownBy(() -> marshall(scl));
    }

}
