// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Utils {

    private static final String LEAVING_PREFIX = "<<< Leaving: ::";
    private static final String ENTERING_PREFIX = ">>> Entering: ::";
    private static final int S1_CONSIDERED_EQUALS_TO_S2 = 0;
    private static final int S1_LOWER_THAN_S2 = -1;
    private static final int S1_GREATER_THAN_S2 = 1;

    /**
     * Private Constructor, should not be instanced
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
     * Blank means : null, empty string or whitespaces (as defined by {@link Character#isWhitespace(char)}) only string.
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
     * Comparator for String
     * Difference with {@link String#compare(CharSequence, CharSequence)} is that
     * blank strings are considered equals and inferior to any not blank String.
     * Blank means : null, empty string or whitespaces (as defined by {@link Character#isWhitespace(char)}) only string.
     * Note: this comparator imposes orderings that are inconsistent with equals.
     *
     * @param s1 first String to compare
     * @param s2 second String to compare
     * @return when s1 and s2 are not blank, same result as {@link String#compare(CharSequence, CharSequence)},
     *  zero when s1 and s2 are both blanks, negative integer when s1 is blank and s2 is not, positive integer when s1 is not blank but s2 is.
     * @see java.util.Comparator#compare(Object, Object)
     * @see org.apache.commons.lang3.StringUtils#isBlank(CharSequence)
     * @see java.util.Comparator#nullsFirst(Comparator)
     */
    public static int blanksFirstComparator(String s1, String s2) {
        if (StringUtils.isBlank(s1)){
            if (StringUtils.isBlank(s2)){
                return S1_CONSIDERED_EQUALS_TO_S2;
            } else {
                return S1_LOWER_THAN_S2;
            }
        } else {
            if (StringUtils.isBlank(s2)){
                return S1_GREATER_THAN_S2;
            } else {
                return s1.compareTo(s2);
            }
        }
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

    /**
     * return null if input string is blank.
     * Blank means : null, empty string or whitespaces (as defined by {@link Character#isWhitespace(char)}) only string.
     * @param s string to split
     * @return null if input string is blank, else s parameter
     */
    public static String nullIfBlank(String s) {
        return StringUtils.isBlank(s) ? null : s;
    }

    /**
     * return empty String (i.e "") if input string is blank.
     * Blank means : null, empty string or whitespaces (as defined by {@link Character#isWhitespace(char)}) only string.
     * @param s string to split
     * @return empty string (i.e "") if input string is blank, else s parameter
     */
    public static String emptyIfBlank(String s) {
        return StringUtils.isBlank(s) ? "" : s;
    }

    /**
     * Check if lnClass as List os the same as lnClass as a String.
     * For some reason, xjc plugin generates a List&lt;String&gt; instead of a String for the lnClass attribute.
     * When marshalling an XML file, lnClass is represented as a List with a single element.
     * @param lnClass1 lnClass attribute value represented as a list
     * @param lnClass2 lnClass attribute value represented as a String
     * @return true if lnClass2 is blank and lnClass1 is either null, empty or contains a blank String.
     *         true if lnClass2 is not blank and lnClass1 contains lnClass2.
     *         false otherwise.
     * Blank means : null, empty string or whitespaces (as defined by {@link Character#isWhitespace(char)}) only string.
     * @throws IllegalArgumentException when lnClass1 contains more than one element
     */
    public static boolean lnClassEquals(List<String> lnClass1, String lnClass2){
        if (lnClass1 == null || lnClass1.isEmpty()){
            return StringUtils.isBlank(lnClass2);
        }
        if (lnClass1.size() > 1){
            throw new IllegalArgumentException("lnClass can only have a single value but got : [%s] " + String.join(",", lnClass1));
        }
        return equalsOrBothBlank(lnClass1.get(0), lnClass2);
    }
}
