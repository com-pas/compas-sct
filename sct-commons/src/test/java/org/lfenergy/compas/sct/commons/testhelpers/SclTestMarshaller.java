// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.testhelpers;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SclTestMarshaller {

    private static final String CONTEXT_PATH = "org.lfenergy.compas.scl2007b4.model";

    private static JAXBContext jaxbContext;
    private static Schema schema;

    private static JAXBContext getJAXBContextInstance() {
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(CONTEXT_PATH);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return jaxbContext;
    }

    private static Schema getSchemaInstance() {
        if (schema == null) {
            schema = newSchema();
        }
        return schema;
    }

    private static SCL unmarshal(InputStream inputStream, String displayName) {
        Objects.requireNonNull(inputStream);
        try {
            // Create Unmarshaller instance each time to ensure thread safety
            return (SCL) getJAXBContextInstance().createUnmarshaller().unmarshal(new StreamSource(inputStream));
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to unmarshal : '%s'. %s".formatted(displayName, getCauseMessage(e)), e);
        }
    }

    public static String marshal(SCL scl) {
        Objects.requireNonNull(scl);
        try {
            // Create Marshaller instance each time to ensure thread safety
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            getJAXBContextInstance().createMarshaller().marshal(scl, outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to marshal SCL : '%s'".formatted(getCauseMessage(e)), e);
        }
    }

    /**
     * Retourne le message de l'exception ou, s'il est vide, le message de la cause.
     * Utile pour les JAXBException qui ont souvent un message "null" et dont le message pertinent est celui de la cause.
     *
     * @param throwable un throwable
     * @return le message du throwable s'il n'est pas vide ou null, sinon le message de la cause. Attention, la valeur retournée peut être "null".
     */
    private static String getCauseMessage(Throwable throwable) {
        if (StringUtils.isNotBlank(throwable.getMessage()) || throwable.getCause() == null) {
            return throwable.getMessage();
        }
        return throwable.getCause().getMessage();
    }

    public static SCL getSCLFromResource(String name) {
        InputStream inputStream = SclTestMarshaller.class.getClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            throw new UncheckedIOException(new IOException("Resource not found : " + name));
        }
        return unmarshal(inputStream, "resource: " + name);
    }

    private static Schema newSchema() {
        try {
            // Utilise SAXParser pour pouvoir traiter les "<include>" du schéma XSD SCL_61850_SCHEMA
            XMLReader xmlReader = SAXParserFactory.newDefaultNSInstance().newSAXParser().getXMLReader();
            Source[] schemaSources = Stream.of("xsd/SCL2007B4/SCL.xsd", "xsd/SCL_CoMPAS.xsd")
                    .map(xsdPath -> toSchemaSource(xsdPath, xmlReader))
                    .toArray(Source[]::new);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "jar,file,nested");
            return schemaFactory.newSchema(schemaSources);
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Source toSchemaSource(String name, XMLReader xmlReader) {
        URL url = SclTestMarshaller.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new UncheckedIOException(new IOException("Error loading resource : " + name));
        }
        InputSource inputSource = new InputSource(url.toString());
        return new SAXSource(xmlReader, inputSource);
    }

    public static void assertSclValidateXsd(SCL scl) {
        Objects.requireNonNull(scl, "SCL cannot be null");
        // Crée une nouvelle instance de ValidatorHandler à chaque appel pour que la méthode soit thread-safe
        ValidatorHandler validatorHandler = getSchemaInstance().newValidatorHandler();
        ErrorHandlerCollector errorHandler = new ErrorHandlerCollector();
        validatorHandler.setErrorHandler(errorHandler);
        try {
            // Crée une nouvelle instance de Marshaller à chaque appel pour que la méthode soit thread-safe
            Marshaller marshaller = getJAXBContextInstance().createMarshaller();
            marshaller.marshal(scl, validatorHandler);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        List<SAXParseException> saxParseExceptions = errorHandler.getExceptions();
        if (!saxParseExceptions.isEmpty()) {
            String aggregatedMessage = saxParseExceptions.stream()
                    .map(saxParseException -> {
                        int lineNumber = saxParseException.getLineNumber();
                        int columnNumber = saxParseException.getColumnNumber();
                        // The first line starts at 1 and the first column starts at 1 according to the SAXParseException documentation.
                        // The line and column are 0 when validating SCL instances, because there is no file concept in that case.
                        if (lineNumber > 0 || columnNumber > 0) {
                            return "\n-" + " line " + lineNumber + ", column " + columnNumber + ": " + saxParseException.getMessage();
                        } else {
                            return "\n- " + saxParseException.getMessage();
                        }
                    })
                    .collect(Collectors.joining(""));

            throw new AssertionError("Validation failed with " + saxParseExceptions.size() + " errors : " + aggregatedMessage);
        }
    }
}
