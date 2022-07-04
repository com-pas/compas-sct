// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

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
        Optional<Long> object1= Optional.empty();
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
        Optional<Long> object1= Optional.of(1L);
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
        Optional<Long> object1= Optional.of(1L);
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
        Optional<Long> object1= Optional.of(1L);
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
        Optional<Long> object1= Optional.of(1L);
        Optional<Long> object2 = Optional.empty();
        // When & Then
        assertThrows(NoSuchElementException.class, () -> Utils.equalsOrNotSet(object1, object2, o -> true, Optional::get));
        assertThrows(NoSuchElementException.class, () -> Utils.equalsOrNotSet(object2, object1, o -> true, Optional::get));
    }

}
