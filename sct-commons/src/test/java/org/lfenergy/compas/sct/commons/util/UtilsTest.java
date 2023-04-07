// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.support.ReflectionSupport;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TLN;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.sct.commons.dto.FCDAInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.createExtRefExample;
import static org.lfenergy.compas.sct.commons.util.Utils.copySclElement;


class UtilsTest {

    @Test
    void constructor_should_throw_exception() {
        // Given
        // When & Then
        assertThatThrownBy(() -> ReflectionSupport.newInstance(Utils.class))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void entering_should_return_text() {
        // Given : method name
        // When
        String entering = Utils.entering();
        // Then
        assertEquals(">>> Entering: ::entering_should_return_text", entering);
    }

    @Test
    void leaving_should_return_text() {
        // Given : method name
        // When
        String leaving = Utils.leaving();
        // Then
        assertEquals("<<< Leaving: ::leaving_should_return_text", leaving);
    }

    @Test
    void leaving_should_return_text_with_time() {
        // Given : method name
        // When
        String leaving = Utils.leaving(System.nanoTime());
        // Then
        assertTrue(leaving.matches("<<< Leaving: ::leaving_should_return_text_with_time - Timer duration: .* sec."), leaving);
    }

    @Test
    void leaving_should_return_text_with_invalid_time() {
        // Given : method name
        // When
        String leaving = Utils.leaving(-1L);
        // Then
        assertEquals("<<< Leaving: ::leaving_should_return_text_with_invalid_time", leaving);
    }

    @Test
    void equalsOrNotSet_should_return_true_when_both_values_are_not_set() {
        // Given
        Optional<Long> object1 = Optional.empty();
        Optional<Long> object2 = Optional.empty();
        // When
        boolean result1 = Utils.equalsOrNotSet(object1, object2, Optional::isPresent, Optional::get);
        boolean result2 = Utils.equalsOrNotSet(object2, object1, Optional::isPresent, Optional::get);
        // Then
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void equalsOrNotSet_should_return_true_when_both_values_are_set_and_equal() {
        // Given
        Optional<Long> object1 = Optional.of(1L);
        Optional<Long> object2 = Optional.of(1L);
        // When
        boolean result1 = Utils.equalsOrNotSet(object1, object2, Optional::isPresent, Optional::get);
        boolean result2 = Utils.equalsOrNotSet(object2, object1, Optional::isPresent, Optional::get);
        // Then
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void equalsOrNotSet_should_return_false_when_both_values_are_set_but_differ() {
        // Given
        Optional<Long> object1 = Optional.of(1L);
        Optional<Long> object2 = Optional.of(2L);
        // When
        boolean result1 = Utils.equalsOrNotSet(object1, object2, Optional::isPresent, Optional::get);
        boolean result2 = Utils.equalsOrNotSet(object2, object1, Optional::isPresent, Optional::get);
        // Then
        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    void equalsOrNotSet_should_return_false_when_one_is_set_and_the_other_is_not() {
        // Given
        Optional<Long> object1 = Optional.of(1L);
        Optional<Long> object2 = Optional.empty();
        // When
        boolean result1 = Utils.equalsOrNotSet(object1, object2, Optional::isPresent, Optional::get);
        boolean result2 = Utils.equalsOrNotSet(object2, object1, Optional::isPresent, Optional::get);
        // Then
        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    void equalsOrNotSet_should_throw_exception_when_value_is_null_and_isSet_is_misleading() {
        // Given
        Optional<Long> object1 = Optional.of(1L);
        Optional<Long> object2 = Optional.empty();
        // When & Then
        assertThrows(NoSuchElementException.class, () -> Utils.equalsOrNotSet(object1, object2, o -> true, Optional::get));
        assertThrows(NoSuchElementException.class, () -> Utils.equalsOrNotSet(object2, object1, o -> true, Optional::get));
    }

    @Test
    void xpathAttributeFilter_should_succeed() {
        // Given
        String attributeName = "name";
        String attributeValue = "value";
        // When
        String result = Utils.xpathAttributeFilter(attributeName, attributeValue);
        // Then
        assertEquals("@name=\"value\"", result);
    }

    @Test
    void xpathAttributeFilter_when_value_is_null_should_succeed() {
        // Given
        String attributeName = "name";
        String attributeValue = null;
        // When
        String result = Utils.xpathAttributeFilter(attributeName, attributeValue);
        // Then
        assertEquals("not(@name)", result);
    }

    @Test
    void xpathAttributeFilter_when_value_is_collection_should_succeed() {
        // Given
        String attributeName = "name";
        List<String> attributeValue = List.of("value1", "value2");
        // When
        String result = Utils.xpathAttributeFilter(attributeName, attributeValue);
        // Then
        assertEquals("@name=\"value1 value2\"", result);
    }

    @Test
    void xpathAttributeFilter_when_value_is_collection_should_ignore_null_values() {
        // Given
        String attributeName = "name";
        List<String> attributeValue = Arrays.asList(null, "value1", "value2");
        // When
        String result = Utils.xpathAttributeFilter(attributeName, attributeValue);
        // Then
        assertEquals("@name=\"value1 value2\"", result);
    }

    @ParameterizedTest
    @MethodSource("xpathAttributeFilterEmptyCollectionSource")
    void xpathAttributeFilter_when_empty_collection_should_succeed(Collection<String> attributeValue) {
        // Given
        String attributeName = "name";
        // When
        String result = Utils.xpathAttributeFilter(attributeName, attributeValue);
        // Then
        assertEquals("not(@name)", result);
    }

    public static Stream<Collection<String>> xpathAttributeFilterEmptyCollectionSource() {
        return Stream.of(
            null,
            Collections.emptyList(),
            Arrays.asList(new String[1]),
            Arrays.asList(new String[5])
        );
    }

    @ParameterizedTest
    @MethodSource("provideEqualsOrBothBlankMatchingSource")
    void equalsOrBothBlank_should_be_true(String s1, String s2) {
        // Given : parameters
        // When
        boolean result = Utils.equalsOrBothBlank(s1, s2);
        // Then
        assertThat(result).isTrue();
    }

    public static Stream<Arguments> provideEqualsOrBothBlankMatchingSource() {
        return Stream.of(
            Arguments.of("a", "a"),
            Arguments.of("", ""),
            Arguments.of(" ", " "),
            Arguments.of(null, null),
            Arguments.of("", null),
            Arguments.of(null, ""),
            Arguments.of(" ", null),
            Arguments.of(null, " "),
            Arguments.of("", " "),
            Arguments.of(" ", "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideEqualsOrBothBlankNotMatchingSource")
    void equalsOrBothBlank_should_be_false(String s1, String s2) {
        // Given : parameters
        // When
        boolean result = Utils.equalsOrBothBlank(s1, s2);
        // Then
        assertThat(result).isFalse();
    }

    public static Stream<Arguments> provideEqualsOrBothBlankNotMatchingSource() {
        return Stream.of(
            Arguments.of("a", "b"),
            Arguments.of("a", ""),
            Arguments.of("a", " "),
            Arguments.of("a", null),
            Arguments.of("", "a"),
            Arguments.of(" ", "a"),
            Arguments.of(null, "a")
        );
    }

    @ParameterizedTest
    @MethodSource("provideRemoveTrailingDigitsSource")
    void removeTrailingDigits_should_remove_trailing_digits(String input, String expected) {
        // Given : parameter
        // When
        String result = Utils.removeTrailingDigits(input);
        // Then
        assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> provideRemoveTrailingDigitsSource() {
        return Stream.of(
            Arguments.of("a", "a"),
            Arguments.of("a1", "a"),
            Arguments.of("a123", "a"),
            Arguments.of("a1b1", "a1b"),
            Arguments.of("a1b123", "a1b"),
            Arguments.of("", ""),
            Arguments.of("1", ""),
            Arguments.of("123", ""));
    }

    @ParameterizedTest
    @MethodSource("provideExtractField")
    void extractField_should(String s, int index, String expected) {
        //Given : parameters
        //When
        String result = Utils.extractField(s, "_", index);
        //Then
        assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> provideExtractField() {
        return Stream.of(
            Arguments.of("a", 0, "a"),
            Arguments.of("a", -1, "a"),
            Arguments.of("a_b", 0, "a"),
            Arguments.of("a_b", 1, "b"),
            Arguments.of("a_b", -1, "b"),
            Arguments.of("a_b", -2, "a"),
            Arguments.of("a_b_c", -2, "b"),
            Arguments.of("a", 1, null),
            Arguments.of("a", -2, null),
            Arguments.of("a_b_c", -4, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideBlankFirstComparatorSource")
    void blankFirstComparator_should_be_false(String s1, String s2, int expectedResult) {
        // Given : parameters
        // When
        int result = Utils.blanksFirstComparator(s1, s2);
        // Then
        assertThat(Integer.signum(result)).isEqualTo(Integer.signum(expectedResult));
    }

    public static Stream<Arguments> provideBlankFirstComparatorSource() {
        final int EQUALS = 0;
        final int GREATER_THAN = 1;
        final int LOWER_THAN = -1;

        return Stream.of(
            // Standard String.compare when not blank
            Arguments.of("a", "a", EQUALS),
            Arguments.of("a", "b", LOWER_THAN),
            Arguments.of("b", "a", GREATER_THAN),

            // Blank equality
            Arguments.of(" ", " ", EQUALS),
            Arguments.of(" ", null, EQUALS),
            Arguments.of(" ", "", EQUALS),
            Arguments.of("", " ", EQUALS),
            Arguments.of("", null, EQUALS),
            Arguments.of("", "", EQUALS),
            Arguments.of(null, " ", EQUALS),
            Arguments.of(null, null, EQUALS),
            Arguments.of(null, "", EQUALS),

            // Not blank compared to blank
            Arguments.of("a", " ", GREATER_THAN),
            Arguments.of(" ", "a", LOWER_THAN),
            Arguments.of(" ", "b", LOWER_THAN), // transitivity " " < "a" && "a" < "b" => " " < "b"
            Arguments.of("a", "", GREATER_THAN),
            Arguments.of("", "a", LOWER_THAN),
            Arguments.of("", "b", LOWER_THAN), // transitivity "" < "a" && "a" < "b" => "" < "b"
            Arguments.of("a", null, GREATER_THAN),
            Arguments.of(null, "a", LOWER_THAN),
            Arguments.of(null, "b", LOWER_THAN)  // transitivity null < "a" && "a" < "b" => null < "b"
        );
    }

    @ParameterizedTest
    @MethodSource("provideLnClassEqualsSourceResultTrue")
    void lnClassEquals_when_input_matches_should_return_true(List<String> lnClass1, String lnClass2){
        // Given : Parameter
        // When
        boolean result = Utils.lnClassEquals(lnClass1, lnClass2);
        // Then
        assertThat(result).isTrue();
    }

    public static Stream<Arguments> provideLnClassEqualsSourceResultTrue() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(null, ""),
            Arguments.of(null, " "),
            Arguments.of(Collections.emptyList(), null),
            Arguments.of(Collections.emptyList(), ""),
            Arguments.of(Collections.emptyList(), " "),
            Arguments.of(listWithNull, null),
            Arguments.of(listWithNull, ""),
            Arguments.of(listWithNull, " "),
            Arguments.of(List.of(""), null),
            Arguments.of(List.of(""), ""),
            Arguments.of(List.of(""), " "),
            Arguments.of(List.of(" "), null),
            Arguments.of(List.of(" "), ""),
            Arguments.of(List.of(" "), " "),
            Arguments.of(List.of("ANCR"), "ANCR")
        );
    }

    @ParameterizedTest
    @MethodSource("provideLnClassEqualsSourceResultFalse")
    void lnClassEquals_when_input_matches_should_return_false(List<String> lnClass1, String lnClass2){
        // Given : Parameter
        // When
        boolean result = Utils.lnClassEquals(lnClass1, lnClass2);
        // Then
        assertThat(result).isFalse();
    }

    public static Stream<Arguments> provideLnClassEqualsSourceResultFalse() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        return Stream.of(
            Arguments.of(List.of("ANCR"), null),
            Arguments.of(List.of("ANCR"), ""),
            Arguments.of(List.of("ANCR"), " "),
            Arguments.of(Collections.emptyList(), "ANCR"),
            Arguments.of(listWithNull, "ANCR"),
            Arguments.of(List.of(""), "ANCR"),
            Arguments.of(List.of(" "), "ANCR"),
            Arguments.of(List.of("ANCR"), "PVOC"),
            Arguments.of(List.of("PVOC"), "ANCR")
        );
    }

    @Test
    void lnClassEquals_when_lnClass_has_more_than_one_value_should_throw_exception(){
        // Given
        List<String> lnClass1 = List.of("ANCR", "PVOC");
        String lnClass2 = "ANCR";
        // When & Then
        assertThatThrownBy(() -> Utils.lnClassEquals(lnClass1, lnClass2))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sequence_should_return_range_of_numbers(){
        // Given
        long startInclusive = 1;
        long endInclusive = 3;
        // When
        PrimitiveIterator.OfLong result = Utils.sequence(startInclusive, endInclusive);
        // Then
        assertThat(result.next()).isEqualTo(1);
        assertThat(result.next()).isEqualTo(2);
        assertThat(result.next()).isEqualTo(3);
        assertThat(result.hasNext()).isFalse();
        assertThatThrownBy(result::nextLong).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void sequence_should_return_empty_iterator(){
        // Given
        long startInclusive = 1;
        long endInclusive = 0;
        // When
        PrimitiveIterator.OfLong result = Utils.sequence(startInclusive, endInclusive);
        // Then
        assertThat(result.hasNext()).isFalse();
        assertThatThrownBy(result::nextLong).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void sequence_should_throw_exception_when_MAX_VALUE(){
        // Given
        long startInclusive = 1;
        long endInclusive = Long.MAX_VALUE;
        // When & Then
        assertThatThrownBy(() -> Utils.sequence(startInclusive, endInclusive))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("End cannot exceed Long.MAX_VALUE - 1");
    }

    @Test
    void macAddressSequence_should_return_range_of_macAddresses(){
        // Given
        String startInclusive = "01-0C-CD-01-00-FE";
        String endInclusive = "01-0C-CD-01-01-01";
        // When
        Iterator<String> result = Utils.macAddressSequence(startInclusive, endInclusive);
        // Then
        assertThat(result.next()).isEqualTo("01-0C-CD-01-00-FE");
        assertThat(result.next()).isEqualTo("01-0C-CD-01-00-FF");
        assertThat(result.next()).isEqualTo("01-0C-CD-01-01-00");
        assertThat(result.next()).isEqualTo("01-0C-CD-01-01-01");
        assertThat(result.hasNext()).isFalse();
        assertThatThrownBy(result::next).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @CsvSource({"0x123456789ABC,12-34-56-78-9A-BC", "0xAA,00-00-00-00-00-AA"})
    void longToMacAddress_should_convert_long_to_mac_address(long macAddress, String expected){
        // Given : parameter
        // When
        String result = Utils.longToMacAddress(macAddress);
        // Then
        assertThat(result.toUpperCase()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {"-1,macAddress must be a positive integer but got : -1", // Negative number
        "281474976710656,macAddress cannot exceed 281474976710655 but got : 281474976710656", //FF-FF-FF-FF-FF-FF
        "9223372036854775807,macAddress cannot exceed 281474976710655 but got : 9223372036854775807"}) //Long MAX_VALUE
    void longToMacAddress_when_long_out_of_range_should_throw_exception(long macAddress, String expectedMessage){
        // Given : parameter
        // When & Then
        assertThatThrownBy(() -> Utils.longToMacAddress(macAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({"00-00-00-00-00-00,0", "12-34-56-78-9A-BC,20015998343868", "FF-FF-FF-FF-FF-FF,281474976710655",
    "11:ab:FF:01:c8:2A,19430415386666"})
    void macAddressToLong_should_convert_mac_address_to_long(String macAddress, long expected){
        // Given : parameters
        // When
        long result = Utils.macAddressToLong(macAddress);
        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "AB-CD", "FF_FF_FF_FF_FF_FF", "LK-JI-HG-FE-DC-AB", "AB-AB-AB-AB-AB-AB-AB"})
    void macAddressToLong_when_malformed_macAddress_should_throw_exception(String macAddress){
        // Given : parameters
        // When & Then
        assertThatThrownBy(() -> Utils.macAddressToLong(macAddress))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"0,6,000000", "255,1,FF", "255,2,FF", "255,3,0FF", "10,2,0A"})
    void toHex_should_return_hexadecimal(long number, int length, String expected) {
        // Given
        // When
        String result = Utils.toHex(number, length);
        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void isExtRefFeedBySameControlBlock_should_return_true() {
        // Given
        TExtRef tExtRefLnClass = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnClass.getSrcLNClass().add(TLLN0Enum.LLN_0.value());
        TExtRef tExtRef = createExtRefExample("CB_1", TServiceType.GOOSE);
        // When
        // Then
        assertThat(Utils.isExtRefFeedBySameControlBlock(tExtRef, tExtRefLnClass)).isTrue();
        assertThat(Utils.isExtRefFeedBySameControlBlock(createExtRefExample("CB_1", TServiceType.GOOSE), tExtRef)).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExtRefsToCompare")
    void isExtRefFeedBySameControlBlock_should_return_false(String testCase, TExtRef tExtRef1, TExtRef tExtRef2) {
        // Given
        // When
        // Then
        assertThat(Utils.isExtRefFeedBySameControlBlock(tExtRef1, tExtRef2)).isFalse();
    }

    private static Stream<Arguments> provideExtRefsToCompare() {
        TExtRef tExtRefLnClass = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnClass.getSrcLNClass().add("XXX");
        TExtRef tExtRefIedName = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefIedName.setIedName("IED_XXX");
        TExtRef tExtRefLdInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLdInst.setSrcLDInst("LD_XXX");
        TExtRef tExtRefLnInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnInst.setSrcLNInst("X");
        TExtRef tExtRefPrefix = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefPrefix.setSrcPrefix("X");

        return Stream.of(
                Arguments.of("ExtRef is not fed by same CB when different ServiceType", createExtRefExample("CB_1", TServiceType.GOOSE),
                        createExtRefExample("CB_1", TServiceType.SMV)),
                Arguments.of("ExtRef is not fed by same CB when different SrcCBName", createExtRefExample("CB_1", TServiceType.GOOSE),
                        createExtRefExample("CB_2", TServiceType.GOOSE)),
                Arguments.of("ExtRef is not fed by same CB when different SrcLnClass", createExtRefExample("CB_1", TServiceType.GOOSE),
                        tExtRefLnClass),
                Arguments.of("ExtRef is not fed by same CB when different IedName", createExtRefExample("CB_1", TServiceType.GOOSE),
                        tExtRefIedName),
                Arguments.of("ExtRef is not fed by same CB when different SrcLdInst", createExtRefExample("CB_1", TServiceType.GOOSE),
                        tExtRefLdInst),
                Arguments.of("ExtRef is not fed by same CB when different SrcLnInst", createExtRefExample("CB_1", TServiceType.GOOSE),
                        tExtRefLnInst),
                Arguments.of("ExtRef is not fed by same CB when different SrcPrefix", createExtRefExample("CB_1", TServiceType.GOOSE),
                        tExtRefPrefix)
        );
    }

    @Test
    void copySclElement_should_copy_by_value() {
        // Given
        TLN tln = new TLN();
        tln.setLnType("T1");
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        // When
        TLN result = copySclElement(tln, TLN.class);
        // Then
        assertThat(result).isNotSameAs(tln);
        assertThat(result).usingRecursiveComparison().isEqualTo(tln);
    }

    @Test
    void copySclElement_should_throwException() {
        // Given
        FCDAInfo fcdaInfo = new FCDAInfo();
        // When
        // Then
        assertThatCode(() -> copySclElement(fcdaInfo, FCDAInfo.class))
                .isInstanceOf(ScdException.class)
                .hasMessage("org.lfenergy.compas.sct.commons.dto.FCDAInfo is not known to this context");
    }
}
