// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Objects;

@Slf4j
public class Utils {

    public static final String LEAVING_PREFIX = "<<< Leaving: ::";
    public static final String ENTERING_PREFIX = ">>> Entering: ::";

    private Utils() {
        throw new IllegalStateException("Utils class");
    }

    public static String entering() {
        return ENTERING_PREFIX +
            getMethodName();
    }

    public static String leaving(Long startTime) {
        if (startTime == null || startTime <= 0) {
            return LEAVING_PREFIX +
                getMethodName();
        }
        return LEAVING_PREFIX +
            getMethodName() +
            " - Timer duration: " +
            (System.nanoTime() - startTime) / Math.pow(10, 9) +
            " sec.";
    }

    private static String getMethodName() {
        try {
            return (new Throwable()).getStackTrace()[2].getMethodName();
        } catch (Exception e) {
            return "-";
        }
    }

    public static String leaving() {
        return LEAVING_PREFIX +
            getMethodName();
    }

    /**
     * Returns the first {@link Field} in the hierarchy for the specified name
     */
    public static Field getField(Class<?> clazz, String name) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(name);
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        log.error("Cannot find field with name '{}' on class '{}'", name, clazz.getName());
        return null;
    }

    /**
     * Sets {@code value} to the first {@link Field} in the {@code object} hierarchy, for the specified name
     */
    public static void setField(Object object, String fieldName, Object value) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(fieldName);
        try {
            Field field = getField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(object, value);
            }
        } catch (Exception e) {
            log.error("Cannot set value on field {}.", fieldName, e);
        }
    }
}
