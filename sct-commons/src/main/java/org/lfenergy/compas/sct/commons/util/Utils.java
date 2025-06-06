// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.util.JAXBSource;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import javax.xml.namespace.QName;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Utils {

    private static final String LEAVING_PREFIX = "<<< Leaving: ::";
    private static final String ENTERING_PREFIX = ">>> Entering: ::";
    private static final int S1_CONSIDERED_EQUALS_TO_S2 = 0;
    private static final int S1_LOWER_THAN_S2 = -1;
    private static final int S1_GREATER_THAN_S2 = 1;
    private static final long MAC_ADDRESS_MAX_VALUE = 0xFFFFFFFFFFFFL;
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("[0-9A-F]{2}([-:][0-9A-F]{2}){5}", Pattern.CASE_INSENSITIVE);
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    private static JAXBContext jaxbContext = null;

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
     * @see StringUtils#isBlank(CharSequence)
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
     * @see Comparator#compare(Object, Object)
     * @see StringUtils#isBlank(CharSequence)
     * @see Comparator#nullsFirst(Comparator)
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
        return equalsOrBothBlank(lnClass1.getFirst(), lnClass2);
    }

    /**
     * Converts long representation of a MAC-Address, by converting it to hexadecimal and separating every 2 characters by a hyphen(-).
     * See macAddressToLong for the reversing method.
     * @param macAddress a long between 0 and 0xFFFFFFFFFFFF
     * @return MAC address separated by hyphens(-). Letters are uppercase (A to F)
     *
     * @see Utils#macAddressToLong(String)
     */
    public static String longToMacAddress(long macAddress){
        if (macAddress < 0){
            throw new IllegalArgumentException("macAddress must be a positive integer but got : %d".formatted(macAddress));
        }
        if (macAddress > MAC_ADDRESS_MAX_VALUE){
            throw new IllegalArgumentException("macAddress cannot exceed %d but got : %d".formatted(MAC_ADDRESS_MAX_VALUE, macAddress));
        }
        String macText = toHex(macAddress, 12);
        return
            macText.substring(0, 2) + "-"
            + macText.substring(2, 4) + "-"
            + macText.substring(4, 6) + "-"
            + macText.substring(6, 8) + "-"
            + macText.substring(8, 10) + "-"
            + macText.substring(10, 12);
    }

    /**
     * Converts a MAC-Address to its long representation, by concatenating the digits and converting it from hexadecimal to long.
     * See longToMacAddress for the reversing method.
     * @param macAddress macAddress should be 6 groups of 2 hexadecimal digits (0 to 9 and A to F or a to f) separated by hyphens(–) or
     *                   colons(:)
     * @return long between 0 and 0xFFFFFFFFFFFF representing this MAC-Address
     *
     * @see Utils#longToMacAddress(long)
     */
    public static long macAddressToLong(String macAddress){
        if (!MAC_ADDRESS_PATTERN.matcher(macAddress).matches()) {
            throw new IllegalArgumentException(("macAddress should be 6 groups of 2 hexadecimal digits (0 to 9 and A to F) separated by hyphens(–) "
                + "or colons(:), but got : %s").formatted(macAddress));
        }
        String hex = macAddress.substring(0, 2)
            + macAddress.substring(3, 5)
            + macAddress.substring(6, 8)
            + macAddress.substring(9, 11)
            + macAddress.substring(12, 14)
            + macAddress.substring(15, 17);
        return Long.valueOf(hex, 16);
    }

    /**
     * Convert number to hexadecimal, with uppercase letters (A to F) and a minimum length (using left padding with zero when necessary).
     * @param number number to be converted in hexadecimal
     * @param length minimum length of resulting string.
     *               When hexadecimal form of number does not reach length, left padding with "0" is done.
     * @return hexadecimal, with uppercase letters (A to F) and minimum length of length.
     * Note that the length of return value can exceed "length" parameter when number hexadecimal form is longer than "length" parameter.
     */
    public static String toHex(long number, int length) {
        return StringUtils.leftPad(Long.toHexString(number).toUpperCase(), length, "0");
    }

    /**
     * creates a copy of Scl element
     *
     * @param object object to copy
     * @param clazz  class type of the object
     * @param <T>    type of the object
     * @return copy of the object
     */
    public static <T> T copySclElement(T object, Class<T> clazz) {
        Unmarshaller unmarshaller;
        try {
            if (jaxbContext == null) {
                jaxbContext = JAXBContext.newInstance("org.lfenergy.compas.scl2007b4.model");
            }
            unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<T> contentObject = new JAXBElement<>(new QName(clazz.getSimpleName()), clazz, object);
            JAXBSource source = new JAXBSource(jaxbContext, contentObject);
            return unmarshaller.unmarshal(source, clazz).getValue();
        } catch (JAXBException e) {
            throw new ScdException(e.getMessage(), e);
        }
    }

    /**
     * Extract content of P element of given type
     *
     * @param type type attribute of P element
     * @param listOfP list of P elements
     * @return content of the first P element matching the given type if it exists, else empty Optional.
     */
    public static Optional<String> extractFromP(String type, List<TP> listOfP) {
        return listOfP.stream().filter(tp -> type.equals(tp.getType())).map(TP::getValue).findFirst();
    }

    public static TLN copyLn(TLN tln) {
        TLN newLn = new TLN();
        newLn.getLnClass().addAll(tln.getLnClass());
        newLn.setInst(tln.getInst());
        newLn.setLnType(tln.getLnType());
        newLn.setPrefix(tln.getPrefix());
        newLn.setDesc(tln.getDesc());
        newLn.setInputs(tln.getInputs());
        newLn.setText(tln.getText());
        newLn.getPrivate().addAll(tln.getPrivate());
        newLn.getDataSet().addAll(tln.getDataSet());
        newLn.getAny().addAll(tln.getAny());
        newLn.getDOI().addAll(tln.getDOI().stream().map(Utils::createDOI).toList());
        newLn.getLog().addAll(tln.getLog());
        newLn.getLogControl().addAll(tln.getLogControl());
        newLn.getOtherAttributes().putAll(tln.getOtherAttributes());
        newLn.getReportControl().addAll(tln.getReportControl());
        return newLn;
    }

    private static TDOI createDOI(TDOI tdoi){
        TDOI newDOI = new TDOI();
        newDOI.setName(tdoi.getName());
        if(tdoi.isSetIx()){
            newDOI.setIx(tdoi.getIx());
        }
        if(tdoi.isSetSDIOrDAI()){
            newDOI.getSDIOrDAI().addAll((tdoi.getSDIOrDAI().stream().map(Utils::createSDIOrDAI).toList()));
        }
        if(tdoi.isSetPrivate()){
            newDOI.getPrivate().addAll(tdoi.getPrivate());
        }
        if(tdoi.isSetAny()){
            newDOI.getAny().addAll(tdoi.getAny());
        }
        updateUnNaming(newDOI, tdoi);
        return newDOI;
    }

    private static TUnNaming createSDIOrDAI(TUnNaming tUnNaming){
        return switch (tUnNaming) {
            case TDAI tdai -> createDAI(tdai);
            case TSDI tsdi -> createSDI(tsdi);
            default -> throw new IllegalStateException("Unexpected value: " + tUnNaming);
        };
    }

    private static TDAI createDAI(TDAI tdai){
        TDAI newDAI = new TDAI();
        newDAI.setName(tdai.getName());
        if(tdai.isSetVal()){
            newDAI.getVal().addAll((tdai.getVal().stream().map(tVal -> {
                TVal newVal = new TVal();
                newVal.setValue(tVal.getValue());
                if(tVal.isSetSGroup()){
                    newVal.setSGroup(tVal.getSGroup());
                }
                return newVal;
            }).toList()));
        }
        if(tdai.isSetIx()){
            newDAI.setIx(tdai.getIx());
        }
        if(tdai.isSetSAddr()){
            newDAI.setSAddr(tdai.getSAddr());
        }
        if(tdai.isSetValImport()){
            newDAI.setValImport(tdai.isValImport());
        }
        if(tdai.isSetValKind()){
            newDAI.setValKind(tdai.getValKind());
        }
        if(tdai.isSetPrivate()){
            newDAI.getPrivate().addAll(tdai.getPrivate());
        }
        if(tdai.isSetAny()){
            newDAI.getAny().addAll(tdai.getAny());
        }
        updateUnNaming(newDAI, tdai);
        return newDAI;
    }

    private static TSDI createSDI(TSDI tsdi){
        TSDI newSDI = new TSDI();
        newSDI.setName(tsdi.getName());
        if(tsdi.isSetSDIOrDAI()){
            newSDI.getSDIOrDAI().addAll((tsdi.getSDIOrDAI().stream().map(Utils::createSDIOrDAI).toList()));
        }
        if(tsdi.isSetIx()){
            newSDI.setIx(tsdi.getIx());
        }
        if(tsdi.isSetSAddr()){
            newSDI.setSAddr(tsdi.getSAddr());
        }
        if(tsdi.isSetPrivate()){
            newSDI.getPrivate().addAll(tsdi.getPrivate());
        }
        if(tsdi.isSetAny()){
            newSDI.getAny().addAll(tsdi.getAny());
        }
        updateUnNaming(newSDI, tsdi);
        return newSDI;
    }

    private static void updateUnNaming(TUnNaming unNaming, TUnNaming unNamingSource){
        if(unNamingSource.isSetText()){
            unNaming.setText(unNamingSource.getText());
        }
        if(unNamingSource.isSetDesc()){
            unNaming.setDesc(unNamingSource.getDesc());
        }
    }

    public static String sha256(String text) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
        byte[] digest = messageDigest.digest(text.getBytes(StandardCharsets.UTF_8));
        return HEX_FORMAT.formatHex(digest);
    }
}
