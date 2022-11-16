// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.support.ReflectionSupport;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


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

    @ParameterizedTest
    @MethodSource("provideEqualsOrBothBlankNotMatchingSource")
    void equalsOrBothBlank_should_be_false(String s1, String s2) {
        // Given : parameters
        // When
        boolean result = Utils.equalsOrBothBlank(s1, s2);
        // Then
        assertThat(result).isFalse();
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
}
