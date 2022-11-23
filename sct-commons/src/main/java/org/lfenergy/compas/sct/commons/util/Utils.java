// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Utils {

    private static final String LEAVING_PREFIX = "<<< Leaving: ::";
    private static final String ENTERING_PREFIX = ">>> Entering: ::";

    /**
     * Private Controlller, should not be instanced
     */
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generic message
     *
     * @return >>> Entering: :: + methode name
     */
    public static String entering() {
        return ENTERING_PREFIX +
            getMethodName();
    }

    /**
     * Generic message for leaving a methode call, its calculates CPU time taken by methode execution
     *
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
     *
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
     *
     * @return >>> Entering: :: + methode name
     */
    public static String leaving() {
        return LEAVING_PREFIX +
            getMethodName();
    }

    /**
     * Test if two fields with primitive values are equals or are both not set.
     *
     * @param o1       object to compare
     * @param o2       object to compare
     * @param isSet    predicate that returns if fields is set
     * @param getValue getter that return the unboxed field
     * @return true if both fields are set and are equals, or if both fields are not set. False otherwise.
     */
    public static <T, R> boolean equalsOrNotSet(T o1, T o2, Predicate<T> isSet, Function<T, R> getValue) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        if (!isSet.test(o1)) {
            return !isSet.test(o2);
        }
        if (!isSet.test(o2)) {
            return false;
        }
        return Objects.equals(getValue.apply(o1), getValue.apply(o2));
    }

    /**
     * Builds string
     *
     * @param name  name to display
     * @param value value to display
     * @return not (name) or name=value
     */
    public static String xpathAttributeFilter(String name, String value) {
        if (value == null) {
            return String.format("not(@%s)", name);
        } else {
            return String.format("@%s=\"%s\"", name, value);
        }
    }

    /**
     * Builds string
     *
     * @param name  name to display
     * @param value values to display
     * @return not (name) or name=values
     */
    public static String xpathAttributeFilter(String name, Collection<String> value) {
        if (value == null || value.isEmpty() || value.stream().allMatch(Objects::isNull)) {
            return String.format("not(@%s)", name);
        } else {
            return xpathAttributeFilter(name, value.stream().filter(Objects::nonNull).collect(Collectors.joining(" ")));
        }
    }

    /**
     * Checks if strings are equals or both blank.
     * Blank means : null, empty string or whitespaces only string.
     *
     * @param s1 first string
     * @param s2 seconde string
     * @return true if strings are equals or both blank, false otherwise
     * @see org.apache.commons.lang3.StringUtils#isBlank(CharSequence)
     */
    public static boolean equalsOrBothBlank(String s1, String s2) {
        return Objects.equals(s1, s2)
            || (StringUtils.isBlank(s1) && StringUtils.isBlank(s2));
    }

    /**
     * Remove all digits at the end of the string, if any
     * @param s input string
     * @return s without digits at the end.
     * Guarantees that the last character of the return value is not a digit.
     * If s is composed only of digits, the result is an empty string
     * @see Character#isDigit(int)
     */
    public static String removeTrailingDigits(String s) {
        Objects.requireNonNull(s);
        if (s.isEmpty()) {
            return s;
        }
        int digitsPos = s.length();
        while (digitsPos >= 1 && Character.isDigit(s.codePointAt(digitsPos - 1))) {
            digitsPos--;
        }
        return s.substring(0, digitsPos);
    }

    /**
     * Split string s using regexDelimiter into an array, and return element at given index.
     * @param s string to split
     * @param regexDelimiter delimiter
     * @param index index of the element in the split array (0 being the first element).
     *              If index is a negative integer, position is counted from the end (-1 behind the last element).
     * @return the element at position index in the split array, or null if index is out of bound
     * @see String#split(String)
     */
    public static String extractField(String s, String regexDelimiter, int index) {
        Objects.requireNonNull(s);
        String[] split = s.split(regexDelimiter);
        int column = index < 0 ? split.length + index : index;
        return 0 <= column && column < split.length ? split[column] : null;
    }
}
