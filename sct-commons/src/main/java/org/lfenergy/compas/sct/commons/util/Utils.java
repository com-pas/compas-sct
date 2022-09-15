// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public final class Utils {

    public static final String LEAVING_PREFIX = "<<< Leaving: ::";
    public static final String ENTERING_PREFIX = ">>> Entering: ::";

    /**
     * Private Controlller, should not be instanced
     */
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generic message
     * @return >>> Entering: :: + methode name
     */
    public static String entering() {
        return ENTERING_PREFIX +
            getMethodName();
    }

    /**
     * Generic message for leaving a methode call, its calculates CPU time taken by methode execution
     * @param startTime methode call start time
     * @return message with methode name and duration
     */
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

    /**
     * Gets methode name
     * @return methode name
     */
    private static String getMethodName() {
        try {
            return (new Throwable()).getStackTrace()[2].getMethodName();
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * Generic message for leaving a methode call
     * @return >>> Entering: :: + methode name
     */
    public static String leaving() {
        return LEAVING_PREFIX +
            getMethodName();
    }

    /**
     * Test if two fields with primitive values are equals or are both not set.
     * @param o1 object to compare
     * @param o2 object to compare
     * @param isSet predicate that returns if fields is set
     * @param getValue getter that return the unboxed field
     * @return true if both fields are set and are equals, or if both fields are not set. False otherwise.
     */
    public static <T, R> boolean equalsOrNotSet(T o1, T o2, Predicate<T> isSet, Function<T, R> getValue) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        if (!isSet.test(o1)){
            return !isSet.test(o2);
        }
        if (!isSet.test(o2)){
            return false;
        }
        return Objects.equals(getValue.apply(o1), getValue.apply(o2));
    }

    /**
     * Builds string
     * @param name name to display
     * @param value value to display
     * @return not (name) or name=value
     */
    public static String xpathAttributeFilter(String name, String value) {
        if (value == null){
            return String.format("not(@%s)", name);
        } else {
            return String.format("@%s=\"%s\"", name, value);
        }
    }

    /**
     * Builds string
     * @param name name to display
     * @param value values to display
     * @return not (name) or name=values
     */
    public static String xpathAttributeFilter(String name, Collection<String> value) {
        if (value == null || value.isEmpty() || value.stream().allMatch(Objects::isNull)){
            return String.format("not(@%s)", name);
        } else {
            return xpathAttributeFilter(name, value.stream().filter(Objects::nonNull).collect(Collectors.joining(" ")));
        }
    }
}
