// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.core.commons.exception.CompasException;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class SclMarshallerBuilderTest {

    @Test
    void withProperties() {
    }

    @Test
    void testGetResource() {
        SclMarshallerBuilder sclMarshallerBuilder = new SclMarshallerBuilder();
        URL url = assertDoesNotThrow(() -> sclMarshallerBuilder.getResource("classpath:marshaller-builder/Employee.xsd"));
        String path = url.getPath();
        int idx = path.indexOf(':'); // for windows
        if(idx > 0){
            path = path.substring(idx + 1);
        }
        String finalPath = path;
        assertDoesNotThrow(() -> sclMarshallerBuilder.getResource(finalPath));
        assertThrows(IOException.class, () -> sclMarshallerBuilder.getResource("classpath:Employee.xsd"));
        assertThrows(IOException.class, () -> sclMarshallerBuilder.getResource("A"));
    }

    @Test
    void build() {
        SclMarshallerBuilder sclMarshallerBuilder = new SclMarshallerBuilder();
        SclMarshallerBuilder.SchemaConfig schemaConfig =
                sclMarshallerBuilder.new SchemaConfig("A", "B", "C");

        assertThrows(CompasException.class, () -> schemaConfig.getXsdPath());

        assertThrows(CompasException.class, () -> sclMarshallerBuilder.build());
        assertThrows(CompasException.class, () -> sclMarshallerBuilder.withProperties("classpath:dummy.yml").build());
        assertThrows(CompasException.class,
                () -> sclMarshallerBuilder.withProperties("classpath:marshaller-builder/scl_schema_error.yml").build());
        MarshallerWrapper marshallerWrapper = sclMarshallerBuilder.withProperties("classpath:scl_schema.yml").build();
        assertNotNull(marshallerWrapper);
    }
}