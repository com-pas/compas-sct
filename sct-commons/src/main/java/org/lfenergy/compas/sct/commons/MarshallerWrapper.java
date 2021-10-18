// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.core.commons.exception.CompasErrorCode;
import org.lfenergy.compas.core.commons.exception.CompasException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@Slf4j
public class MarshallerWrapper {
    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    protected MarshallerWrapper(Unmarshaller unmarshaller, Marshaller marshaller) {
        this.unmarshaller = unmarshaller;
        this.marshaller = marshaller;
    }

    public static SclMarshallerBuilder builder(){
        return new SclMarshallerBuilder();
    }

    public <T> String marshall(final T obj) {
        try {
            StringWriter sw = new StringWriter();
            Result result = new StreamResult(sw);
            marshaller.marshal(obj, result);

            return sw.toString();
        } catch (JAXBException exp) {
            String message = "Error marshalling the Class";
            log.error(message, exp);
            throw new CompasException(CompasErrorCode.MARSHAL_ERROR_CODE, message);
        }
    }

    public <T> T unmarshall(final byte[] xml, Class<T> cls) {
        ByteArrayInputStream input = new ByteArrayInputStream(xml);
        return unmarshall(input, cls);
    }

    public <T> T unmarshall(final InputStream xml, Class<T> cls) {
        try {
            Object result = unmarshaller.unmarshal(new StreamSource(xml));
            if (!result.getClass().isAssignableFrom(cls)) {
                throw new CompasException(CompasErrorCode.UNMARSHAL_ERROR_CODE,
                        "Error unmarshalling to the Class. Invalid class");
            }
            return cls.cast(result);
        } catch (JAXBException exp) {
            String message = "Error unmarshalling to the Class.";
            log.error(message, exp);
            throw new CompasException(CompasErrorCode.UNMARSHAL_ERROR_CODE, message);
        }
    }
}
