// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public final class Utils {

    public static final String LEAVING_PREFIX = "<<< Leaving: ::";
    public static final String ENTERING_PREFIX = ">>> Entering: ::";

    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
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

}
