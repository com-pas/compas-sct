// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.app.testhelpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.lfenergy.compas.core.commons.exception.CompasErrorCode;
import org.lfenergy.compas.core.commons.exception.CompasException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class SclMarshallerBuilder {

    private static final String COMPAS_SCL_SCHEMAS_JSONPATH = "/compas/scl/schemas";
    private static final String CONTEXT_PATH_PROP = "contextPath";
    private static final String XSD_PATH_PROP = "xsdPath";
    private static final String NAMESPACE_PROP = "namespace";

    // Path to the YAML File containing the schema properties.
    private String yamlFilePath;
    private Map<String,String> jaxbProperties;

    public SclMarshallerBuilder withProperties(String yamlFilePath) {
        this.yamlFilePath = yamlFilePath;
        return this;
    }


    public URL getResource(String filePath) throws IOException {
        final String clsPathIndicator = "classpath:";
        if(filePath.startsWith(clsPathIndicator)){
            String classPathRes = filePath.substring(clsPathIndicator.length());
            return IOUtils.resourceToURL("/" + classPathRes);
        }
        Path path = Paths.get(filePath).toAbsolutePath();
        if(!Files.exists(path)) {
            throw new  IOException(path + ": No such file or directory");
        }
        return path.toUri().toURL();
    }


    private List<SchemaConfig> getSchemaConfigs() {
        if (yamlFilePath == null || yamlFilePath.isBlank()) {
            throw new CompasException(CompasErrorCode.CONFIGURATION_ERROR_CODE,
                    "No configuration file configured (yamlFilePath)");
        }

        URL source;
        try {
            source = getResource(yamlFilePath);
        } catch (IOException e){
            String message = String.format("Resource %s not found", yamlFilePath);
            log.error(message, e);
            throw new CompasException(CompasErrorCode.RESOURCE_NOT_FOUND_ERROR_CODE,message);
        }

        List<SchemaConfig> schemaConfigs = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            JsonNode jsonNode = objectMapper.readTree(source);

            JsonNode pathsNode = jsonNode.at(COMPAS_SCL_SCHEMAS_JSONPATH);
            if (pathsNode != null && pathsNode.getNodeType() == JsonNodeType.ARRAY) {
                Iterable<JsonNode> nodes = pathsNode::elements;
                // Walk through the schemas and check if all needed parameters are filled.
                nodes.forEach(node -> {
                    if (node.has(XSD_PATH_PROP) && node.has(NAMESPACE_PROP) && node.has(CONTEXT_PATH_PROP)) {
                        schemaConfigs.add(
                                new SchemaConfig(node.get(XSD_PATH_PROP).textValue(),
                                        node.get(NAMESPACE_PROP).textValue(),
                                        node.get(CONTEXT_PATH_PROP).textValue()));
                    } else {
                        throw new CompasException(CompasErrorCode.PROPERTY_ERROR_ERROR_CODE,
                                String.format("One of the properties (%s, %s, %s) has no value",
                                        XSD_PATH_PROP, NAMESPACE_PROP, CONTEXT_PATH_PROP));
                    }
                });
            } else {
                throw new CompasException(CompasErrorCode.CONFIGURATION_ERROR_CODE,
                        String.format("Configuration for marshaller (%s) didn't contain the path %s",
                                yamlFilePath, COMPAS_SCL_SCHEMAS_JSONPATH));
            }
        } catch (IOException exp) {
            var message = "I/O Error reading YAML File";
            log.error(message, exp);
            throw new CompasException(CompasErrorCode.INVALID_YML_ERROR_CODE, message);
        }

        return schemaConfigs;
    }

    private List<String> getContextPaths(List<SchemaConfig> schemaConfigs) {
        // Convert the SchemaConfig List to a List containing only the ContextPaths.
        return schemaConfigs.stream()
                .map(SchemaConfig::getContextPath)
                .collect(Collectors.toList());
    }

    private String getImportStatements(List<SchemaConfig> schemaConfigs) {
        // Convert the SchemaConfig List to a String containing the Import Statements sued to combine multiple
        // XSD Schemas.
        return schemaConfigs.stream()
                .map(schemaConfig ->
                        "<xs:import namespace=\"" + schemaConfig.getNamespace() + "\" schemaLocation=\""
                                + schemaConfig.getXsdPath() + "\"/>\n")
                .collect(Collectors.joining());
    }

    public MarshallerWrapper build() {
        try {
            // Create the JAXB Context with the configured context paths. The list is separated by a colon
            // as needed by the JAxbContext.
            var schemaConfigs = getSchemaConfigs();
            var contextPaths = String.join(":", getContextPaths(schemaConfigs));

            var jaxbContext = JAXBContext.newInstance(contextPaths);
            var jaxbMarshaller = jaxbContext.createMarshaller();
            var jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Setup schema validator
            var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // To make load of all different type of schemas and also make imports/includes in these schemas
            // work the solution is to make a combined XSD Schema containing all other schemas as import.
            // Import is used, because the schemas can have different namespaces.
            var combinedXsdSchema =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                            + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "
                            + "elementFormDefault=\"qualified\">\n"
                            + getImportStatements(schemaConfigs)
                            + "</xs:schema>";
            var schema = factory.newSchema(
                    new StreamSource(new StringReader(combinedXsdSchema), "topSchema")
            );
            jaxbMarshaller.setSchema(schema);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbUnmarshaller.setSchema(schema);

            return new MarshallerWrapper(jaxbUnmarshaller,jaxbMarshaller);

        } catch (JAXBException | SAXException exp) {
            var message = "Error creating JAXB Marshaller and Unmarshaller.";
            log.error(message, exp);
            throw new CompasException(CompasErrorCode.CREATION_ERROR_CODE, message);
        }
    }

    /**
     * POJO Class to hold all the schema configuration, so that we only need to walk through the JSON Content once.
     */
    protected class SchemaConfig {
        private final String xsdPath;
        private final String namespace;
        private final String contextPath;

        public SchemaConfig(String xsdPath, String namespace, String contextPath) {
            // Convert the XSD Path to a URL, that will be used in the import statement.
            // If the XSD Path is not correct loading will fail.
            this.xsdPath = xsdPath;
            this.namespace = namespace;
            this.contextPath = contextPath;
        }

        public String getXsdPath() {
            try {
                return getResource(xsdPath).getPath();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
                throw new CompasException(CompasErrorCode.RESOURCE_NOT_FOUND_ERROR_CODE,e.getMessage());
            }
        }

        public String getNamespace() {
            return namespace;
        }

        public String getContextPath() {
            return contextPath;
        }
    }
}


