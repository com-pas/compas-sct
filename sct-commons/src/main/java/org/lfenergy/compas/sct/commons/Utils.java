// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class Utils {

    private Utils() {
        throw new IllegalStateException("Utils class");
    }

    public static String entering(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> ");
        stringBuilder.append("Entering: ");
        stringBuilder.append("-::");
        stringBuilder.append(getMethodName());
        return stringBuilder.toString();
    }

    public static String leaving(Long startTime){
        if(startTime == null || startTime <= 0){
            return leaving();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<<< ");
        stringBuilder.append("Leaving: ");
        stringBuilder.append("-::");
        stringBuilder.append(getMethodName());
        stringBuilder.append(" - Timer duration: ");
        stringBuilder.append((System.nanoTime() - startTime)/Math.pow(10,9));
        stringBuilder.append(" sec.");
        return stringBuilder.toString();
    }

    public static String getMethodName() {
        try {
            return (new Throwable()).getStackTrace()[2].getMethodName();
        } catch (Exception e){
            // do nothing
        }
        return "-";
    }

    public static String leaving(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<<< ");
        stringBuilder.append("Leaving: ");
        stringBuilder.append("::");
        stringBuilder.append(getMethodName());
        return stringBuilder.toString();
    }

    /**
     * Returns the first {@link Field} in the hierarchy for the specified name
     */
    public static Field getField(Class<?> clazz, String name) {
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        return field;
    }

    /**
     * Sets {@code value} to the first {@link Field} in the {@code object} hierarchy, for the specified name
     */
    public static void setField(Object object, String fieldName, Object value) {
        try {
            Field field = getField(object.getClass(), fieldName);
            if(field != null){
                field.setAccessible(true);
                field.set(object, value);
            }
        } catch (Exception e) {
            log.error("Cannot nullify {} : ",fieldName, e);
        }
    }
}
