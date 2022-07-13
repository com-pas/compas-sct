// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.*;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBElement;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PrivateEnum {

    COMPAS_BAY("COMPAS-Bay", TCompasBay.class, (new ObjectFactory())::createBay),
    COMPAS_CRITERIA("COMPAS-Criteria", TCompasCriteria.class, (new ObjectFactory())::createCriteria),
    COMPAS_FLOW("COMPAS-Flow", TCompasFlow.class, (new ObjectFactory())::createFlow),
    COMPAS_FUNCTION("COMPAS-Function", TCompasFunction.class, (new ObjectFactory())::createFunction),
    COMPAS_ICDHEADER("COMPAS-ICDHeader", TCompasICDHeader.class, (new ObjectFactory())::createICDHeader),
    COMPAS_LDEVICE("COMPAS-LDevice", TCompasLDevice.class, (new ObjectFactory())::createLDevice),
    COMPAS_SCL_FILE_TYPE("COMPAS-SclFileType", TCompasSclFileType.class, (new ObjectFactory())::createSclFileType),
    COMPAS_SYSTEM_VERSION("COMPAS-SystemVersion", TCompasSystemVersion.class, (new ObjectFactory())::createSystemVersion);

    private static final Map<Class<?>, PrivateEnum> classToEnum = Arrays.stream(PrivateEnum.values()).collect(Collectors.toMap(
        compasPrivateEnum -> compasPrivateEnum.compasClass,
        Function.identity()));

    /**
     * By construction, the types of key and value are bound.
     * It means that for a class T, get(T) returns a Function&lt;T, ? extends JAXBElement&lt;T&gt;&gt;
     */
    private static final Map<Class<?>, Function<?, ? extends JAXBElement<?>>> classToJaxbWrapper = Arrays.stream(PrivateEnum.values()).collect(Collectors.toMap(
        compasPrivateEnum -> compasPrivateEnum.compasClass,
        compasPrivateEnum -> compasPrivateEnum.jaxbWrapper
    ));

    @Getter
    private final String privateType;
    @Getter
    private final Class<?> compasClass;

    private final Function<?, ? extends JAXBElement<?>> jaxbWrapper;

    <T> PrivateEnum(String privateType, Class<T> compasClass, Function<T, JAXBElement<T>> jaxbWrapper) {
        this.privateType = privateType;
        this.compasClass = compasClass;
        this.jaxbWrapper = jaxbWrapper;
    }

    @NotNull
    public static PrivateEnum fromClass(Class<?> compasClass) {
        PrivateEnum result = classToEnum.get(compasClass);
        if (result == null) {
            throw new NoSuchElementException(String.format("Class %s is not mapped to a compas type. See %s", compasClass.getName(),
                PrivateEnum.class.getName()));
        }
        return result;
    }

    @NotNull
    public static <T> JAXBElement<T> createJaxbElement(T compasElement) {
        Function<?, ? extends JAXBElement<?>>  jaxbWrapper = classToJaxbWrapper.get(compasElement.getClass());
        if (jaxbWrapper == null) {
            throw new NoSuchElementException(String.format("Class %s is not mapped to a compas type. See %s", compasElement.getClass().getName(),
                PrivateEnum.class.getName()));
        }
        // Cast is safe per construction of map classToJaxbWrapper
        return ((Function<T, ? extends JAXBElement<T>>) jaxbWrapper).apply(compasElement);
    }

    @Override
    public String toString() {
        return privateType;
    }

}
