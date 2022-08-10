// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.*;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A representation of the literals of the enumeration '<em><b>Private Enum</b></em>',
 * and utility methods for working with them.
 */
public enum PrivateEnum {

    COMPAS_BAY("COMPAS-Bay", TCompasBay.class),
    COMPAS_CRITERIA("COMPAS-Criteria", TCompasCriteria.class),
    COMPAS_FLOW("COMPAS-Flow", TCompasFlow.class),
    COMPAS_FUNCTION("COMPAS-Function", TCompasFunction.class),
    COMPAS_ICDHEADER("COMPAS-ICDHeader", TCompasICDHeader.class),
    COMPAS_LDEVICE("COMPAS-LDevice", TCompasLDevice.class),
    COMPAS_SCL_FILE_TYPE("COMPAS-SclFileType", TCompasSclFileType.class),
    COMPAS_SYSTEM_VERSION("COMPAS-SystemVersion", TCompasSystemVersion.class);

    private static final Map<Class<?>, PrivateEnum> classToEnum = Arrays.stream(PrivateEnum.values()).collect(Collectors.toMap(
        compasPrivateEnum -> compasPrivateEnum.compasClass,
        Function.identity()));

    @Getter
    private final String privateType;
    @Getter
    private final Class<?> compasClass;

    <T> PrivateEnum(String privateType, Class<T> compasClass) {
        this.privateType = privateType;
        this.compasClass = compasClass;
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

    @Override
    public String toString() {
        return privateType;
    }

}
